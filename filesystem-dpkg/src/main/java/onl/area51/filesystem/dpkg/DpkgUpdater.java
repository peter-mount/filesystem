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
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlaySender;

/**
 *
 * @author peter
 */
public class DpkgUpdater
        implements OverlaySender,
                   Constants
{

    private final FileSystemIO delegate;
    private final DpkgScanner scanner;

    public DpkgUpdater( FileSystemIO delegate, Map<String, ?> env )
    {
        this.delegate = delegate;
        scanner = new DpkgScanner( delegate, env );

        if( FileSystemUtils.isTrue( env, REFRESH_PACKAGES_ON_STARTUP ) ) {
            Thread t = new Thread( scanner::refresh );
            t.setDaemon( true );
            t.start();
        }

    }

    @Override
    public void send( char[] path )
            throws IOException
    {
        Path p = delegate.toPath( path );
        Path f = p.getName( p.getNameCount() - 1 );
        if( f.toString().endsWith( DEB ) ) {
            scanner.scanPackages( p.getParent() );
        }
    }
}
