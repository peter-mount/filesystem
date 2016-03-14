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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.FileSystemIOWrapper;

/**
 * A wrapper that delegates to another {@link FileSystemIO} instance with a hook implemented for retrieving a path from another
 * location if it does not exist.
 *
 * @author peter
 */
public abstract class OverlayFileSystemIO
        extends FileSystemIOWrapper
{

    private static final Logger LOG = Logger.getLogger( "filesystem" );

    private final PathSynchronizer pathSynchronizer;
    private final OverlayRetriever retriever;
    private final OverlaySender sender;

    public OverlayFileSystemIO( FileSystemIO delegate, OverlayRetriever retriever )
    {
        this( delegate, null, retriever );
    }

    public OverlayFileSystemIO( FileSystemIO delegate, PathSynchronizer pathSynchronizer, OverlayRetriever retriever )
    {
        this( delegate, pathSynchronizer, retriever, null );
    }

    public OverlayFileSystemIO( FileSystemIO delegate, OverlaySender sender )
    {
        this( delegate, null, sender );
    }

    public OverlayFileSystemIO( FileSystemIO delegate, PathSynchronizer pathSynchronizer, OverlaySender sender )
    {
        this( delegate, pathSynchronizer, null, sender );
    }

    public OverlayFileSystemIO( FileSystemIO delegate, PathSynchronizer pathSynchronizer, OverlayRetriever retriever,
                                OverlaySender sender )
    {
        super( delegate );
        this.pathSynchronizer = pathSynchronizer;
        this.retriever = retriever;
        this.sender = sender;
    }

    @Override
    public InputStream newInputStream( char[] path )
            throws IOException
    {
        if( retriever == null )
        {
            return getDelegate().newInputStream( path );
        }
        else
        {
            return newInputStreamRemote( path );
        }
    }

    @Override
    public OutputStream newOutputStream( char[] path, OpenOption... options )
            throws IOException
    {
        if( sender == null )
        {
            return getDelegate().newOutputStream( path, options );
        }
        else
        {
            return new OverlayOutputStream( sender, pathSynchronizer, path, getDelegate().newOutputStream( path, options ) );
        }
    }

    protected final InputStream newInputStreamRemote( char[] path )
            throws IOException
    {
        if( exists( path ) )
        {
            try
            {
                return getDelegate().newInputStream( path );
            } catch( FileNotFoundException ex )
            {
            }
        }

        if( pathSynchronizer == null )
        {
            retriever.retrieve( path );
        }
        else
        {
            pathSynchronizer.execute( path, ()
                                      -> 
                                      {
                                          retriever.retrieve( path );
                                          return null;
                              } );
        }

        return getDelegate().newInputStream( path );
    }

    @Override
    public void close()
            throws IOException
    {
        try
        {
            if( pathSynchronizer != null )
            {
                pathSynchronizer.close();
            }
        }
        finally
        {
            super.close();
        }
    }

}
