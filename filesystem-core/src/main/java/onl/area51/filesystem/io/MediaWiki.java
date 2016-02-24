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
import onl.area51.filesystem.FileSystemUtils;
import org.kohsuke.MetaInfServices;

/**
 * A FileSystem which matches the way MediaWiki lays out it's images.
 * <p>
 * The local filesystem will have the files under directories formed from the first octet of the md5 of the filename.
 * <p>
 * So for the file "/Harry-Green-HampsteadHeath-copy.jpg" then the local file will be
 * "0/02/Harry-Green-HampsteadHeath-copy.jpg"
 */
@MetaInfServices(FileSystemIO.class)
public class MediaWiki
        extends LocalFileSystemIO
{

    public MediaWiki( Path basePath,
                      Map<String, ?> env )
    {
        super( basePath, env );
    }

    @Override
    protected String getPath( char[] path )
            throws IOException
    {
        String p = String.valueOf( path );
        return FileSystemUtils.getMediaWikiPrefix( p ) + "/" + p;
    }

}
