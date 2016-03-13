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
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

/**
 *
 * @author peter
 */
abstract class MemChannel
        implements SeekableByteChannel
{

    private final MemoryBuffer buffer;

    public MemChannel( MemoryBuffer buffer )
    {
        this.buffer = buffer;
    }

    @Override
    public long size()
            throws IOException
    {
        return buffer.size();
    }

    @Override
    public SeekableByteChannel truncate( long size )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @Override
    public void close()
            throws IOException
    {
    }

    public static class Read
            extends MemChannel
    {

        private final MemoryBuffer.Reader r;

        public Read( MemoryBuffer buffer )
        {
            super( buffer );
            r = buffer.getReader();
        }

        @Override
        public int read( ByteBuffer dst )
                throws IOException
        {
            return r.read( dst );
        }

        @Override
        public int write( ByteBuffer src )
                throws IOException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public long position()
                throws IOException
        {
            return r.position();
        }

        @Override
        public SeekableByteChannel position( long newPosition )
                throws IOException
        {
            r.seek( (int) newPosition );
            return this;
        }

    }

    public static class Appender
            extends MemChannel
    {

        private final MemoryBuffer.Appender w;

        public Appender( MemoryBuffer buffer )
        {
            super( buffer );
            w = buffer.getAppender();
        }

        @Override
        public int read( ByteBuffer dst )
                throws IOException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int write( ByteBuffer src )
                throws IOException
        {
            return w.append( src );
        }

        @Override
        public long position()
                throws IOException
        {
            return w.position();
        }

        @Override
        public SeekableByteChannel position( long newPosition )
                throws IOException
        {
            throw new UnsupportedOperationException();
        }

    }

}
