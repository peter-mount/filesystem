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
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import onl.area51.filesystem.io.overlay.OverlayRetriever;

/**
 * {@link OverlayFileSystemIO} implementation to retrieve content from a remote HTTP/HTTPS server
 *
 * @author peter
 */
public class HttpRetriever
        implements OverlayRetriever
{

    private static final Logger LOG = Logger.getLogger( "HTTP" );

    private static final String URL = "remoteUrl";
    private static final String USER_AGENT = "User-Agent";
    private static final String DEFAULT_USER_AGENT = "Area51 Mozilla/5.0 (Linux x86_64)";

    private final FileSystemIO delegate;
    private final CloseableHttpClient client;
    private final URI remoteUrl;
    private final String userAgent;

    public HttpRetriever( FileSystemIO delegate, Map<String, ?> env )
    {
        this.delegate = delegate;

        userAgent = FileSystemUtils.getString( env, USER_AGENT, DEFAULT_USER_AGENT );

        try {
            Object url = Objects.requireNonNull( FileSystemUtils.get( env, URL ), URL + " not defined" );
            if( url instanceof String ) {
                remoteUrl = new URI( url.toString() );
            }
            else if( url instanceof URI ) {
                remoteUrl = (URI) url;
            }
            else if( url instanceof URL ) {
                remoteUrl = ((URL) url).toURI();
            }
            else {
                throw new IllegalArgumentException( "Unsupported URL " + url );
            }
        }
        catch( URISyntaxException ex ) {
            throw new IllegalArgumentException( ex );
        }

        client = HttpClients.createDefault();
    }

    @Override
    public void retrieve( char[] path )
            throws IOException
    {
        try {
            if( path == null || path.length == 0 ) {
                throw new FileNotFoundException( "/" );
            }

            StringBuilder b = new StringBuilder().append( remoteUrl.getPath() );
            if( b.length() == 0 || b.charAt( b.length() - 1 ) != '/' ) {
                b.append( '/' );
            }
            if( path[0] == '/' ) {
                b.append( path, 1, path.length );
            }
            else {
                b.append( path );
            }
            String p = b.toString();

            URI uri = new URI( remoteUrl.getScheme(), remoteUrl.getAuthority(), p, remoteUrl.getQuery(), null );

            LOG.log( Level.INFO, () -> "Retrieving " + uri );

            HttpGet get = new HttpGet( uri );
            get.setHeader( USER_AGENT, userAgent );

            HttpResponse response = client.execute( get );

            int returnCode = response.getStatusLine().getStatusCode();
            LOG.log( Level.INFO, () -> "ReturnCode " + returnCode + ": " + response.getStatusLine().getReasonPhrase() );

            switch( returnCode ) {
                case 200:
                case 304:
                    FileSystemUtils.copyFromRemote( () -> response.getEntity().getContent(), delegate, path );
                    break;

                case 404:
                case 500:
                default:
                    throw new FileNotFoundException( String.valueOf( path ) );
            }
        }
        catch( URISyntaxException ex ) {
            throw new IOException( String.valueOf( path ), ex );
        }
    }

}
