/**
 * 
 */
package org.geotools.data.postgis.fidmapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.geotools.data.jdbc.fidmapper.AutoIncrementFIDMapper;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.Feature;

/**
 * @author Jesse
 *
 */
public class PostGISAutoIncrementFIDMapper extends AutoIncrementFIDMapper
		implements FIDMapper {

	private static final long serialVersionUID = -6082930630426171079L;

	boolean can_usepg_get_serial_sequence=true;
	
	public PostGISAutoIncrementFIDMapper(String tableName, String colName, int dataType) {
		super(tableName, colName, dataType);
	}

	
	public String createID(Connection conn, Feature feature, Statement statement) throws IOException {
		if( can_usepg_get_serial_sequence ){
			try{
				statement.execute("SELECT currval(pg_get_serial_sequence(\'"+tableName+"\',\'"+colName+"\'))");
		   		ResultSet resultSet = statement.getResultSet();
	    		if( resultSet.next() )
	    			return resultSet.getString(this.colName);
	    		else
	    			return super.createID(conn, feature, statement);
			}catch (Exception e) {
				can_usepg_get_serial_sequence=false;
				return super.createID(conn, feature, statement);
			}
		}else{
			return super.createID(conn, feature, statement);
		}
	}
	
	
}
