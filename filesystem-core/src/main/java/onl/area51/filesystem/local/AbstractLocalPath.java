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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Set;
import onl.area51.filesystem.AbstractPath;

/**
 *
 * @author Xueming Shen, Rajendra Gutupalli,Jaya Hangal
 * @param <F>
 * @param <P>
 */
public abstract class AbstractLocalPath<F extends AbstractLocalFileSystem<F, P, ?>, P extends AbstractLocalPath<F, P>>
        extends AbstractPath<F, P>
{

    protected AbstractLocalPath( F fs, char[] path )
    {
        super( fs, path, false );
    }

    protected AbstractLocalPath( F fs, char[] path, boolean normalized )
    {
        super( fs, path );
    }

    @Override
    public void createDirectory( FileAttribute<?>... attrs )
            throws IOException
    {
        fs.getFileSystemIO().createDirectory( getResolvedPath(), attrs );
    }

    @Override
    public InputStream newInputStream( OpenOption... options )
            throws IOException
    {
        if( options.length > 0 ) {
            for( OpenOption opt: options ) {
                if( opt != READ ) {
                    throw new UnsupportedOperationException( "'" + opt + "' not allowed" );
                }
            }
        }
        return fs.getFileSystemIO().newInputStream( getResolvedPath() );
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream( DirectoryStream.Filter<? super Path> filter )
            throws IOException
    {
        return fs.getFileSystemIO().newDirectoryStream( getResolvedPath(), filter );
    }

    public boolean isHidden()
    {
        return false;
    }

    @Override
    public void delete()
            throws IOException
    {
        fs.getFileSystemIO().deleteFile( getResolvedPath(), true );
    }

    @Override
    public void deleteIfExists()
            throws IOException
    {
        fs.getFileSystemIO().deleteFile( getResolvedPath(), false );
    }

    @Override
    public BasicFileAttributes getAttributes()
            throws IOException
    {
        BasicFileAttributes zfas = fs.getFileSystemIO().getAttributes( getResolvedPath() );
        if( zfas == null ) {
            throw new NoSuchFileException( toString() );
        }
        return zfas;
    }

    @Override
    public boolean isSameFile( Path other )
            throws IOException
    {
        if( this.equals( other ) ) {
            return true;
        }
        if( other == null || this.getFileSystem() != other.getFileSystem() ) {
            return false;
        }
        return Arrays.equals( this.getResolvedPath(), ((AbstractPath) other).getResolvedPath() );
    }

    @Override
    public boolean isFile()
            throws IOException
    {
        return fs.getFileSystemIO().isFile( getResolvedPath() );
    }

    @Override
    public boolean isDirectory()
            throws IOException
    {
        return fs.getFileSystemIO().isDirectory( getResolvedPath() );
    }

    @Override
    public SeekableByteChannel newByteChannel( Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return fs.getFileSystemIO().newByteChannel( getResolvedPath(), options, attrs );
    }

    @Override
    public FileChannel newFileChannel( Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException
    {
        return fs.getFileSystemIO().newFileChannel( getResolvedPath(), options, attrs );
    }

    @Override
    public boolean exists()
    {
        if( path.length == 1 && path[0] == '/' ) {
            return true;
        }
        try {
            return fs.getFileSystemIO().exists( getResolvedPath() );
        }
        catch( IOException x ) {
        }
        return false;
    }

    @Override
    public OutputStream newOutputStream( OpenOption... options )
            throws IOException
    {
        if( options.length == 0 ) {
            return fs.getFileSystemIO().newOutputStream( getResolvedPath(), CREATE_NEW, WRITE );
        }
        return fs.getFileSystemIO().newOutputStream( getResolvedPath(), options );
    }

    @Override
    public void move( P target, CopyOption... options )
            throws IOException
    {
        if( Files.isSameFile( getFileSystem().getCachePath(), target.getFileSystem().getCachePath() ) ) {
            fs.getFileSystemIO().copyFile( true, getResolvedPath(), target.getResolvedPath(), options );
        }
        else {
            copyToTarget( target, options );
            delete();
        }
    }

    @Override
    public void copy( P target, CopyOption... options )
            throws IOException
    {
        if( Files.isSameFile( getFileSystem().getCachePath(), target.getFileSystem().getCachePath() ) ) {
            fs.getFileSystemIO().copyFile( false, getResolvedPath(), target.getResolvedPath(), options );
        }
        else {
            copyToTarget( target, options );
        }
    }

    private void copyToTarget( AbstractPath target, CopyOption... options )
            throws IOException
    {
        boolean replaceExisting = false;
        for( CopyOption opt: options ) {
            if( opt == REPLACE_EXISTING ) {
                replaceExisting = true;
            }
        }

        // attributes of source file
        BasicFileAttributes attrs = getAttributes();

        // check if target exists
        boolean exists;
        if( replaceExisting ) {
            try {
                target.deleteIfExists();
                exists = false;
            }
            catch( DirectoryNotEmptyException x ) {
                exists = true;
            }
        }
        else {
            exists = target.exists();
        }
        if( exists ) {
            throw new FileAlreadyExistsException( target.toString() );
        }

        if( attrs.isDirectory() ) {
            // create directory or file
            target.createDirectory();
        }
        else {
            try( InputStream is = fs.getFileSystemIO().newInputStream( getResolvedPath() );
                 OutputStream os = target.newOutputStream() ) {
                byte[] buf = new byte[8192];
                int n;
                while( (n = is.read( buf )) != -1 ) {
                    os.write( buf, 0, n );
                }
            }
        }
    }
}
