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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class used to ensure we handle some operation on a path one thread at a time
 *
 * @author peter
 */
public class PathSynchronizer
        implements Closeable
{

    private final Lock lock = new ReentrantLock();
    private final Map<String, Condition> conditions = new HashMap<>();

    private final ExecutorService executorService;

    public PathSynchronizer()
    {
        this( Executors.newSingleThreadExecutor() );
    }

    public PathSynchronizer( int parallelism )
    {
        this( parallelism == 0 ? Executors.newWorkStealingPool() : Executors.newWorkStealingPool( parallelism ) );
    }

    public PathSynchronizer( ExecutorService executor )
    {
        this.executorService = executor;
    }

    public final ExecutorService getExecutorService()
    {
        return executorService;
    }

    public final void execute( String key, Callable<Void> t )
            throws IOException
    {
        lock.lock();
        try
        {
            Condition c = conditions.get( key );
            if( c == null )
            {
                c = lock.newCondition();
                conditions.put( key, c );
                lock.unlock();
                try
                {
                    Future<Void> f = executorService.submit( t );
                    f.get();
                }
                finally
                {
                    lock.lock();
                    c.signalAll();
                    conditions.remove( key );
                }
            }
            else
            {
                c.await();
            }
        } catch( InterruptedException ex )
        {
            throw new IOException( ex );
        } catch( ExecutionException ex )
        {
            Throwable tr = ex.getCause();
            if( tr instanceof UncheckedIOException )
            {
                IOException cause = ((UncheckedIOException) tr).getCause();
                throw new IOException(cause.getMessage(), cause);
            }
            else
            {
                throw new IOException( tr.getMessage(),tr );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public final void execute( char[] path, Callable<Void> t )
            throws IOException
    {
        if( path == null || path.length == 0 )
        {
            throw new FileNotFoundException( "/" );
        }
        execute( String.valueOf( path ), t );
    }

    @Override
    public void close()
            throws IOException
    {
        executorService.shutdownNow();

        lock.lock();
        try
        {
            conditions.values().forEach( Condition::signalAll );
        }
        finally
        {
            lock.unlock();
        }
    }

}
