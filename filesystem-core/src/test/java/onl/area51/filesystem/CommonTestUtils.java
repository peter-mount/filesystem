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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author peter
 */
public class CommonTestUtils
{

    private static final List<String> BODY = Arrays.asList( "test\n" );
    protected static final File BASE_FILE = new File( "target/filesystems" ).getAbsoluteFile();
    protected static final Path BASE_PATH = BASE_FILE.toPath().toAbsolutePath();

    public static void write( URI uri )
            throws IOException
    {
        write( Paths.get( uri ) );
    }

    public static void write( Path p )
    {
        try {
            Files.write( p, BODY, StandardOpenOption.CREATE, StandardOpenOption.WRITE );
        }
        catch( IOException ex ) {
            throw new UncheckedIOException( ex );
        }
    }

    public static void createFiles( Path parent, String prefix, int count )
            throws IOException
    {
        IntStream.range( 0, count )
                .mapToObj( i -> prefix + i + ".txt" )
                .map( parent::resolve )
                .forEach( CommonTestUtils::write );
    }

}
