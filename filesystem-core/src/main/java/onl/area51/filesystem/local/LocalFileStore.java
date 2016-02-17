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
import java.nio.file.Files;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;

/**
 * Details about this file store
 *
 * @author peter
 * @param <F>
 * @param <P>
 */
public class LocalFileStore<F extends AbstractLocalFileSystem<F, P, ?>, P extends AbstractLocalPath<F, P>>
        extends FileStore
{

    private final F fs;
    private final String type;

    protected LocalFileStore( P path, String type )
    {
        this.fs = path.getFileSystem();
        this.type = type;
    }

    @Override
    public String name()
    {
        return fs.toString() + "/";
    }

    @Override
    public String type()
    {
        return type;
    }

    @Override
    public boolean isReadOnly()
    {
        return fs.isReadOnly();
    }

    @Override
    public boolean supportsFileAttributeView( Class<? extends FileAttributeView> type )
    {
        return type == BasicFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView( String name )
    {
        return name.equals( "basic" );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView( Class<V> type )
    {
        if( type == null ) {
            throw new NullPointerException();
        }
        return (V) null;
    }

    @Override
    public long getTotalSpace()
            throws IOException
    {
        return new FileStoreAttributes( this ).totalSpace();
    }

    @Override
    public long getUsableSpace()
            throws IOException
    {
        return new FileStoreAttributes( this ).usableSpace();
    }

    @Override
    public long getUnallocatedSpace()
            throws IOException
    {
        return new FileStoreAttributes( this ).unallocatedSpace();
    }

    @Override
    public Object getAttribute( String attribute )
            throws IOException
    {
        if( attribute.equals( "totalSpace" ) ) {
            return getTotalSpace();
        }
        if( attribute.equals( "usableSpace" ) ) {
            return getUsableSpace();
        }
        if( attribute.equals( "unallocatedSpace" ) ) {
            return getUnallocatedSpace();
        }
        throw new UnsupportedOperationException( "does not support the given attribute" );
    }

    private static class FileStoreAttributes
    {

        final FileStore fstore;
        final long size;

        public FileStoreAttributes( LocalFileStore fileStore )
                throws IOException
        {
            Path path = fileStore.fs.getCachePath();
            // FIXME get the size of the cache
            this.size = 0L;//Files.size( path );
            this.fstore = Files.getFileStore( path );
        }

        public long totalSpace()
        {
            return size;
        }

        public long usableSpace()
                throws IOException
        {
            if( !fstore.isReadOnly() ) {
                return fstore.getUsableSpace();
            }
            return 0;
        }

        public long unallocatedSpace()
                throws IOException
        {
            if( !fstore.isReadOnly() ) {
                return fstore.getUnallocatedSpace();
            }
            return 0;
        }
    }
}
