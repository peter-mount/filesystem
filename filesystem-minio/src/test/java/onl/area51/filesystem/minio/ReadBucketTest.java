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
package onl.area51.filesystem.minio;

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
import onl.area51.filesystem.FileSystemUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests reading from a bucket.
 * <p>
 * Note this test is disabled as it requires access to S3. To run it yourself you must ensure that your credentials are
 * configured and BUCKET is set to your bucket name.
 * <p>
 * If you don't do this you will find the test will fail as you wont have the correct permissions.
 * <p>
 * Also you must ensure that a file with the name in TEST_FILE exists in the bucket so the read test will have something to
 * retrieve.
 *
 * @author peter
 */
public class ReadBucketTest
        extends CommonTestUtils
{

    /**
     * Set this to your bucket name
     */
    private static final String BUCKET = "test.area51.onl";
    /**
     * Test file that must exist in the bucket. Ensure that any file is uploaded to the bucket using the S3 console with this
     * name.
     */
    private static final String TEST_FILE = "/18_Close-16.png";

    private static final Logger LOG = Logger.getGlobal();

    private static final String PREFIX = "cache://minioread.test";

    private static Map<String, Object> createMap()
    {
        Map<String, Object> map = new HashMap<>();
        map.put( "fileSystemType", "flat" );
        map.put( "fileSystemWrapper", "minioread" );
        map.put( "bucket", BUCKET );
        map.put( "maxAge", "60000" );
        map.put( "clearOnStartup", "true" );
        return map;
    }

    @Test
    @Ignore
    public void readObject()
            throws IOException
    {
        System.setProperty( FileSystemUtils.CACHEBASE_PROPERTY, BASE_FILE.toString() );
        FileSystems.newFileSystem( URI.create( PREFIX ), createMap() );

        Path path = Paths.get( URI.create( PREFIX + TEST_FILE ) );

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

}
