/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
 */package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.type.GeometricAttributeType;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeShape;

/**
 * Implements an attribute reader that is aware of the particulars of ArcSDE.
 * This class sends its logging to the log named "org.geotools.data".
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
class ArcSDEAttributeReader implements AttributeReader {
	/** Shared package's logger */
	private static final Logger LOGGER = Logger.getLogger("org.geotools.data");

	/** query passed to the constructor */
	private ArcSDEQuery query;

	/** schema of the features this attribute reader iterates over */
	private FeatureType schema;

    /** current sde java api row being read */
    private SdeRow currentRow;

    /**
	 * the unique id of the current feature. -1 means the feature id was not
	 * retrieved
	 */
	private long currentFid = -1;

	/**
	 * the builder for the geometry type of the schema's default geometry, or
	 * null if the geometry attribute is not included in the schema
	 */
	private ArcSDEGeometryBuilder geometryBuilder;

	/**
	 * holds the "&lt;DATABASE_NAME&gt;.&lt;USER_NAME&gt;." string and is used
	 * to efficiently create String FIDs from the SeShape feature id, which is a
	 * long number.
	 */
	private StringBuffer fidPrefix;

	/**
	 * lenght of the prefix string for creating string based feature ids, used
	 * to truncate the <code>fidPrefix</code> and append it the SeShape's
	 * feature id number
	 */
	private int fidPrefixLen;

    /**
     * Strategy to read FIDs
     */
    private FIDReader fidReader;
    
    /**
	 * flag to avoid the processing done in <code>hasNext()</code> if next()
	 * was not called between calls to hasNext()
	 */
	private boolean hasNextAlreadyCalled = false;

	/**
	 * The query that defines this readers interaction with an ArcSDE instance.
	 * 
	 * @param query
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public ArcSDEAttributeReader(ArcSDEQuery query) throws IOException {
        this.query = query;
        this.fidReader = query.getFidReader();
        this.schema = query.getSchema();

        String typeName = schema.getTypeName();
        
        this.fidPrefix = new StringBuffer(typeName).append('.');
        this.fidPrefixLen = this.fidPrefix.length();

        final GeometryAttributeType geomType = schema.getDefaultGeometry();

        if (geomType != null) {
            Class geometryClass = geomType.getType();
            this.geometryBuilder = ArcSDEGeometryBuilder.builderFor(geometryClass);
        }
	}
	
	/**
	 * 
	 */
	public int getAttributeCount() {
		return this.schema.getAttributeCount();
	}

	/**
	 * 
	 */
	public AttributeType getAttributeType(int index)
			throws ArrayIndexOutOfBoundsException {
		return this.schema.getAttributeType(index);
	}

	/**
	 * Closes the associated query object.
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void close() throws IOException {
		this.query.close();
	}

	/**
	 * 
	 */
	public boolean hasNext() throws IOException {
        if (!this.hasNextAlreadyCalled) {
            try {
                currentRow = query.fetch();
                if (currentRow == null) {
                    this.query.close();
                } else {
                    this.currentFid = fidReader.readFid(currentRow);
                }
                hasNextAlreadyCalled = true;
            } catch (IOException dse) {
                this.hasNextAlreadyCalled = true;
                this.query.close();
                LOGGER.log(Level.SEVERE, dse.getLocalizedMessage(), dse);
                throw dse;
            } catch (RuntimeException ex) {
                this.hasNextAlreadyCalled = true;
                this.query.close();
                throw new DataSourceException("Fetching row:" + ex.getMessage(), ex);
            }
        }
        
        return this.currentRow != null;
	}

	/**
	 * Retrieves the next row, or throws a DataSourceException if not more rows
	 * are available.
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 */
	public void next() throws IOException {
        if (this.currentRow == null) {
            throw new DataSourceException("There are no more rows");
        }

        this.hasNextAlreadyCalled = false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             never, since the feature retrieve was done in
	 *             <code>hasNext()</code>
	 * @throws ArrayIndexOutOfBoundsException
	 *             if <code>index</code> is outside the bounds of the schema
	 *             attribute's count
	 */
	public Object read(int index) throws IOException,
			ArrayIndexOutOfBoundsException {
        Object value = currentRow.getObject(index);
        
        if (schema.getAttributeType(index) instanceof GeometricAttributeType) {
            try{
                SeShape shape = (SeShape) value;
                /**
                Class geomClass = ArcSDEAdapter.getGeometryType(shape.getType());
                geometryBuilder = GeometryBuilder.builderFor(geomClass);
                */
                value = geometryBuilder.construct(shape);
            }catch(SeException e){
                throw new DataSourceException(e);
            }
        } 

        return value;
	}

	public Object[] readAll() throws ArrayIndexOutOfBoundsException, IOException {
        int size = schema.getAttributeCount();
        Object []all = new Object[size];
        for(int i = 0; i < size; i++){
            all[i] = read(i);
        }
        return all;
	}

	/**
	 * 
	 */
	public String readFID() throws IOException {
        if (this.currentFid == -1) {
            throw new DataSourceException("The feature id was not fetched");
        }
        this.fidPrefix.setLength(this.fidPrefixLen);
        this.fidPrefix.append(this.currentFid);

        return this.fidPrefix.toString();
	}
}
