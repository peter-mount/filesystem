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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import onl.area51.filesystem.io.FileSystemIO;

/**
 *
 * @author peter
 */
public class DpkgScanner
        implements Constants
{

    private static final Logger LOG = Logger.getGlobal();
    private final FileSystemIO delegate;

    public DpkgScanner( FileSystemIO delegate, Map<String, ?> env )
    {
        this.delegate = delegate;
    }

    /**
     * Scan the directory represented by a {@link Path} and update it's Packages.gz file
     *
     * @param dir
     *
     * @throws IOException
     */
    public synchronized void scanPackages( Path dir )
    {
        Path basePath = delegate.getBaseDirectory();
        String publicPath = basePath.relativize( dir ).toString();

        final Path packages = dir.resolve( PACKAGES_GZ );

        LOG.log( Level.INFO, () -> BEGINING_REFRESH_OF + dir );
        try {
            final File tempFile = File.createTempFile( PACKAGES, GZ );
            try {
                ProcessBuilder b = new ProcessBuilder( "dpkg-scanpackages", publicPath )
                        .directory( basePath.toFile() )
                        // Send errors to the same stderr as the jvm
                        .redirectError( ProcessBuilder.Redirect.INHERIT );
                Process p = b.start();

                // Accept the output from the process and stream it to the temp file compressing it at the same time
                try( InputStream is = p.getInputStream() ) {
                    try( OutputStream os = Files.newOutputStream( packages,
                                                                  StandardOpenOption.CREATE,
                                                                  StandardOpenOption.TRUNCATE_EXISTING,
                                                                  StandardOpenOption.WRITE,
                                                                  StandardOpenOption.DSYNC ) ) {
                        try( GZIPOutputStream gz = new GZIPOutputStream( os ) ) {
                            copy( is, gz );
                        }
                    }
                }

                LOG.log( Level.INFO, () -> COMPLETED_REFRESH_OF + dir );
            }
            finally {
                tempFile.delete();
            }
        }
        catch( IOException ex ) {
            LOG.log( Level.SEVERE, ex, () -> FAILED_REFRESH_OF + dir );
        }
    }

    public void refresh()
    {
        LOG.log( Level.INFO, () -> BEGINING_REFRESH_OF + delegate.getBaseDirectory() );
        try {
            // For each directory that contains Packages.gz or *.deb then run a scan
            Files.find( delegate.getBaseDirectory(),
                        Integer.MAX_VALUE,
                        ( path, attr ) -> {
                            String n = path.getName( path.getNameCount() - 1 ).toString();
                            return PACKAGES_GZ.equals( n ) || n.endsWith( ".deb" );
                        } )
                    .map( Path::getParent )
                    .distinct()
                    .forEach( this::scanPackages );
            LOG.log( Level.INFO, () -> COMPLETED_REFRESH_OF + delegate.getBaseDirectory() );
        }
        catch( IOException ex ) {
            LOG.log( Level.SEVERE, ex, () -> FAILED_REFRESH_OF + delegate.getBaseDirectory() );
        }
    }

    private static long copy( InputStream source, OutputStream sink )
            throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while( (n = source.read( buf )) > 0 ) {
            sink.write( buf, 0, n );
            nread += n;
        }
        return nread;
    }
}
