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
package onl.area51.filesystem.ftp.proxy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.OverlayingFileSystemIO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kohsuke.MetaInfServices;

/**
 * FTP overlay - will retrieve from the remote file system and store in a local cache
 * @author peter
 */
@MetaInfServices(OverlayingFileSystemIO.class)
public class FTP
        extends OverlayingFileSystemIO.Synchronous
{

    private static final Logger LOG = Logger.getLogger(FTP.class.getName() );

    private static final String URL = "remoteUrl";

    private final CloseableHttpClient client;
    private final URI remoteUrl;

    public FTP( FileSystemIO delegate, Map<String, ?> env )
    {
        super( delegate, Executors.newSingleThreadExecutor() );

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
    protected void retrievePath( String path )
            throws IOException
    {
        try {
            if( path == null || path.isEmpty() ) {
                throw new FileNotFoundException( "/" );
            }

            StringBuilder b = new StringBuilder().append( remoteUrl.getPath() );
            if( b.charAt( b.length() - 1 ) != '/' ) {
                b.append( '/' );
            }
            if( path.startsWith( "/" ) ) {
                b.append( path, 1, path.length() );
            }
            else {
                b.append( path );
            }
            String p = b.toString();

            LOG.log( Level.INFO, () -> "Retrieving " + p );

            URI uri = new URI( remoteUrl.getScheme(), remoteUrl.getAuthority(), p, remoteUrl.getQuery(), null );

            LOG.log( Level.INFO, () -> "Retrieving " + uri );

            HttpGet get = new HttpGet( uri );

            HttpResponse response = client.execute( get );

            switch( response.getStatusLine().getStatusCode() ) {
                case 200:
                    try( InputStream is = response.getEntity().getContent() ) {
                        try( OutputStream os = getDelegate().newOutputStream( path.toCharArray(),
                                                                              StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                                                                              StandardOpenOption.WRITE ) ) {
                            FileSystemUtils.copy( is, os );
                        }
                    }
                    break;

                case 404:
                case 500:
                default:
                    throw new FileNotFoundException( path );
            }
        }
        catch( URISyntaxException ex ) {
            throw new IOException( path, ex );
        }
    }

}
