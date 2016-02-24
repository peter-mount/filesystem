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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import onl.area51.filesystem.FileSystemUtils;

/**
 *
 * @author peter
 */
public class FileSystemIORepository
{

    /**
     * Environment key of a function to create a wrapper
     */
    public static final String WRAPPER = "fileSystemWrapper";

    /**
     * Environment key for some FileSystems to allow the underlying format to be changed
     */
    public static final String KEY = "fileSystemType";

    private static final Map<String, BiFunction<Path, Map<String, ?>, FileSystemIO>> IMPLEMENTATIONS = new ConcurrentHashMap<>();

    static {
        try {
            Enumeration<URL> en = FileSystemIORepository.class.getClassLoader().getResources( FileSystemIO.class.getName() );
            while( en.hasMoreElements() ) {
                try( BufferedReader r = new BufferedReader( new InputStreamReader( en.nextElement().openStream() ) ) ) {
                    r.lines()
                            .map( String::trim )
                            .filter( s -> !s.isEmpty() || !s.startsWith( "#" ) )
                            .forEach( l -> {
                                try {
                                    Class<FileSystemIO> clazz = (Class<FileSystemIO>) Class.forName( l );
                                    IMPLEMENTATIONS.computeIfAbsent( clazz.getSimpleName().toLowerCase(), k -> ( p, e ) -> {
                                                                 try {
                                                                     return clazz.getConstructor( Path.class, Map.class ).newInstance( p, e );
                                                                 }
                                                                 catch( NoSuchMethodException |
                                                                        InstantiationException |
                                                                        IllegalAccessException |
                                                                        InvocationTargetException ex ) {
                                                                     throw new RuntimeException( ex );
                                                                 }
                                                             } );
                                }
                                catch( ClassNotFoundException ex ) {
                                    throw new RuntimeException( ex );
                                }
                            }
                            );
                }
            }
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    private FileSystemIORepository()
    {
    }

    public static FileSystemIO create( Path basePath, Map<String, ?> env )
    {
        return create( basePath, env, Cache::new );
    }

    public static FileSystemIO create( Path basePath, Map<String, ?> env, BiFunction<Path, Map<String, ?>, FileSystemIO> defaultIO )
    {
        return create( FileSystemUtils.getString( env, KEY, "" ), basePath, env, defaultIO );
    }

    public static FileSystemIO create( String type, Path basePath, Map<String, ?> env )
    {
        return create( type, basePath, env, Cache::new );
    }

    public static FileSystemIO create( String type, Path basePath, Map<String, ?> env, BiFunction<Path, Map<String, ?>, FileSystemIO> defaultIO )
    {
        FileSystemIO io = IMPLEMENTATIONS.getOrDefault( type == null ? "" : type.trim().toLowerCase(), defaultIO ).apply( basePath, env );

        BiFunction<FileSystemIO, Map<String, ?>, FileSystemIO> mapper = FileSystemUtils.get( env, WRAPPER );
        if( mapper != null ) {
            io = mapper.apply( io, env );
        }

        return io;
    }

}
