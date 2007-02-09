package org.geotools.data.h2;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Map;

import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 * A very lame connection pool datasource which actually doesn't pool 
 * connections!!.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2ConnectionPoolDataSource implements ConnectionPoolDataSource {

	/**
	 * connection url
	 */
	String connectionURL;
	/**
	 * log writer
	 */
	PrintWriter logWriter;
	/**
	 * login timeout
	 */
	int loginTimeout;
	
	/**
	 * Creates the connection pool datasource.
	 * 
	 * @param connectionURL The url describing how to connect to the db.
	 */
	H2ConnectionPoolDataSource( String connectionURL ) {
		this.connectionURL = connectionURL;
	}
	
	public PrintWriter getLogWriter() throws SQLException {
		return logWriter;
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		this.logWriter = out;
	}

	public int getLoginTimeout() throws SQLException {
		return loginTimeout;
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		this.loginTimeout = seconds;
	}
	
	public PooledConnection getPooledConnection() throws SQLException {
		return new H2PooledConnection();
	}

	public PooledConnection getPooledConnection(String user, String password)
			throws SQLException {
		
		return getPooledConnection();
	}

	class H2PooledConnection implements PooledConnection {

		/**
		 * The actual connection resource
		 */
		Connection connection;
		
		public void addConnectionEventListener(ConnectionEventListener listener) {
			throw new UnsupportedOperationException();
		}

		public void removeConnectionEventListener(ConnectionEventListener listener) {
			throw new UnsupportedOperationException();
		}
		
		public void close() throws SQLException {
			if ( !connection.isClosed() ) {
				connection.close();
			}
		}

		public Connection getConnection() throws SQLException {
			if ( connection == null ) {
				synchronized ( this ) {
					if ( connection == null ) {
						try {
							Class.forName("org.h2.Driver");
						} 
						catch (ClassNotFoundException e) {
							throw new RuntimeException( e );
						}
						connection = DriverManager.getConnection( connectionURL );
					}
				}
			}
			
			return connection;
		
		}
	}
}
