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

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

/**
 * Class that performs the actual IO to the underlying store
 *
 * @author peter
 */
public interface FileSystemIO
        extends Closeable
{

    /**
     * The local base directory of the filesystem. For non-local filesystems, i.e. those who have no local storage this may be
     * null.
     *
     * @return
     */
    Path getBaseDirectory();

    /**
     * Is the filesystem temporary
     *
     * @return
     */
    default boolean isTemporary()
    {
        return false;
    }

    /**
     * Does the path exist in the filesystem
     *
     * @param path
     * @return
     * @throws IOException
     */
    boolean exists( char path[] )
            throws IOException;

    /**
     * Create a directory
     *
     * @param path
     * @param attrs
     * @throws IOException
     */
    void createDirectory( char path[], FileAttribute<?> attrs[] )
            throws IOException;

    /**
     * Create an InputStream for a path
     *
     * @param path
     * @return
     * @throws IOException           on error
     * @throws FileNotFoundException if the path does not exist
     */
    InputStream newInputStream( char path[] )
            throws IOException;

    /**
     * Create an output stream for a path
     *
     * @param path
     * @param options
     * @return
     * @throws IOException
     */
    OutputStream newOutputStream( char path[], OpenOption... options )
            throws IOException;

    /**
     * Delete a path
     *
     * @param path
     * @param exists
     * @throws IOException
     */
    void deleteFile( char path[], boolean exists )
            throws IOException;

    /**
     * Is the Path a file
     *
     * @param path
     * @return
     * @throws IOException
     */
    boolean isFile( char path[] )
            throws IOException;

    /**
     * Is the path a directory
     *
     * @param path
     * @return
     * @throws IOException
     */
    boolean isDirectory( char path[] )
            throws IOException;

    /**
     * Create a ByteChannel
     *
     * @param path
     * @param options
     * @param attrs
     * @return
     * @throws IOException
     */
    SeekableByteChannel newByteChannel( char path[], Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException;

    /**
     * Create a FileChannel
     *
     * @param path
     * @param options
     * @param attrs
     * @return
     * @throws IOException
     */
    FileChannel newFileChannel( char path[], Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException;

    /**
     * Copy a file. Both src and dest must be paths within this filesystem
     *
     * @param b
     * @param src
     * @param dest
     * @param options
     * @throws IOException
     */
    void copyFile( boolean b, char src[], char dest[], CopyOption... options )
            throws IOException;

    /**
     * Get the attributes for a path
     *
     * @param path
     * @return
     * @throws IOException
     */
    BasicFileAttributes getAttributes( char path[] )
            throws IOException;

    /**
     * Get an attribute view of a path
     *
     * @param path
     * @return
     */
    BasicFileAttributeView getAttributeView( char path[] );

    /**
     * Get a directory stream for a path
     *
     * @param path
     * @param filter
     * @return
     * @throws IOException
     */
    DirectoryStream<Path> newDirectoryStream( char path[], Filter<? super Path> filter )
            throws IOException;

    /**
     * If present represents the directory base for this FileSystem
     */
    public static final String BASE_DIRECTORY = "baseDirectory";

    /**
     * Flag that if present on a file system's environment then the file system is temporary and will be destroyed when the vm
     * exists or is closed
     */
    public static final String DELETE_ON_EXIT = "deleteOnExit";

    /**
     * For caches, trigger an expire run
     */
    default void expire()
    {
    }

    /**
     * The length of the file at a path
     *
     * @param path
     * @return
     * @throws IOException if the path does not exist or is a directory
     */
    long size( char[] path )
            throws IOException;
}
