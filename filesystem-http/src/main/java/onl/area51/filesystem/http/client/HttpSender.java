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
import static onl.area51.filesystem.http.client.AbstractHttpClient.USER_AGENT;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlaySender;
import onl.area51.httpd.util.PathEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

/**
 *
 * @author peter
 */
public class HttpSender
        extends AbstractHttpClient
        implements OverlaySender
{

    private static final Logger LOG = Logger.getLogger( "HTTP" );

    public HttpSender( FileSystemIO delegate, Map<String, Object> env )
    {
        super( delegate, env );
    }

    @Override
    public void send( char[] path )
            throws IOException
    {
        try {
            if( path == null || path.length == 0 ) {
                throw new FileNotFoundException( "/" );
            }

            URI uri = getRemoteURI( path );

            HttpEntity entity = new PathEntity( getPath( path ) );

            LOG.log( Level.INFO, () -> "Sending " + uri );

            HttpPut put = new HttpPut( uri );
            put.setHeader( USER_AGENT, getUserAgent() );
            put.setEntity( entity );

            HttpResponse response = getClient().execute( put );

            int returnCode = response.getStatusLine().getStatusCode();
            LOG.log( Level.INFO, () -> "ReturnCode " + returnCode + ": " + response.getStatusLine().getReasonPhrase() );
        }
        catch( URISyntaxException ex ) {
            throw new IOException( String.valueOf( path ), ex );
        }
    }

}
