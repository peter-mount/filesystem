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
import java.nio.file.Path;
import java.util.Map;
import org.kohsuke.MetaInfServices;

/**
 * A flat FileSystem which locally matches it's structure
 */
@MetaInfServices(FileSystemIO.class)
public class Flat
        extends LocalFileSystemIO
{

    public Flat( Path basePath,
                 Map<String, ?> env )
    {
        super( basePath, env );
    }

    @Override
    protected String getPath( char[] path )
            throws IOException
    {
        return String.valueOf( path );
    }

}
