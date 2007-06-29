package org.geotools.referencing.factory.epsg;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.jdbc.jdbcDataSource;

/**
 * This utility class knows everything there is to know about the care and
 * feeding of our pet EPSG database. This utility class is used to hold logic
 * previously associated with our own custom DataSource.
 * <p>
 * The EPSG database can be downloaded from <A
 * HREF="http://www.epsg.org">http://www.epsg.org</A>. The SQL scripts
 * (modified for the HSQL syntax as <A HREF="doc-files/HSQL.html">explained here</A>)
 * are bundled into this plugin. The database version is given in the
 * {@linkplain org.opengis.metadata.citation.Citation#getEdition edition attribute}
 * of the
 * {@linkplain org.opengis.referencing.AuthorityFactory#getAuthority authority}.
 * The HSQL database is read only.
 * <p>
 * 
 * @author Jody Garnett
 */
public class HsqlEpsgDatabase {

	/**
	 * The key for fetching the database directory from {@linkplain System#getProperty(String)
	 * system properties}.
	 *
	 * @since 2.3
	 */
	public static final String DIRECTORY_KEY = "EPSG-HSQL.directory";
	/**
	 * The database name.
	 *
	 * @since 2.3
	 */
	public static final String DATABASE_NAME = "EPSG";	
	
	/**
	 * Creates a DataSource that is set up and ready to go.
	 * <p>
	 * This method pays attention to the system property "EPSG-HSQL.directory"
	 * and makes use of the default database name "EPSG".
	 * </p>
	 * 
	 * @return
	 */
	public static javax.sql.DataSource createDataSource(){
		return createDataSource( getDirectory() );
	}
	
	public static javax.sql.DataSource createDataSource( File directory ){
		if (directory == null) {
			return null; // not available
		}
		jdbcDataSource dataSource = new jdbcDataSource();
        /*
         * Constructs the full path to the HSQL database. Note: we do not use
         * File.toURI() because HSQL doesn't seem to expect an encoded URL
         * (e.g. "%20" instead of spaces).
         */
        final StringBuffer url = new StringBuffer("jdbc:hsqldb:file:");
        final String path = directory.getAbsolutePath().replace(File.separatorChar, '/');
        if (path.length()==0 || path.charAt(0)!='/') {
            url.append('/');
        }
        url.append(path);
        if (url.charAt(url.length()-1) != '/') {
            url.append('/');
        }
        url.append(HsqlEpsgDatabase.DATABASE_NAME);
        dataSource.setDatabase(url.toString());
        
        /*
         * If the temporary directory do not exists or can't be created,
         * lets the 'database' attribute unset. If the user do not set it
         * explicitly (for example through JNDI), an exception will be thrown
         * when 'getConnection()' will be invoked.
         */
        dataSource.setUser("SA"); // System administrator. No password.
        
        return dataSource;
	}
	
	/**
	 * Returns the default directory for the EPSG database. If the
	 * {@value #DIRECTORY_KEY}
	 * {@linkplain System#getProperty(String) system property} is defined and
	 * contains the name of a directory with a valid
	 * {@linkplain File#getParent parent}, then the {@value #DATABASE_NAME}
	 * database will be saved in that directory. Otherwise, a temporary
	 * directory will be used.
	 */
	static File getDirectory() {
		try {
			final String property = System
					.getProperty(HsqlEpsgDatabase.DIRECTORY_KEY);
			if (property != null) {
				final File directory = new File(property);
				/*
				 * Creates the directory if needed (mkdir), but NOT the parent
				 * directories (mkdirs) because a missing parent directory may
				 * be a symptom of an installation problem. For example if
				 * 'directory' is a subdirectory in the temporary directory
				 * (~/tmp/), this temporary directory should already exists. If
				 * it doesn't, an administrator should probably looks at this
				 * problem.
				 */
				if (directory.isDirectory() || directory.mkdir()) {
					return directory;
				}
			}
		} catch (SecurityException e) {
			/*
			 * Can't fetch the base directory from system properties. Fallback
			 * on the default temporary directory.
			 */
		}
		File directory = new File(System.getProperty("java.io.tmpdir", "."),
				"Geotools");
		if (directory.isDirectory() || directory.mkdir()) {
			directory = new File(directory, "Databases/HSQL");
			if (directory.isDirectory() || directory.mkdirs()) {
				return directory;
			}
		}
		return null;
	}

	/**
	 * Returns {@code true} if the database contains data. This method returns {@code false}
	 * if an empty EPSG database has been automatically created by HSQL and not yet populated.
	 */
	static boolean dataExists(final Connection connection) throws SQLException {
	    final ResultSet tables = connection.getMetaData().getTables(
	            null, null, "EPSG_%", new String[] {"TABLE"});
	    final boolean exists = tables.next();
	    tables.close();
	    return exists;
	}

}
