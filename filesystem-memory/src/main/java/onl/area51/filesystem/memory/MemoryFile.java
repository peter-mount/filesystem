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
package onl.area51.filesystem.memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author peter
 */
public class MemoryFile
        extends MemoryNode
{

    private static final Logger logger = Logger.getLogger( "MemoryFS" );

    private final MemoryBuffer buffer = new MemoryBuffer();

    public MemoryFile( MemoryDirectory parent, String name )
    {
        super( parent, name );
    }

    @Override
    protected void free()
    {
        logger.log( Level.INFO, () -> "Free " + getName() );
    }

    public boolean isEmpty()
    {
        return buffer.isEmpty();
    }

    public int size()
    {
        return buffer.size();
    }

    public void truncate()
    {
        buffer.truncate();
    }

    public MemoryBuffer.Reader getReader()
    {
        return buffer.getReader();
    }

    public MemoryBuffer.Appender getAppender()
    {
        return buffer.getAppender();
    }

    public InputStream getInputStream()
    {
        if( buffer.isEmpty() ) {
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

        MemoryBuffer.Reader reader = buffer.getReader();
        return new InputStream()
        {
            @Override
            public int read()
                    throws IOException
            {
                return reader.read();
            }

            @Override
            public int read( byte[] b )
                    throws IOException
            {
                return reader.read( b );
            }

            @Override
            public int read( byte[] b, int off, int len )
                    throws IOException
            {
                return reader.read( b, off, len );
            }

        };
    }

    public OutputStream getOutputStream( StandardOpenOption... opts )
    {
        boolean append = true;
        for( OpenOption opt: opts ) {
            if( opt == StandardOpenOption.APPEND ) {
                append = true;
            }
            if( opt == StandardOpenOption.TRUNCATE_EXISTING ) {
                append = false;
            }
        }
        return getOutputStream( append );
    }

    public OutputStream getOutputStream( boolean append )
    {
        if( !append ) {
            buffer.truncate();
        }

        MemoryBuffer.Appender appender = buffer.getAppender();

        return new OutputStream()
        {
            @Override
            public void write( int b )
                    throws IOException
            {
                appender.append( (byte) b );
            }

            @Override
            public void write( byte[] b )
                    throws IOException
            {
                appender.append( b );
            }

            @Override
            public void write( byte[] b, int off, int len )
                    throws IOException
            {
                appender.append( b, off, len );
            }

        };
    }

    public SeekableByteChannel newByteChannel( Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        boolean append = true;
        if( options.contains( StandardOpenOption.APPEND ) ) {
            append = true;
        }
        else if( options.contains( StandardOpenOption.TRUNCATE_EXISTING ) ) {
            append = false;
        }

        if( options.contains( StandardOpenOption.READ ) ) {
            return new MemChannel.Read( buffer );
        }

        if( options.contains( StandardOpenOption.WRITE ) ) {
            if( !append ) {
                truncate();
            }
            return new MemChannel.Appender( buffer );
        }

        throw new IOException( "Must read or write" );
    }

}
