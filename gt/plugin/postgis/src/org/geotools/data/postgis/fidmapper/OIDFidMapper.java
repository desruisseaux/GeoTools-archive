package org.geotools.data.postgis.fidmapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;

import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.fidmapper.AbstractFIDMapper;
import org.geotools.feature.Feature;
import org.postgresql.PGStatement;


/**
 * @author wolf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OIDFidMapper extends AbstractFIDMapper {

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#initSupportStructures()
     */
    public void initSupportStructures() {
        // nothing to do, oid are supported in the table or not depending on 
        // database configuration and table creation commands (not sure)
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getID(java.lang.Object[])
     */
    public String getID(Object[] attributes) {
        return attributes[0].toString();
    }

    /**
     * Will always return an emtpy array since OIDs are not updatable, 
     * so we don't try to parse the Feature ID at all.
     * Um - this causes failures in SQLEncoder - that may be the place
     * to fix it, but I'm putting it in here for now.  I believe that
     * the oid will not try to get updated since auto increment
     * is set to false.
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getPKAttributes(java.lang.String)
     */
    public Object[] getPKAttributes(String FID) throws IOException {
	try {
	    return new Object[] { new Long(Long.parseLong(FID)) };
	} catch (NumberFormatException nfe) {
	    //if we get a really bad featureid we want to return something
	    //that will not mess up the database and throw an exception,
	    //we just want to not match against it, so we return -1
	    return new Object[] { new Integer(-1) };
	}
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#createID(java.sql.Connection, org.geotools.feature.Feature, Statement)
     */
    public String createID(Connection conn, Feature feature, Statement statement) throws IOException {
        try {
            PGStatement pgStatement = (PGStatement) statement;
            return String.valueOf(pgStatement.getLastOID());
        } catch (SQLException e) {
            throw new DataSourceException("Problems occurred while getting last generate oid from Postgresql statement", e);
        } catch (ClassCastException e) {
            throw new DataSourceException("Statement is not a PGStatement. OIDFidMapper can be used only with Postgres!", e);
        }
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#returnFIDColumnsAsAttributes()
     */
    public boolean returnFIDColumnsAsAttributes() {
        return false;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnCount()
     */
    public int getColumnCount() {
        return 1;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnName(int)
     */
    public String getColumnName(int colIndex) {
        return "oid";
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnType(int)
     */
    public int getColumnType(int colIndex) {
        return Types.NUMERIC;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnSize(int)
     */
    public int getColumnSize(int colIndex) {
        return 8;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnDecimalDigits(int)
     */
    public int getColumnDecimalDigits(int colIndex) {
        return 0;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int colIndex) {
        return true;
    }

}
