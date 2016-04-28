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
package onl.area51.filesystem.cdi;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;
import javax.enterprise.inject.spi.CDI;
import onl.area51.filesystem.AbstractFileSystem;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;
import onl.area51.filesystem.io.overlay.OverlaySender;
import org.kohsuke.MetaInfServices;

/**
 * A FileSystem overlay that will trigger a CDI Event of type {@link PathUpdated} when a path is written to.
 * 
 * @author peter
 */
@MetaInfServices( OverlayFileSystemIO.class )
public class CDINotifyUpdate
        extends OverlayFileSystemIO
{

    public CDINotifyUpdate( FileSystemIO delegate, Map<String, Object> env )
    {
        super( delegate,
               (OverlaySender) pathName -> {
                   FileSystem fs = (FileSystem) env.get( FileSystem.class.getName() );
                   if( fs != null ) {
                       if( fs instanceof AbstractFileSystem ) {
                           Path path = ((AbstractFileSystem) fs).createPath( pathName );
                           CDI.current().getBeanManager().fireEvent( (PathUpdated) () -> path );
                       }
                   }
               } );
    }

}
