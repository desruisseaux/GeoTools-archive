/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.ObjectPool;
import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.data.SdeRow;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRelease;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Provides thread safe access to an SeConnection.
 * <p>
 * This class has become more and more magic over time! It no longer represents a Connection but
 * provides "safe" access to a connection.
 * <p>
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.3.x
 */
public class Session {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /** Actual SeConnection being protected */
    SeConnection connection;

    /**
     * ObjectPool used to manage open connections (shared).
     */
    private ObjectPool pool;

    private ArcSDEConnectionConfig config;

    private static int connectionCounter;

    private int connectionId;

    private boolean transactionInProgress;

    private boolean isPassivated;

    private Map<String, SeTable> cachedTables = new WeakHashMap<String, SeTable>();

    private Map<String, SeLayer> cachedLayers = new WeakHashMap<String, SeLayer>();

    private Map<String, SeRasterColumn> cachedRasters = new HashMap<String, SeRasterColumn>();

    /**
     * The SeConnection bound task executor, ensures all operations against a given connection are
     * performed in the same thread regardless of the thread the {@link #issue(Command)} is being
     * called from.
     */
    private final ExecutorService taskExecutor;

    /**
     * Thread used by the taskExecutor; so we can detect recursion.
     */
    private Thread commandThread;

    /**
     * Provides safe access to an SeConnection.
     * 
     * @param pool ObjectPool used to manage SeConnection
     * @param config Used to set up a SeConnection
     * @throws SeException If we cannot connect
     */
    Session(final ObjectPool pool, final ArcSDEConnectionConfig config) throws SeException {
        {
            String serverName = config.getServerName();
            int intValue = config.getPortNumber().intValue();
            String databaseName = config.getDatabaseName();
            String userName = config.getUserName();
            String userPassword = config.getUserPassword();

            this.connection = new SeConnection(serverName, intValue, databaseName, userName,
                    userPassword);
        }
        this.config = config;
        this.pool = pool;
        this.connection.setConcurrency(SeConnection.SE_TRYLOCK_POLICY);
        this.taskExecutor = Executors.newSingleThreadExecutor();

        // grab command thread
        taskExecutor.execute(new Runnable() {
            public void run() {
                commandThread = Thread.currentThread();
            }
        });
        synchronized (Session.class) {
            connectionCounter++;
            connectionId = connectionCounter;
        }
    }

    /**
     * Executes the given command and returns its result.
     * 
     * @param command the command to execute
     * @throws IOException if an exception occurs handling any ArcSDE resource while executing the
     *             command
     */
    public <T> T issue(final Command<T> command) throws IOException {
        final Thread callingThread = Thread.currentThread();
        if (callingThread == commandThread) {
            // Called command inside command
            try {
                return command.execute(this, connection);
            } catch (SeException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new ArcSdeException(e);
            }
        } else {
            // StackTraceElement ste = callingThread.getStackTrace()[3];
            // System.out.println("executing command " + ste.getClassName() + "."
            // + ste.getMethodName() + ":" + ste.getLineNumber() + " ("
            // + callingThread.getName() + ")");

            FutureTask<T> task = new FutureTask<T>(new Callable<T>() {
                public T call() throws Exception {
                    // used to detect when thread has been
                    // restarted after error
                    commandThread = Thread.currentThread();
                    return command.execute(Session.this, connection);
                }
            });

            taskExecutor.execute(task);
            T result;
            try {
                result = task.get();
            } catch (InterruptedException e) {
                throw new RuntimeException("Command execution abruptly interrupted", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new DataSourceException(cause);
            }
            return result;
        }
    }

    public final boolean isClosed() {
        return this.connection.isClosed();
    }

    /**
     * Marks the connection as being active (i.e. its out of the pool and ready to be used).
     * <p>
     * Shall be called just before being returned from the connection pool
     * </p>
     * 
     * @see #markInactive()
     * @see #isPassivated
     * @see #checkActive()
     */
    void markActive() {
        this.isPassivated = false;
    }

    /**
     * Marks the connection as being inactive (i.e. laying on the connection pool)
     * <p>
     * Shall be callled just before sending it back to the pool
     * </p>
     * 
     * @see #markActive()
     * @see #isPassivated
     * @see #checkActive()
     */
    void markInactive() {
        this.isPassivated = true;
    }

    /**
     * Returns whether this connection is on the connection pool domain or not.
     * 
     * @return <code>true</code> if this connection has beed returned to the pool and thus cannot
     *         be used, <code>false</code> if its safe to keep using it.
     */
    public boolean isPassivated() {
        return isPassivated;
    }

    /**
     * Sanity check method called before every public operation delegates to the superclass.
     * 
     * @throws IllegalStateException if {@link #isPassivated() isPassivated() == true} as this is a
     *             serious workflow breackage.
     */
    private void checkActive() {
        if (isPassivated()) {
            throw new IllegalStateException("Unrecoverable error: " + toString()
                    + " is passivated, shall not be used!");
        }
    }

    public SeLayer getLayer(final String layerName) throws IOException {
        checkActive();
        return issue(new Command<SeLayer>() {
            @Override
            public SeLayer execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                if (!cachedLayers.containsKey(layerName)) {
                    synchronized (Session.this) {
                        if (!cachedLayers.containsKey(layerName)) {
                            cacheLayers();
                        }
                    }
                }
                SeLayer seLayer = cachedLayers.get(layerName);
                if (seLayer == null) {
                    throw new NoSuchElementException("Layer '" + layerName + "' not found");
                }
                return seLayer;
            }
        });
    }

