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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.Args;

/**
 * A java nio2 equivalent to {@link org.apache.http.entity.FileEntity}
 *
 * @author peter
 */
public class PathEntity
        extends AbstractHttpEntity
        implements Cloneable
{

    protected final Path file;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public PathEntity( final Path file, final ContentType contentType )
    {
        super();
        this.file = Args.notNull( file, "Path" );
        if( contentType != null ) {
            setContentType( contentType.toString() );
        }
    }

    public PathEntity( final Path file )
    {
        this( file, ContentTypeResolver.resolve( file ) );
    }

    @Override
    public boolean isRepeatable()
    {
        return true;
    }

    @Override
    public long getContentLength()
    {
        try {
            return Files.size( file );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public InputStream getContent()
            throws IOException
    {
        return Files.newInputStream( file, StandardOpenOption.READ );
    }

    @Override
    public void writeTo( final OutputStream outstream )
            throws IOException
    {
        Files.copy( file, outstream );
    }

    /**
     * Tells that this entity is not streaming.
     *
     * @return {@code false}
     */
    @Override
    public boolean isStreaming()
    {
        return false;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException
    {
        // Path instance is considered immutable
        // No need to make a copy of it
        return super.clone();
    }

    public static class SizeOnly
            extends PathEntity
    {

        public SizeOnly( final Path file, final ContentType contentType )
        {
            super( file, contentType );
        }

        public SizeOnly( final Path file )
        {
            super( file );
        }

        @Override
        public InputStream getContent()
                throws IOException
        {
            return new InputStream()
            {
                @Override
                public int read()
                        throws IOException
                {
                    return -1;
                }
            };
        }

        @Override
        public void writeTo( OutputStream outstream )
                throws IOException
        {
        }

    }
}
