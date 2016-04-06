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
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Builds a map of file systems with a specified path prefix. This map can then be used via an HttpAction chain to resolve a
 * path the an nio Path and an appropriate action.
 *
 * @author peter
 */
public interface FileSystemMap
{

    FileSystem getFileSystem( String path );

    Path getPath( String path );

    Map<String, Object> getConfig( String path );

    String getPrefix( String path );

    Stream<String> prefixes();

    static interface Builder
    {

        Builder addFileSystem( Map<String, Object> cfg );

        default Builder addFileSystems( Collection<Map<String, Object>> ary )
        {
            ary.forEach( this::addFileSystem );
            return this;
        }

        default Builder addFileSystems( Stream<Map<String, Object>> s )
        {
            s.forEach( this::addFileSystem );
            return this;
        }

        FileSystemMap build();
    }

    static Builder builder()
    {
        return new Builder()
        {
            private final Map<String, Map<String, Object>> config = new ConcurrentHashMap<>();
            private final Map<String, FileSystem> prefixes = new ConcurrentHashMap<>();

            @Override
            public Builder addFileSystem( Map<String, Object> cfg )
            {
                try {
                    String prefix = FileSystemFactory.getPrefix( cfg );

                    if( prefixes.containsKey( prefix ) ) {
                        throw new IllegalArgumentException( "Path " + prefix + " already exists" );
                    }

                    FileSystem fileSystem = FileSystemFactory.getFileSystem( cfg );

                    config.put( prefix, cfg );
                    prefixes.put( prefix, fileSystem );
                    return this;
                } catch( IOException ex ) {
                    throw new UncheckedIOException( ex );
                } catch( URISyntaxException ex ) {
                    throw new IllegalArgumentException( ex );
                }
            }

            @Override
            public FileSystemMap build()
            {
                return new FileSystemMap()
                {
                    @Override
                    public FileSystem getFileSystem( String path )
                    {
                        String prefix = getPrefix( path );
                        if( prefix == null ) {
                            return null;
                        }
                        return prefixes.get( prefix );
                    }

                    @Override
                    public Path getPath( String path )
                    {
                        String prefix = getPrefix( path );
                        if( prefix == null ) {
                            return null;
                        }
                        FileSystem fs = prefixes.get( prefix );
                        if( fs == null ) {
                            return null;
                        }
                        return fs.getPath( path.substring( prefix.length() ) );
                    }

                    @Override
                    public Map<String, Object> getConfig( String path )
                    {
                        String prefix = getPrefix( path );
                        if( prefix == null ) {
                            return null;
                        }
                        return config.get( prefix );
                    }

                    @Override
                    public String getPrefix( String target )
                    {
                        int s1 = target.indexOf( '/' ), s2 = s1 > -1 && s1 < target.length() ? target.indexOf( '/', s1 + 1 ) : -1;
                        return s1 > -1 && s2 > s1 ? target.substring( s1, s2 + 1 ) : null;
                    }

                    @Override
                    public Stream<String> prefixes()
                    {
                        return prefixes.keySet().stream();
                    }

                };
            }

        };
    }
}
