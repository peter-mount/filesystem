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
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiFunction;
import onl.area51.filesystem.AbstractFileSystem;
import onl.area51.filesystem.io.FileSystemIO;

/**
 *
 * @author peter
 */
public class MemoryFileSystem
        extends AbstractFileSystem<MemoryFileSystem, MemoryPath, MemoryFileStore>
{

    private MemoryFileStore fileStore;

    MemoryFileSystem( URI uri, MemoryFileSystemProvider provider, Path cachePath, Map<String, ?> env,
                      BiFunction<Path, Map<String, ?>, FileSystemIO> fileSystemIO )
            throws IOException
    {
        super( uri, provider, env, cachePath, fileSystemIO );
        fileStore = new MemoryFileStore( "name", "memory" );
    }

    public MemoryFileStore getFileStore()
    {
        return fileStore;
    }

    @Override
    public MemoryPath createPath( char[] p )
    {
        return new MemoryPath( this, p );
    }

    @Override
    public MemoryFileStore createFileStore( MemoryPath p )
    {
        return new MemoryFileStore( "", "memory" );
    }

}
