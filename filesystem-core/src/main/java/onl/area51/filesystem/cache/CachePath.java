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
package onl.area51.filesystem.cache;

import java.net.URI;
import onl.area51.filesystem.local.AbstractLocalPath;

/**
 *
 * @author Xueming Shen, Rajendra Gutupalli,Jaya Hangal
 */
public class CachePath
        extends AbstractLocalPath<CacheFileSystem, CachePath>
{

    CachePath( CacheFileSystem fs, char[] path )
    {
        super( fs, path, true );
    }

    @Override
    public CachePath getRoot()
    {
        if( this.isAbsolute() ) {
            return fs.createPath( new char[]{path[0]} );
        }
        else {
            return null;
        }
    }

    @Override
    public URI toUri()
    {
        try {
            return new URI( "cache", fs.getCachePath().toString(), String.valueOf( toAbsolutePath().path ), null, null );
        }
        catch( Exception ex ) {
            throw new AssertionError( ex );
        }
    }

}
