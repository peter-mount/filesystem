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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * {@link HttpRequestHandler} that can provide readonly access to a filesystem.
 *
 * @author peter
 */
public class HttpPathHandler
        extends AbstractFileSystemRequestHandler
{

    private static final Logger LOG = Logger.getLogger( HttpPathHandler.class.getName() );

    /**
     * 
     * @param prefix path prefix to be removed from request URI's before being turned into a path within the FileSystem.
     * @param filesystem FileSystem to expose.
     */
    public HttpPathHandler( String prefix, FileSystem filesystem )
    {
        super( prefix, filesystem );
    }

    @Override
    protected void doGet( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        Path path = getPath( request );
        if( path == null || !Files.exists( path, LinkOption.NOFOLLOW_LINKS ) )
        {
            sendError( response, HttpStatus.SC_NOT_FOUND, "%s not found", request.getRequestLine().getUri() );
        }
//        else if( !Files.isReadable( path ) || Files.isDirectory( path, LinkOption.NOFOLLOW_LINKS ) ) {
//            response.setStatusCode( HttpStatus.SC_FORBIDDEN );
//            StringEntity entity = new StringEntity( "<html><body><h1>Access denied</h1></body></html>", ContentType.TEXT_HTML );
//            response.setEntity( entity );
//            LOG.log( Level.INFO, () -> "Cannot read file " + target );
//        }
        else
        {
            HttpCoreContext coreContext = HttpCoreContext.adapt( context );
            HttpConnection conn = coreContext.getConnection( HttpConnection.class );
            response.setStatusCode( HttpStatus.SC_OK );
            PathEntity body = new PathEntity( path );
            response.setEntity( body );
            LOG.log( Level.INFO, () -> conn + ": serving file " + request.getRequestLine().getUri() );
        }
    }
}
