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
package onl.area51.filesystem.local;

import com.sun.nio.zipfs.ZipFileAttributes;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import onl.area51.filesystem.AbstractFileSystemProvider;

/**
 * Common base provider for FileSystem's that deal with the local FileSystem.
 *
 * @author peter
 * @param <F>
 * @param <P>
 */
public abstract class AbstractLocalFileSystemProvider<F extends AbstractLocalFileSystem<F, P, ?>, P extends AbstractLocalPath<F, P>>
        extends AbstractFileSystemProvider<F, P>
{

    /**
     * Checks that the given file is a UnixPath
     *
     * @param path
     * @return
     */
    protected abstract P toCachePath( Path path );

    @Override
    public void checkAccess( Path path, AccessMode... modes )
            throws IOException
    {
        //toCachePath( path ).checkAccess( modes );
    }

    @Override
    public void copy( Path src, Path target, CopyOption... options )
            throws IOException
    {
        toCachePath( src ).copy( toCachePath( target ), options );
    }

    @Override
    public void createDirectory( Path path, FileAttribute<?>... attrs )
            throws IOException
    {
        toCachePath( path ).createDirectory( attrs );
    }

    @Override
    public final void delete( Path path )
            throws IOException
    {
        toCachePath( path ).delete();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <V extends FileAttributeView> V getFileAttributeView( Path path, Class<V> type, LinkOption... options )
    {
        if( type.isAssignableFrom( BasicFileAttributeView.class ) )
        {
            P p = toCachePath( path );
            return (V) p.getFileSystem().getFileSystemIO().getAttributeView( p.getResolvedPath() );
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore( Path path )
            throws IOException
    {
        return toCachePath( path ).getFileStore();
    }

    @Override
    public boolean isHidden( Path path )
    {
        return toCachePath( path ).isHidden();
    }

    @Override
    public boolean isSameFile( Path path, Path other )
            throws IOException
    {
        return toCachePath( path ).isSameFile( other );
    }

    @Override
    public void move( Path src, Path target, CopyOption... options )
            throws IOException
    {
        toCachePath( src ).move( toCachePath( target ), options );
    }

    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel( Path path, Set<? extends OpenOption> options,
                                                               ExecutorService exec, FileAttribute<?>... attrs )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel( Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return toCachePath( path ).newByteChannel( options, attrs );
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream( Path path, DirectoryStream.Filter<? super Path> filter )
            throws IOException
    {
        return toCachePath( path ).newDirectoryStream( filter );
    }

    @Override
    public FileChannel newFileChannel( Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return toCachePath( path ).newFileChannel( options, attrs );
    }

    @Override
    public InputStream newInputStream( Path path, OpenOption... options )
            throws IOException
    {
        return toCachePath( path ).newInputStream( options );
    }

    @Override
    public OutputStream newOutputStream( Path path, OpenOption... options )
            throws IOException
    {
        return toCachePath( path ).newOutputStream( options );
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes( Path path, Class<A> type, LinkOption... options )
            throws IOException
    {
        if( type == BasicFileAttributes.class || type == ZipFileAttributes.class )
        {
            return (A) toCachePath( path ).getAttributes();
        }
        return null;
    }

    @Override
    public Map<String, Object> readAttributes( Path path, String attribute, LinkOption... options )
            throws IOException
    {
        return new HashMap<>();
        //return toCachePath( path ).readAttributes( attribute, options );
    }

    @Override
    public Path readSymbolicLink( Path link )
            throws IOException
    {
        throw new UnsupportedOperationException( "Not supported." );
    }

    @Override
    public void setAttribute( Path path, String attribute,
                              Object value, LinkOption... options )
            throws IOException
    {
        //toCachePath( path ).setAttribute( attribute, value, options );
    }
}
