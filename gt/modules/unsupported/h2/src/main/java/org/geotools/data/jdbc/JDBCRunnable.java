package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runnable interface for code blocks to execute against a jdbc connection.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * @see JDBCUtils#run(JDBCDataStore, JDBCRunnable)
 */
public interface JDBCRunnable {
	/**
	 * Executes a block of code against a statement.
	 * <p>
	 * The block of code may return a value, or <code>null</code>. Code must 
	 * not return an "jdbc" objects from this method as the statement is closed
	 * after the block executes and all jdbc objects are released.
	 * </p>
	 * 
	 * @param statement A jdbc statement.
	 * 
	 * @return An arbitrary value, or <code>null</code>.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	Object run( Statement statement ) throws IOException, SQLException;
}