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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import onl.area51.filesystem.io.overlay.OverlayRetriever;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * {@link OverlayFileSystemIO} implementation to retrieve content from a remote HTTP/HTTPS server
 *
 * @author peter
 */
public class HttpRetriever
        extends AbstractHttpClient
        implements OverlayRetriever
{

    private static final Logger LOG = Logger.getLogger( "HTTP" );

    public HttpRetriever( FileSystemIO delegate, Map<String, Object> env )
    {
        super( delegate, env );
    }

    @Override
    public void retrieve( char[] path )
            throws IOException
    {
        try {
            if( path == null || path.length == 0 ) {
                throw new FileNotFoundException( "/" );
            }

            URI uri = getRemoteURI( path );

            LOG.log( Level.INFO, () -> "Retrieving " + uri );

            HttpGet get = new HttpGet( uri );
            get.setHeader( USER_AGENT, getUserAgent() );

            try( CloseableHttpClient client = HttpClients.createDefault() ) {
                HttpResponse response = client.execute( get );

                int returnCode = response.getStatusLine().getStatusCode();
                LOG.log( Level.INFO, () -> "ReturnCode " + returnCode + ": " + response.getStatusLine().getReasonPhrase() );

                switch( returnCode ) {
                    case 200:
                    case 304:
                        FileSystemUtils.copyFromRemote( () -> response.getEntity().getContent(), getDelegate(), path );
                        break;

                    case 404:
                    case 500:
                    default:
                        throw new FileNotFoundException( String.valueOf( path ) );
                }
            }
        }
        catch( URISyntaxException ex ) {
            throw new IOException( String.valueOf( path ), ex );
        }
    }

}
