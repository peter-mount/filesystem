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
package onl.area51.filesystem.dpkg;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;

/**
 *
 * @author peter
 */
public class DpkgSigner
{

    private static final Logger LOG = Logger.getGlobal();
    private final FileSystemIO delegate;

    private final boolean enabled;

    public DpkgSigner( FileSystemIO delegate, Map<String, ?> env )
    {
        this.delegate = delegate;
        enabled = Boolean.valueOf( System.getenv( "SIGNING_ENABLED" ) );
    }

    public void sign( Path path )
    {
        if( enabled ) {
            signImpl( path );
        }
    }

    private void signImpl( Path path )
    {
        Path basePath = delegate.getBaseDirectory();
        String publicPath = basePath.relativize( path ).toString();

        try {
            LOG.log( Level.SEVERE, () -> "Signing " + publicPath );
            ProcessBuilder b = new ProcessBuilder( "dpkg-sig", publicPath )
                    .directory( basePath.toFile() )
                    // Send errors to the same stderr as the jvm
                    .redirectError( ProcessBuilder.Redirect.INHERIT );
            Process p = b.start();
            p.waitFor();
            LOG.log( Level.SEVERE, () -> "Signed " + publicPath );
        }
        catch( IOException |
               InterruptedException ex ) {
            LOG.log( Level.SEVERE, ex, () -> "Failed to sign " + publicPath );
        }
    }
}
