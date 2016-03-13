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
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author peter
 */
public class MemoryBuffer
{

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    private ByteBuffer buffer;

    private static final int SIZE = 1024;

    private ByteBuffer allocate()
    {
        if( buffer == null || !buffer.hasRemaining() ) {
            buffer = allocate( buffer, SIZE );
        }
        return buffer;
    }

    private ByteBuffer allocate( ByteBuffer b, int expandBy )
    {
        int newSize = b == null ? expandBy : (b.limit() + expandBy);
        ByteBuffer newBuffer = ByteBuffer.allocate( newSize );
        if( b != null ) {
            newBuffer.put( b );
        }
        return newBuffer;
    }

    public boolean isEmpty()
    {
        readLock.lock();
        try {
            return buffer == null || buffer.position() == 0;
        }
        finally {
            readLock.unlock();
        }
    }

    public int size()
    {
        readLock.lock();
        try {
            return buffer == null ? 0 : buffer.position();
        }
        finally {
            readLock.unlock();
        }
    }

    public void truncate()
    {
        writeLock.lock();
        try {
            // Remove the buffer if we are over the initial block size
            if( buffer != null && buffer.limit() > SIZE ) {
                buffer = null;
            }
            // If we still have a buffer then set append position to the start
            if( buffer != null ) {
                buffer.position( 0 );
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    public Reader getReader()
    {
        return new Reader()
        {
            private int pos = 0;

            @Override
            public int position()
            {
                return pos;
            }

            @Override
            public int size()
            {
                return MemoryBuffer.this.size();
            }

            @Override
            public int seek( int pos )
            {
                int oldPos = this.pos;
                this.pos = pos;
                return oldPos;
            }

            @Override
            public int read( ByteBuffer dst )
                    throws IOException
            {
                readLock.lock();
                try {
                    if( buffer == null ) {
                        return 0;
                    }

                    int c = 0;
                    for( ; buffer.hasRemaining() && dst.hasRemaining(); c++ ) {
                        dst.put( buffer.get() );
                    }
                    return c;
                }
                finally {
                    readLock.unlock();
                }
            }

            @Override
            public int read()
                    throws IOException
            {
                readLock.lock();
                try {
                    return buffer == null || pos < buffer.position() ? buffer.get( pos++ ) : -1;
                }
                finally {
                    readLock.unlock();
                }
            }

            @Override
            public int read( byte[] b, int off, int len )
                    throws IOException
            {
                readLock.lock();
                try {
                    int c = 0;
                    if( buffer != null ) {
                        while( c < len && pos < buffer.position() ) {
                            b[off + c] = buffer.get( pos++ );
                            c++;
                        }
                    }
                    return c;
                }
                finally {
                    readLock.unlock();
                }
            }
        };
    }

    public Appender getAppender()
    {
        return new Appender()
        {

            @Override
            public void append( byte b )
                    throws IOException
            {
                writeLock.lock();
                try {
                    allocate().put( b );
                }
                finally {
                    writeLock.unlock();
                }
            }

            @Override
            public void append( byte[] b, int off, int len )
                    throws IOException
            {
                writeLock.lock();
                try {
                    ByteBuffer buf = allocate();
                    for( int i = 0, j = off; i < len; i++, j++ ) {
                        if( !buf.hasRemaining() ) {
                            buf = allocate();
                        }
                        buf.put( b[j] );
                    }
                }
                finally {
                    writeLock.unlock();
                }
            }

            @Override
            public int append( ByteBuffer src )
                    throws IOException
            {
                writeLock.lock();
                try {
                    ByteBuffer dst = allocate();
                    int c = 0;
                    while( src.hasRemaining() ) {
                        if( !dst.hasRemaining() ) {
                            dst = allocate();
                        }
                        dst.put( src.get() );
                        c++;
                    }
                    return c;
                }
                finally {
                    writeLock.unlock();
                }
            }

            @Override
            public int position()
            {
                readLock.lock();
                try {
                    return buffer == null ? 0 : buffer.position();
                }
                finally {
                    readLock.unlock();
                }
            }

        };
    }

    public static interface Reader
    {

        int position();

        int read( ByteBuffer dst )
                throws IOException;

        int read()
                throws IOException;

        default int read( byte[] b )
                throws IOException
        {
            return read( b, 0, b.length );
        }

        int read( byte[] b, int off, int len )
                throws IOException;

        int size();

        int seek( int pos );
    }

    public static interface Appender
    {

        int append( ByteBuffer src )
                throws IOException;

        void append( byte b )
                throws IOException;

        default void append( byte[] b )
                throws IOException
        {
            append( b, 0, b.length );
        }

        void append( byte[] b, int off, int len )
                throws IOException;

        int position();

    }
}
