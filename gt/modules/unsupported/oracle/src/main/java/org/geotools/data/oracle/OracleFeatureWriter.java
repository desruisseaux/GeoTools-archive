/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.oracle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureLockException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.JDBCTextFeatureWriter;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.oracle.io.MutableFIDFeature;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.filter.SQLEncoderOracle;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Subclasses JDBCTextFeatureWriter to issue Oracle transactions  directly as
 * sql text statements.  The super class takes care of all the nasty details,
 * this just returns the encoded geometry. To get some speed increases Jody
 * maintains that this class should not be used, that the updatable result
 * sets of JDBCFeatureWriter will work better.  But I couldn't get those to
 * work at all, whereas this works great for me.  We could also consider
 * putting the option for this or jdbc in the factory for OracleDataStore.
 * Should also consider using prepared statements for inserts, as they should
 * work faster - this should probably be done in the superclass.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public class OracleFeatureWriter implements FeatureWriter {
	private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
	
	GeometryConverter converter;
	
    protected FIDMapper mapper = null;

    /** indicates the lock attempt is in progress */
    final int STATE_WAIT = 1;

    /** indicates the lock attempt was successful */
    final int STATE_SUCCESS = 2;

    /** indicates the lock attempt failed horribly */
    final int STATE_FAILURE = 3;
    
    protected QueryData queryData;
    protected FeatureReader reader;
    protected Feature live; // current for FeatureWriter
    protected Feature current; // copy of live returned to user
    protected FeatureListenerManager listenerManager = new FeatureListenerManager();
    protected boolean closed;
    protected Object[] fidAttributes;
    
    public OracleFeatureWriter(FeatureReader fReader, QueryData queryData )
        throws IOException {
        this.reader = reader;
        this.queryData = queryData;
        mapper = queryData.getMapper();
        this.converter = new GeometryConverter( (OracleConnection) queryData.getConnection() );
        
    }

    protected String getGeometryInsertText(Geometry geom, int srid)
        throws IOException {
    	return "?"; // Please use a prepaired statement to insert your geometry
    	
        //String geomText = SQLEncoderOracle.toSDOGeom(geom, srid);
        //return geomText;
        }

    /**
     * Override that uses sql statements to perform the operation.
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doUpdate(org.geotools.feature.Feature,
     *      org.geotools.feature.Feature)
     */
    protected void doUpdate(Feature live, Feature current)
        throws IOException, SQLException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("updating postgis feature " + current);
        }

        PreparedStatement statement = null;
        Connection conn = null;

        try {
            conn = queryData.getConnection();

            String sql = makeUpdateSql(live, current);
            statement = conn.prepareStatement(sql);

            FeatureType schema = current.getFeatureType();
            int position = 1;

            for (int i = 0; i < current.getNumberOfAttributes(); i++) {
                AttributeType type = schema.getAttributeType(i);

                if (type instanceof GeometricAttributeType) {
                    Geometry geometry = (Geometry) current.getAttribute(i);

                    if (geometry == null) {
                        geometry = current.getDefaultGeometry();
                    }

                    LOGGER.fine("ORACLE SPATIAL: geometry to be written:"
                        + geometry);

                    STRUCT struct = converter.toSDO(geometry);
                    statement.setObject(position, struct);
                    LOGGER.fine(
                        "ORACLE SPATIAL: set geometry parameter at position:"
                        + position);
                    position++;

                    break;
                }
            }

            // System.out.println(sql);
            LOGGER.fine(sql);
            statement.execute();
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column"
                + sqle.getLocalizedMessage();
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    String msg = "Error closing JDBC Statement";
                    LOGGER.log(Level.WARNING, msg, e);
                }
            }
        }
    }

    
    /**
     * Override that uses sql prepaired statements to perform the operation.
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doInsert(org.geotools.data.jdbc.MutableFIDFeature)
     */
    protected void doInsert(MutableFIDFeature current)
        throws IOException, SQLException {
        LOGGER.fine("inserting into postgis feature " + current);

        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = queryData.getConnection();
            String sql = makeInsertSql(current);
            statement = conn.prepareStatement( sql );
            
            int position = 1;
            FeatureType schema = current.getFeatureType();
            for( int i=0; i<current.getNumberOfAttributes();i++){
            	AttributeType type = schema.getAttributeType( i );
            	if( type instanceof GeometricAttributeType ){
            		Geometry geometry = (Geometry) current.getAttribute( i );
            		STRUCT struct = converter.toSDO( geometry );
            		statement.setObject( position, struct );
            		position++;
            	}
            }
            LOGGER.fine(sql);
            statement.execute();

            // should the ID be generated during an insert, we need to read it back
            // and set it into the feature
          if (((mapper.getColumnCount() > 0)
          && mapper.hasAutoIncrementColumns())) {
//          if (((mapper.getColumnCount() > 0))) {
                current.setID(mapper.createID(conn, current, statement));
            }
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column" + sqle.getLocalizedMessage();
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    String msg = "Error closing JDBC Statement";
                    LOGGER.log(Level.WARNING, msg, e);
                }
            }
        }
    }
    
    /**
     * Creates a sql insert statement.  Uses each feature's schema, which makes
     * it possible to insert out of order, as well as inserting less than all
     * features.
     *
     * @param feature the feature to add.
     *
     * @return an insert sql statement.
     *
     * @throws IOException
     */
    protected String makeInsertSql(Feature feature) throws IOException {
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
        FeatureType featureType = ftInfo.getSchema();

        String tableName = encodeName(featureType.getTypeName());
        AttributeType[] attributeTypes = featureType.getAttributeTypes();

        String attrValue;

        StringBuffer statementSQL = new StringBuffer("INSERT INTO " + tableName
                + " (");

        if (!mapper.returnFIDColumnsAsAttributes()) {
            for (int i = 0; i < mapper.getColumnCount(); i++) {
                if (!mapper.isAutoIncrement(i)) {
                    statementSQL.append(mapper.getColumnName(i)).append(",");
                }
            }
        }

        for (int i = 0; i < attributeTypes.length; i++) {
            String colName = encodeColumnName(attributeTypes[i].getName());
            statementSQL.append(colName).append(",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');
        statementSQL.append(" VALUES (");

        if (!mapper.returnFIDColumnsAsAttributes()
                && !mapper.hasAutoIncrementColumns()) {
            String FID = mapper.createID(queryData.getConnection(), feature,
                    null);
            if( current instanceof MutableFIDFeature ){
                ((MutableFIDFeature)current).setID(FID);
            }
            Object[] primaryKey = mapper.getPKAttributes(FID);

            for (int i = 0; i < primaryKey.length; i++) {
                if (!mapper.isAutoIncrement(i)) {
                    attrValue = addQuotes(primaryKey[i]);
                    statementSQL.append(attrValue).append(",");
                }
            }
        }

        Object[] attributes = feature.getAttributes(null);

        for (int i = 0; i < attributeTypes.length; i++) {
            if (attributeTypes[i] instanceof GeometryAttributeType) {
                String geomName = attributeTypes[i].getName();
                int srid = ftInfo.getSRID(geomName);
                attrValue = getGeometryInsertText((Geometry) attributes[i], srid);
            } else {
                attrValue = addQuotes(attributes[i]);
            }

            statementSQL.append(attrValue + ",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');

        return (statementSQL.toString());
    }

    /**
     * Adds quotes to an object for storage in postgis.  The object should be a
     * string or a number.  To perform an insert strings need quotes around
     * them, and numbers work fine with quotes, so this method can be called
     * on unknown objects.
     *
     * @param value The object to add quotes to.
     *
     * @return a string representation of the object with quotes.
     */
    protected String addQuotes(Object value) {
        String retString;

        if (value != null) {
            retString = "'" + doubleQuote(value) + "'";
        } else {
            retString = "null";
        }

        return retString;
    }

    String doubleQuote(Object obj) {
        return obj.toString().replaceAll("'", "''");
    }

    /**
     * Encodes the tableName, default is to do nothing, but postgis will
     * override and put double quotes around the tablename.
     */
    protected String encodeName(String tableName) {
        return tableName;
    }
    
    /**
     * Encodes the colName, default just calls {@link #encodeName(String)}.
     */
    protected String encodeColumnName(String colName) {
        return encodeName(colName);
    }

    /**
     * Override that uses sql statements to perform the operation.
     *
     * @see org.geotools.data.FeatureWriter#remove()
     */
    public void remove() throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) 
              LOGGER.fine("inserting into postgis feature " + current);

        Statement statement = null;
        Connection conn = null;

        try {
            conn = queryData.getConnection();
            statement = conn.createStatement();
            Envelope bounds = this.live.getBounds();
            String sql = makeDeleteSql(current);
            if (LOGGER.isLoggable(Level.FINE)) 
                LOGGER.fine(sql);
            //System.out.println(sql);
            statement.executeUpdate(sql);

            listenerManager.fireFeaturesRemoved(getFeatureType()
                                                    .getTypeName(),
                queryData.getTransaction(), bounds, false);
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    String msg = "Error closing JDBC Statement";
                    LOGGER.log(Level.WARNING, msg, e);
                }
            }
        }
    }

    /**
     * Generates the query for the sql delete statement
     *
     * @param feature
     *
     *
     * @throws IOException
     */
    protected String makeDeleteSql(Feature feature) throws IOException {
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
        FeatureType fetureType = ftInfo.getSchema();

        String tableName = encodeName(fetureType.getTypeName());

        StringBuffer statementSQL = new StringBuffer("DELETE FROM " + tableName
                + " WHERE ");
        Object[] pkValues = mapper.getPKAttributes(feature.getID());

        for (int i = 0; i < mapper.getColumnCount(); i++) {
            statementSQL.append( encodeColumnName( mapper.getColumnName(i) )).append(" = ").append(addQuotes(
                    pkValues[i]));

            if (i < (mapper.getColumnCount() - 1)) {
                statementSQL.append(" AND ");
            }
        }

        return (statementSQL.toString());
    }

    /**
     * Generate the select for update statement, which will attempt to
     * lock features for update.  This should be overwritten by databases
     * which want to take advantage of this method.
     * 
     * This method is called in a timer thread, to prevent blocking.
     * 
     * @since 2.2.0
     * @param current
     * @return sql string or null
     */
    protected String makeSelectForUpdateSql(Feature current) {
        return null;
    }
    
    /**
     * Generate the update sql statement
     *
     * @param live
     * @param current
     *
     *
     * @throws IOException
     */
    protected String makeUpdateSql(Feature live, Feature current)
        throws IOException {
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
        FeatureType featureType = ftInfo.getSchema();
        AttributeType[] attributes = featureType.getAttributeTypes();

        String tableName = encodeName(featureType.getTypeName());

        StringBuffer statementSQL = new StringBuffer("UPDATE " + tableName
                + " SET ");

        for (int i = 0; i < current.getNumberOfAttributes(); i++) {
            Object currAtt = current.getAttribute(i);
            Object liveAtt = live.getAttribute(i);

            if (!DataUtilities.attributesEqual(currAtt, liveAtt)) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.fine("modifying att# " + i + " to " + currAtt);
                }

                String attrValue = null;
                if (attributes[i] instanceof GeometryAttributeType) {
                    String geomName = attributes[i].getName();
                    int srid = ftInfo.getSRID(geomName);
                    attrValue = getGeometryInsertText((Geometry) currAtt, srid);
                } else {
                    attrValue = addQuotes(currAtt);
                }


                String colName = encodeColumnName(attributes[i].getName());
                statementSQL.append(colName).append(" = ")
                            .append(attrValue).append(", ");
            }
        }

        //erase the last comma
        statementSQL.setLength(statementSQL.length() - 2);
        statementSQL.append(" WHERE ");

        Object[] pkValues = mapper.getPKAttributes(current.getID());

        for (int i = 0; i < mapper.getColumnCount(); i++) {
            statementSQL.append(mapper.getColumnName(i)).append(" = ").append(addQuotes(
                    pkValues[i]));

            if (i < (mapper.getColumnCount() - 1)) {
                statementSQL.append(" AND ");
            }
        }

        return (statementSQL.toString());
    }

    /**
     * This version does not use QueryData udpate/insert/remove methods, but
     * uses separate queries instead
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#useQueryDataForInsert()
     */
    protected boolean useQueryDataForInsert() {
        return false;
    }
    
    /**
     * @see org.geotools.data.FeatureWriter#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return reader.getFeatureType();
    }

    /**
     * @see org.geotools.data.FeatureWriter#next()
     */
    public Feature next() throws IOException {
        if (reader == null) {
            throw new IOException("FeatureWriter has been closed");
        }

        FeatureType featureType = getFeatureType();

        if (hasNext()) {
            try {
                live = reader.next();
                current = featureType.duplicate(live);
                LOGGER.finer("Calling next on writer");
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Unable to edit " + live.getID()
                    + " of " + featureType.getTypeName(), e);
            }
        } else {
            //          new content
            live = null;

            try {
                Feature temp = DataUtilities.template(featureType);

                /* Here we create a Feature with a Mutable FID.
                 * We use data utilities to create a default set of attributes
                 * for the feature and these are copied into the a new
                 * MutableFIDFeature.  Thsi can probably be improved later,
                 * there is also a dependency on DefaultFeatureType here since
                 * DefaultFeature depends on it and MutableFIDFeature extends default
                 * feature.  This may be an issue if someone reimplements the Feature
                 * interfaces.  It could address by providing a full implementation
                 * of Feature in MutableFIDFeature at a later date.
                 *
                 */
                current = new MutableFIDFeature((DefaultFeatureType) featureType,
                        temp.getAttributes(
                            new Object[temp.getNumberOfAttributes()]), null);

                if (useQueryDataForInsert()) {
                    queryData.startInsert();
                }
            } catch (IllegalAttributeException e) {
                throw new DataSourceException(
                    "Unable to add additional Features of "
                    + featureType.getTypeName(), e);
            } catch (SQLException e) {
                throw new DataSourceException("Unable to move to insert row. "
                    + e.getMessage(), e);
            }
        }

        return current;
    }

    /**
     * @see org.geotools.data.FeatureWriter#write()
     */
    public void write() throws IOException {
        if (closed) {
            throw new IOException("FeatureWriter has been closed");
        }

        if (current == null) {
            throw new IOException("No feature available to write");
        }

        LOGGER.fine("write called, live is " + live + " and cur is " + current);

        if (live != null) {
            if (live.equals(current)) {
                // no modifications made to current
                live = null;
                current = null;
            } else {
                try {
                    doUpdate(live, current);
                } catch (SQLException sqlException) {
                    queryData.close(sqlException);
                    throw new DataSourceException("Error updating row",
                        sqlException);
                }

                Envelope bounds = new Envelope();
                bounds.expandToInclude(live.getBounds());
                bounds.expandToInclude(current.getBounds());
                listenerManager.fireFeaturesChanged(getFeatureType()
                                                        .getTypeName(),
                    queryData.getTransaction(), bounds, false);
                live = null;
                current = null;
            }
        } else {
            LOGGER.fine("doing insert in jdbc featurewriter");

            try {
                doInsert((MutableFIDFeature) current);
            } catch (SQLException e) {
                throw new DataSourceException("Row adding failed.", e);
            }

            listenerManager.fireFeaturesAdded(getFeatureType().getTypeName(),
                queryData.getTransaction(), current.getBounds(), false);
            current = null;
        }
    }

    /**
     * @see org.geotools.data.FeatureWriter#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (queryData.isClosed()) {
            throw new IOException("Feature writer is closed");
        }

        return reader.hasNext();
    }

    /**
     * @see org.geotools.data.FeatureWriter#close()
     */
    public void close() throws IOException {
        //changed this from throwing an exception if already closed to just
        //issuing a warning.  Mysql was having trouble with this, but I see
        //no great harm in not throwing an exception, since this will only
        //be in clean-up.
        if (queryData.isClosed()) {
            LOGGER.warning("Feature writer calling close when queryData is " +
                        " already closed");
        } else {
            reader.close();
    }
    }

    public void setFeatureListenerManager( FeatureListenerManager listenerManager2 ) {
        this.listenerManager=listenerManager2;
    }
}