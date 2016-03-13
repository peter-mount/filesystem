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
package onl.area51.filesystem.s3;

import onl.area51.filesystem.s3.CommonTestUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.cache.CacheFileSystemProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author peter
 */
public class ReadBucketTest
        extends CommonTestUtils
{

    private static final Logger LOG = Logger.getGlobal();

    private static final String PREFIX = "cache://s3.test";

    private static Map<String, Object> createMap()
    {
        Map<String, Object> map = new HashMap<>();
        map.put( "fileSystemType", "flat" );
        map.put( "fileSystemWrapper", "s3" );
        map.put( "bucket", "test.area51.onl" );
        map.put( "maxAge", "60000" );
        map.put( "clearOnStartup", "true" );
        return map;
    }

    @BeforeClass
    public static void setUpClass()
            throws IOException
    {
        System.setProperty( CacheFileSystemProvider.class.getName(), BASE_FILE.toString() );

        FileSystems.newFileSystem( URI.create( PREFIX ), createMap() );
    }

    @Test
    public void readObject()
            throws IOException
    {
        Path path = Paths.get( URI.create( PREFIX + "/18_Close-16.png" ) );

        LOG.log( Level.INFO, () -> "Retrieving " + path );
        // Just prove we can read a file
        try( InputStream is = Files.newInputStream( path, StandardOpenOption.READ ) ) {
            int c = 0;
            while( is.read() > -1 ) {
                c++;
            }
        }

        LOG.log( Level.INFO, () -> "Retrieved " + path );
    }

//    public void writeObject()
//            throws IOException
//    {
//        Path path = Paths.get( URI.create( prefix + "/images/375-logo.png" ) );
//
//        // Just prove we can read a file
//        try( InputStream is = Files.newInputStream( path, StandardOpenOption.READ ) )
//        {
//            int c = 0;
//            while( is.read() > -1 )
//            {
//                c++;
//            }
//        }
//    }
}
