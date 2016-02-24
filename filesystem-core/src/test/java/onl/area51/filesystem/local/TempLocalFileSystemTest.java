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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import onl.area51.filesystem.CommonTestUtils;
import onl.area51.filesystem.io.FileSystemIO;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author peter
 */
public class TempLocalFileSystemTest
        extends CommonTestUtils
{

    private FileSystem create( String authority )
            throws IOException
    {
        Map<String, Object> env = new HashMap<>();
        env.put( FileSystemIO.BASE_DIRECTORY, getFile( authority ).toString() );
        env.put( FileSystemIO.DELETE_ON_EXIT, true );
        return FileSystems.newFileSystem( URI.create( "local://" + authority ), env );
    }

    /**
     * This creates the temporary filesystem.
     * <p>
     * Note: This will always pass as we can't then test if the filesystem has been deleted until after this JVM has exited
     *
     * @throws IOException
     */
    @Test
    public void testDeleteOnExit()
            throws IOException
    {
        String authority = "temp.delOnExit";
        File delFS = getFile( authority );
        create( authority );
        write( URI.create( "local://" + authority + "/file1.txt" ) );
        Assert.assertTrue( delFS.exists() );
        Assert.assertTrue( delFS.isDirectory() );
    }

    /**
     * Tests that when we implicitly close the filesystem it is deleted
     *
     * @throws IOException
     */
    @Test
    public void testTemporary()
            throws IOException
    {
        String authority = "temp.tryresource";
        File delFS = getFile( authority );

        Map<String, Object> env = new HashMap<>();
        env.put( FileSystemIO.BASE_DIRECTORY, delFS.toString() );
        env.put( FileSystemIO.DELETE_ON_EXIT, true );
        try( FileSystem fs = create( authority ) ) {
            write( URI.create( "local://" + authority + "/file1.txt" ) );
            Assert.assertTrue( delFS.exists() );
            Assert.assertTrue( delFS.isDirectory() );
        }

        // The file system should now be deleted
        assertFalse( delFS.exists() );
    }
}
