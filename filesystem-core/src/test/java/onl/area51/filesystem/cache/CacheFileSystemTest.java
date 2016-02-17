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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import onl.area51.filesystem.CommonTestUtils;
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
            throws URISyntaxException,
                   IOException
    {
        createFiles( Paths.get( URI.create( URI_PREFIX1 ) ), "file", 50, ".jpg" );
    }

    @Test
    public void createFile2()
            throws URISyntaxException,
                   IOException
    {
        createFiles( Paths.get( URI.create( URI_PREFIX2 ) ), "file", 50, ".jpg" );
    }

    @Test
    public void createFile3()
            throws URISyntaxException,
                   IOException
    {
        createFiles( Paths.get( URI.create( URI_PREFIX3 ) ), "file", 50, ".jpg" );
    }
}
