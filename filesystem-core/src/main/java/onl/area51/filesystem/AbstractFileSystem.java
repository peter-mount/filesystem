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
package onl.area51.filesystem;

import onl.area51.filesystem.io.FileSystemIO;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * A FileSystem built on a zip file
 *
 * @author Xueming Shen
 * @param <F>
 * @param <P>
 * @param <S>
 */
public abstract class AbstractFileSystem<F extends AbstractFileSystem<F, P, S>, P extends AbstractPath<F, P>, S extends FileStore>
        extends FileSystem
{

    private final URI uri;
    private final FileSystemProvider provider;
    private boolean readOnly = false;
    private final FileSystemIO fileSystemIO;

    protected AbstractFileSystem( URI uri, FileSystemProvider provider, Map<String, ?> env,
                                  Path path,
                                  BiFunction<Path, Map<String, ?>, FileSystemIO> fileSystemIO )
            throws IOException
    {
        this.uri = uri;
        this.provider = provider;
        this.fileSystemIO = fileSystemIO.apply( path, env );

        readOnly = FileSystemUtils.isTrue( env, "readOnly" );
    }

    public URI getUri()
    {
        return uri;
    }

    public final void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }

    public abstract P createPath( char[] p );

    public abstract S createFileStore( P p );

    public FileSystemIO getFileSystemIO()
    {
        return fileSystemIO;
    }

    @Override
    public FileSystemProvider provider()
    {
        return provider;
    }

    @Override
    public String getSeparator()
    {
        return "/";
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @Override
    public boolean isReadOnly()
    {
        return readOnly;
    }

    @Override
    public Iterable<Path> getRootDirectories()
    {
        ArrayList<Path> pathArr = new ArrayList<>();
        pathArr.add( createPath( new char[]{
            '/'
        } ) );
        return pathArr;
    }

    @Override
    public P getPath( String first, String... more )
    {
        String path;
        if( more.length == 0 ) {
            path = first;
        }
        else {
            StringJoiner j = new StringJoiner( "/" );
            j.add( first );
            for( String segment: more ) {
                j.add( segment );
            }
            path = j.toString();
        }
        return createPath( path.toCharArray() );
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<FileStore> getFileStores()
    {
        ArrayList<FileStore> list = new ArrayList<>( 1 );
        list.add( createFileStore( createPath( new char[]{
            '/'
        } ) ) );
        return list;
    }

    private static final Set<String> SUPPORTED_FILE_ATTRIBUTE_VIEWS = Collections.unmodifiableSet( new HashSet<>( Arrays.asList( "basic" ) ) );

    @Override
    public Set<String> supportedFileAttributeViews()
    {
        return SUPPORTED_FILE_ATTRIBUTE_VIEWS;
    }

    private static final String REGEX_SYNTAX = "regex";

    @Override
    public PathMatcher getPathMatcher( String syntaxAndInput )
    {
        int pos = syntaxAndInput.indexOf( ':' );
        if( pos <= 0 || pos == syntaxAndInput.length() ) {
            throw new IllegalArgumentException();
        }
        String syntax = syntaxAndInput.substring( 0, pos );
        String input = syntaxAndInput.substring( pos + 1 );
        String expr;
        if( syntax.equals( REGEX_SYNTAX ) ) {
            expr = input;
        }
        else {
            throw new UnsupportedOperationException( "Syntax '" + syntax + "' not recognized" );
        }

        // return matcher
        final Pattern pattern = Pattern.compile( expr );
        return path -> pattern.matcher( path.toString() ).matches();
    }

    @Override
    public void close()
            throws IOException
    {
        fileSystemIO.close();
    }

}
