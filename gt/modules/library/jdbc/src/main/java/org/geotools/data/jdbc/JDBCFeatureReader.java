/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 * 
 *    Created on 7-apr-2004
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.SQLException;

import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Geometry;


/**
 * JDBCDataStore specific implementation of the FeatureReader interface
 *
 * @author aaime
 * @source $URL$
 */
public class JDBCFeatureReader implements FeatureReader {
    FeatureType featureType;
    QueryData queryData;
    Object[] attributes;
    Object[] fidAttributes;

    /**
     * Creates a new JDBCFeatureReader object.
     *
     * @param queryData 
     *
     * @throws IOException 
     */
    public JDBCFeatureReader(QueryData queryData) throws IOException {
        this.queryData = queryData;
        attributes = new Object[queryData.getAttributeHandlers().length];
        fidAttributes = new Object[queryData.getMapper().getColumnCount()];
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        close(null);
    }

    void close(SQLException sqlException) {
        queryData.close(sqlException);
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (queryData.isClosed()) {
            throw new IOException("Reader is closed");
        }

        return queryData.hasNext();
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next() throws IllegalAttributeException, IOException {
        if (queryData.isClosed()) {
            throw new IOException("The feature reader has been closed");
        }

        return readFeature();
    }

    /**
     * Really reads the feature from the QueryData object
     * @throws IllegalAttributeException
     * @throws IOException
     */
    private Feature readFeature() throws IllegalAttributeException, IOException {
        queryData.next();
        
        for (int i = 0; i < fidAttributes.length; i++) {
            fidAttributes[i] = queryData.readFidColumn(i);
        }

        FIDMapper mapper = queryData.getMapper();
        String fid = mapper.getID(fidAttributes);

        for (int i = 0; i < attributes.length; i++) {
        	Object attribute = queryData.read( i );
        	
        	//JD: check for a coordinate system, if present on the type, set on the geometry
        	// I know this is pretty loose, but its better then nothing
        	if ( attribute instanceof Geometry && 
        			queryData.getFeatureType().getAttributeType( i ) instanceof GeometryAttributeType ) {
        		Geometry geometry = (Geometry) attribute;
        		GeometryAttributeType geometryType = 
        			(GeometryAttributeType) queryData.getFeatureType().getAttributeType( i );
        		
        		if ( geometryType.getCoordinateSystem() != null ) {
        			geometry.setUserData( geometryType.getCoordinateSystem() );
        		}
        	}
        	
            attributes[i] = attribute;
        }

        return queryData.getFeatureType().create(attributes, fid);
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return queryData.getFeatureType();
    }
}
