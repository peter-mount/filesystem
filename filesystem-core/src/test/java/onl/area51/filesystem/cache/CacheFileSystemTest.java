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
import onl.area51.filesystem.CommonTestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class CacheFileSystemTest
        extends CommonTestUtils
{

    private static final String SCHEME = "cache";
    private static final String AUTHORITY = "cache.test";
    private static final String URI_PREFIX = SCHEME + "://" + AUTHORITY;

    @BeforeClass
    public static void setUpClass()
            throws IOException
    {
        System.setProperty( CacheFileSystemProvider.class.getName(), BASE_FILE.toString() );

        FileSystems.newFileSystem( URI.create( URI_PREFIX ), new HashMap<>() );
    }

    @Test
    public void test()
    {
        assertTrue( BASE_FILE.exists() );
        assertTrue( BASE_FILE.isDirectory() );
    }

    @Test
    public void createFile()
            throws URISyntaxException,
                   IOException
    {
        createFiles( Paths.get( URI.create( URI_PREFIX ) ), "file", 50 );
    }
}
