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
import java.net.URI;
import java.nio.file.AccessMode;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.function.BiFunction;
import onl.area51.filesystem.AbstractFileSystem;
import onl.area51.filesystem.AbstractPath;
import onl.area51.filesystem.FileSystemIO;

public abstract class AbstractLocalFileSystem<F extends AbstractFileSystem<F, P, S>, P extends AbstractPath<F, P>, S extends FileStore>
        extends AbstractFileSystem<F, P, S>
{

    private final Path cachePath;

    public AbstractLocalFileSystem( URI uri, FileSystemProvider provider, Path cachePath, Map<String, ?> env,
                                    BiFunction<Path, Map<String, ?>, FileSystemIO> fileSystemIO )
            throws IOException
    {
        super( uri, provider, env, fileSystemIO.apply( cachePath, env ), cachePath.getName( cachePath.getNameCount() - 1 ).toString() );
        this.cachePath = getFileSystemIO().getBaseDirectory();

        if( Files.notExists( cachePath ) ) {
            Files.createDirectories( cachePath );
        }

        // sm and existence check
        cachePath.getFileSystem().provider().checkAccess( cachePath, AccessMode.READ );
        if( !Files.isWritable( cachePath ) ) {
            setReadOnly( true );
        }
    }

    @Override
    public String toString()
    {
        return cachePath.toString();
    }

    public Path getCachePath()
    {
        return cachePath;
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            if( getFileSystemIO().isTemporary() ) {
                ((AbstractLocalFileSystemProvider) provider()).deleteFileSystem( this );
            }
        }
        finally {
            getFileSystemIO().close();
        }
    }

}
