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
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.FileSystemIOWrapper;

/**
 * A wrapper that delegates to another {@link FileSystemIO} instance with a hook implemented for retrieving a path from another location if it does not exist.
 *
 * @author peter
 */
public abstract class OverlayFileSystemIO
        extends FileSystemIOWrapper
{

    private final PathSynchronizer pathSynchronizer;
    private final RemoteRetriever remoteRetriever;
    private final RemoteSender remoteSender;

    public OverlayFileSystemIO( FileSystemIO delegate, RemoteRetriever remoteRetriever )
    {
        this( delegate, null, remoteRetriever );
    }

    public OverlayFileSystemIO( FileSystemIO delegate, PathSynchronizer pathSynchronizer, RemoteRetriever remoteRetriever )
    {
        this( delegate, pathSynchronizer, remoteRetriever, null );
    }

    public OverlayFileSystemIO( FileSystemIO delegate, PathSynchronizer pathSynchronizer, RemoteRetriever remoteRetriever, RemoteSender remoteSender )
    {
        super( delegate );
        this.pathSynchronizer = pathSynchronizer;
        this.remoteRetriever = remoteRetriever;
        this.remoteSender = remoteSender;
    }

    @Override
    public InputStream newInputStream( char[] path )
            throws IOException
    {
        if( remoteRetriever == null ) {
            return getDelegate().newInputStream( path );
        }
        else {
            return newInputStreamRemote( path );
        }
    }

    @Override
    public OutputStream newOutputStream( char[] path, OpenOption... options )
            throws IOException
    {
        if( remoteSender == null ) {
            return getDelegate().newOutputStream( path, options );
        }
        else {
            try {
                return getDelegate().newOutputStream( path, options );
            }
            finally {
                remoteSender.send( path );
            }
        }
    }

    protected final InputStream newInputStreamRemote( char[] path )
            throws IOException
    {
        if( exists( path ) ) {
            try {
                return getDelegate().newInputStream( path );
            }
            catch( FileNotFoundException ex ) {
            }
        }

        if( pathSynchronizer == null ) {
            remoteRetriever.retrieve( path );
        }
        else {
            pathSynchronizer.execute( path, () -> {
                                  remoteRetriever.retrieve( path );
                                  return null;
                              } );
        }

        return getDelegate().newInputStream( path );
    }
}
