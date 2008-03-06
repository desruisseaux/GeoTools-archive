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
package org.geotools.data.postgis;

import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderPostgis;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Builds sql for postgis.
 *
 * @author Chris Holmes
 * @source $URL$
 */
public class PostgisSQLBuilder extends DefaultSQLBuilder {
    /** If true, WKB format is used instead of WKT */
    protected boolean WKBEnabled = false;
    
    /** If true, ByteA function is used to transfer WKB data*/
    protected boolean byteaEnabled = false;

    /** If true, tables are qualified with a schema **/
    protected boolean schemaEnabled = true;
    
    /** the datastore **/
    protected JDBCDataStoreConfig config;
    
    /**
     *
     */
    public PostgisSQLBuilder(int srid, JDBCDataStoreConfig config) {
        this((SQLEncoder) new SQLEncoderPostgis(srid),config);
    }

    /**
     * Constructor with encoder.  Use PostgisSQLBuilder(encoder, config, ft) if possible.
     * 
     * @param encoder
     */
    public PostgisSQLBuilder(SQLEncoder encoder, JDBCDataStoreConfig config) {
        super(encoder);
        this.config = config;
    }
    
    public PostgisSQLBuilder(SQLEncoder encoder, JDBCDataStoreConfig config, SimpleFeatureType ft) {
    	super(encoder);
        this.config = config;
        this.ft = ft;
        encoder.setFeatureType( ft );
    }

    /**
     * Produces the select information required.
     * 
     * <p>
     * The featureType, if known, is always requested.
     * </p>
     * 
     * <p>
     * sql: <code>featureID (,attributeColumn)</code>
     * </p>
     * 
     * <p>
     * We may need to provide AttributeReaders with a hook so they can request
     * a wrapper function.
     * </p>
     *
     * @param sql
     * @param mapper
     * @param attributes
     */
    public void sqlColumns(StringBuffer sql, FIDMapper mapper,
        AttributeDescriptor[] attributes) {
        for (int i = 0; i < mapper.getColumnCount(); i++) {
            sql.append("\""+mapper.getColumnName(i)+"\"");
            // DJB: add quotes in. NOTE: if FID mapper isnt oid (ie. PK - Primary Key), you could be
            // requesting PK columns multiple times
            if ((attributes.length > 0) || (i < (mapper.getColumnCount() - 1))) {
                sql.append(", ");
            }
        }

        for (int i = 0; i < attributes.length; i++) {
            AttributeDescriptor attribute = attributes[i];

                if (attribute instanceof GeometryDescriptor) {   
                    GeometryDescriptor geometryAttribute = (GeometryDescriptor) attribute;
                    CoordinateReferenceSystem crs = geometryAttribute.getCRS();
                    final int D = crs == null ? 2 : crs.getCoordinateSystem().getDimension();
                    
                    if (WKBEnabled) {
                        if(byteaEnabled) {
                            columnGeometryByteaWKB( sql, geometryAttribute, D );
                        } else {
                            columnGeometryWKB( sql, geometryAttribute, D );
                        }
                    } else {
                        columnGeometry( sql, geometryAttribute, D );
                    }
                } else {
                    columnAttribute(sql, attribute);
                }

                if (i < (attributes.length - 1)) {
                    sql.append(", ");
                }
            }
            System.out.println( sql );
        }
        /** Used when WKB "ByteA" is enabled */
        private void columnGeometryByteaWKB(StringBuffer sql,
                GeometryDescriptor geometryAttribute, final int D) {
            
            sql.append("encode(");
            if( D == 3 ){
                sql.append("asEWKB(");
            }
            else {
                sql.append("asBinary(");
            }
            columnGeometry(sql, geometryAttribute.getLocalName(), D );
            sql.append(",'XDR'),'base64')");
        }
        /** Used when plain WKB is enabled */   
        private void columnGeometryWKB(StringBuffer sql,
                GeometryDescriptor geometryAttribute, final int D) {
            
            if( D == 3 ){
                sql.append("asEWKB(");
            }
            else {
                sql.append("asBinary(");
            }
            columnGeometry(sql, geometryAttribute.getLocalName(), D );
            sql.append(",'XDR')");
        }
        /** Used to request a text format. */    
        private void columnGeometry(StringBuffer sql,
                GeometryDescriptor geometryAttribute, final int D) {
            if( D == 3 && !isForce2D() ){
                sql.append("asEWKT(");
            }
            else {
                sql.append("asText(");
            }
            columnGeometry(sql, geometryAttribute.getLocalName(), D );
            sql.append(")");
        }
        /**
         * Used to wrap the correct function (force_3d or force3d) around
         * the request for geometry data.
         * <p>
         * This method prevents the request of extra ordinates that will
         * not be used.
         * 
         * @see isForce2D
         * @see Hints.FEATURE_2D
         *  
         * @param sql
         * @param geomName
         * @param D
         */
        private void columnGeometry(StringBuffer sql,String geomName, final int D) {
            if (D == 2 || isForce2D() ){ 
                sql.append("force_2d(\"" + geomName + "\")");
            }
            else if( D == 3 ){
                sql.append("force_3d(\"" + geomName + "\")");
            }
            else {
                // D = 4?
                // force 2D is the default behaviour until you report
                // this as a bug and are willing to test with real data!
                sql.append("force_2d(\"" + geomName + "\")" );
            }
        }
        
