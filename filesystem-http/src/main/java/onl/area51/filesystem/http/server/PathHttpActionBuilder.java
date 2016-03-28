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
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import onl.area51.httpd.action.Action;
import onl.area51.httpd.action.Actions;
import onl.area51.httpd.util.PathEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

/**
 * Builds a map of file systems with a specified path prefix. This map can then be used via an HttpAction chain to resolve a
 * path the an nio Path and an appropriate action.
 *
 * @author peter
 */
public interface PathHttpActionBuilder
{

    PathHttpActionBuilder add( PathHttpAction action );

    default PathHttpActionBuilder assertPathExists()
    {
        return add( ( req, path ) -> {
            if( path == null || !Files.exists( path, LinkOption.NOFOLLOW_LINKS ) ) {
                Actions.sendError( req, HttpStatus.SC_NOT_FOUND, req.getHttpRequest().getRequestLine().getUri() );
            }
        } );
    }

    /**
     * Return the content of a path to the client - specifically a GET request.
     *
     * @return
     */
    default PathHttpActionBuilder returnPathContent()
    {
        return add( ( req, path ) -> {
            req.getHttpResponse().setStatusCode( HttpStatus.SC_OK );
            PathEntity body = new PathEntity( path );
            req.getHttpResponse().setEntity( body );
        } );
    }

    /**
     * Returns just the head (i.e. no actual content) to the client - specifically a HEAD request.
     *
     * @return
     */
    default PathHttpActionBuilder returnPathSizeOnly()
    {
        return add( ( req, path ) -> {
            req.getHttpResponse().setStatusCode( HttpStatus.SC_OK );
            req.getHttpResponse().setEntity( new PathEntity.SizeOnly( path ) );
        } );
    }

    /**
     * Post the request content to a cache
     *
     * @return
     */
    default PathHttpActionBuilder saveContent()
    {
        return add( ( req, path ) -> {
            if( req instanceof HttpEntityEnclosingRequest ) {
                HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) req;
                HttpEntity entity = request.getEntity();
                Files.copy( entity.getContent(), path, StandardCopyOption.REPLACE_EXISTING );
                req.getHttpResponse().setStatusCode( HttpStatus.SC_OK );
                req.getHttpResponse().setEntity( new StringEntity( "OK" ) );
            }
            else {
                req.getHttpResponse().setStatusCode( HttpStatus.SC_BAD_REQUEST );
                req.getHttpResponse().setEntity( new StringEntity( "BAD REQUEST" ) );
            }
        } );
    }

    PathHttpAction build();

    default Action build( FileSystemMap map )
    {
        PathHttpAction action = build();
        return ( req ) -> {
            String uri = req.getHttpRequest().getRequestLine().getUri();
            Path path = map.getPath( uri );
            if( path == null ) {
                Actions.sendError( req, HttpStatus.SC_NOT_FOUND, uri );
            }
            else {
                action.apply( req, path );
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
