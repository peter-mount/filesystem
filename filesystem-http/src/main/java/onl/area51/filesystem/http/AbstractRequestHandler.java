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
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * {@link HttpRequestHandler} that splits out the common HTTP Methods
 *
 * @author peter
 */
public abstract class AbstractRequestHandler
        implements HttpRequestHandler
{

    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";

    @Override
    public final void handle( final HttpRequest request, final HttpResponse response, final HttpContext context )
            throws HttpException,
                   IOException
    {
        switch( request.getRequestLine().getMethod().toUpperCase( Locale.ROOT ) )
        {
            case METHOD_GET:
                doGet( request, response, context );
                break;

            case METHOD_HEAD:
                doHead( request, response, context );
                break;

            case METHOD_POST:
                doPost( request, response, context );
                break;

            case METHOD_PUT:
                doPut( request, response, context );
                break;

            case METHOD_DELETE:
                doDelete( request, response, context );
                break;

            case METHOD_TRACE:
                doTrace( request, response, context );
                break;

            case METHOD_OPTIONS:
                doOptions( request, response, context );
                break;

            default:
                doDefault( request, response, context );
                break;
        }
    }

    /**
     * Called when a method is not supported
     *
     * @param request
     * @param response
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    protected void doDefault( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        methodNotAllowed( response );
    }

    /**
     * Called for GET requests
     *
     * @param request
     * @param response
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    protected void doGet( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        methodNotAllowed( response );
    }

    /**
     * Called for HEAD requests. This will default to calling {@link #doGet(org.apache.http.HttpRequest, org.apache.http.HttpResponse, org.apache.http.protocol.HttpContext)
     * } unless its overridden.
     *
     * @param request
     * @param response
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    protected void doHead( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        doGet( request, response, context );
    }

    /**
     * Called for POST requests
     *
     * @param request
     * @param response
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    protected void doPost( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        methodNotAllowed( response );
    }

    /**
     * Called for PUT requests
     *
     * @param request
     * @param response
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    protected void doPut( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        methodNotAllowed( response );
    }

    protected void doDelete( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        methodNotAllowed( response );
    }

    protected void doTrace( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        methodNotAllowed( response );
    }

    protected void doOptions( HttpRequest request, HttpResponse response, HttpContext context )
            throws HttpException,
                   IOException
    {
        methodNotAllowed( response );
    }

    /**
     * Sets the error output for unsupported method
     *
     * @param response
     */
    protected final void methodNotAllowed( HttpResponse response )
    {
        sendError( response, HttpStatus.SC_METHOD_NOT_ALLOWED, "Method not allowed" );
    }

    /**
     * Set the error response
     *
     * @param response
     * @param sc
     * @param message
     */
    protected final void sendError( HttpResponse response, int sc, String message )
    {
        response.setStatusCode( sc );
        response.setEntity( new StringEntity( "<html><body><h1>" + message + "</h1></body></html>", ContentType.TEXT_HTML ) );
    }

    /**
     * Set the error response
     *
     * @param response
     * @param sc
     * @param fmt
     * @param args
     */
    protected final void sendError( HttpResponse response, int sc, String fmt, Object... args )
    {
        sendError( response, sc, String.format( fmt, args ) );
    }

    /**
     * Send an OK entity
     *
     * @param response
     * @param entity
     */
    protected final void sendOk( HttpResponse response, HttpEntity entity )
    {
        response.setStatusCode( HttpStatus.SC_OK );
        response.setEntity( entity );
    }
}
