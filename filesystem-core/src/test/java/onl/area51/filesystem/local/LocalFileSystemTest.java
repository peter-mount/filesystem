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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import onl.area51.filesystem.CommonTestUtils;
import onl.area51.filesystem.io.FileSystemIO;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class LocalFileSystemTest
        extends CommonTestUtils
{

    private static final String SCHEME = "local";
    private static final String AUTHORITY = "local.test";
    private static final String URI_PREFIX = SCHEME + "://" + AUTHORITY;

    @BeforeClass
    public static void setUpClass()
            throws IOException
    {
        Map<String, Object> env = new HashMap<>();
        env.put( FileSystemIO.BASE_DIRECTORY, BASE_FILE.toString() + "/" + AUTHORITY );
        FileSystems.newFileSystem( URI.create( URI_PREFIX ), env );
    }

    @Test
    public void test()
    {
        assertTrue( BASE_FILE.exists() );
        assertTrue( BASE_FILE.isDirectory() );
    }

    @Test
    public void createFile()
            throws IOException
    {
        write( URI.create( URI_PREFIX + "/file1.txt" ) );
    }

    @Test
    public void walk_subdirectory()
            throws URISyntaxException,
                   IOException
    {
        String authority = "local.walksub";
        String uri = SCHEME + "://" + authority;
        Map<String, Object> env = new HashMap<>();
        env.put( FileSystemIO.BASE_DIRECTORY, BASE_FILE.toString() + "/" + authority );
        FileSystems.newFileSystem( URI.create( uri ), env );
        Path root = Paths.get( URI.create( uri + "/plaindir" ) );
        Files.createDirectories( root );
        createFiles( root, "plain", 50 );
        walk( authority, root, "plaindir/plain", 50 );
    }

    @Test
    public void walk_root()
            throws URISyntaxException,
                   IOException
    {
        String authority = "local.walkroot";
        String uri = SCHEME + "://" + authority;
        Map<String, Object> env = new HashMap<>();
        env.put( FileSystemIO.BASE_DIRECTORY, BASE_FILE.toString() + "/" + authority );
        FileSystems.newFileSystem( URI.create( uri ), env );
        Path root = Paths.get( URI.create( uri ) );
        createFiles( root, "root", 50 );
        walk( authority, root, "root", 50 );
    }

    private void walk( String authority, Path root, String prefix, int count )
            throws IOException
    {
        Path cachePath = BASE_PATH.resolve( authority );
        Path dirPath = Paths.get( cachePath.toString(), root.toString() );

        Set<String> fileNames = Files.walk( dirPath )
                .filter( p -> p.toString().endsWith( ".txt" ) )
                .map( p -> Paths.get( p.toAbsolutePath().toString() ) )
                .map( cachePath::relativize )
                .map( Object::toString )
                .collect( Collectors.toSet() );
        assertFalse( fileNames.isEmpty() );

        Set<String> expected = IntStream.range( 0, count )
                .mapToObj( i -> prefix + i + ".txt" )
                .collect( Collectors.toSet() );
        assertFalse( expected.isEmpty() );

        Set<String> extra = new HashSet<>( fileNames );
        extra.removeAll( expected );
        if( !extra.isEmpty() ) {
            System.out.println( "Extra entries " + extra );
        }
        assertTrue( "Extra entries ", extra.isEmpty() );

        Set<String> missing = new HashSet<>( expected );
        missing.removeAll( fileNames );
        if( !extra.isEmpty() ) {
            System.out.println( "Missing entries " + missing );
        }
        assertTrue( "Missing entries", missing.isEmpty() );
    }
}
