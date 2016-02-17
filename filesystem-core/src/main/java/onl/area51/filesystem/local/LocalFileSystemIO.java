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
import java.nio.file.Path;
import java.util.Map;
import onl.area51.filesystem.AbstractLocalFileSystemIO;
import onl.area51.filesystem.FileSystemIO;
import onl.area51.filesystem.FileSystemUtils;

/**
 * {@link FileSystemIO} implementation of a directory
 *
 * @author peter
 */
public abstract class LocalFileSystemIO
        extends AbstractLocalFileSystemIO
{

    public LocalFileSystemIO( Path basePath, Map<String, ?> env )
    {
        super( basePath, env );
    }

    protected abstract String getPath( char[] path )
            throws IOException;

    @Override
    protected Path toPath( char[] path )
            throws IOException
    {
        Path p = getBaseDirectory().resolve( getPath( path ) ).toAbsolutePath();
        if( p.startsWith( getBaseDirectory() ) ) {
            return p;
        }
        throw new IOException( "Path is outside the FileSystem" );
    }

    public static class Flat
            extends LocalFileSystemIO
    {

        public Flat( Path basePath, Map<String, ?> env )
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

    public static class MediaWiki
            extends LocalFileSystemIO
    {

        public MediaWiki( Path basePath, Map<String, ?> env )
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

    public static class OpenDataCMS
            extends LocalFileSystemIO
    {

        public OpenDataCMS( Path basePath, Map<String, ?> env )
        {
            super( basePath, env );
        }

        @Override
        protected String getPath( char[] path )
                throws IOException
        {
            String p = String.valueOf( path );
            return FileSystemUtils.getOpenDataCMSPrefix( p ) + "/" + p;
        }

    }
}
