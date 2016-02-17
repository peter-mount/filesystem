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
            throws Exception
    {
        String p = FileSystemUtils.getMediaWikiPrefix( "Harry-Green-HampsteadHeath-copy.jpg" );
        assertEquals( "0/02", p );
    }

    @Test
    public void getOpenDataCMSPrefix()
            throws Exception
    {
        String p = FileSystemUtils.getOpenDataCMSPrefix( "Harry-Green-HampsteadHeath-copy.jpg" );
        assertEquals( "H", p );
    }

}
