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
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import onl.area51.filesystem.AbstractFileSystemProvider;
import onl.area51.filesystem.io.Flat;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author peter
 */
@MetaInfServices(FileSystemProvider.class)
public class MemoryFileSystemProvider
        extends AbstractFileSystemProvider<MemoryFileSystem, MemoryPath>
{

    @Override
    public String getScheme()
    {
        return "memory";
    }

    @Override
    protected MemoryFileSystem createFileSystem( URI uri, Path p, Map<String, Object> env )
            throws IOException
    {
        return new MemoryFileSystem( uri, this, p, env, Flat::new );
    }

    //@Override
    protected MemoryPath toPath( Path path )
    {
        Objects.requireNonNull( path );
        if( !(path instanceof MemoryPath) ) {
            throw new ProviderMismatchException();
        }
        return (MemoryPath) path.toAbsolutePath();
    }

    @Override
    public SeekableByteChannel newByteChannel( Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return toPath( path ).newByteChannel( options, attrs );
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream( Path dir, DirectoryStream.Filter<? super Path> filter )
            throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createDirectory( Path dir, FileAttribute<?>... attrs )
            throws IOException
    {
        toPath( dir ).createDirectory();
    }

    @Override
    public void delete( Path path )
            throws IOException
    {
        toPath( path ).delete();
    }

    @Override
    public void copy( Path source, Path target, CopyOption... options )
            throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void move( Path source, Path target, CopyOption... options )
            throws IOException
    {
        MemoryPath src = toPath( source );
        MemoryPath dst = toPath( target );

        if( !src.isSameFile( dst ) ) {
            src.move( dst, options );
        }
    }

    @Override
    public boolean isSameFile( Path path, Path path2 )
            throws IOException
    {
        return toPath( path ).isSameFile( toPath( path2 ) );
    }

    @Override
    public boolean isHidden( Path path )
            throws IOException
    {
        return false;
    }

    @Override
    public FileStore getFileStore( Path path )
            throws IOException
    {
        return toPath( path ).getFileStore();
    }

    @Override
    public void checkAccess( Path path, AccessMode... modes )
            throws IOException
    {
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView( Path path, Class<V> type, LinkOption... options )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes( Path path, Class<A> type, LinkOption... options )
            throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, Object> readAttributes( Path path, String attributes, LinkOption... options )
            throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttribute( Path path, String attribute, Object value, LinkOption... options )
            throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
}
