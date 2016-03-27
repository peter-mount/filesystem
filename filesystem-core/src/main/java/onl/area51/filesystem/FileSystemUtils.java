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
package onl.area51.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import onl.area51.filesystem.io.FileSystemIO;

/**
 * General utility functions
 *
 * @author peter
 */
public class FileSystemUtils
{
    /**
     * System property holding the cache base directory
     */
    public static final String CACHEBASE_PROPERTY = "area51.cacheBase";
    /**
     * Environment variable holding the cache base directory
     */
    public static final String CACHEBASE_ENV = "FILESYSTEM_BASE";

    public static final String UTF8 = StandardCharsets.UTF_8.name();

    private static final ScheduledExecutorService TIMERS = Executors.newScheduledThreadPool( 1, r
                                                                                             -> {
                                                                                         Thread t = new Thread( r );
                                                                                         t.setDaemon( true );
                                                                                         return t;
                                                                                     } );

    private static String CACHE_BASE;

    public static synchronized String getCacheBase()
    {
        if( CACHE_BASE == null ) {
            CACHE_BASE = System.getenv(CACHEBASE_ENV);
            if( CACHE_BASE == null || CACHE_BASE.trim().isEmpty() ) {
                CACHE_BASE = System.getProperty(CACHEBASE_PROPERTY);
            }
            if( CACHE_BASE == null || CACHE_BASE.trim().isEmpty() ) {
                CACHE_BASE = System.getProperty( "user.home" ) + "/.area51/";
            }
        }
        return CACHE_BASE;
    }

    public static Path getCacheDirectory()
    {
        return Paths.get( getCacheBase() );
    }

    /**
     * Schedule a task repeatedly
     *
     * @param command      Task to execute
     * @param initialDelay the initial delay from calling this method until it first executes
     * @param period       the delay between executions
     * @param unit         TimeUnit for initialDelay and period
     *
     * @return ScheduledFuture which can be used to cancel the task
     */
    public static ScheduledFuture<?> scheduleAtFixedRate( Runnable command, long initialDelay, long period, TimeUnit unit )
    {
        return TIMERS.scheduleAtFixedRate( command, initialDelay, period, unit );
    }

    /**
     * Schedule a task repeatedly
     *
     * @param command Task to execute
     * @param delay   the delay between executions
     * @param unit    TimeUnit for initialDelay and period
     *
     * @return ScheduledFuture which can be used to cancel the task
     */
    public static ScheduledFuture<?> schedule( Runnable command, long delay, TimeUnit unit )
    {
        return TIMERS.schedule( command, delay, unit );
    }

    /**
     * Get the MD5 of a string
     * <p>
     * @param s <p>
     * @return @throws java.io.IOException
     */
    public static byte[] md5( String s )
            throws IOException
    {
        try {
            return MessageDigest.getInstance( "MD5" ).digest( s.getBytes() );
        }
        catch( NoSuchAlgorithmException ex ) {
            throw new IOException( ex );
        }
    }

    /**
     * Returns a byte as a 2 digit hexadecimal number
     *
     * @param b
     *
     * @return
     */
    private static String fix( byte b )
    {
        String p = Integer.toHexString( Byte.toUnsignedInt( b ) );
        return p.length() == 1 ? ("0" + p) : p;
    }

    /**
     * The Media image prefix. For example "Harry-Green-HampsteadHeath-copy.jpg" will return "0/02"
     *
     * @param path
     *
     * @return
     *
     * @throws IOException
     */
    public static String getMediaWikiPrefix( String path )
            throws IOException
    {
        String p = fix( md5( path )[0] );
        return p.substring( 0, 1 ) + "/" + p;
    }

    /**
     * Implements the old system of creating a single directory consisting of the first character of a file.
     *
     * @param path
     *
     * @return
     *
     * @throws java.io.IOException
     */
    public static String getOpenDataCMSPrefix( String path )
            throws IOException
    {
        if( path == null || path.isEmpty() ) {
            throw new IOException( "Empty path" );
        }
        return path.substring( 0, 1 );
    }

    /**
     * Converts the supplied path into a path for a cache. This path will be of the form 0/01/012345.suffix where 0 is the first
     * digit of the original path's md5, 01 the first two digits of the md5 and 012345 the full md5 value.
     * <p>
     * If the original path had a suffix/file type (i.e. ".jpg") then that will also be appended to the final path.
     *
     * @param path Path to convert
     *
     * @return the full cache path name
     *
     * @throws IOException
     */
    public static String getCachePrefix( String path )
            throws IOException
    {
        String suffix = "";
        int i = path.lastIndexOf( '.' ), j = path.lastIndexOf( '/' );
        if( i > -1 && i > j ) {
            suffix = path.substring( i );
        }

        StringBuilder sb = new StringBuilder();
        for( byte b: md5( path ) ) {
            String p = Integer.toHexString( Byte.toUnsignedInt( b ) );
            if( p.length() == 1 ) {
                sb.append( '0' );
            }
            sb.append( p );
        }

        String p = sb.toString();
        return p.substring( 0, 1 ) + "/" + p.substring( 0, 2 ) + "/" + p + suffix;
    }

    /**
     * Is an environment parameter true. If the parameter a Boolean then that value is used. Otherwise this returns true only if
     * the value is not false (which allows us to default to true if the parameter does not exist).
     *
     * @param env  Environment
     * @param name parameter name
     *
     * @return
     */
    public static boolean isTrue( Map<String, ?> env, String name )
    {
        if( env != null && env.containsKey( name ) ) {
            Object o = env.get( name );
            if( o instanceof Boolean ) {
                return (Boolean) o;
            }
            else {
                return !Boolean.FALSE.equals( env.get( name ) );
            }
        }

        return false;
    }

