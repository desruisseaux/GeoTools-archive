package org.geotools.data.geometryless;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
// import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.attributeio.WKTAttributeIO;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderLocationsXY;
import org.geotools.data.geometryless.attributeio.PointXYAttributeIO;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * An implementation of the GeoTools Data Store API for a generic non-spatial database platform.
 * 
 * This specialisation uses X,Y (lat/lon) database colums to hold point geometries
 * 
 * the constructor is used to pass metadata from datastore to SQLEncoder class
 *
 * <br>
 * Please see {@link org.geotools.data.jdbc.JDBCDataStore class JDBCDataStore} and
 * {@link org.geotools.data.DataStore interface DataStore} for DataStore usage details.
 * @author Rob Atkinson rob@socialchange.net.au
 */

public class LocationsXYDataStore extends org.geotools.data.geometryless.JDBCDataStore {
    /** The logger for the mysql module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.geometryless");

  private String XCoordColumnName = null;
   private String YCoordColumnName = null;
    private String geomName = null;
   
    public LocationsXYDataStore(ConnectionPool connectionPool) throws IOException {
        super(connectionPool);
    }

    /**
     * Constructor for LocationsXYDataStore where the database schema name is provided.
     * @param connectionPool a MySQL {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @throws IOException if the database cannot be properly accessed
     */
    public LocationsXYDataStore(ConnectionPool connectionPool, String databaseSchemaName, String x, String y, String geomName)    
        throws IOException {
        super(connectionPool, databaseSchemaName);
       this.XCoordColumnName = x;
       this.YCoordColumnName = y;
       this.geomName = geomName;
    }

    /**
     * Utility method for getting a FeatureWriter for modifying existing features,
     * using no feature filtering and auto-committing.  Not used for adding new
     * features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for modifying existing features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriter(String typeName) throws IOException {
        return getFeatureWriter(typeName, Filter.NONE, Transaction.AUTO_COMMIT);
    }

    /**
     * Utility method for getting a FeatureWriter for adding new features, using
     * auto-committing.  Not used for modifying existing features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for adding new features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriterAppend(String typeName) throws IOException {
        return getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);
    }

    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet
     * contains the information retrieved by a call to  getColumns() on the
     * DatabaseMetaData object.  This information  can be used to construct an
     * Attribute Type.
     * 
     * <p>
     * In addition to standard SQL types, this method identifies MySQL 4.1's geometric
     * datatypes and creates attribute types accordingly.  This happens when the
     * datatype, identified by column 5 of the ResultSet parameter, is equal to
     * java.sql.Types.OTHER.  If a Types.OTHER ends up not being geometric, this
     * method simply calls the parent class's buildAttributeType method to do something
     * with it.
     * </p>
     * 
     * <p>
     * Note: Overriding methods must never move the current row pointer in the
     * result set.
     * </p>
     *
     * @param rs The ResultSet containing the result of a
     *        DatabaseMetaData.getColumns call.
     *
     * @return The AttributeType built from the ResultSet.
     *
     * @throws SQLException If an error occurs processing the ResultSet.
     * @throws DataSourceException Provided for overriding classes to wrap
     *         exceptions caused by other operations they may perform to
     *         determine additional types.  This will only be thrown by the
     *         default implementation if a type is present that is not present
     *         in the TYPE_MAPPINGS.
     */
    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        final int TYPE_NAME = 6;

        try {
            int dataType = rs.getInt(DATA_TYPE);
	    String colName = rs.getString(COLUMN_NAME);
	    LOGGER.fine("dataType: " + dataType + " " + rs.getString(TYPE_NAME) + " " + colName );
	    Class type = (Class) TYPE_MAPPINGS.get(new Integer(dataType));

	     //This should be improved - first should probably check for 
	     //presence of both the x and y columns, only create the geometry
	     //if both are found, instead of just ignoring the y - right now
	     //the y could just not exist.  And then if either do not exist
	     //an exception should be thrown.
	     //Also, currently the name of the geometry is hard coded - 
	     //do we want it to be user configurable?  ch
	     if (colName.equals(XCoordColumnName)) {
		 //do type checking here, during config, not during reading.
		 if (Number.class.isAssignableFrom(type)) {
		     return AttributeTypeFactory.newAttributeType(geomName,
								  Point.class);
		 } else {
		     String excMesg = "Specified X column of " + colName + 
			 " of type: " + type + ", can not be used as x point";
		     throw new DataSourceException(excMesg);
		 }
      
	     } else if (colName.equals(YCoordColumnName)) {
		 if (Number.class.isAssignableFrom(type)) {
		     return null;
		 } else {
		     String excMesg = "Specified X column of " + colName + 
			 " of type: " + type + ", can not be used as x point";
		     throw new DataSourceException(excMesg);
		 }
	     } else {
		 return super.buildAttributeType(rs);
            }
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred: " + e.getMessage());
        }
    }

    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
    	
    
        SQLEncoder encoder = new SQLEncoderLocationsXY(XCoordColumnName,YCoordColumnName);
        encoder.setFIDMapper(getFIDMapper(typeName));
        return new LocationsXYSQLBuilder(encoder, XCoordColumnName, 
                                         YCoordColumnName);
    }

    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeType type, QueryData queryData) {
        return new PointXYAttributeIO();
    }

    protected JDBCFeatureWriter createFeatureWriter(FeatureReader reader, QueryData queryData)
        throws IOException {
        LOGGER.fine("returning jdbc feature writer");

        return new GeometrylessFeatureWriter(reader, queryData);
    }

}
