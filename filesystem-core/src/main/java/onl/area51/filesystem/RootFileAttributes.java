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

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 *
 * @author peter
 */
public enum RootFileAttributes
        implements BasicFileAttributes
{
    INSTANCE;

    private final FileTime epoch = FileTime.fromMillis( 0L );

    @Override
    public FileTime lastModifiedTime()
    {
        return epoch;
    }

    @Override
    public FileTime lastAccessTime()
    {
        return epoch;
    }

    @Override
    public FileTime creationTime()
    {
        return epoch;
    }

    @Override
    public boolean isRegularFile()
    {
        return false;
    }

    @Override
    public boolean isDirectory()
    {
        return true;
    }

    @Override
    public boolean isSymbolicLink()
    {
        return false;
    }

    @Override
    public boolean isOther()
    {
        return false;
    }

    @Override
    public long size()
    {
        return 0L;
    }

    @Override
    public Object fileKey()
    {
        return this;
    }

}
