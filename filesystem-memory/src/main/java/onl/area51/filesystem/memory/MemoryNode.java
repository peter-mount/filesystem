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

/**
 *
 * @author peter
 */
public abstract class MemoryNode
{

    private MemoryDirectory parent;

    private String name;

    public MemoryNode( MemoryDirectory parent, String name )
    {
        this.parent = parent;
        this.name = name;
    }

    public final String getName()
    {
        return name;
    }

    public final MemoryDirectory getParent()
    {
        return parent;
    }

    public final void setName( String name )
    {
        this.name = name;
    }

    public final void setParent( MemoryDirectory parent )
    {
        this.parent = parent;
    }

    protected abstract void free();
}
