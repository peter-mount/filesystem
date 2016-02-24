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
package onl.area51.filesystem.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import onl.area51.filesystem.CommonTestUtils;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author peter
 */
public class CacheFileSystemTest
        extends CommonTestUtils
{

    private static final String SCHEME = "cache";
    private static final String AUTHORITY = "cache.test";
    private static final String URI_PREFIX1 = SCHEME + "://" + AUTHORITY + "1";
    private static final String URI_PREFIX2 = SCHEME + "://" + AUTHORITY + "2";
    private static final String URI_PREFIX3 = SCHEME + "://" + AUTHORITY + "3";

    @BeforeClass
    public static void setUpClass()
            throws IOException
    {
        System.setProperty( CacheFileSystemProvider.class.getName(), BASE_FILE.toString() );

        // FileSystem 1 is a default cache
        FileSystems.newFileSystem( URI.create( URI_PREFIX1 ), new HashMap<>() );

        // FileSystem 2 is a mediawiki style cache
        Map<String, Object> env = new HashMap<>();
        env.put( "fileSystemType", "mediawiki" );
        FileSystems.newFileSystem( URI.create( URI_PREFIX2 ), env );

        // FileSystem 3 is a opendata style cache
        env = new HashMap<>();
        env.put( "fileSystemType", "opendata" );
        FileSystems.newFileSystem( URI.create( URI_PREFIX3 ), env );
    }

    @Test
    public void createFile1()
            throws IOException
    {
        createFiles( Paths.get( URI.create( URI_PREFIX1 ) ), "file", 50, ".jpg" );
    }

    @Test
    public void createFile2()
            throws IOException
    {
        createFiles( Paths.get( URI.create( URI_PREFIX2 ) ), "file", 50, ".jpg" );
    }

    @Test
    public void createFile3()
            throws IOException
    {
        createFiles( Paths.get( URI.create( URI_PREFIX3 ) ), "file", 50, ".jpg" );
    }

    @Test
    public void expireCache()
            throws IOException,
                   InterruptedException
    {
        expire( "cache" );
    }

    @Test
    public void expireFlat()
            throws IOException,
                   InterruptedException
    {
        expire( "flat" );
    }

    @Test
    public void expireMediaWiki()
            throws IOException,
                   InterruptedException
    {
        expire( "mediawiki" );
    }

    @Test
    public void expireOpenData()
            throws IOException,
                   InterruptedException
    {
        expire( "opendata" );
    }

    private void expire( String fileSystemType )
            throws IOException,
                   InterruptedException
    {
        URI uri = URI.create( SCHEME + "://expire." + fileSystemType );

        System.out.println( "Testing expiry for " + fileSystemType );

        Map<String, Object> env = new HashMap<>();
        env.put( "fileSystemType", fileSystemType );
        env.put( "fileSystemType", "flat" );
        env.put( "maxAge", 3000L );
        env.put( "scanDelay", 1000L );

        // We need this initially otherwise file timestamps go out of sync if this is not a clean build
        env.put( "clearOnStartup", true );

        try( FileSystem fs = FileSystems.newFileSystem( uri, env ) ) {
            Path base = fs.getPath( "/" );

            System.out.println( "Creating files that will expire" );
            createFiles( base, "file", 0, 10, ".jpg" );
            testPresent( base, 0, 10 );

            sleep( 2 );

            System.out.println( "Creating files that will not expire" );
            createFiles( base, "file", 10, 20, ".jpg" );
            testPresent( base, 0, 20 );

            sleep( 2 );

            System.out.println( "Creating files that will not expire when we reopen" );
            createFiles( base, "file", 20, 30, ".jpg" );
            testExpired( base, 0, 10 );
            testPresent( base, 10, 30 );

            System.out.println( "Closing filesystem" );
        }

        sleep( 1 );

        // Reopen the filesystem
        System.out.println( "Reopen filesystem" );
        // We don't want to clear on startup
        env.remove( "clearOnStartup" );

        try( FileSystem fs = FileSystems.newFileSystem( uri, env ) ) {
            Path base = fs.getPath( "/" );

            sleep( 1 );
            testExpired( base, 0, 20 );
            testPresent( base, 20, 30 );
        }
    }

    private void sleep( long s )
            throws InterruptedException
    {
        System.out.println( "Waiting " + s + " seconds" );
        Thread.sleep( s * 1000L );
    }

    private void testExpired( Path b, int s, int e )
    {
        System.out.println( "Testing files " + s + " to " + e + " have been expired" );
        for( int i = s; i < e; i++ ) {
            String f = "/file" + i + ".jpg";
            Path p = b.resolve( f );
            try( InputStream is = Files.newInputStream( p, StandardOpenOption.READ ) ) {
                fail( "File " + f + " exists" );
            }
            catch( FileNotFoundException ex ) {
                // This is a pass
            }
            catch( Exception ex ) {
                throw new AssertionError( f + " " + ex.getClass().getSimpleName(), ex );
            }
        }
    }

    private void testPresent( Path b, int s, int e )
    {
        System.out.println( "Testing files " + s + " to " + e + " have not been expired" );
        for( int i = s; i < e; i++ ) {
            String f = "/file" + i + ".jpg";
            Path p = b.resolve( f );
            try( InputStream is = Files.newInputStream( p, StandardOpenOption.READ ) ) {
                // We have passed
            }
            catch( Exception ex ) {
                throw new AssertionError( f + " " + ex.getClass().getSimpleName(), ex );
            }
        }
    }
}
