package org.geotools.referencing.factory.epsg;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.geotools.factory.Hints;
import org.geotools.referencing.factory.epsg.OracleDialectEpsgMediatorOnlineStressTest.ClientThread;
import org.geotools.referencing.factory.epsg.oracle.OracleOnlineTestCase;

/**
 * Multi-threaded test to check that no connections are leaked by the EPSG
 * mediator/factory code.
 * 
 * @author Cory Horner (Refractions Research)
 */
public class OracleDialectEpsgMediatorConnectionLeakOnlineTest extends OracleOnlineTestCase {

    final static int RUNNER_COUNT = 3;
    final static int ITERATIONS = 3;
    final static int MAX_TIME = 2 * 60 * 1000;
    final static int MAX_WORKERS = 2;
    final static boolean VERBOSE = false;
    
    OracleDialectEpsgMediator mediator;
    DataSourceWrapper wrappedDataSource;
    String[] codes;
    Hints hints;

    protected void connect() throws Exception {
        super.connect();
        hints = new Hints(Hints.BUFFER_POLICY, "none");     
        hints.put(Hints.AUTHORITY_MAX_ACTIVE, new Integer(MAX_WORKERS));
        if (datasource == null) {
            fail("no datasource available");
        }
        wrappedDataSource = new DataSourceWrapper(datasource);
        mediator = new OracleDialectEpsgMediator(80, hints, wrappedDataSource);
        codes = OracleDialectEpsgMediatorOnlineStressTest.getCodes();
    }

