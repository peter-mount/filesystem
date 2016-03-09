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
package onl.area51.filesystem.http.server;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.http.PathEntity;
import onl.area51.httpd.HttpAction;
import org.apache.http.HttpConnection;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpCoreContext;

/**
 * Builds a map of file systems with a specified path prefix. This map can then be used via an HttpAction chain to resolve a path the an nio Path
 * and an appropriate action.
 *
 * @author peter
 */
public interface PathHttpActionBuilder
{

    PathHttpActionBuilder add( PathHttpAction action );

    default PathHttpActionBuilder assertPathExists()
    {
        return add( ( req, resp, context, path ) -> {
            if( path == null || !Files.exists( path, LinkOption.NOFOLLOW_LINKS ) ) {
                HttpAction.sendError( resp, HttpStatus.SC_NOT_FOUND, req.getRequestLine().getUri() );
            }
        } );
    }

    default PathHttpActionBuilder logPath( Logger logger )
    {
        return add( ( req, resp, ctx, path ) -> logger.log( Level.INFO, () -> req.getRequestLine().getMethod() + ": " + req.getRequestLine().getUri() ) );
    }

    default PathHttpActionBuilder returnPathContent()
    {
        return add( ( req, resp, ctx, path ) -> {
            HttpCoreContext coreContext = HttpCoreContext.adapt( ctx );
            HttpConnection conn = coreContext.getConnection( HttpConnection.class );
            resp.setStatusCode( HttpStatus.SC_OK );
            resp.setEntity( new PathEntity( path ) );
        } );
    }

    default PathHttpActionBuilder returnPathSizeOnly()
    {
        return add( ( req, resp, ctx, path ) -> {
            HttpCoreContext coreContext = HttpCoreContext.adapt( ctx );
            HttpConnection conn = coreContext.getConnection( HttpConnection.class );
            resp.setStatusCode( HttpStatus.SC_OK );
            resp.setEntity( new PathEntity.SizeOnly( path ) );
        } );
    }

    PathHttpAction build();

    default HttpAction build( FileSystemMap map )
    {
        PathHttpAction action = build();
        return ( req, resp, ctx ) -> {
            String uri = req.getRequestLine().getUri();
            Path path = map.getPath( uri );
            if( path == null ) {
                HttpAction.sendError( resp, HttpStatus.SC_NOT_FOUND, uri );
            }
            else {
                action.apply( req, resp, ctx, path );
            }
        };
    }

    static PathHttpActionBuilder create()
    {
        return new PathHttpActionBuilder()
        {
            private PathHttpAction action;

            @Override
            public PathHttpActionBuilder add( PathHttpAction action )
            {
                this.action = this.action == null ? action : this.action.andThen( action );
                return this;
            }

            @Override
            public PathHttpAction build()
            {
                Objects.requireNonNull( action, "No actions defined" );
                return action;
            }

        };
    }
}
