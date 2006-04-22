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
 */
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureWriter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Implementation fo the FeatureWriter interface for use with the
 * ArcSDEDataStore class.
 * 
 * @author Jake Fear, jfear@polexis.com
 * @source $URL$
 * @version
 */
class ArcSDEFeatureWriter implements FeatureWriter {
	private static final Logger LOGGER = Logger
			.getLogger(ArcSDEFeatureWriter.class.getPackage().getName());

	private ArcSDEDataStore dataStore;

	private ArcTransactionState transactionState;

	private SeLayer layer;

	private SeColumnDefinition[] columnDefinitions;

	private List features;

	// Pointer into the current List of features
	private int currentIndex;

	// Indicates that the current has not yet been added to the database
	// when the values is true.
	private boolean notInserted;

	// Because not all attributes are mutable we use
	// these two variables to indicate the column name
	// and FeatureType attribute index of the values
	// that are mutable. We consult the state of these
	// arrays before we attempt to insert a row of data.
	private String[] columns;

	// Used to create "pointers" to attributes that are mutable.
	private Integer[] mutableAttributeIndexes;

	/**
	 * Holds the name of the spatially enabled column in an ArcSDE SeLayer
	 * object that is represented by this writer.
	 */
	private String spatialColumnName;

	/**
	 * Creates a new ArcSDEFeatureWriter.
	 * 
	 * @param store
	 * @param state
	 *            DOCUMENT ME!
	 * @param layer
	 * @param features
	 */
	public ArcSDEFeatureWriter(ArcSDEDataStore store,
			ArcTransactionState state, SeLayer layer, List features) {
		transactionState = state;

		if (features != null) {
			this.features = features;
		} else {
			this.features = new ArrayList();
		}

		this.dataStore = store;
		this.layer = layer;

		// We essentially use this as our primary key column. This seems to
		// work ok with ArcSDE for the most part...
		this.spatialColumnName = layer.getSpatialColumn();
		this.currentIndex = -1;
	}

	/**
	 * Creates a new ArcSDEFeatureWriter object.
	 * 
	 * @param store
	 *            DOCUMENT ME!
	 * @param state
	 *            DOCUMENT ME!
	 * @param layer
	 *            DOCUMENT ME!
	 */
	public ArcSDEFeatureWriter(ArcSDEDataStore store,
			ArcTransactionState state, SeLayer layer) {
		this(store, state, layer, null);
	}

