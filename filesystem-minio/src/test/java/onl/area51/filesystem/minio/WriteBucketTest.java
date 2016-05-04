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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import onl.area51.filesystem.FileSystemUtils;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests reading and writing to a bucket.
 * <p>
 * Note this test is disabled as it requires access to Minio. To run it yourself you must ensure that your credentials are
 * configured and BUCKET is set to your bucket name.
 * <p>
 * If you don't do this you will find the test will fail as you wont have the correct permissions.
 *
 * @author peter
 */
public class WriteBucketTest
        extends CommonTestUtils
{

    /**
     * Set this to your bucket name
     */
    private static final String BUCKET = "test.area51.onl";

    private static final Logger LOG = Logger.getGlobal();

    private static final String PREFIX = "cache://miniowrite.test";

    private static Map<String, Object> createMap()
    {
        Map<String, Object> map = new HashMap<>();
        map.put( "fileSystemType", "flat" );
        map.put( "fileSystemWrapper", "minioread,miniowrite" );
        map.put( "bucket", BUCKET );
        map.put( "maxAge", "60000" );
        map.put( "clearOnStartup", "true" );
        return map;
    }

    @Test
    @Ignore
    public void rwObject()
            throws IOException
    {
        System.setProperty( FileSystemUtils.CACHEBASE_PROPERTY, BASE_FILE.toString() );
        FileSystems.newFileSystem( URI.create( PREFIX ), createMap() );

        String value = UUID.randomUUID().toString();

        Path path = Paths.get( URI.create( PREFIX + "/" + TEST_FILE ) );

        LOG.log( Level.INFO, "Write to cache" );
        testWrite( path, value );

        LOG.log( Level.INFO, "Read should then be from cache" );
        testRead( path, value );

        LOG.log( Level.INFO, "Delete from cache as should now be in Minio" );
        Files.deleteIfExists( path );

        assertFalse( TEST_FILE + " still exists", new File( BASE_FILE, TEST_FILE ).exists() );

        LOG.log( Level.INFO, "Read should pull clean copy from Minio" );
        testRead( path, value );

        LOG.log( Level.INFO, "Read should then be from cache" );
        testRead( path, value );
    }
    private static final String TEST_FILE = "test.txt";

    private void testRead( Path path, String expected )
            throws IOException
    {
        LOG.log( Level.INFO, () -> "Reading " + path );

        // Just prove we can read a file
        try( Stream<String> s = Files.lines( path ) ) {
            String file = s.collect( Collectors.joining( "\n" ) );

            Assert.assertEquals( expected, file );
        }

        LOG.log( Level.INFO, () -> "Read " + path );
    }

    public void testWrite( Path path, String content )
            throws IOException
    {
        LOG.log( Level.INFO, () -> "Writing " + path );

        try( OutputStream os = Files.newOutputStream( path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING ) ) {
            try( Writer w = new OutputStreamWriter( os ) ) {
                w.write( content );
            }
        }

        LOG.log( Level.INFO, () -> "Wrote " + path );
    }
}
