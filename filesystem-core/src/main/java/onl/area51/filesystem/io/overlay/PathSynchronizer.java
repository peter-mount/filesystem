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
package onl.area51.filesystem.io.overlay;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class used to ensure we handle some operation on a path one thread at a time
 *
 * @author peter
 */
public class PathSynchronizer
        implements Closeable
{

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, Condition> conditions = new HashMap<>();

    /**
     * Execute a task with the given Path locked so that multiple tasks for the same path will run atomically
     *
     * @param key Path
     * @param t   Task
     *
     * @throws IOException
     */
    public final void execute( String key, Callable<Void> t )
            throws IOException
    {
        lock.lock();
        try {
            // If a condition exists then wait
            Condition c = conditions.get( key );
            if( c == null ) {
                c = lock.newCondition();
                conditions.put( key, c );

                lock.unlock();
                try {
                    t.call();
                }
                catch( Exception ex ) {
                    if( ex instanceof IOException ) {
                        throw (IOException) ex;
                    }
                    if( ex instanceof UncheckedIOException ) {
                        throw ((UncheckedIOException) ex).getCause();
                    }
                    throw new IOException( ex );
                }
                finally {
                    lock.lock();
                    c.signalAll();
                    conditions.remove( key );
                }
            }
            else {
                c.await();
            }
        }
        catch( InterruptedException ex ) {
            throw new IOException( ex );
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Execute a task with the given Path locked so that multiple tasks for the same path will run atomically
     *
     * @param key Path
     * @param t   Task
     *
     * @throws IOException
     */
    public final void execute( char[] key, Callable<Void> t )
            throws IOException
    {
        if( key == null || key.length == 0 ) {
            throw new FileNotFoundException( "/" );
        }
        execute( String.valueOf( key ), t );
    }

    /**
     * Close this synchronizer
     *
     * @throws IOException
     */
    @Override
    public void close()
            throws IOException
    {
        lock.lock();
        try {
            conditions.values().forEach( Condition::signalAll );
        }
        finally {
            lock.unlock();
        }
    }

}
