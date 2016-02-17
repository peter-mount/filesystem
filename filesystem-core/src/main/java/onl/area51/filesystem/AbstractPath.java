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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * @author Xueming Shen, Rajendra Gutupalli,Jaya Hangal
 * @param <F>
 * @param <P>
 */
public abstract class AbstractPath<F extends AbstractFileSystem<F, P, ?>, P extends AbstractPath<F, P>>
        implements Path
{

    protected static final char[] ROOT_PATH = {'/'};

    protected final F fs;
    protected final char[] path;
    @SuppressWarnings("VolatileArrayField")
    private volatile int[] offsets;
    private int hashcode = 0;  // cached hashcode (created lazily)

    protected AbstractPath( F fs, char[] path )
    {
        this( fs, path, false );
    }

    protected AbstractPath( F fs, char[] path, boolean normalized )
    {
        this.fs = fs;
        if( normalized ) {
            this.path = path;
        }
        else {
            this.path = normalize( path );
        }
    }

    public final FileStore getFileStore()
            throws IOException
    {
        // each ZipFileSystem only has one root (as requested for now)
        if( exists() ) {
            return fs.createFileStore( (P) this );
        }
        throw new NoSuchFileException( String.valueOf( path ) );
    }

    @Override
    public abstract P getRoot();

    @Override
    public Path getFileName()
    {
        initOffsets();
        int count = offsets.length;
        if( count == 0 ) {
            return null;  // no elements so no name
        }
        if( count == 1 && path[0] != '/' ) {
            return this;
        }
        int lastOffset = offsets[count - 1];
        int len = path.length - lastOffset;
        char[] result = new char[len];
        System.arraycopy( path, lastOffset, result, 0, len );
        return fs.createPath( result );
    }

    @Override
    public P getParent()
    {
        initOffsets();
        int count = offsets.length;
        if( count == 0 ) // no elements so no parent
        {
            return null;
        }
        int len = offsets[count - 1] - 1;
        if( len <= 0 ) // parent is root only (may be null)
        {
            return fs.createPath( ROOT_PATH );
        }
        char[] result = new char[len];
        System.arraycopy( path, 0, result, 0, len );
        return fs.createPath( result );
    }

    @Override
    public int getNameCount()
    {
        initOffsets();
        return offsets.length;
    }

    @Override
    public P getName( int index )
    {
        initOffsets();
        if( index < 0 || index >= offsets.length ) {
            throw new IllegalArgumentException();
        }
        int begin = offsets[index];
        int len;
        if( index == (offsets.length - 1) ) {
            len = path.length - begin;
        }
        else {
            len = offsets[index + 1] - begin - 1;
        }
        // construct result
        char[] result = new char[len];
        System.arraycopy( path, begin, result, 0, len );
        return fs.createPath( result );
    }

    @Override
    public P subpath( int beginIndex, int endIndex )
    {
        initOffsets();
        if( beginIndex < 0
            || beginIndex >= offsets.length
            || endIndex > offsets.length
            || beginIndex >= endIndex ) {
            throw new IllegalArgumentException();
        }

        // starting offset and length
        int begin = offsets[beginIndex];
        int len;
        if( endIndex == offsets.length ) {
            len = path.length - begin;
        }
        else {
            len = offsets[endIndex] - begin - 1;
        }
        // construct result
        char[] result = new char[len];
        System.arraycopy( path, begin, result, 0, len );
        return fs.createPath( result );
    }

    @Override
    public P toRealPath( LinkOption... options )
            throws IOException
    {
        return fs.createPath( getResolvedPath() ).toAbsolutePath();
    }

    @Override
    public P toAbsolutePath()
    {
        if( isAbsolute() ) {
            return (P) this;
        }
        else if( path.length == 0 ) {
            return fs.createPath( ROOT_PATH );
        }
        else {
            char c[] = new char[path.length + 1];
            c[0] = '/';
            System.arraycopy( path, 0, c, 1, path.length );
            return fs.createPath( c );
        }
    }

    @Override
    public abstract URI toUri();

    private boolean equalsNameAt( AbstractPath other, int index )
    {
        int mbegin = offsets[index];
        int mlen;
        if( index == (offsets.length - 1) ) {
            mlen = path.length - mbegin;
        }
        else {
            mlen = offsets[index + 1] - mbegin - 1;
        }
        int obegin = other.offsets[index];
        int olen;
        if( index == (other.offsets.length - 1) ) {
            olen = other.path.length - obegin;
        }
        else {
            olen = other.offsets[index + 1] - obegin - 1;
        }
        if( mlen != olen ) {
            return false;
        }
        int n = 0;
        while( n < mlen ) {
            if( path[mbegin + n] != other.path[obegin + n] ) {
                return false;
            }
            n++;
        }
        return true;
    }

    @Override
    public Path relativize( Path other )
    {
        final AbstractPath o = checkPath( other );
        if( o.equals( this ) ) {
            return fs.createPath( new char[0] );
        }
        if(/* this.getFileSystem() != o.getFileSystem() || */ this.isAbsolute() != o.isAbsolute() ) {
            throw new IllegalArgumentException();
        }
        int mc = this.getNameCount();
        int oc = o.getNameCount();
        int n = Math.min( mc, oc );
        int i = 0;
        while( i < n ) {
            if( !equalsNameAt( o, i ) ) {
                break;
            }
            i++;
        }
        int dotdots = mc - i;
        int len = dotdots * 3 - 1;
        if( i < oc ) {
            len += (o.path.length - o.offsets[i] + 1);
        }
        char[] result = new char[len];

        int pos = 0;
        while( dotdots > 0 ) {
            result[pos++] = '.';
            result[pos++] = '.';
            if( pos < len ) // no tailing slash at the end
            {
                result[pos++] = (char) '/';
            }
            dotdots--;
        }
        if( i < oc ) {
            System.arraycopy( o.path, o.offsets[i],
                              result, pos,
                              o.path.length - o.offsets[i] );
        }
        return fs.createPath( result );
    }

    @Override
    public F getFileSystem()
    {
        return fs;
    }

    @Override
    public boolean isAbsolute()
    {
        return path.length > 0 && path[0] == '/';
    }

    @Override
    public P resolve( Path other )
    {
        final P o = checkPath( other );
        if( o.isAbsolute() ) {
            return o;
        }
        char[] resolvedPath;
        if( this.path.length == 0 ) {
            // Handle "" alias for the root
            resolvedPath = new char[o.path.length + 1];
            resolvedPath[0] = '/';
            System.arraycopy( o.path, 0, resolvedPath, 1, o.path.length );
        }
        else if( this.path[path.length - 1] == '/' ) {
            // Handle ours path ending with "/"
            resolvedPath = new char[path.length + o.path.length];
            System.arraycopy( path, 0, resolvedPath, 0, path.length );
            System.arraycopy( o.path, 0, resolvedPath, path.length, o.path.length );
        }
        else {
            resolvedPath = new char[path.length + 1 + o.path.length];
            System.arraycopy( path, 0, resolvedPath, 0, path.length );
            resolvedPath[path.length] = '/';
            System.arraycopy( o.path, 0, resolvedPath, path.length + 1, o.path.length );
        }
        return fs.createPath( resolvedPath );
    }

    @Override
    public Path resolveSibling( Path other )
    {
        if( other == null ) {
            throw new NullPointerException();
        }
        Path parent = getParent();
        return (parent == null) ? other : parent.resolve( other );
    }

    @Override
    public boolean startsWith( Path other )
    {
        final P o = checkPath( other );
        if( o.isAbsolute() != this.isAbsolute()
            || o.path.length > this.path.length ) {
            return false;
        }
        int olast = o.path.length;
        for( int i = 0; i < olast; i++ ) {
            if( o.path[i] != this.path[i] ) {
                return false;
            }
        }
        olast--;
        return o.path.length == this.path.length
               || o.path[olast] == '/'
               || this.path[olast + 1] == '/';
    }

    @Override
    public boolean endsWith( Path other )
    {
        final P o = checkPath( other );
        int olast = o.path.length - 1;
        if( olast > 0 && o.path[olast] == '/' ) {
            olast--;
        }
        int last = this.path.length - 1;
        if( last > 0 && this.path[last] == '/' ) {
            last--;
        }
        if( olast == -1 ) // o.path.length == 0
        {
            return last == -1;
        }
        if( (o.isAbsolute() && (!this.isAbsolute() || olast != last))
            || (last < olast) ) {
            return false;
        }
        for( ; olast >= 0; olast--, last-- ) {
            if( o.path[olast] != this.path[last] ) {
                return false;
            }
        }
        return o.path[olast + 1] == '/'
               || last == -1 || this.path[last] == '/';
    }

    @Override
    public P resolve( String other )
    {
        return resolve( getFileSystem().getPath( other ) );
    }

    @Override
    public final Path resolveSibling( String other )
    {
        return resolveSibling( getFileSystem().getPath( other ) );
    }

    @Override
    public final boolean startsWith( String other )
    {
        return startsWith( getFileSystem().getPath( other ) );
    }

    @Override
    public final boolean endsWith( String other )
    {
        return endsWith( getFileSystem().getPath( other ) );
    }

    @Override
    public Path normalize()
    {
        char[] resolvedPath = getResolved();
        if( resolvedPath == path ) // no change
        {
            return this;
        }
        return fs.createPath( resolvedPath );
    }

    private P checkPath( Path path )
    {
        if( path == null ) {
            throw new NullPointerException();
        }
        if( !(path instanceof AbstractPath) ) {
            throw new ProviderMismatchException();
        }
        return (P) path;
    }

    // create offset list if not already created
    private void initOffsets()
    {
        if( offsets == null ) {
            int count, index;
            // count names
            count = 0;
            index = 0;
            while( index < path.length ) {
                char c = path[index++];
                if( c != '/' ) {
                    count++;
                    while( index < path.length && path[index] != '/' ) {
                        index++;
                    }
                }
            }
            // populate offsets
            int[] result = new int[count];
            count = 0;
            index = 0;
            while( index < path.length ) {
                char c = path[index];
                if( c == '/' ) {
                    index++;
                }
                else {
                    result[count++] = index++;
                    while( index < path.length && path[index] != '/' ) {
                        index++;
                    }
                }
            }
            synchronized( this ) {
                if( offsets == null ) {
                    offsets = result;
                }
            }
        }
    }

    // resolved path for locating zip entry inside the zip file,
    // the result path does not contain ./ and .. components
    @SuppressWarnings("VolatileArrayField")
    private volatile char[] resolved = null;

    public char[] getResolvedPath()
    {
        char[] r = resolved;
        if( r == null ) {
            if( isAbsolute() ) {
                r = getResolved();
            }
            else {
                r = toAbsolutePath().getResolvedPath();
            }
            if( r.length > 0 && r[0] == '/' ) {
                r = Arrays.copyOfRange( r, 1, r.length );
            }
            resolved = r;
        }
        return resolved;
    }

    // removes redundant slashs, replace "\" to zip separator "/"
    // and check for invalid characters
    private char[] normalize( char[] path )
    {
        if( path.length == 0 ) {
            return path;
        }
        char prevC = 0;
        for( int i = 0; i < path.length; i++ ) {
            char c = path[i];
            if( c == '\\' ) {
                return normalize( path, i );
            }
            if( c == (char) '/' && prevC == '/' ) {
                return normalize( path, i - 1 );
            }
            if( c == '\u0000' ) {
                throw new InvalidPathException( String.valueOf( path ), "Path: nul character not allowed" );
            }
            prevC = c;
        }
        return path;
    }

    private char[] normalize( char[] path, int off )
    {
        char[] to = new char[path.length];
        int n = 0;
        while( n < off ) {
            to[n] = path[n];
            n++;
        }
        int m = n;
        char prevC = 0;
        while( n < path.length ) {
            char c = path[n++];
            if( c == (char) '\\' ) {
                c = (char) '/';
            }
            if( c == (char) '/' && prevC == (char) '/' ) {
                continue;
            }
            if( c == '\u0000' ) {
                throw new InvalidPathException( String.valueOf( path ), "Path: nul character not allowed" );
            }
            to[m++] = c;
            prevC = c;
        }
        if( m > 1 && to[m - 1] == '/' ) {
            m--;
        }
        return (m == to.length) ? to : Arrays.copyOf( to, m );
    }

    // Remove DotSlash(./) and resolve DotDot (..) components
    private char[] getResolved()
    {
        if( path.length == 0 ) {
            return path;
        }
        for( int i = 0; i < path.length; i++ ) {
            char c = path[i];
            if( c == (char) '.' ) {
                return resolve0();
            }
        }
        return path;
    }

    // TBD: performance, avoid initOffsets
    private char[] resolve0()
    {
        char[] to = new char[path.length];
        int nc = getNameCount();
        int[] lastM = new int[nc];
        int lastMOff = -1;
        int m = 0;
        for( int i = 0; i < nc; i++ ) {
            int n = offsets[i];
            int len = (i == offsets.length - 1)
                      ? (path.length - n) : (offsets[i + 1] - n - 1);
            if( len == 1 && path[n] == (char) '.' ) {
                if( m == 0 && path[0] == '/' ) // absolute path
                {
                    to[m++] = '/';
                }
                continue;
            }
            if( len == 2 && path[n] == '.' && path[n + 1] == '.' ) {
                if( lastMOff >= 0 ) {
                    m = lastM[lastMOff--];  // retreat
                    continue;
                }
                if( path[0] == '/' ) {  // "/../xyz" skip
                    if( m == 0 ) {
                        to[m++] = '/';
                    }
                }
                else {               // "../xyz" -> "../xyz"
                    if( m != 0 && to[m - 1] != '/' ) {
                        to[m++] = '/';
                    }
                    while( len-- > 0 ) {
                        to[m++] = path[n++];
                    }
                }
                continue;
            }
            if( m == 0 && path[0] == '/'
                || // absolute path
                    m != 0 && to[m - 1] != '/' ) {   // not the first name
                to[m++] = '/';
            }
            lastM[++lastMOff] = m;
            while( len-- > 0 ) {
                to[m++] = path[n++];
            }
        }
        if( m > 1 && to[m - 1] == '/' ) {
            m--;
        }
        return (m == to.length) ? to : Arrays.copyOf( to, m );
    }

    @Override
    public String toString()
    {
        return String.valueOf( path );
    }

    @Override
    public int hashCode()
    {
        int h = hashcode;
        if( h == 0 ) {
            hashcode = h = Arrays.hashCode( path );
        }
        return h;
    }

    @Override
    public boolean equals( Object obj )
    {
        return obj != null
               && obj instanceof AbstractPath
               && this.fs == ((AbstractPath) obj).fs
               && compareTo( (Path) obj ) == 0;
    }

    @Override
    public int compareTo( Path other )
    {
        final AbstractPath o = checkPath( other );
        int len1 = this.path.length;
        int len2 = o.path.length;

        int n = Math.min( len1, len2 );
        char v1[] = this.path;
        char v2[] = o.path;

        int k = 0;
        while( k < n ) {
            int c1 = v1[k] & 0xff;
            int c2 = v2[k] & 0xff;
            if( c1 != c2 ) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    @Override
    public WatchKey register( WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers )
    {
        if( watcher == null || events == null || modifiers == null ) {
            throw new NullPointerException();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register( WatchService watcher, WatchEvent.Kind<?>... events )
    {
        return register( watcher, events, new WatchEvent.Modifier[0] );
    }

    @Override
    public final File toFile()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator()
    {
        return new Iterator<Path>()
        {
            private int i = 0;

            @Override
            public boolean hasNext()
            {
                return (i < getNameCount());
            }

            @Override
            public Path next()
            {
                if( i < getNameCount() ) {
                    Path result = getName( i );
                    i++;
                    return result;
                }
                else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove()
            {
                throw new ReadOnlyFileSystemException();
            }
        };
    }

    /////////////////////////////////////////////////////////////////////
    public abstract void createDirectory( FileAttribute<?>... attrs )
            throws IOException;

    public abstract InputStream newInputStream( OpenOption... options )
            throws IOException;

    public abstract DirectoryStream<Path> newDirectoryStream( Filter<? super Path> filter )
            throws IOException;

    public abstract void delete()
            throws IOException;

    public abstract void deleteIfExists()
            throws IOException;

    public abstract BasicFileAttributes getAttributes()
            throws IOException;

    public abstract boolean isSameFile( Path other )
            throws IOException;

    public abstract SeekableByteChannel newByteChannel( Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException;

    public abstract FileChannel newFileChannel( Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException;

    public abstract boolean exists();

    public abstract OutputStream newOutputStream( OpenOption... options )
            throws IOException;

    public abstract void move( P target, CopyOption... options )
            throws IOException;

    public abstract void copy( P target, CopyOption... options )
            throws IOException;
}
