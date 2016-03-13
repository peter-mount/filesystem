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
package onl.area51.filesystem.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author peter
 */
public class MemoryDirectory
        extends MemoryNode
{

    private static final Logger logger = Logger.getLogger( "MemoryFS" );

    private final Map<String, MemoryNode> files = new ConcurrentHashMap<>();

    public MemoryDirectory( MemoryDirectory parent, String name )
    {
        super( parent, name );
    }

    public boolean isRoot()
    {
        return getParent() == null;
    }

    /**
     * Return a memory node by name.
     * <p>
     * "." will return this node
     * ".." will return the parent or null if this is the root
     *
     * @param name
     *
     * @return
     */
    public MemoryNode get( String name )
    {
        if( ".".equals( name ) ) {
            return this;
        }
        if( "..".equals( name ) ) {
            return getParent();
        }
        return files.get( name );
    }

    /**
     * Put a memory node to an entry.
     *
     * @param name
     * @param node
     */
    public void put( String name, MemoryNode node )
    {
        if( node == null ) {
            remove( name );
        }
        else {
            cleanup( files.put( name, node ) );
        }
    }

    public void remove( String name )
    {
        cleanup( files.remove( name ) );
    }

    private void cleanup( MemoryNode n )
    {
        if( n != null ) {
            n.free();
        }
    }

    @Override
    protected void free()
    {
        logger.log( Level.INFO, () -> "Free " + getName() );
        // Free each entry. This will recurse as needed
        files.values().forEach( MemoryNode::free );
        files.clear();
    }

    public Stream<MemoryNode> entries()
    {
        return files.values().stream();
    }
}
