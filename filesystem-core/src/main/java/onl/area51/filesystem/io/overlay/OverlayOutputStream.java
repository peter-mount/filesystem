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
package onl.area51.filesystem.io.overlay;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author peter
 */
public class OverlayOutputStream
        extends OutputStream
{

    private final OverlaySender remoteSender;
    private final PathSynchronizer pathSynchronizer;
    private final char[] path;
    private final OutputStream delegate;
    /**
     * Is this stream closed. Also used to prevent us from sending multiple times which can occur when we wrap with say a
     * reader, then close() can be called multiple times.
     */
    private boolean closed;

    public OverlayOutputStream( OverlaySender remoteSender, PathSynchronizer pathSynchronizer, char[] path,
                                OutputStream delegate )
    {
        this.remoteSender = remoteSender;
        this.pathSynchronizer = pathSynchronizer;
        this.path = path;
        this.delegate = delegate;
    }

    protected final void assertOpen()
    {
        if( closed )
        {
            throw new IllegalStateException( "Stream closed" );
        }
    }

    @Override
    public void write( int b )
            throws IOException
    {
        assertOpen();
        delegate.write( b );
    }

    @Override
    public void write( byte[] b )
            throws IOException
    {
        assertOpen();
        delegate.write( b );
    }

    @Override
    public void write( byte[] b, int off, int len )
            throws IOException
    {
        assertOpen();
        delegate.write( b, off, len );
    }

    @Override
    public void flush()
            throws IOException
    {
        delegate.flush();
    }

    @Override
    public void close()
            throws IOException
    {
        if( !closed )
        {
            closed = true;

            delegate.close();

            if( pathSynchronizer == null )
            {
                closeImpl();
            }
            else
            {
                pathSynchronizer.execute( path, this::closeImpl );
            }
        }
    }

    private Void closeImpl()
            throws IOException
    {
        remoteSender.send( path );
        return null;
    }

}
