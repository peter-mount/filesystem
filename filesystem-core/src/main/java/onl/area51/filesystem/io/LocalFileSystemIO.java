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
package onl.area51.filesystem.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
     * Environment key to tell the cache how often to expire files. If this is missing then it will default to MAX_AGE. If it's
     * negative then it will disable the scan.
     */
    public static final String SCAN_DELAY = "scanDelay";
    /**
     * Environment key to tell the cache to expire on startup if MAX_AGE is defined. This is on by default, it may be disabled
     * by setting this to false.
     */
    public static final String EXPIRE_ON_STARTUP = "expireOnStartup";
    /**
     * Environment key to tell the cache to clear the filesystem on startup
     */
    public static final String CLEAR_ON_STARTUP = "clearOnStartup";
    private final long maxAge;

    private ScheduledFuture<?> task;

    public LocalFileSystemIO( Path basePath, Map<String, ?> env )
    {
        super( basePath, env );

        maxAge = FileSystemUtils.getLong( env, MAX_AGE, 0 );

        long delay = FileSystemUtils.getLong( env, SCAN_DELAY, maxAge );

        boolean expireOnStartup = FileSystemUtils.isFalse( env, EXPIRE_ON_STARTUP );

        boolean clearOnStartup = FileSystemUtils.isTrue( env, CLEAR_ON_STARTUP );
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

    protected abstract String getPath( char[] path )
            throws IOException;

    @Override
    public Path toPath( char[] path )
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

}
