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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Objects;
import onl.area51.httpd.HttpAction;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * Base {@link HttpRequestHandler} that's backed by a {@link FileSystem}.
 *
 * @author peter
 */
public interface PathHttpAction
{

    void apply( HttpRequest request, HttpResponse response, HttpContext context, Path path )
            throws HttpException,
                   IOException;

    default PathHttpAction andThen( PathHttpAction after )
    {
        Objects.requireNonNull( after );
        return ( req, resp, ctx, path ) -> {
            apply( req, resp, ctx, path );
            if( HttpAction.isOk( resp ) ) {
                after.apply( req, resp, ctx, path );
            }
        };
    }

    default PathHttpAction compose( PathHttpAction before )
    {
        Objects.requireNonNull( before );
        return before.andThen( this );
    }

}
