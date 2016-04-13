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
import java.nio.file.ProviderMismatchException;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Objects;
import onl.area51.filesystem.io.FileSystemIORepository;
import onl.area51.filesystem.local.AbstractLocalFileSystemProvider;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author peter
 */
@MetaInfServices(FileSystemProvider.class)
public class CacheFileSystemProvider
        extends AbstractLocalFileSystemProvider<CacheFileSystem, CachePath>
{

    @Override
    public String getScheme()
    {
        return "cache";
    }

    @Override
    protected CacheFileSystem createFileSystem( URI uri, Path p, Map<String, Object> env )
            throws IOException
    {
        return new CacheFileSystem( uri, this, p, env, FileSystemIORepository::create );
    }

    @Override
    protected CachePath toCachePath( Path path )
    {
        Objects.requireNonNull( path );
        if( !(path instanceof CachePath) ) {
            throw new ProviderMismatchException();
        }
        return (CachePath) path.toAbsolutePath();
    }
}
