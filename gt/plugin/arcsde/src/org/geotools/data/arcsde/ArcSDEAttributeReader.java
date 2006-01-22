/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRow;
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

	// private SeRow currentRow;
	/** the sde java api shape of the current row */

	// private SeShape currentShape;
	private Object[] currentValues;

	/**
	 * the unique id of the current feature. -1 means the feature id was not
	 * retrieved
	 */
	private long currentFid = -1;

	/**
	 * the builder for the geometry type of the schema's default geometry, or
	 * null if the geometry attribute is not included in the schema
	 */
	private GeometryBuilder geometryBuilder;

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
		this.schema = query.getSchema();
		this.currentValues = new Object[this.schema.getAttributeCount()];

		this.fidPrefix = new StringBuffer(this.schema.getTypeName())
				.append(".");
		this.fidPrefixLen = this.fidPrefix.length();

		final GeometryAttributeType geomType = this.schema.getDefaultGeometry();

		if (geomType != null) {
			Class geometryClass = geomType.getType();
			this.geometryBuilder = GeometryBuilder.builderFor(geometryClass);
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
				SeRow currentRow = this.query.fetch();
				this.hasNextAlreadyCalled = true;

				// ensure closing the query to release the connection, may be
				// the
				// user is not so smart to doing it itself
				if (currentRow == null) {
					this.query.close();
					this.currentValues = null;
				} else {
					// number of attributes as defined in the queried
					// featuretype
					int attCount = this.schema.getAttributeCount();

					// actual number of columns returned. Can be 1 more then
					// attCount. In this case, it means the default geometry
					// was not included in the query, and the SeShape object
					// is returned as the last column since it is the only way
					// to fetch the feature id.
					int columns = currentRow.getColumns().length;
					Object value;

					for (int i = 0; i < columns; i++) {
						value = currentRow.getObject(i);

						if (value instanceof SeShape) {
							SeShape shape = (SeShape) value;

							// grab the feature id
							this.currentFid = shape.getFeatureId().longValue();

							// see if the geometry was part of the query
							if (columns == attCount) {
								value = this.geometryBuilder.construct(shape);
								this.currentValues[i] = value;
							}
						} else {
							this.currentValues[i] = value;
						}
					}
				}
			} catch (SeException ex) {
				this.query.close();
				LOGGER.log(Level.SEVERE, ex.getSeError().getErrDesc(), ex);
				throw new DataSourceException("Fetching row:"
						+ ex.getSeError().getErrDesc(), ex);
			}
		}

		return this.currentValues != null;
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
		if (this.currentValues == null) {
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
		return this.currentValues[index];
	}

	public Object[] readAll() {
		return this.currentValues;
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
