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

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class FileSystemUtilsTest
{

    @Test
    public void getMediaWikiPrefix()
            throws IOException
    {
        String p = FileSystemUtils.getMediaWikiPrefix( "Harry-Green-HampsteadHeath-copy.jpg" );
        assertEquals( "0/02", p );
    }

    @Test
    public void getOpenDataCMSPrefix()
            throws IOException
    {
        String p = FileSystemUtils.getOpenDataCMSPrefix( "Harry-Green-HampsteadHeath-copy.jpg" );
        assertEquals( "H", p );
    }

    @Test
    public void getCachePrefix()
            throws IOException
    {
        assertEquals( "0/09/098f6bcd4621d373cade4e832627b4f6", FileSystemUtils.getCachePrefix( "test" ) );
        assertEquals( "0/07/073402a6e3f1a393222038b31c140998", FileSystemUtils.getCachePrefix( "dir/test" ) );
        assertEquals( "0/04/0412c29576c708cf0155e8de242169b1.jpg", FileSystemUtils.getCachePrefix( "test.jpg" ) );
        assertEquals( "b/ba/bac3ba4acda45d834747e5b3339f0f1c.png", FileSystemUtils.getCachePrefix( "dir/test.png" ) );
        assertEquals( "0/02/021ad58091421abab4be786251454727.jpg", FileSystemUtils.getCachePrefix( "Harry-Green-HampsteadHeath-copy.jpg" ) );
    }
}
