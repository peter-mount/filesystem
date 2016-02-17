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

import java.io.Closeable;
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
 * @param <P>
 * @param <A>
 */
public interface FileSystemIO
        extends Closeable
{

    Path getBaseDirectory();

    default boolean isTemporary()
    {
        return false;
    }

    boolean exists( char path[] )
            throws IOException;

    void createDirectory( char path[], FileAttribute<?> attrs[] )
            throws IOException;

    InputStream newInputStream( char path[] )
            throws IOException;

    OutputStream newOutputStream( char path[], OpenOption... options )
            throws IOException;

    void deleteFile( char path[], boolean exists )
            throws IOException;

    SeekableByteChannel newByteChannel( char path[], Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException;

    FileChannel newFileChannel( char path[], Set<? extends OpenOption> options, FileAttribute<?>... attrs )
            throws IOException;

    void copyFile( boolean b, char src[], char dest[], CopyOption... options )
            throws IOException;

    BasicFileAttributes getAttributes( char path[] )
            throws IOException;

    BasicFileAttributeView getAttributeView( char path[] );

    DirectoryStream<Path> newDirectoryStream( char path[], Filter<? super Path> filter )
            throws IOException;

    /**
     * If present represents the directory base for this FileSystem
     */
    public static final String BASE_DIRECTORY = "baseDirectory";

    /**
     * Flag that if present on a file system's environment then the file system is temporary and will be destroyed when the vm exists or is closed
     */
    public static final String DELETE_ON_EXIT = "deleteOnExit";

}
