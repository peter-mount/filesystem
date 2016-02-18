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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author peter
 */
public class FileSystemUtils
{

    private static ScheduledExecutorService timers = Executors.newScheduledThreadPool( 1, r -> {
                                                                                   Thread t = new Thread( r );
                                                                                   t.setDaemon( true );
                                                                                   return t;
                                                                               } );

    public static ScheduledFuture<?> scheduleAtFixedRate( Runnable command, long initialDelay, long period, TimeUnit unit )
    {
        return timers.scheduleAtFixedRate( command, initialDelay, period, unit );
    }

    public static ScheduledFuture<?> schedule( Runnable command, long delay, TimeUnit unit )
    {
        return timers.schedule( command, delay, unit );
    }

    /**
     * Get the MD5 of a string
     * <p>
     * @param s <p>
     * @return
     * @throws java.io.IOException
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
}
