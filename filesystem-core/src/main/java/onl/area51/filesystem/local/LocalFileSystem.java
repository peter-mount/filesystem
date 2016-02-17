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
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiFunction;
import onl.area51.filesystem.FileSystemIO;

/**
 * A FileSystem built on a zip file
 *
 * @author Xueming Shen
 */
public class LocalFileSystem
        extends AbstractLocalFileSystem<LocalFileSystem, LocalPath, LocalFileStore>
{

    LocalFileSystem( URI uri, LocalFileSystemProvider provider, Path cachePath, Map<String, ?> env, BiFunction<Path, Map<String, ?>, FileSystemIO> fileSystemIO )
            throws IOException
    {
        super( uri, provider, cachePath, env, fileSystemIO );
    }

    @Override
    public LocalPath createPath( char[] p )
    {
        return new LocalPath( this, p );
    }

    @Override
    public LocalFileStore createFileStore( LocalPath p )
    {
        return new LocalFileStore( p, "local" );
    }

}
