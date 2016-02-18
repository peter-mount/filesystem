/*
 * Copyright 2016 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onl.area51.filesystem.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import onl.area51.filesystem.AbstractLocalFileSystemIO;
import onl.area51.filesystem.FileSystemIO;
import onl.area51.filesystem.FileSystemUtils;

/**
 * {@link FileSystemIO} implementation of a directory
 *
 * @author peter
 */
public abstract class LocalFileSystemIO
        extends AbstractLocalFileSystemIO
{

    /**
     * Environment key for some FileSystems to allow the underlying format to be changed
     */
    public static final String KEY = "fileSystemType";
    /**
     * Environment key to allow a Cache to expire files based on their age in milliseconds if the value is greater than 0.
     */
    public static final String MAX_AGE = "maxAge";
    /**
     * Environment key to tell the cache how often to expire files. If this is missing then it will default to MAX_AGE. If it's negative then it will disable
     * the scan.
     */
    public static final String SCAN_DELAY = "scanDelay";
    /**
     * Environment key to tell the cache to expire on startup if MAX_AGE is defined. This is on by default, it may be disabled by setting this to false.
     */
    public static final String EXPIRE_ON_STARTUP = "expireOnStartup";
    /**
     * Environment key to tell the cache to clear the filesystem on startup
     */
    public static final String CLEAR_ON_STARTUP = "clearOnStartup";
    private final long maxAge;

    private ScheduledFuture<?> task;

    public static FileSystemIO create( Path basePath, Map<String, ?> env, BiFunction<Path, Map<String, ?>, FileSystemIO> defaultIO )
    {
        String type = env == null ? "" : Objects.toString( env.get( KEY ), "" ).trim().toLowerCase();
        switch( type ) {
            case "flat":
                return new Flat( basePath, env );
            case "mediawiki":
                return new MediaWiki( basePath, env );
            case "opendata":
                return new OpenDataCMS( basePath, env );
            case "cache":
                return new Cache( basePath, env );
            default:
                return defaultIO.apply( basePath, env );
        }
    }

    public LocalFileSystemIO( Path basePath, Map<String, ?> env )
    {
        super( basePath, env );

        maxAge = getLong( env, MAX_AGE, 0 );

        long delay = getLong( env, SCAN_DELAY, maxAge );

        boolean expireOnStartup;
        try {
            // Note: !False here as anything other than false should enable expire
            expireOnStartup = env != null && !Boolean.FALSE.equals( env.get( EXPIRE_ON_STARTUP ) );
        }
        catch( Exception ex ) {
            expireOnStartup = true;
        }

        boolean clearOnStartup = env != null && Boolean.TRUE.equals( env.get( CLEAR_ON_STARTUP ) );
        if( clearOnStartup ) {
            try {
                clearFileSystem();
            }
            catch( IOException ex ) {
                // Ignore
            }
        }

        if( maxAge > 0L ) {
            if( delay > 0L ) {
                task = FileSystemUtils.scheduleAtFixedRate( this::expire,
                                                            // Wait 1 second if expiring on startup otherwise wait for delay
                                                            expireOnStartup && !clearOnStartup ? 1000L : delay,
                                                            delay,
                                                            TimeUnit.MILLISECONDS );
            }
            else if( expireOnStartup && !clearOnStartup ) {
                // Repeating has been disabled so wait 1 second then run expiry just once
                task = FileSystemUtils.schedule( this::expire, 1000L, TimeUnit.MILLISECONDS );
            }
        }
    }

    private long getLong( Map<String, ?> env, String key, long defaultValue )
    {
        if( env != null ) {
            Object o = env.get( key );
            if( o instanceof Number ) {
                return ((Number) o).longValue();
            }
            if( o instanceof String ) {
                return Long.parseLong( (String) o );
            }
        }
        return defaultValue;
    }

    protected abstract String getPath( char[] path )
            throws IOException;

    @Override
    protected Path toPath( char[] path )
            throws IOException
    {
        Path p = getBaseDirectory().resolve( getPath( path ) ).toAbsolutePath();
        if( p.startsWith( getBaseDirectory() ) ) {
            return p;
        }
        throw new IOException( "Path is outside the FileSystem" );
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            if( task != null ) {
                task.cancel( true );
            }
        }
        finally {
            task = null;
            super.close();
        }
    }

    @Override
    public void expire()
    {
        if( maxAge > 0L ) {
            final long cull = System.currentTimeMillis() - maxAge;
            for( File f: baseFile.listFiles() ) {
                expireFile( cull, f );
            }
        }
    }

    private boolean expireFile( final long cull, final File f )
    {
        if( f.isDirectory() ) {
            boolean d = true;
            for( File f1: f.listFiles() ) {
                d = d & expireFile( cull, f1 );
            }
            return d && f.lastModified() < cull && delete( f );
        }
        else if( f.isFile() && f.lastModified() < cull ) {
            return delete( f );
        }
        return false;
    }

    /**
     * A flat FileSystem which locally matches it's structure
     */
    public static class Flat
            extends LocalFileSystemIO
    {

        public Flat( Path basePath, Map<String, ?> env )
        {
            super( basePath, env );
        }

        @Override
        protected String getPath( char[] path )
                throws IOException
        {
            return String.valueOf( path );
        }

    }

    /**
     * A FileSystem which matches the way MediaWiki lays out it's images.
     * <p>
     * The local filesystem will have the files under directories formed from the first octet of the md5 of the filename.
     * <p>
     * So for the file "/Harry-Green-HampsteadHeath-copy.jpg" then the local file will be "0/02/Harry-Green-HampsteadHeath-copy.jpg"
     */
    public static class MediaWiki
            extends LocalFileSystemIO
    {

        public MediaWiki( Path basePath, Map<String, ?> env )
        {
            super( basePath, env );
        }

        @Override
        protected String getPath( char[] path )
                throws IOException
        {
            String p = String.valueOf( path );
            return FileSystemUtils.getMediaWikiPrefix( p ) + "/" + p;
        }

    }

    /**
     * A FileSystem which stores files with their MD5's.
     * <p>
     * So for the file "/Harry-Green-HampsteadHeath-copy.jpg" then the local file will be "0/02/021ad58091421abab4be786251454727.jpg".
     * Note the last portion of the file after '.' will be included in the final file name.
     * <p>
     * Also this instance will not return a directory listing and creating directories do nothing as they are meaningless when stored locally.
     */
    public static class Cache
            extends LocalFileSystemIO
    {

        public Cache( Path basePath, Map<String, ?> env )
        {
            super( basePath, env );
        }

        @Override
        protected String getPath( char[] path )
                throws IOException
        {
            return FileSystemUtils.getCachePrefix( String.valueOf( path ) );
        }

        @Override
        public void createDirectory( char[] path, FileAttribute<?>[] attrs )
                throws IOException
        {
            // No-op as we cache by file name
        }

        @Override
        public DirectoryStream<Path> newDirectoryStream( char[] path, DirectoryStream.Filter<? super Path> filter )
                throws IOException
        {
            // We cannot view directories so don't show anything
            return new DirectoryStream<Path>()
            {
                @Override
                public Iterator<Path> iterator()
                {
                    return new Iterator<Path>()
                    {
                        @Override
                        public boolean hasNext()
                        {
                            return false;
                        }

                        @Override
                        public Path next()
                        {
                            throw new NoSuchElementException();
                        }
                    };
                }

                @Override
                public void close()
                        throws IOException
                {
                }
            };
        }

    }

    /**
     * A FileSystem which matches the way MediaWiki lays out it's images.
     * <p>
     * The local filesystem will have the files under directories formed from the first character of the file.
     * <p>
     * So for the file "/Harry-Green-HampsteadHeath-copy.jpg" then the local file will be "H/Harry-Green-HampsteadHeath-copy.jpg"
     */
    public static class OpenDataCMS
            extends LocalFileSystemIO
    {

        public OpenDataCMS( Path basePath, Map<String, ?> env )
        {
            super( basePath, env );
        }

        @Override
        protected String getPath( char[] path )
                throws IOException
        {
            String p = String.valueOf( path );
            return FileSystemUtils.getOpenDataCMSPrefix( p ) + "/" + p;
        }

    }
}
