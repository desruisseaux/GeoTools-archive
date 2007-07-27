package org.geotools.test;

import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

/**
 * Simple wrapper for which the only purpose is to count the number of
 * connections and ensure that all are closed. Each connection is also wrapped
 * as it is given out.
 */
public class DataSourceWrapper implements javax.sql.DataSource {

    javax.sql.DataSource datasource;
    int connectionsInUse = 0;
    int maxConnections = 0;
    boolean verbose;

    /**
     * Constructor to wrap an existing DataSource and track it's connection use.
     * 
     * @param datasource victim to wrap
     */
    public DataSourceWrapper(javax.sql.DataSource datasource) {
        this(datasource, false);
    }
    
    /**
     * Constructor to wrap an existing DataSource and track it's connection use.
     * 
     * @param datasource victim to wrap
     * @param verbose flag to add system.out of connection opening and closing
     */
    public DataSourceWrapper(javax.sql.DataSource datasource, boolean verbose) {
        this.datasource = datasource;
        this.verbose = verbose;
    }
    
    /**
     * @return the number of connections currently open
     */
    public int getConnectionsInUse() {
        return connectionsInUse;
    }
    
    /**
     * @return the highest number of connections open
     */
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public Connection getConnection() throws SQLException {
        Connection connection = datasource.getConnection();
        connectionsInUse++; //increment connection count
        if (connectionsInUse > maxConnections) {
            maxConnections = connectionsInUse; //set max
        }
        if (verbose) {
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
            if (verbose) {
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

		public Array createArrayOf(String arg0, Object[] arg1)
				throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Blob createBlob() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Clob createClob() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Struct createStruct(String arg0, Object[] arg1)
				throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Properties getClientInfo() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getClientInfo(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isValid(int arg0) throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public <T> T unwrap(Class<T> iface) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}
        
    }
    public boolean isWrapperFor(Class<?> type) throws SQLException {
		throw new UnsupportedOperationException("unsupported java 6 method");
	}

	public <T> T unwrap(Class<T> type) throws SQLException {
		throw new UnsupportedOperationException("unsupported java 6 method");
	}
	
}
