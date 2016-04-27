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
package onl.area51.filesystem.http.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;
import org.apache.http.client.methods.HttpGet;
import onl.area51.httpd.util.PathEntity;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * {@link OverlayFileSystemIO} implementation to retrieve content from a remote HTTP/HTTPS server
 *
 * @author peter
 */
public final class HttpUtils
{

    private static final Logger LOG = Logger.getLogger( "HTTP" );

    protected static final String USER_AGENT = "User-Agent";

    private HttpUtils()
    {
    }

    public static void retrieve( char[] path, Function<char[], String> remoteUri, Supplier<FileSystemIO> delegate, Supplier<String> userAgent )
            throws IOException
    {
        if( path == null || path.length == 0 ) {
            throw new FileNotFoundException( "/" );
        }

        String uri = remoteUri.apply( path );
        if( uri != null ) {

            LOG.log( Level.FINE, () -> "Retrieving " + uri );

            HttpGet get = new HttpGet( uri );
            get.setHeader( USER_AGENT, userAgent.get() );

            try( CloseableHttpClient client = HttpClients.createDefault() ) {
                try( CloseableHttpResponse response = client.execute( get ) ) {

                    int returnCode = response.getStatusLine().getStatusCode();
                    LOG.log( Level.FINE, () -> "ReturnCode " + returnCode + ": " + response.getStatusLine().getReasonPhrase() );

                    switch( returnCode ) {
                        case 200:
                        case 304:
                            FileSystemUtils.copyFromRemote( () -> response.getEntity().getContent(), delegate.get(), path );
                            return;

                        default:
                    }
                }
            }
        }

        throw new FileNotFoundException( String.valueOf( path ) );
    }

    public static void send( char[] path, Function<char[], String> remoteUri, Function<char[], Path> getPath, Supplier<String> userAgent )
            throws IOException
    {
        if( path == null || path.length == 0 ) {
            throw new FileNotFoundException( "/" );
        }

        String uri = remoteUri.apply( path );
        if( uri != null ) {

            HttpEntity entity = new PathEntity( getPath.apply( path ) );

            LOG.log( Level.FINE, () -> "Sending " + uri );

            HttpPut put = new HttpPut( uri );
            put.setHeader( USER_AGENT, userAgent.get() );
            put.setEntity( entity );

            try( CloseableHttpClient client = HttpClients.createDefault() ) {
                try( CloseableHttpResponse response = client.execute( put ) ) {

                    int returnCode = response.getStatusLine().getStatusCode();
                    LOG.log( Level.FINE, () -> "ReturnCode " + returnCode + ": " + response.getStatusLine().getReasonPhrase() );
                }
            }
        }
    }
}
