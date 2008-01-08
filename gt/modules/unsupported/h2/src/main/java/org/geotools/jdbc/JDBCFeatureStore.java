/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.logging.Level;
import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.Association;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.GmlObjectId;
import org.opengis.filter.sort.SortBy;
import org.geotools.data.ContentFeatureCollection;
import org.geotools.data.FeatureStore;
import org.geotools.data.GmlObjectStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;


/**
 * FeatureStore implementation for jdbc based relational database tables.
 * <p>
 * All the operations of this class are delegated to {@link JDBCFeatureCollection}
 * via the {@link #all(ContentState)} and {@link #filtered(ContentState, Filter)}
 * methods.
 *
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class JDBCFeatureStore extends ContentFeatureStore {
    
    /**
     * primary key of the table
     */
    PrimaryKey primaryKey;

    /**
     * Creates the new feature store.
     * @param entry The datastore entry.
     */
    public JDBCFeatureStore(ContentEntry entry) throws IOException {
        super(entry);

        //TODO: cache this
        primaryKey = ((JDBCDataStore) entry.getDataStore()).getPrimaryKey(entry);
    }

    /**
     * Type narrow to {@link JDBCDataStore}.
     */
    public JDBCDataStore getDataStore() {
        return (JDBCDataStore) super.getDataStore();
    }

    /**
     * Type narrow to {@link JDBCState}.
     */
    public JDBCState getState() {
        return (JDBCState) super.getState();
    }

    /**
     * Returns the primary key of the table backed by feature store.
     */
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * This method operates by delegating to the
     * {@link JDBCFeatureCollection#update(AttributeDescriptor[], Object[])}
     * method provided by the feature collection resulting from
     * {@link #filtered(ContentState, Filter)}.
     *
     * @see FeatureStore#modifyFeatures(AttributeDescriptor[], Object[], Filter)
     */
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
        throws IOException {
        if (filter == null) {
            String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException(msg);
        }

        JDBCFeatureCollection features = (JDBCFeatureCollection) filtered(getState(), filter);
        features.update(type, value);
    }

    /**
     * Builds the feature type from database metadata.
     */
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();

        //set up the name
        String tableName = entry.getName().getLocalPart();
        tb.setName(tableName);

        //set the namespace, if not null
        if (entry.getName().getNamespaceURI() != null) {
            tb.setNamespaceURI(entry.getName().getNamespaceURI());
        } else {
            //use the data store
            tb.setNamespaceURI(getDataStore().getNamespaceURI());
        }

        //grab the schema
        String databaseSchema = getDataStore().getDatabaseSchema();

        //ensure we have a connection
        Connection cx = getDataStore().getConnection(getState());

        //get metadata about columns from database
        try {
            DatabaseMetaData metaData = cx.getMetaData();

            /*
             *        <LI><B>COLUMN_NAME</B> String => column name
             *        <LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
             *        <LI><B>TYPE_NAME</B> String => Data source dependent type name,
             *  for a UDT the type name is fully qualified
             *        <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
             *            types this is the maximum number of characters, for numeric or
             *            decimal types this is precision.
             *        <LI><B>BUFFER_LENGTH</B> is not used.
             *        <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
             *        <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
             *        <LI><B>NULLABLE</B> int => is NULL allowed.
             *      <UL>
             *      <LI> columnNoNulls - might not allow <code>NULL</code> values
             *      <LI> columnNullable - definitely allows <code>NULL</code> values
             *      <LI> columnNullableUnknown - nullability unknown
             *      </UL>
             *         <LI><B>COLUMN_DEF</B> String => default value (may be <code>null</code>)
             *        <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
             *      does not allow NULL values; "YES" means the column might
             *      allow NULL values.  An empty string means nobody knows.
             */
            ResultSet columns = metaData.getColumns(null, databaseSchema, tableName, "%");

            try {
                SQLDialect dialect = getDataStore().getSQLDialect();

                while (columns.next()) {
                    String name = columns.getString("COLUMN_NAME");

                    //do not include primary key in the type
                    /*
                     *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
                     *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
                     *        <LI><B>TABLE_NAME</B> String => table name
                     *        <LI><B>COLUMN_NAME</B> String => column name
                     *        <LI><B>KEY_SEQ</B> short => sequence number within primary key
                     *        <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
                     */
                    ResultSet primaryKeys = metaData.getPrimaryKeys(null, databaseSchema, tableName);

                    try {
                        while (primaryKeys.next()) {
                            String keyName = primaryKeys.getString("COLUMN_NAME");

                            if (name.equals(keyName)) {
                                name = null;

                                break;
                            }
                        }
                    } finally {
                        JDBCDataStore.closeSafe(primaryKeys);
                    }

                    if (name == null) {
                        continue;
                    }

                    //check for association
                    if (getDataStore().isAssociations()) {
                        getDataStore().ensureAssociationTablesExist(cx);

                        //check for an association
                        String sql = getDataStore().selectRelationshipSQL(tableName, name);

                        Statement st = cx.createStatement();

                        try {
                            ResultSet relationships = st.executeQuery(sql);

                            try {
                                if (relationships.next()) {
                                    //found, create a special mapping 
                                    tb.add(name, Association.class);

                                    continue;
                                }
                            } finally {
                                JDBCDataStore.closeSafe(relationships);
                            }
                        } finally {
                            JDBCDataStore.closeSafe(st);
                        }
                    }

                    //figure out the type mapping

                    //first ask the dialect
                    Class binding = dialect.getMapping(columns);

                    if (binding == null) {
                        //determine from type mappings
                        int dataType = columns.getInt("DATA_TYPE");
                        binding = getDataStore().getMapping(dataType);
                    }

                    if (binding == null) {
                        //determine from type name mappings
                        String typeName = columns.getString("TYPE_NAME");
                        binding = getDataStore().getMapping(typeName);
                    }

                    //if still not found, resort to Object
                    if (binding == null) {
                        JDBCDataStore.LOGGER.warning("Could not find mapping for:" + name);
                        binding = Object.class;
                    }

                    //nullability
                    if ( "NO".equalsIgnoreCase( columns.getString( "IS_NULLABLE" ) ) ) {
                        tb.nillable(false);
                        tb.minOccurs(1);
                    }
                    
                    //determine if this attribute is a geometry or not
                    if (Geometry.class.isAssignableFrom(binding)) {
                        //add the attribute as a geometry, try to figure out 
                        // its srid first
                        Integer srid = null;

                        try {
                            srid = dialect.getGeometrySRID(databaseSchema, tableName, name, cx);
                        } catch (Exception e) {
                            String msg = "Error occured determing srid for " + tableName + "."
                                + name;
                            getDataStore().LOGGER.log(Level.WARNING, msg, e);
                        }

                        tb.add(name, binding, srid);
                    } else {
                        //add the attribute
                        tb.add(name, binding);
                    }
                }

                return tb.buildFeatureType();
            } finally {
                getDataStore().closeSafe(columns);
            }
        } catch (SQLException e) {
            String msg = "Error occurred building feature type";
            throw (IOException) new IOException().initCause(e);
        }
    }

    protected JDBCFeatureCollection all(ContentState state) {
        return new JDBCFeatureCollection(this, getState());
    }

    protected JDBCFeatureCollection filtered(ContentState state, Filter filter) {
        return new JDBCFeatureCollection(this, getState(), filter);
    }

    protected JDBCFeatureCollection sorted(ContentState state, SortBy[] sort, Filter filter) {
        JDBCFeatureCollection collection = filtered(state, filter);
        collection.setSort(sort);

        return collection;
    }
}
