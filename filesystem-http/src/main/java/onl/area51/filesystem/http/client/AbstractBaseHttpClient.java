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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.AbstractFileSystem;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;

/**
 * {@link OverlayFileSystemIO} implementation to retrieve content from a remote HTTP/HTTPS server
 *
 * @author peter
 */
public abstract class AbstractBaseHttpClient
{

    protected static final String USER_AGENT = "User-Agent";
    protected static final String DEFAULT_USER_AGENT = "Area51 Mozilla/5.0 (Linux x86_64)";

    private final FileSystemIO delegate;
    private final AbstractFileSystem fileSystem;
    private final String userAgent;

    public AbstractBaseHttpClient( FileSystemIO delegate, Map<String, Object> env )
    {
        this.delegate = delegate;
        fileSystem = (AbstractFileSystem) env.get( FileSystem.class.getName() );

        userAgent = FileSystemUtils.getString( env, USER_AGENT, DEFAULT_USER_AGENT );
    }

    protected final FileSystemIO getDelegate()
    {
        return delegate;
    }

    protected abstract URI getRemoteServerUrl( char[] path );

    protected final String getUserAgent()
    {
        return userAgent;
    }

    protected char[] getRemotePath( char[] path )
    {
        return path;
    }

    protected final String getRemoteURI( char[] path )
    {
        URI remoteUrl = getRemoteServerUrl( path );
        StringBuilder b = new StringBuilder().append( remoteUrl.getPath() );

        if( b.length() == 0 || b.charAt( b.length() - 1 ) != '/' ) {
            b.append( '/' );
        }

        char[] remotePath = getRemotePath( path );
        if( path[0] == '/' ) {
            b.append( remotePath, 1, remotePath.length );
        }
        else {
            b.append( remotePath );
        }

        return remoteUrl.getScheme() + "://" + remoteUrl.getAuthority() + "/" + b.toString();
    }

    protected final Path getPath( char[] path )
    {
        if( fileSystem == null ) {
            return getDelegate().getBaseDirectory().resolve( String.valueOf( path ) );
        }
        return fileSystem.createPath( path );
    }

}
