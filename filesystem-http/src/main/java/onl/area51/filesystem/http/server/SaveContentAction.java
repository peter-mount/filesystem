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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import onl.area51.httpd.action.Action;
import onl.area51.httpd.action.Request;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

/**
 *
 * @author peter
 */
public class SaveContentAction
        implements Action
{

    @Override
    public void apply( Request request )
            throws HttpException,
                   IOException
    {
        Path path = request.getAttribute( "path" );

        HttpRequest req = request.getHttpRequest();

        if( path != null && req instanceof HttpEntityEnclosingRequest ) {
            HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) req;
            HttpEntity entity = entityRequest.getEntity();
            Files.copy( entity.getContent(), path, StandardCopyOption.REPLACE_EXISTING );
            request.getHttpResponse().setStatusCode( HttpStatus.SC_OK );
            request.getHttpResponse().setEntity( new StringEntity( "OK" ) );
        }
        else {
            request.getHttpResponse().setStatusCode( HttpStatus.SC_BAD_REQUEST );
            request.getHttpResponse().setEntity( new StringEntity( "BAD REQUEST" ) );
        }
    }

}