    public void testLeak() throws Throwable {
        TestRunnable runners[] = new TestRunnable[RUNNER_COUNT];
        for (int i = 0; i < RUNNER_COUNT; i++) {
            ClientThread thread = new OracleDialectEpsgMediatorOnlineStressTest.ClientThread(i, mediator); 
            thread.iterations = ITERATIONS;
            runners[i] = thread;
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(runners, null);
        mttr.runTestRunnables(MAX_TIME);
        
        //count exceptions and metrics
        int exceptions = 0;
        for (int i = 0; i < RUNNER_COUNT; i++) {
            ClientThread thread = (ClientThread) runners[i];
            exceptions += thread.exceptions;
        }
        //destroy the mediator, check for open connections or exceptions
        mediator.dispose();
        assertEquals(0, wrappedDataSource.connectionsInUse);
        assertEquals(0, exceptions);
    }
    
    /**
     * Simple wrapper which only purpose is to count the number of connections
     * and ensure that all are closed. Each connection is also wrapped as it is
     * given out.
     */
    class DataSourceWrapper implements javax.sql.DataSource {

        javax.sql.DataSource datasource;
        public int connectionsInUse = 0;
        
        public DataSourceWrapper(javax.sql.DataSource datasource) {
            this.datasource = datasource;
        }
        
        public Connection getConnection() throws SQLException {
            Connection connection = datasource.getConnection();
            connectionsInUse++; //increment connection count
            if (VERBOSE) {
                System.out.println("CONNECTION OPENED");
            }
            return new ConnectionWrapper(connection);
        }

        public Connection getConnection(String username, String password)
                throws SQLException {
            return datasource.getConnection(username, password);
        }

        public PrintWriter getLogWriter() throws SQLException {
            return datasource.getLogWriter();
        }

        public int getLoginTimeout() throws SQLException {
            return datasource.getLoginTimeout();
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
            datasource.setLogWriter(out);
        }

        public void setLoginTimeout(int seconds) throws SQLException {
            datasource.setLoginTimeout(seconds);
        }
        
        class ConnectionWrapper implements Connection {

            Connection connection;
            
            public ConnectionWrapper(Connection connection) {
                this.connection = connection;
            }
            
            public void clearWarnings() throws SQLException {
                connection.clearWarnings();
            }

            public void close() throws SQLException {
                connectionsInUse--; //decrement connection count
                if (VERBOSE) {
                    System.out.println("CONNECTION CLOSED");
                }
                connection.close();
            }

            public void commit() throws SQLException {
                connection.commit();
            }

            public Statement createStatement() throws SQLException {
                return connection.createStatement();
            }

            public Statement createStatement(int resultSetType,
                    int resultSetConcurrency) throws SQLException {
                return connection.createStatement(resultSetType, resultSetConcurrency);
            }

            public Statement createStatement(int resultSetType,
                    int resultSetConcurrency, int resultSetHoldability)
                    throws SQLException {
                return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            }

            public boolean getAutoCommit() throws SQLException {
                return connection.getAutoCommit();
            }

            public String getCatalog() throws SQLException {
                return connection.getCatalog();
            }

            public int getHoldability() throws SQLException {
                return connection.getHoldability();
            }

            public DatabaseMetaData getMetaData() throws SQLException {
                return connection.getMetaData();
            }

            public int getTransactionIsolation() throws SQLException {
                return connection.getTransactionIsolation();
            }

            public Map getTypeMap() throws SQLException {
                return connection.getTypeMap();
            }

            public SQLWarning getWarnings() throws SQLException {
                return connection.getWarnings();
            }

            public boolean isClosed() throws SQLException {
                return connection.isClosed();
            }

            public boolean isReadOnly() throws SQLException {
                return connection.isReadOnly();
            }

            public String nativeSQL(String sql) throws SQLException {
                return connection.nativeSQL(sql);
            }

            public CallableStatement prepareCall(String sql)
                    throws SQLException {
                return connection.prepareCall(sql);
            }

            public CallableStatement prepareCall(String sql, int resultSetType,
                    int resultSetConcurrency) throws SQLException {
                return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
            }

            public CallableStatement prepareCall(String sql, int resultSetType,
                    int resultSetConcurrency, int resultSetHoldability)
                    throws SQLException {
                return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            }

            public PreparedStatement prepareStatement(String sql)
                    throws SQLException {
                return connection.prepareStatement(sql);
            }

            public PreparedStatement prepareStatement(String sql,
                    int autoGeneratedKeys) throws SQLException {
                return connection.prepareStatement(sql, autoGeneratedKeys);
            }

            public PreparedStatement prepareStatement(String sql,
                    int[] columnIndexes) throws SQLException {
                return connection.prepareStatement(sql, columnIndexes);
            }

            public PreparedStatement prepareStatement(String sql,
                    String[] columnNames) throws SQLException {
                return connection.prepareStatement(sql, columnNames);
            }

            public PreparedStatement prepareStatement(String sql,
                    int resultSetType, int resultSetConcurrency)
                    throws SQLException {
                return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
            }

            public PreparedStatement prepareStatement(String sql,
                    int resultSetType, int resultSetConcurrency,
                    int resultSetHoldability) throws SQLException {
                return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            }

            public void releaseSavepoint(Savepoint savepoint)
                    throws SQLException {
                connection.releaseSavepoint(savepoint);
            }

            public void rollback() throws SQLException {
                connection.rollback();
            }

            public void rollback(Savepoint savepoint) throws SQLException {
                connection.rollback(savepoint);
            }

            public void setAutoCommit(boolean autoCommit) throws SQLException {
                connection.setAutoCommit(autoCommit);
            }

            public void setCatalog(String catalog) throws SQLException {
                connection.setCatalog(catalog);
            }

            public void setHoldability(int holdability) throws SQLException {
                connection.setHoldability(holdability);
            }

            public void setReadOnly(boolean readOnly) throws SQLException {
                connection.setReadOnly(readOnly);
            }

            public Savepoint setSavepoint() throws SQLException {
                return connection.setSavepoint();
            }

            public Savepoint setSavepoint(String name) throws SQLException {
                return connection.setSavepoint(name);
            }

            public void setTransactionIsolation(int level) throws SQLException {
                connection.setTransactionIsolation(level);
            }

            public void setTypeMap(Map arg0) throws SQLException {
                connection.setTypeMap(arg0);
            }
            
        }
    }
    
}
