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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common base {@link FileSystemProvider} implementation handling the creation and management of each {@link FileSystem} created
 * by this provider.
 *
 * @author peter
 * @param <F>
 * @param <P>
 */
public abstract class AbstractFileSystemProvider<F extends AbstractFileSystem<F, P, ?>, P extends AbstractPath<F, P>>
        extends FileSystemProvider
{

    private String cacheBase;
    private final Map<URI, F> filesystems = new ConcurrentHashMap<>();

    public final synchronized String getCacheBase()
    {
        if( cacheBase == null ) {
            String base = System.getProperty( getClass().getName() );
            if( base == null || base.trim().isEmpty() ) {
                base = System.getProperty( "user.home" ) + "/.area51/" + getScheme();
            }
            if( base == null || base.trim().isEmpty() ) {
                throw new IllegalStateException( "Invalid cacheBase" );
            }
            cacheBase = base;
        }
        return cacheBase;
    }

    @Override
    public abstract String getScheme();

    protected final Path uriToPath( URI uri )
            throws IOException
    {
        String scheme = uri.getScheme();
        if( (scheme == null) || !scheme.equalsIgnoreCase( getScheme() ) ) {
            throw new IllegalArgumentException( "URI scheme is not '" + getScheme() + "'" );
        }

        Path path = Paths.get( getCacheBase(), uri.getAuthority() ).toAbsolutePath();
        Files.createDirectories( path );
        return path;
    }

    protected abstract F createFileSystem( URI uri, Path p, Map<String, ?> env )
            throws IOException;

    private F createFileSystem( URI uri, Map<String, ?> env )
    {
        try {
            return createFileSystem( uri, uriToPath( uri ).toRealPath(), env );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public final FileSystem newFileSystem( URI uri, Map<String, ?> env )
            throws IOException
    {
        return filesystems.computeIfAbsent( FileSystemUtils.getFileSystemURI( uri ),
                                            fsUri -> createFileSystem( fsUri, FileSystemUtils.getFileSystemEnv( uri, env ) ) );
    }

    public final void deleteFileSystem( FileSystem fs )
    {
        if( fs instanceof AbstractFileSystem ) {
            filesystems.remove( ((AbstractFileSystem) fs).getUri() );
        }
    }

    @Override
    public final FileSystem newFileSystem( Path path, Map<String, ?> env )
            throws IOException
    {
        if( path.getFileSystem() != FileSystems.getDefault() ) {
            throw new UnsupportedOperationException();
        }

        Files.createDirectories( path );
        return createFileSystem( path.toUri(), path, env );
    }

    @Override
    public final Path getPath( URI uri )
    {
        return getFileSystem( uri ).getPath( uri.getPath() );
    }

    @Override
    public final FileSystem getFileSystem( URI uri )
    {
        URI fsUri = FileSystemUtils.getFileSystemURI( uri );
        try {
            return newFileSystem( uri, new HashMap<>() );
        }
        catch( IOException ex ) {
            throw new FileSystemNotFoundException( fsUri.toString() );
        }
    }
}
