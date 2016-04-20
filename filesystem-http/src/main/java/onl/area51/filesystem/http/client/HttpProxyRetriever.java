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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayRetriever;

/**
 * {@link OverlayFileSystemIO} implementation to retrieve content from a remote HTTP/HTTPS server
 *
 * @author peter
 */
public class HttpProxyRetriever
        extends AbstractBaseHttpClient
        implements OverlayRetriever
{

    public static final String MAPPINGS = "mappings";

    private final Map<String, URI> remoteURIS;

    public HttpProxyRetriever( FileSystemIO delegate, Map<String, Object> env )
    {
        super( delegate, env );

        remoteURIS = FileSystemUtils.<Map<String, String>>get( env, MAPPINGS, HashMap::new )
                .entrySet()
                .stream()
                .collect( Collectors.toConcurrentMap( Map.Entry::getKey, e -> URI.create( e.getValue() )
                ) );
    }

    @Override
    public void retrieve( char[] path )
            throws IOException
    {
        HttpUtils.retrieve( path, this::getRemoteURI, this::getDelegate, this::getUserAgent );
    }

    private String[] extractPath( char[] path )
    {
        String p = path.length > 1 && path[0] == '/' ? String.valueOf( path, 1, path.length - 1 ) : String.valueOf( path );
        return p.split( "/", 2 );
    }

    /**
     * Return the remote URI for a server where the mapping key is /key/remotePath
     *
     * @param path
     *
     * @return
     */
    @Override
    protected URI getRemoteServerUrl( char[] path )
    {
        String p[] = extractPath( path );
        return p.length == 2 ? remoteURIS.get( p[0] ) : null;
    }

    /**
     * Return the remote path for a server where the remote path is /key/remotePath
     *
     * @param path
     *
     * @return
     */
    @Override
    protected char[] getRemotePath( char[] path )
    {
        String p[] = extractPath( path );
        return p.length == 2 ? p[1].toCharArray() : null;
    }

}