	/**
	 * Provides the <code>FeatureType</code> that is acceptable for features
	 * handled by this <code>FeatureWriter</code>
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws RuntimeException
	 *             DOCUMENT ME!
	 */
	public FeatureType getFeatureType() {
		try {
			return ArcSDEAdapter.fetchSchema(
					this.dataStore.getConnectionPool(), this.layer
							.getQualifiedName(), this.dataStore.getNamespace());
		} catch (SeException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Implements an operation similar to next in java.util.Iterator. This
	 * allows the caller to iterate over features obtained from the backing
	 * store and to instantiate new features and add them to the layer in the
	 * backing store.
	 * 
	 * @return If no 'next feature' is available then a new feature object is
	 *         created and returned to the caller so that they may modify its
	 *         contents and later write it to the backing store.
	 * 
	 * @throws IOException
	 */
	public synchronized Feature next() throws IOException {
		Feature feature;

		if (!hasNext()) {
			// In this case we must instantiate a new feature and add it
			// to our internal list, thus allowing it to be modified...
			// It is not clear that this cast will always be safe, but it seems
			// to
			// be a safe approach for the default implementation provided.
			DefaultFeatureType featureType = (DefaultFeatureType) getFeatureType();
			Object[] attributes = new Object[featureType.getAttributeCount()];

			try {
				feature = featureType.create(attributes);
			} catch (IllegalAttributeException iae) {
				LOGGER.log(Level.WARNING, iae.getMessage(), iae);
				throw new IOException(iae.getMessage());
			}

			this.features.add(feature);
			this.currentIndex++;
			this.notInserted = true;
		} else {
			// Simply return the next feature in the list...
			feature = (Feature) this.features.get(++this.currentIndex);
		}

		return feature;
	}

	/**
	 * Removes the current feature from the backing store.
	 * 
	 * @throws IOException
	 *             If there are no features or if the backing store throws an
	 *             exception.
	 */
	public synchronized void remove() throws IOException {
		if ((this.features == null)
				|| (this.currentIndex >= this.features.size())) {
			throw new IOException("No current feature available.");
		}

		if (this.notInserted) {
			this.features.remove(this.currentIndex--);
			this.notInserted = false;
		} else {
			Feature feature = (Feature) this.features.get(this.currentIndex);
			PooledConnection connection = null;

			try {
				connection = getConnection();

				SeDelete seDelete = new SeDelete(connection);

				long featureId = ArcSDEAdapter.getNumericFid(feature.getID());
				SeObjectId objectID = new SeObjectId(featureId);
				seDelete.byId(this.layer.getQualifiedName(), objectID);
				//this.dataStore.fireRemoved(feature);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				throw new IOException(e.getMessage());
			} finally {
				releaseConnection(connection);
			}
		}
	}

	/**
	 * Writes the feature at the current index to the backing store. If this
	 * feature is not yet in the backing store it will be inserted.
	 * 
	 * @throws IOException
	 *             In the case that it is not possible to write the feature to
	 *             the backing store because it either does not exist or the
	 *             backing store throws its own exception that indicates an
	 *             error.
	 */
	public synchronized void write() throws IOException {
		if ((this.features == null) || (this.features.size() == 0)) {
			throw new IOException("No feature to be written.");
		}

		PooledConnection connection = null;

		try {
			Feature feature = (Feature) this.features.get(this.currentIndex);
			FeatureType featureType = feature.getFeatureType();
			AttributeType[] attributeTypes = featureType.getAttributeTypes();
			connection = getConnection();

			if (this.notInserted) {
				// We must insert the record into ArcSDE
				SeInsert insert = new SeInsert(connection);
				String[] cols = getColumns(attributeTypes, connection);
				insert.intoTable(this.layer.getQualifiedName(), cols);
				insert.setWriteMode(true);

				SeRow row = insert.getRowToSet();

				// Now set the values for the new row here...
				for (int i = 0; i < cols.length; i++) {
					setRowValue(row, i, feature
							.getAttribute(this.mutableAttributeIndexes[i]
									.intValue()));
				}

				// Now "commit" the changes.
				insert.execute();
				insert.close();
				//this.dataStore.fireAdded(feature);
			} else {
				// The record is already inserted, so we will be updating
				// the values associated with the given record.
				SeUpdate update = new SeUpdate(connection);
				String[] cols = getColumns(attributeTypes, connection);
				String featureId = feature.getID().substring(
						feature.getID().lastIndexOf('.') + 1,
						feature.getID().length());
				update.toTable(this.layer.getQualifiedName(), cols,
						this.spatialColumnName + " = " + featureId);
				update.setWriteMode(true);

				SeRow row = update.getRowToSet();

				// Set values on rows here.....
				for (int i = 0; i < cols.length; i++) {
					Object value = feature
							.getAttribute(this.mutableAttributeIndexes[i]
									.intValue());
					setRowValue(row, i, value);
				}

				update.execute();
				update.close();

			}
		} catch (Exception e) {
			
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			if (LOGGER.isLoggable(Level.FINE)) {
				e.printStackTrace();
			}
			throw new DataSourceException(e.getMessage(), e);
		} finally {
			releaseConnection(connection);
		}
	}

	/**
	 * Indicates whether or not this <code>FeatureWriter</code> contains more
	 * feature instances or not.
	 * 
	 * @return true if the next call to <code>next()</code> will return an
	 *         already existing feature from the backing store.
	 * 
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException {
		int size = this.features.size();

		return ((this.features != null) && (size > 0) && ((this.currentIndex + 1) < size));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void close() throws IOException {
		this.dataStore = null;
		this.layer = null;
		this.features = null;
		this.currentIndex = 0;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param attributeTypes
	 *            DOCUMENT ME!
	 * @param connection
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws SeException
	 *             DOCUMENT ME!
	 */
	private synchronized String[] getColumns(AttributeType[] attributeTypes,
			SeConnection connection) throws SeException {
		if (this.columnDefinitions == null) {
			SeTable table = new SeTable(connection, this.layer
					.getQualifiedName());

			// We are going to inspect the column defintions in order to
			// determine which attributes are actually mutable...
			this.columnDefinitions = table.describe();

			ArrayList columnList = new ArrayList();
			ArrayList indexes = new ArrayList();

			for (int i = 0; i < attributeTypes.length; i++) {
				// We need to exclude read only types from the set of "mutable"
				// column names. See the ArcSDE documentation for the
				// explanation
				// of "1", if they provided a symbolic constant I would use
				// it...
				// As it is, I think this is easier to understand along with
				// their
				// documentation. 1 indicates an ArcSDE managed field.
				if (this.columnDefinitions[i].getRowIdType() != 1) {
					columnList.add(attributeTypes[i].getName().toUpperCase());
					indexes.add(new Integer(i));
				}
			}

			this.columns = new String[columnList.size()];
			this.mutableAttributeIndexes = new Integer[indexes.size()];
			columnList.toArray(this.columns);
			indexes.toArray(this.mutableAttributeIndexes);
		}

		return this.columns;
	}

	/**
	 * Used to set a value on an SeRow object. The values is converted to the
	 * appropriate type based on an inspection of the SeColumnDefintion object.
	 * 
	 * @param row
	 * @param index
	 * @param value
	 * 
	 * @throws SeException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	private void setRowValue(SeRow row, int index, Object value)
			throws SeException, IOException {
		SeColumnDefinition seColumnDefinition = null;
		seColumnDefinition = row.getColumnDef(index);

		switch (seColumnDefinition.getType()) {
		case SeColumnDefinition.TYPE_INTEGER: {
			if (value != null) {
				row.setInteger(index, new Integer(value.toString()));
			} else {
				row.setInteger(index, null);
			}

			break;
		}

		case SeColumnDefinition.TYPE_SMALLINT: {
			if (value != null) {
				row.setShort(index, new Short(value.toString()));
			} else {
				row.setShort(index, null);
			}

			break;
		}

		case SeColumnDefinition.TYPE_FLOAT: {
			if (value != null) {
				row.setFloat(index, new Float(value.toString()));
			} else {
				row.setFloat(index, null);
			}

			break;
		}

		case SeColumnDefinition.TYPE_DOUBLE: {
			if (value != null) {
				row.setDouble(index, new Double(value.toString()));
			} else {
				row.setDouble(index, null);
			}

			break;
		}

		case SeColumnDefinition.TYPE_STRING: {
			if (value != null) {
				row.setString(index, value.toString());
			} else {
				row.setString(index, null);
			}

			break;
		}

		case SeColumnDefinition.TYPE_DATE: {
			if (value != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime((Date) value);
				row.setTime(index, calendar);
			} else {
				row.setTime(index, null);
			}

			break;
		}

		case SeColumnDefinition.TYPE_SHAPE: {
			if (value != null) {
				try {
					GeometryBuilder geometryBuilder = GeometryBuilder
							.builderFor(value.getClass());
					SeCoordinateReference coordRef = this.layer.getCoordRef();
					Geometry geom = (Geometry) value;
					SeShape shape = geometryBuilder.constructShape(geom,
							coordRef);
					row.setShape(index, shape);
				} catch (Exception e) {
					String msg = e instanceof SeException ? ((SeException) e)
							.getSeError().getErrDesc() : e.getMessage();
					LOGGER.log(Level.WARNING, msg, e);
					throw new DataSourceException(msg, e);
				}
			} else {
				row.setShape(index, null);
			}

			break;
		}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 * @throws UnavailableConnectionException
	 *             DOCUMENT ME!
	 */
	private synchronized PooledConnection getConnection()
			throws DataSourceException, UnavailableConnectionException {
		if (this.transactionState != null) {
			return this.transactionState.getConnection();
		}
		return this.dataStore.getConnectionPool().getConnection();

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param connection
	 */
	private synchronized void releaseConnection(PooledConnection connection) {
		if (this.transactionState != null) {
			// NO-OP, the transactionState object will release the connection
			// after it commits or rollsback the operations.
		} else {
			connection.close();
		}
	}
}
