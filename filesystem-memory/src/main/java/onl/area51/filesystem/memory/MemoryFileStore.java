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
package onl.area51.filesystem.memory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import onl.area51.filesystem.local.AbstractLocalFileSystem;
import onl.area51.filesystem.local.AbstractLocalPath;

/**
 *
 * @author peter
 * @param <F>
 * @param <P>
 */
public class MemoryFileStore<F extends AbstractLocalFileSystem<F, P, ?>, P extends AbstractLocalPath<F, P>>
        extends FileStore
{

    private final String name;
    private final String type;
    private final MemoryDirectory root;

    protected MemoryFileStore( String name, String type )
    {
        this.name = name;
        this.type = type;
        root = new MemoryDirectory( null, name );
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public String type()
    {
        return type;
    }

    @Override
    public boolean isReadOnly()
    {
        return false;
    }

    @Override
    public long getTotalSpace()
            throws IOException
    {
        return 0L;
    }

    @Override
    public long getUsableSpace()
            throws IOException
    {
        return 0L;
    }

    @Override
    public long getUnallocatedSpace()
            throws IOException
    {
        return 0L;
    }

    @Override
    public boolean supportsFileAttributeView( Class<? extends FileAttributeView> type )
    {
        return false;
    }

    @Override
    public boolean supportsFileAttributeView( String name )
    {
        return false;
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView( Class<V> type )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute( String attribute )
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public MemoryNode findNode( MemoryPath p )
            throws IOException
    {
        MemoryDirectory d = root;
        MemoryNode n = d;
        int i = 0, c = p.getNameCount();
        while( n instanceof MemoryDirectory && i < c ) {
            String s = p.getName( i++ ).toString();

            n = d.get( s );
            if( n instanceof MemoryDirectory ) {
                d = (MemoryDirectory) n;
            }
        }
        if( i < c ) {
            return null;
        }
        return n;
    }

    public MemoryDirectory findDirectory( MemoryPath p )
            throws IOException
    {
        return findDirectory( p, false );
    }

    public MemoryDirectory findDirectory( MemoryPath p, boolean create )
            throws IOException
    {
        MemoryNode n = findNode( p );
        if( n == null && create ) {
            n = getOrCreateDirectory( p );
        }
        if( n instanceof MemoryDirectory ) {
            return (MemoryDirectory) n;
        }
        throw new FileNotFoundException( p.toString() );
    }

    public MemoryFile findFile( MemoryPath p )
            throws IOException
    {
        return findFile( p, false );
    }

    public MemoryFile findFile( MemoryPath p, boolean create )
            throws IOException
    {
        MemoryNode n = findNode( p );
        if( n == null && create ) {
            n = getOrCreateFile( p );
        }
        if( n instanceof MemoryFile ) {
            return (MemoryFile) n;
        }
        throw new FileNotFoundException( p.toString() );
    }

    public MemoryDirectory getOrCreateDirectory( MemoryPath p )
            throws IOException
    {
        MemoryPath rp = p.toAbsolutePath();
        MemoryDirectory d = root;
        for( int i = 0; i < p.getNameCount(); i++ ) {
            String s = p.getName( i ).toString();
            MemoryNode n = d.get( s );
            if( n == null ) {
                d = new MemoryDirectory( d, name );
            }
            else if( n instanceof MemoryDirectory ) {
                d = (MemoryDirectory) n;
            }
            else {
                throw new FileNotFoundException( p.toString() );
            }
        }
        return d;
    }

    public MemoryFile getOrCreateFile( MemoryPath p )
            throws IOException
    {
        MemoryDirectory d = getOrCreateDirectory( p.getParent() );
        String s = p.getName( p.getNameCount() - 1 ).toString();
        MemoryNode n = d.get( s );
        if( n == null ) {
            return new MemoryFile( d, s );
        }
        else if( n instanceof MemoryFile ) {
            return (MemoryFile) n;
        }
        else {
            throw new FileNotFoundException( p.toString() );
        }
    }
}
