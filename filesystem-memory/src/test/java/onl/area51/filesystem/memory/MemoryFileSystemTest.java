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
package onl.area51.filesystem.memory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author peter
 */
public class MemoryFileSystemTest
        extends CommonTestUtils
{

    private static final String SCHEME = "memory";
    private static final String AUTHORITY = "memory.test";
    private static final String URI_PREFIX = SCHEME + "://" + AUTHORITY;

    @BeforeClass
    public static void setUpClass()
            throws IOException
    {
        Map<String, Object> env = new HashMap<>();
        FileSystems.newFileSystem( URI.create( URI_PREFIX ), env );
    }

    @Test
    public void createFile()
            throws IOException
    {
        write( URI.create( URI_PREFIX + "/file1.txt" ) );
        
        
    }
}
