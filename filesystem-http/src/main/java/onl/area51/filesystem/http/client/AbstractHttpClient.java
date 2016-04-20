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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;

/**
 * {@link OverlayFileSystemIO} implementation to retrieve content from a remote HTTP/HTTPS server
 *
 * @author peter
 */
public abstract class AbstractHttpClient
        extends AbstractBaseHttpClient
{

    private static final String URL = "remoteUrl";

    private final URI remoteUrl;

    public AbstractHttpClient( FileSystemIO delegate, Map<String, Object> env )
    {
        super( delegate, env );

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

    }

    @Override
    protected URI getRemoteServerUrl( char[] path )
    {
        return remoteUrl;
    }
}
