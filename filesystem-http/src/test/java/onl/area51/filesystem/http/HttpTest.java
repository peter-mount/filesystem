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
package onl.area51.filesystem.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import onl.area51.filesystem.cache.CacheFileSystemProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.trainwatch.util.MapBuilder;

/**
 *
 * @author peter
 */
public class HttpTest
        extends CommonTestUtils
{

    private static final String HTTP_PREFIX = "cache://http.test";
    private static final String HTTPS_PREFIX = "cache://https.test";

    @BeforeClass
    public static void setUpClass()
            throws IOException
    {
        System.setProperty( CacheFileSystemProvider.class.getName(), BASE_FILE.toString() );

        FileSystems.newFileSystem( URI.create( HTTP_PREFIX ),
                                   MapBuilder.<String, Object>builder()
                                   .add( "fileSystemType", "flat" )
                                   .add( "fileSystemWrapper", "http" )
                                   .add( "remoteUrl", "http://uktra.in/" )
                                   .add( "maxAge", "60000" )
                                   .add( "clearOnStartup", "true" )
                                   .build() );

        FileSystems.newFileSystem( URI.create( HTTPS_PREFIX ),
                                   MapBuilder.<String, Object>builder()
                                   .add( "fileSystemType", "flat" )
                                   .add( "fileSystemWrapper", "http" )
                                   .add( "remoteUrl", "https://uktra.in/" )
                                   .add( "maxAge", "60000" )
                                   .add( "clearOnStartup", "true" )
                                   .build() );
    }

    @Test
    public void http()
            throws IOException
    {
        get( HTTP_PREFIX );
    }

    /**
     * Note: If this fails it will be due to the LetsEncrypt CA not being present in cacerts, so you need to update it.
     * <p>
     * The error will look like:
     * <p>
     * javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed:
     * sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
     *
     * @throws IOException
     */
    @Test
    public void https()
            throws IOException
    {
        get( HTTPS_PREFIX );
    }

    private void get( String prefix )
            throws IOException
    {
        Path path = Paths.get( URI.create( prefix + "/images/375-logo.png" ) );

        // Just prove we can read a file
        try( InputStream is = Files.newInputStream( path, StandardOpenOption.READ ) ) {
            int c = 0;
            while( is.read() > -1 ) {
                c++;
            }
        }
    }

}
