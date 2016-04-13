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
package onl.area51.filesystem.http.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.httpd.action.Action;

/**
 *
 * @author peter
 */
public class FileSystemFactory
{

    public static Path getPath( String target, FileSystem fs )
    {
        int s1 = target.indexOf( '/' ), s2 = s1 > -1 && s1 < target.length() ? target.indexOf( '/', s1 + 1 ) : -1;
        if( s1 > -1 && s2 > s1 ) {
            return fs.getPath( target.substring( s2 ) );
        }
        else if( s1 > -1 ) {
            return fs.getPath( "/" );
        }
        return null;
    }

    public static String getPrefix( Map<String, Object> cfg )
    {
        Objects.requireNonNull( cfg );

        String name = FileSystemUtils.getString( cfg, "name" );
        String prefix = "/" + FileSystemUtils.getString( cfg, "prefix", name ) + "/";

        // Ensure prefix starts with / at least 2 chars long and not "//"
        if( !prefix.startsWith( "/" ) || prefix.length() < 2 || prefix.equals( "//" ) ) {
            throw new IllegalArgumentException( prefix );
        }

        // ensure prefix ends with /
        prefix = prefix.endsWith( "/" ) ? prefix : (prefix + "/");
        return prefix;
    }

    public static FileSystem getFileSystem( Map<String, Object> cfg )
            throws IOException,
                   URISyntaxException
    {
        Objects.requireNonNull( cfg );

        String name = FileSystemUtils.getString( cfg, "name" );

        URI uri = new URI( FileSystemUtils.getString( cfg, "uri", "local://" + name ) );

        Map<String, Object> env = (Map<String, Object>) cfg.get( "environment" );
        return FileSystems.newFileSystem( uri, env == null ? new HashMap<>() : env );
    }

    public static Action extractPath()
    {
        return r -> {
            Path path = r.getAttribute( "path", () -> r.<FileSystemMap>getAttribute( "fileSystemMap" )
                                        .getPath( r.getHttpRequest().getRequestLine().getUri() ) );
            if( path == null || !Files.exists( path, LinkOption.NOFOLLOW_LINKS ) ) {
                r.removeAttribute( "path" );
            }
        };
    }

    public static Action extractPath( FileSystem fs )
    {
        return r -> {
            Path path = r.getAttribute( "path", () -> getPath( r.getHttpRequest().getRequestLine().getUri(), fs ) );
            if( path == null || !Files.exists( path, LinkOption.NOFOLLOW_LINKS ) ) {
                r.removeAttribute( "path" );
            }
        };
    }

    public static Action extractPathMayNotExist( FileSystem fs )
    {
        return r -> r.getAttribute( "path", () -> getPath( r.getHttpRequest().getRequestLine().getUri(), fs ) );
    }

}
