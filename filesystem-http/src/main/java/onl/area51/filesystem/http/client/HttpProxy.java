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
package onl.area51.filesystem.http.client;

import java.util.Map;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayFileSystemIO;
import onl.area51.filesystem.io.overlay.PathSynchronizer;
import org.kohsuke.MetaInfServices;

/**
 * {@link OverlayFileSystemIO} implementation to retrieve content from multiple remote HTTP/HTTPS servers
 *
 * @author peter
 */
@MetaInfServices(OverlayFileSystemIO.class)
public class HttpProxy
        extends OverlayFileSystemIO
{

    public HttpProxy( FileSystemIO delegate, Map<String, Object> env )
    {
        super( delegate, new PathSynchronizer(), new HttpProxyRetriever( delegate, env ) );
    }

}
