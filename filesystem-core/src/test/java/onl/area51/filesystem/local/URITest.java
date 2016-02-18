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
import java.util.Collections;
import onl.area51.filesystem.CommonTestUtils;
import static onl.area51.filesystem.CommonTestUtils.write;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * Test we can pass environment details via the URI's query parameters
 *
 * @author peter
 */
public class URITest
        extends CommonTestUtils
{

    @Test
    public void uri()
            throws IOException
    {
        String authority = "uri.tryresource";
        File delFS = getFile( authority );
        URI uri = URI.create( "cache://" + authority + "?deleteOnExit=true&baseDirectory=" + delFS.toString() );

        try( FileSystem fs = FileSystems.newFileSystem( uri, Collections.emptyMap() ) )
        {
            write( fs.getPath( "/file1.txt" ) );
            Assert.assertTrue( delFS.exists() );
            Assert.assertTrue( delFS.isDirectory() );
        }

        // The file system should now be deleted
        assertFalse( delFS.exists() );
    }
}