    public synchronized SeRasterColumn getRasterColumn(final String rasterName) throws IOException {
        checkActive();
        if (!cachedRasters.containsKey(rasterName)) {
            try {
                cacheRasters();
            } catch (SeException e) {
                throw new DataSourceException("Can't obtain raster " + rasterName, e);
            }
        }
        SeRasterColumn raster = cachedRasters.get(rasterName);
        if (raster == null) {
            throw new NoSuchElementException("Raster '" + rasterName + "' not found");
        }
        return raster;
    }

    public SeTable getTable(final String tableName) throws IOException {
        checkActive();
        return issue(new Command<SeTable>() {
            @Override
            public SeTable execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                if (!cachedTables.containsKey(tableName)) {
                    synchronized (Session.this) {
                        if (!cachedTables.containsKey(tableName)) {
                            cacheLayers();
                        }
                    }
                }
                SeTable seTable = (SeTable) cachedTables.get(tableName);
                if (seTable == null) {
                    throw new NoSuchElementException("Table '" + tableName + "' not found");
                }
                return seTable;
            }
        });
    }

    /**
     * Caches both tables and layers
     * 
     * @throws SeException
     */
    @SuppressWarnings("unchecked")
    private void cacheLayers() throws IOException {
        try {
            Vector/* <SeLayer> */layers = connection.getLayers();
            String qualifiedName;
            SeLayer layer;
            SeTable table;
            cachedTables.clear();
            cachedLayers.clear();
            for (Iterator it = layers.iterator(); it.hasNext();) {
                layer = (SeLayer) it.next();
                qualifiedName = layer.getQualifiedName();
                table = new SeTable(connection, qualifiedName);
                cachedLayers.put(qualifiedName, layer);
                cachedTables.put(qualifiedName, table);
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void cacheRasters() throws SeException {
        Vector<SeRasterColumn> rasters = this.connection.getRasterColumns();
        cachedRasters.clear();
        for (SeRasterColumn raster : rasters) {
            cachedRasters.put(raster.getQualifiedTableName(), raster);
        }
    }

    /**
     * Starts a transaction over the connection held by this Session
     * <p>
     * If this method succeeds, {@link #isTransactionActive()} will return true afterwards
     * </p>
     * 
     * @throws IOException
     * @see {@link #issueStartTransaction(Session)}
     */
    public void startTransaction() throws IOException {
        checkActive();
        issue(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                connection.setTransactionAutoCommit(0);
                connection.startTransaction();
                transactionInProgress = true;
                return null;
            }
        });
    }

    /**
     * Commits the current transaction.
     * <p>
     * This method shall only be called from inside a command
     * </p>
     * 
     * @throws IOException
     */
    public void commitTransaction() throws IOException {
        checkActive();
        issue(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                connection.commitTransaction();
                return null;
            }
        });
        transactionInProgress = false;
    }

    /**
     * Returns whether a transaction is in progress over this connection
     * <p>
     * As for any other public method, this one can't be called if {@link #isPassivated()} is true.
     * </p>
     * 
     * @return
     */
    public boolean isTransactionActive() {
        checkActive();
        return transactionInProgress;
    }

    /**
     * Rolls back the current transaction
     * <p>
     * When this method returns it is guaranteed that {@link #isTransactionActive()} will return
     * false, regardless of the success of the rollback operation.
     * </p>
     * 
     * @throws IOException
     */
    public void rollbackTransaction() throws IOException {
        checkActive();
        try {
            issue(new Command<Void>() {
                @Override
                public Void execute(Session session, SeConnection connection) throws SeException,
                        IOException {
                    connection.rollbackTransaction();
                    return null;
                }
            });
        } finally {
            transactionInProgress = false;
        }
    }

    /**
     * Return to the pool (may not close the internal connection, depends on pool settings).
     * 
     * @throws IllegalStateException if close() is called while a transaction is in progress
     * @see #destroy()
     */
    public void close() throws IllegalStateException {
        checkActive();
        if (transactionInProgress) {
            throw new IllegalStateException(
                    "Transaction is in progress, should commit or rollback before closing");
        }

        try {
            if (LOGGER.isLoggable(Level.FINER)) {
                // StackTraceElement[] stackTrace =
                // Thread.currentThread().getStackTrace();
                // String caller = stackTrace[3].getClassName() + "." +
                // stackTrace[3].getMethodName();
                // System.err.println("<- " + caller + " returning " +
                // toString() + " to pool");
                LOGGER.finer("<- returning " + toString() + " to pool");
            }
            this.pool.returnObject(this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "ArcSDEPooledConnection[" + connectionId + "]";
    }

    /**
     * Actually closes the connection
     */
    void destroy() {
        try {
            this.connection.close();
        } catch (SeException e) {
            LOGGER.info("closing connection: " + e.getMessage());
        }
    }

    /**
     * Compares for reference equality
     */
    @Override
    public boolean equals(Object other) {
        return other == this;
    }

    @Override
    public int hashCode() {
        return 17 ^ this.config.hashCode();
    }

    /**
     * Returns the live list of layers, not the cached ones, so it may pick up the differences in
     * the database.
     * 
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public List<SeLayer> getLayers() throws IOException {
        return issue(new Command<List<SeLayer>>() {
            @Override
            public List<SeLayer> execute(Session session, SeConnection connection)
                    throws SeException, IOException {
                return connection.getLayers();
            }
        });
    }

    public String getUser() throws IOException {
        return issue(new Command<String>() {
            @Override
            public String execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return connection.getUser();
            }
        });
    }

    public SeRelease getRelease() throws IOException {
        return issue(new Command<SeRelease>() {
            @Override
            public SeRelease execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return connection.getRelease();
            }
        });
    }

    public String getDatabaseName() throws IOException {
        return issue(new Command<String>() {
            @Override
            public String execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return connection.getDatabaseName();
            }
        });
    }
    
    //
    // Factory methods that make use of internal connection
    // Q: How "long" are these objects good for? until the connection closes - or longer...
    //
    public SeLayer createSeLayer() throws IOException {
        return issue(new Command<SeLayer>() {
            @Override
            public SeLayer execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeLayer(connection);
            }
        });
    }

    public SeRegistration createSeRegistration(final String typeName) throws IOException {
        return issue(new Command<SeRegistration>() {
            @Override
            public SeRegistration execute(Session session, SeConnection connection)
                    throws SeException, IOException {
                return new SeRegistration(connection, typeName);
            }
        });
    }

    /**
     * Creates an SeTable named
     * <code>qualifiedName<code>; the layer does not need to exist on the server.
     * 
     * @param qualifiedName
     * @return
     * @throws IOException
     */
    public SeTable createSeTable(final String qualifiedName) throws IOException {
        return issue(new Command<SeTable>() {
            @Override
            public SeTable execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeTable(connection, qualifiedName);
            }
        });
    }

    public SeInsert createSeInsert() throws IOException {
        return issue(new Command<SeInsert>() {
            @Override
            public SeInsert execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeInsert(connection);
            }
        });
    }

    public SeUpdate createSeUpdate() throws IOException {
        return issue(new Command<SeUpdate>() {
            @Override
            public SeUpdate execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeUpdate(connection);
            }
        });
    }

    public SeDelete createSeDelete() throws IOException {
        return issue(new Command<SeDelete>() {
            @Override
            public SeDelete execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeDelete(connection);
            }
        });
    }

    public SeRasterColumn createSeRasterColumn() throws IOException {
        return issue(new Command<SeRasterColumn>() {
            @Override
            public SeRasterColumn execute(Session session, SeConnection connection)
                    throws SeException, IOException {
                return new SeRasterColumn(connection);
            }
        });
    }

    public SeRasterColumn createSeRasterColumn(final SeObjectId rasterColumnId) throws IOException {
        return issue(new Command<SeRasterColumn>() {
            @Override
            public SeRasterColumn execute(Session session, SeConnection connection)
                    throws SeException, IOException {
                return new SeRasterColumn(connection, rasterColumnId);
            }
        });
    }

    public SeColumnDefinition[] describe(final String tableName) throws IOException {
        return issue(new Command<SeColumnDefinition[]>() {
            @Override
            public SeColumnDefinition[] execute(Session session, SeConnection connection)
                    throws SeException, IOException {
                SeTable table = session.getTable(tableName);
                return table.describe();
            }
        });
    }

    /**
     * Issues a command that fetches a row from an already executed SeQuery and returns the
     * {@link SdeRow} object with its contents.
     * <p>
     * The point in returning an {@link SdeRow} instead of a plain {@link SeRow} is that the former
     * prefetches the row values and this can be freely used outside a {@link Command}. Otherwise
     * the SeRow should only be used inside a command as accessing its values implies using the
     * connection.
     * </p>
     * 
     * @param query
     * @return
     * @throws IOException
     */
    public SdeRow fetch(final SeQuery query) throws IOException {
        return issue(new Command<SdeRow>() {
            @Override
            public SdeRow execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                SeRow row = query.fetch();
                SdeRow populatedRow = null;
                if (row != null) {
                    populatedRow = new SdeRow(row);
                }
                return populatedRow;
            }
        });
    }

    public void close(final SeState state) throws IOException {
        issue(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                state.close();
                return null;
            }
        });
    }

    public void close(final SeStreamOp stream) throws IOException {
        issue(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                stream.close();
                return null;
            }
        });
    }

    public SeState createState(final SeObjectId stateId) throws IOException {
        return issue(new Command<SeState>() {
            @Override
            public SeState execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeState(connection, stateId);
            }
        });
    }

    public SeQuery createSeQuery() throws IOException {
        return issue(new Command<SeQuery>() {
            @Override
            public SeQuery execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeQuery(connection);
            }
        });
    }

    /**
     * Creates an SeQuery to fetch the given propertyNames with the provided attribute based
     * restrictions
     * <p>
     * This method shall only be called from inside a {@link Command}
     * </p>
     * 
     * @param propertyNames
     * @param sql
     * @return
     * @throws IOException
     */
    public SeQuery createSeQuery(final String[] propertyNames, final SeSqlConstruct sql)
            throws IOException {

        return issue(new Command<SeQuery>() {
            @Override
            public SeQuery execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                return new SeQuery(connection, propertyNames, sql);
            }
        });
    }

    public SeQuery createAndExecuteQuery(final String[] propertyNames, final SeSqlConstruct sql)
            throws IOException {
        return issue(new Command<SeQuery>() {
            @Override
            public SeQuery execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                SeQuery query = new SeQuery(connection, propertyNames, sql);
                query.prepareQuery();
                query.execute();
                return query;
            }
        });
    }

}