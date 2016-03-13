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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;
import onl.area51.filesystem.AbstractPath;
import onl.area51.filesystem.FileSystemUtils;

/**
 *
 * @author peter
 */
public class MemoryPath
        extends AbstractPath<MemoryFileSystem, MemoryPath>
{

    MemoryPath( MemoryFileSystem fs, char[] path )
    {
        super( fs, path, true );
    }

    @Override
    public MemoryPath getRoot()
    {
        if( this.isAbsolute() ) {
            return fs.createPath( new char[]{path[0]} );
        }
        else {
            return null;
        }
    }

    @Override
    public URI toUri()
    {
        try {
            // FIXME confirm uri format
            return new URI( "memory", fs.getFileStore().name(), String.valueOf( toAbsolutePath().path ), null, null );
        }
        catch( Exception ex ) {
            throw new AssertionError( ex );
        }
    }

    @Override
    public void createDirectory( FileAttribute<?>... attrs )
            throws IOException
    {
        String name = getName( getNameCount() - 1 ).toString();
        MemoryDirectory parent = fs.getFileStore().findDirectory( this ).getParent();
        parent.put( name, new MemoryDirectory( parent, name ) );
    }

    @Override
    public InputStream newInputStream( OpenOption... options )
            throws IOException
    {
        MemoryFile file = fs.getFileStore().findFile( this );

        // TODO implement StandardOpenOption.DELETE_ON_CLOSE ?
        return file.getInputStream();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream( DirectoryStream.Filter<? super Path> filter )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete()
            throws IOException
    {
        String name = getName( getNameCount() - 1 ).toString();
        MemoryDirectory parent = fs.getFileStore().findDirectory( this ).getParent();
        parent.remove( name );
    }

    @Override
    public void deleteIfExists()
            throws IOException
    {
        try {
            delete();
        }
        catch( FileNotFoundException ex ) {
            // ignore
        }
    }

    @Override
    public BasicFileAttributes getAttributes()
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile( Path other )
            throws IOException
    {
        if( other instanceof MemoryPath ) {
            try {
                MemoryFileStore mfs = fs.getFileStore();
                return mfs.findNode( this ) == mfs.findNode( (MemoryPath) other );
            }
            catch( FileNotFoundException ex ) {
            }
        }
        return false;
    }

    @Override
    public SeekableByteChannel newByteChannel( Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        MemoryFileStore mfs = fs.getFileStore();
        MemoryFile file;
        if( options.contains( StandardOpenOption.CREATE_NEW ) ) {
            // FIXME need to fail if it already exists
            file = mfs.getOrCreateFile( this );
        }
        else if( options.contains( StandardOpenOption.CREATE ) ) {
            file = mfs.getOrCreateFile( this );
        }
        else {
            file = mfs.findFile( this );
        }

        return file.newByteChannel( this, options, attrs );
    }

    @Override
    public FileChannel newFileChannel( Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists()
    {
        try {
            return fs.getFileStore().findNode( this ) != null;
        }
        catch( IOException ex ) {
            return false;
        }
    }

    @Override
    public OutputStream newOutputStream( OpenOption... options )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move( MemoryPath target, CopyOption... options )
            throws IOException
    {
        boolean repExisting = false;
        for( CopyOption opt: options ) {
            // No need to implement StandardCopyOption.ATOMIC_MOVE as we always are & StandardCopyOption.COPY_ATTRIBUTES is unsupported
            if( opt == StandardCopyOption.REPLACE_EXISTING ) {
                repExisting = true;
            }
        }
    }

    @Override
    public void copy( MemoryPath target, CopyOption... options )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFile()
            throws IOException
    {
        return fs.getFileStore().findNode( this ) instanceof MemoryFile;
    }

    @Override
    public boolean isDirectory()
            throws IOException
    {
        return fs.getFileStore().findNode( this ) instanceof MemoryDirectory;
    }

}
