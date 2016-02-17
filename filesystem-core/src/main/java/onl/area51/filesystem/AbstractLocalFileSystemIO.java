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
package onl.area51.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * {@link FileSystemIO} implementation of a directory
 *
 * @author peter
 */
public abstract class AbstractLocalFileSystemIO
        implements FileSystemIO
{

    private final Map<String, ?> env;

    private final Path basePath;
    private final File baseFile;

    public AbstractLocalFileSystemIO( Path basePath, Map<String, ?> env )
    {
        this.env = env;

        String customBasePath = env != null ? (String) env.get( BASE_DIRECTORY ) : null;
        Path path = customBasePath == null || customBasePath.trim().isEmpty() ? basePath : Paths.get( customBasePath );
        this.basePath = Objects.requireNonNull( path.toAbsolutePath(), "No basePath provided" );

        baseFile = this.basePath.toFile();
        if( isTemporary() ) {
            baseFile.deleteOnExit();
        }

        baseFile.mkdirs();
    }

    @Override
    public void close()
            throws IOException
    {
        if( isTemporary() ) {
            deleteDir( baseFile );
        }
    }

    private void deleteDir( File d )
            throws IOException
    {
        if( d.isDirectory() ) {
            for( File f: d.listFiles() ) {
                deleteDir( f );
            }
        }
        d.delete();
    }

    @Override
    public final Path getBaseDirectory()
    {
        return basePath;
    }

    @Override
    public final boolean isTemporary()
    {
        return env != null && env.containsKey( DELETE_ON_EXIT );
    }

    protected abstract Path toPath( char[] path )
            throws IOException;

    protected final File toFile( char[] path )
            throws IOException
    {
        File f = toPath( path ).toFile();
        if( isTemporary() ) {
            f.deleteOnExit();
        }
        return f;
    }

    @Override
    public final boolean exists( char[] path )
            throws IOException
    {
        return toFile( path ).exists();
    }

    @Override
    public  void createDirectory( char[] path, FileAttribute<?>[] attrs )
            throws IOException
    {
        Files.createDirectories( toPath( path ) );
    }

    @Override
    public final InputStream newInputStream( char[] path )
            throws IOException
    {
        return new FileInputStream( toFile( path ) );
    }

    @Override
    public final OutputStream newOutputStream( char[] path, OpenOption... options )
            throws IOException
    {
        Path p = toPath( path );
        Files.createDirectories( p.getParent() );
        return new FileOutputStream( p.toFile() );
    }

    @Override
    public final void deleteFile( char[] path, boolean exists )
            throws IOException
    {
        toFile( path ).delete();
    }

    @Override
    public final SeekableByteChannel newByteChannel( char[] path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return Files.newByteChannel( toPath( path ), options, attrs );
    }

    @Override
    public final FileChannel newFileChannel( char[] path, Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return FileChannel.open( toPath( path ), options, attrs );
    }

    @Override
    public final void copyFile( boolean b, char[] src, char[] dest, CopyOption... options )
            throws IOException
    {
        Files.copy( toPath( src ), toPath( dest ), options );
    }

    @Override
    public final BasicFileAttributes getAttributes( char[] path )
            throws IOException
    {
        if( path.length == 0 || (path.length == 1 && path[0] == '/') ) {
            return RootFileAttributes.INSTANCE;
        }
        Path p = toPath( path );
        try {
            return p.getFileSystem().provider().readAttributes( p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS );
        }
        catch( IOException ex ) {
            throw new IOException( "path=\"" + String.valueOf( path ) + "\" " + p.toString(), ex );
        }
    }

    @Override
    public final BasicFileAttributeView getAttributeView( char[] path )
    {
        try {
            Path p = toPath( path );
            return p.getFileSystem().provider().getFileAttributeView( p, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream( char[] path, DirectoryStream.Filter<? super Path> filter )
            throws IOException
    {
        Path p = toPath( path );
        DirectoryStream<Path> ds = p.getFileSystem().provider().newDirectoryStream( p, filter );

        // FIXME ensure we cannot go outside of the cache directory, i.e. root does not show ..
        return ds;
    }

}
