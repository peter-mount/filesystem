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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * Base {@link HttpRequestHandler} that's backed by a {@link FileSystem}.
 *
 * @author peter
 */
public abstract class AbstractFileSystemRequestHandler
        extends AbstractRequestHandler
{

    private final String prefix;
    private final int prefixLength;
    private final FileSystem filesystem;

    /**
     *
     * @param prefix     Path prefix for this handler
     * @param filesystem FileSystem
     */
    public AbstractFileSystemRequestHandler( String prefix, FileSystem filesystem )
    {
        Objects.requireNonNull( prefix );
        Objects.requireNonNull( filesystem );

        // Ensure prefix starts with / at least 2 chars long and not "//"
        if( !prefix.startsWith( "/" ) || prefix.length() < 2 || prefix.equals( "//" ) )
        {
            throw new IllegalArgumentException( prefix );
        }

        // ensure prefix ends with /
        this.prefix = prefix.endsWith( "/" ) ? prefix : (prefix + "/");

        // PrefixLength does not include trailing /
        prefixLength = this.prefix.length() - 1;

        this.filesystem = filesystem;
    }

    /**
     * The underlying FileSystem
     *
     * @return
     */
    protected final FileSystem getFilesystem()
    {
        return filesystem;
    }

    /**
     * The path prefix
     *
     * @return
     */
    protected final String getPrefix()
    {
        return prefix;
    }

    /**
     * Translates the request uri to a Path in the FileSystem. If the path cannot be translated then this returns null.
     *
     * @param request
     * @return Path or null
     */
    protected final Path getPath( HttpRequest request )
    {
        String target = request.getRequestLine().getUri();
        if( target.equals( prefix ) || target.equals( prefix.substring( 0, prefixLength ) ) )
        {
            return filesystem.getPath( "/" );
        }
        else if( target.startsWith( prefix ) )
        {
            return filesystem.getPath( target.substring( prefixLength ) );
        }
        return null;
    }
}
