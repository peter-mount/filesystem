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
package onl.area51.filesystem.io;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import onl.area51.filesystem.FileSystemUtils;
import org.kohsuke.MetaInfServices;

/**
 * A FileSystem which stores files with their MD5's.
 * <p>
 * So for the file "/Harry-Green-HampsteadHeath-copy.jpg" then the local file will be
 * "0/02/021ad58091421abab4be786251454727.jpg". Note the last portion of the file after '.' will be included in the final
 * file name.
 * <p>
 * Also this instance will not return a directory listing and creating directories do nothing as they are meaningless when
 * stored locally.
 */
@MetaInfServices(FileSystemIO.class)
public class Cache
        extends LocalFileSystemIO
{

    public Cache( Path basePath,
                  Map<String, ?> env )
    {
        super( basePath, env );
    }

    @Override
    protected String getPath( char[] path )
            throws IOException
    {
        return FileSystemUtils.getCachePrefix( String.valueOf( path ) );
    }

    @Override
    public void createDirectory( char[] path,
                                 FileAttribute<?>[] attrs )
            throws IOException
    {
        // No-op as we cache by file name
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream( char[] path,
                                                     DirectoryStream.Filter<? super Path> filter )
            throws IOException
    {
        // We cannot view directories so don't show anything
        return new DirectoryStream<Path>()
        {
            @Override
            public Iterator<Path> iterator()
            {
                return new Iterator<Path>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return false;
                    }

                    @Override
                    public Path next()
                    {
                        throw new NoSuchElementException();
                    }
                };
            }

            @Override
            public void close()
                    throws IOException
            {
            }
        };
    }

}