    /**
     * Is an environment parameter false. Any other value will return false here.
     *
     * @param env  Environment
     * @param name parameter name
     *
     * @return
     */
    public static boolean isFalse( Map<String, ?> env, String name )
    {
        return env != null && Boolean.FALSE.equals( env.get( name ) );
    }

    /**
     * Returns an environment parameter as a string
     *
     * @param env
     * @param key
     *
     * @return
     */
    public static String getString( Map<String, ?> env, String key )
    {
        return getString( env, key, null );
    }

    /**
     * Returns an environment parameter as a string
     *
     * @param env
     * @param key
     * @param defaultValue
     *
     * @return
     */
    public static String getString( Map<String, ?> env, String key, String defaultValue )
    {
        return Objects.toString( env == null ? null : env.get( key ), defaultValue );
    }

    /**
     * Returns an environment parameter as a long
     *
     * @param env
     * @param key
     * @param defaultValue
     *
     * @return
     */
    public static long getLong( Map<String, ?> env, String key, long defaultValue )
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

    public static <T> T get( Map<String, ?> env, String key )
    {
        return env == null ? null : (T) env.get( key );
    }

    public static <T> T get( Map<String, ?> env, String key, Supplier<T> defaultValue )
    {
        T t = env == null ? null : (T) env.get( key );
        return t == null ? defaultValue.get() : t;
    }

    /**
     * Returns a clean URI. This uri will be the supplied one with no query string or fragment.
     *
     * @param uri
     *
     * @return
     */
    public static final URI getCleanURI( URI uri )
    {
        if( uri == null ) {
            return uri;
        }

        try {
            return new URI( uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null );
        }
        catch( URISyntaxException x ) {
            throw new IllegalArgumentException( x.getMessage(), x );
        }
    }

    /**
     * Returns the filesystem URI. This will consist of just the scheme and authority from the supplied URI.
     *
     * @param uri
     *
     * @return
     */
    public static final URI getFileSystemURI( URI uri )
    {
        if( uri == null ) {
            return uri;
        }

        try {
            return new URI( uri.getScheme(), uri.getAuthority(), null, null, null );
        }
        catch( URISyntaxException x ) {
            throw new IllegalArgumentException( x.getMessage(), x );
        }
    }

    /**
     * Returns an environment map based on the supplied map (if non-null) and any query parameters in the uri.
     *
     * @param uri
     * @param env
     *
     * @return
     */
    public static final Map<String, ?> getFileSystemEnv( URI uri, Map<String, ?> env )
    {
        final Map<String, Object> newEnv = new HashMap<>();

        if( uri == null ) {
            mergeEnv( newEnv, env );
        }
        else {
            readConfig( newEnv, uri );
            mergeEnv( newEnv, env );
            parseQueryParams( newEnv, uri );
        }

        return newEnv;
    }

    private static void readConfig( Map<String, Object> newEnv, URI uri )
    {
        // Read in any config from file system
        Path path = getCacheDirectory().resolve( uri.getAuthority() + ".properties" );
        if( Files.isRegularFile( path, LinkOption.NOFOLLOW_LINKS ) ) {
            try( Reader r = Files.newBufferedReader( path ) ) {
                Properties p = new Properties();
                p.load( r );

                if( !p.isEmpty() ) {
                    p.forEach( ( k, v ) -> newEnv.put( k.toString(), v ) );
                }
            }
            catch( IOException ex ) {
                // Ignore
            }
        }
    }

    private static void mergeEnv( Map<String, Object> newEnv, Map<String, ?> env )
    {
        if( env != null ) {
            newEnv.putAll( env );
        }
    }

    private static void parseQueryParams( Map<String, Object> env, URI uri )
    {
        if( uri.getQuery() != null && !uri.getQuery().isEmpty() ) {
            try {
                for( String param: uri.getQuery().split( "&" ) ) {
                    int idx = param.indexOf( "=" );
                    if( idx < 0 ) {
                        env.put( URLDecoder.decode( param, UTF8 ), "" );
                    }
                    else {
                        env.put( URLDecoder.decode( param.substring( 0, idx ), UTF8 ), URLDecoder.decode( param.substring( idx + 1 ), UTF8 ) );
                    }
                }
            }
            catch( UnsupportedEncodingException ex ) {
                throw new IllegalArgumentException( ex );
            }
        }
    }

    // buffer size used for reading and writing
    private static final int BUFFER_SIZE = 8192;

    public static long copy( InputStream source, OutputStream sink )
            throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while( (n = source.read( buf )) > 0 ) {
            sink.write( buf, 0, n );
            nread += n;
        }
        return nread;
    }

    public static <E extends Enum<E>> E getEnum( Class<E> clazz, Map<String, ?> env, String key, E defaultValue )
    {
        String v = getString( env, key );
        if( v == null ) {
            return defaultValue;
        }

        try {
            return Enum.valueOf( clazz, v );
        }
        catch( IllegalArgumentException ex ) {
            return defaultValue;
        }
    }

    /**
     * Copy from an InputStream and write to the filesystem
     *
     * @param src
     * @param io
     * @param path
     *
     * @throws IOException
     */
    public static void copyFromRemote( IOSupplier<InputStream> src, FileSystemIO io, char[] path )
            throws IOException
    {
        try( InputStream is = src.get() ) {
            try( OutputStream os = io.newOutputStream( path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE ) ) {
                FileSystemUtils.copy( is, os );
            }
        }
    }

    @FunctionalInterface
    public static interface IOSupplier<T>
    {

        T get()
                throws IOException;
    }

}
