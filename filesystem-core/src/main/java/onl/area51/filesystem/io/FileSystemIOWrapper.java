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
package onl.area51.filesystem.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

/**
 * A wrapper that delegates to another {@link FileSystemIO} instance with a hook implemented for retrieving a path from another
 * location if it does not exist.
 *
 * @author peter
 */
public abstract class FileSystemIOWrapper
        implements FileSystemIO
{

    private final FileSystemIO delegate;

    public FileSystemIOWrapper( FileSystemIO delegate )
    {
        this.delegate = delegate;
    }

    @Override
    public Path toPath( char[] path )
            throws IOException
    {
        return delegate.toPath( path );
    }

    protected final FileSystemIO getDelegate()
    {
        return delegate;
    }

    @Override
    public void close()
            throws IOException
    {
        delegate.close();
    }

    @Override
    public Path getBaseDirectory()
    {
        return delegate.getBaseDirectory();
    }

    @Override
    public boolean isTemporary()
    {
        return delegate.isTemporary();
    }

    @Override
    public boolean isDirectory( char[] path )
            throws IOException
    {
        return delegate.isDirectory( path );
    }

    @Override
    public boolean isFile( char[] path )
            throws IOException
    {
        return delegate.isFile( path );
    }

    @Override
    public boolean exists( char[] path )
            throws IOException
    {
        return delegate.exists( path );
    }

    @Override
    public void createDirectory( char[] path, FileAttribute<?>[] attrs )
            throws IOException
    {
        delegate.createDirectory( path, attrs );
    }

    @Override
    public InputStream newInputStream( char[] path )
            throws IOException
    {
        return delegate.newInputStream( path );
    }

    @Override
    public OutputStream newOutputStream( char[] path, OpenOption... options )
            throws IOException
    {
        return delegate.newOutputStream( path, options );
    }

    @Override
    public void deleteFile( char[] path, boolean exists )
            throws IOException
    {
        delegate.deleteFile( path, exists );
    }

    @Override
    public SeekableByteChannel newByteChannel( char[] path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return delegate.newByteChannel( path, options, attrs );
    }

    @Override
    public FileChannel newFileChannel( char[] path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return delegate.newFileChannel( path, options, attrs );
    }

    @Override
    public void copyFile( boolean b, char[] src, char[] dest, CopyOption... options )
            throws IOException
    {
        // This will retrieve source as necessary
        exists( src );
        delegate.copyFile( b, src, dest, options );
    }

    @Override
    public BasicFileAttributes getAttributes( char[] path )
            throws IOException
    {
        exists( path );
        return delegate.getAttributes( path );
    }

    @Override
    public BasicFileAttributeView getAttributeView( char[] path )
    {
        return delegate.getAttributeView( path );
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream( char[] path, DirectoryStream.Filter<? super Path> filter )
            throws IOException
    {
        return delegate.newDirectoryStream( path, filter );
    }

    @Override
    public void expire()
    {
        delegate.expire();
    }

    @Override
    public long size( char[] path )
            throws IOException
    {
        return delegate.size( path );
    }

}
