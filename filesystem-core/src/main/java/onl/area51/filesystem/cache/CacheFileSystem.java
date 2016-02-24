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
package onl.area51.filesystem.cache;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiFunction;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.local.AbstractLocalFileSystem;

/**
 * A FileSystem built on a zip file
 *
 * @author Xueming Shen
 */
public class CacheFileSystem
        extends AbstractLocalFileSystem<CacheFileSystem, CachePath, CacheFileStore>
{

    CacheFileSystem( URI uri, CacheFileSystemProvider provider, Path cachePath, Map<String, ?> env,
                     BiFunction<Path, Map<String, ?>, FileSystemIO> fileSystemIO )
            throws IOException
    {
        super( uri, provider, cachePath, env, fileSystemIO );

        // If we are not readonly then expire us
        if( !isReadOnly() )
        {
            getFileSystemIO().expire();
        }
    }

    @Override
    public CachePath createPath( char[] p )
    {
        return new CachePath( this, p );
    }

    @Override
    public CacheFileStore createFileStore( CachePath p )
    {
        return new CacheFileStore( p );
    }

}
