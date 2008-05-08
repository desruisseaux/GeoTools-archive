package org.geotools.process;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@link Executor} that provides methods to manage termination and
 * methods that can produce a {@link Progress} for tracking one or more
 * asynchronous tasks.
 * <p>
 * Method <tt>submit</tt> extends base method {@link
 * ExecutorService#submit} by creating and returning a
 * {@link Progress} that can be used to track how a process
 * is doing in addition to cancelling execution and/or waiting
 * for completion.
 * 
 * @author Jody
 */
public interface ProcessExecutor extends ExecutorService {
    /**
     * Submits a process for execution and returns a Progress
     * representing the pending results of the task. 
     *
     * <p>
     * If you would like to immediately block waiting
     * for a task, you can use constructions of the form
     * <tt>result = exec.submit(aProcess).get();</tt>
     *
     * <p> Note: The {@link Processors} class includes a set of methods
     * that can convert some other common closure-like objects,
     * for example, {@link Callable} to {@link Process} form so
     * they can be submitted.
     *
     * @param task the task to submit
     * @return a Progress representing pending completion of the task
     * @throws RejectedExecutionException if task cannot be scheduled for execution
     * @throws NullPointerException if task null
     */
    public Progress submit( Process task, Map<String,Object> input );    
}