        private final void columnAttribute(StringBuffer sql, AttributeDescriptor attribute){
            sql.append( "\"" );
            sql.append( attribute.getLocalName() );
            sql.append( "\"" );
        }
    
    /**
     * Constructs FROM clause for featureType
     * 
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     *
     * @param sql
     * @param typeName
     */
    public void sqlFrom(StringBuffer sql, String typeName) {
        sql.append(" FROM ");
        sql.append(encodeTableName(typeName));
    }

    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * 
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     *
     * @param sql DOCUMENT ME!
     * @param preFilter DOCUMENT ME!
     *
     * @throws SQLEncoderException DOCUMENT ME!
     */
    public void sqlWhere(StringBuffer sql, Filter preFilter)
        throws SQLEncoderException {
        if ((preFilter != null) || (preFilter == org.geotools.filter.Filter.NONE)) {
            String where = encoder.encode(preFilter);
            sql.append(" ");
            sql.append(where);
        }
    }

    /**
     * Returns true if the WKB format is used to transfer geometries, false
     * otherwise
     *
     */
    public boolean isWKBEnabled() {
        return WKBEnabled;
    }

    /**
     * If turned on, WKB will be used to transfer geometry data instead of  WKT
     *
     * @param enabled
     */
    public void setWKBEnabled(boolean enabled) {
        WKBEnabled = enabled;
    }
    
    /**
     * Enables the use of the bytea function to transfer faster WKB geometries
     */
    public boolean isByteaEnabled() {
        return byteaEnabled;
    }
    /**
     * Enables/disables the use of the bytea function
     * @param byteaEnable
     */
    public void setByteaEnabled(boolean byteaEnable) {
        byteaEnabled = byteaEnable;
    }
    /**
     * Enables/disables schema name qualification.
     */
    public void setSchemaEnabled(boolean schemaEnabled) {
		this.schemaEnabled = schemaEnabled;
	}
    /**
     * @return true if table names are prefixed with the containing schema.
     */
    public boolean isSchemaEnabled() {
		return schemaEnabled;
	}
    
    public String encodeTableName(String tableName) {
    	return schemaEnabled ? 
			"\"" + config.getDatabaseSchemaName() + "\".\"" + tableName + "\"" : 
			"\"" + tableName + "\""; 
    }
    
    public String encodeColumnName(String columnName) {
    	return "\"" + columnName + "\""; 
    }
}
