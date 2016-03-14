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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

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

    /**
     * Convenience method to create a single thread PathSynchronizer. This is the same as
     * {@code create(env, ()-> new PathSynchronzier());}
     *
     * @param env
     * @return
     */
    public static PathSynchronizer create( Map<String, Object> env )
    {
        return create( env, PathSynchronizer::new );
    }

    /**
     * Create a PathSynchronizer. This will use the supplier just once, so subsequent calls on the same environment will share
     * the same instance, so actions on the same path will be atomic, specifically writes to some remote store will block reads
     * to the same path until that action has been completed.
     *
     * @param env
     * @param s
     * @return
     */
    public static PathSynchronizer create( Map<String, Object> env, Supplier<PathSynchronizer> s )
    {
        Objects.requireNonNull( env, "No environment" );
        return (PathSynchronizer) env.computeIfAbsent( PathSynchronizer.class.getName(), k -> s.get() );
    }

    /**
     * Create a single thread synchronizer.
     *
     * Note: You should only use this constructor when using {@link #create(java.util.Map, java.util.function.Supplier) } method
     * otherwise you may find that reads and writes lock each other.
     */
    public PathSynchronizer()
    {
        this( Executors.newSingleThreadExecutor() );
    }

    /**
     * Create a synchronzier that uses a work stealing thread pool.
     *
     * Note: You should only use this constructor when using {@link #create(java.util.Map, java.util.function.Supplier) } method
     * otherwise you may find that reads and writes lock each other.
     *
     * @param parallelism 0 = number of cores of system otherwise the targeted parallelism level
     */
    public PathSynchronizer( int parallelism )
    {
        this( parallelism == 0 ? Executors.newWorkStealingPool() : Executors.newWorkStealingPool( parallelism ) );
    }

    /**
     * Create a synchronizer using an ExecutorService
     *
     * Note: You should only use this constructor when using {@link #create(java.util.Map, java.util.function.Supplier) } method
     * otherwise you may find that reads and writes lock each other.
     *
     * @param executor
     */
    public PathSynchronizer( ExecutorService executor )
    {
        this.executorService = executor;
    }

    /**
     * The ExecutorService in use
     * @return 
     */
    public final ExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * Execute a task with the given Path locked so that multiple tasks for the same path will run atomically
     * @param key Path
     * @param t Task
     * @throws IOException 
     */
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
                throw new IOException( cause.getMessage(), cause );
            }
            else
            {
                throw new IOException( tr.getMessage(), tr );
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Execute a task with the given Path locked so that multiple tasks for the same path will run atomically
     * @param key Path
     * @param t Task
     * @throws IOException 
     */
    public final void execute( char[] key, Callable<Void> t )
            throws IOException
    {
        if( key == null || key.length == 0 )
        {
            throw new FileNotFoundException( "/" );
        }
        execute( String.valueOf( key ), t );
    }

    /**
     * Close this synchronizer
     * @throws IOException 
     */
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
