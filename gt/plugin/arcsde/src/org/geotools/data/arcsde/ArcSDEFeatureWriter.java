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
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
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
 * @version
 */
class ArcSDEFeatureWriter implements FeatureWriter {
    private static final Logger LOGGER = Logger.getLogger(ArcSDEFeatureWriter.class.getPackage()
                                                                                   .getName());
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
    // that are mutable.  We consult the state of these
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
     * @param state DOCUMENT ME!
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

        // We essentially use this as our primary key column.  This seems to 
        // work ok with ArcSDE for the most part...
        spatialColumnName = layer.getSpatialColumn();
        currentIndex = -1;
    }

    /**
     * Creates a new ArcSDEFeatureWriter object.
     *
     * @param store DOCUMENT ME!
     * @param state DOCUMENT ME!
     * @param layer DOCUMENT ME!
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
     * @throws RuntimeException DOCUMENT ME!
     */
    public FeatureType getFeatureType() {
        try {
            return ArcSDEAdapter.createSchema(dataStore.getConnectionPool(),
                layer.getQualifiedName());
        } catch (SeException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Implements an operation similar to next in java.util.Iterator.  This
     * allows the  caller to iterate over features obtained from the backing
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
            // It is not clear that this cast will always be safe, but it seems to 
            // be a safe approach for the default implementation provided.
            DefaultFeatureType featureType = (DefaultFeatureType) getFeatureType();
            Object[] attributes = new Object[featureType.getAttributeCount()];

            try {
                feature = featureType.create(attributes);
            } catch (IllegalAttributeException iae) {
                LOGGER.log(Level.WARNING, iae.getMessage(), iae);
                throw new IOException(iae.getMessage());
            }

            features.add(feature);
            currentIndex++;
            notInserted = true;
        } else {
            // Simply return the next feature in the list...
            feature = (Feature) features.get(++currentIndex);
        }

        return feature;
    }

    /**
     * Removes the current feature from the backing store.
     *
     * @throws IOException If there are no features or if the backing store
     *         throws an exception.
     */
    public synchronized void remove() throws IOException {
        if ((features == null) || (currentIndex >= features.size())) {
            throw new IOException("No current feature available.");
        }

        if (notInserted) {
            features.remove(currentIndex--);
            notInserted = false;
        } else {
            Feature feature = (Feature) features.get(currentIndex);
            SeConnection connection = null;

            try {
                connection = getConnection();

                SeDelete seDelete = new SeDelete(connection);

                // PENDING JMF: I'm not sure we really know just how we are going to know the feature
                // id at this point.  I expect it is possible that it may have alternative names.  Can
                // we work with it that way, or are we only going to be able to modify data in layers
                // we have actually created?
                String featureId = feature.getID().substring(feature.getID()
                                                                    .lastIndexOf('.')
                        + 1, feature.getID().length());
                seDelete.fromTable(layer.getQualifiedName(),
                    spatialColumnName + " = " + featureId);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new IOException(e.getMessage());
            } finally {
                releaseConnection(connection);
            }
        }
    }

    /**
     * Writes the feature at the current index to the backing store.  If this
     * feature is not yet in the backing store it will be inserted.
     *
     * @throws IOException In the case that it is not possible to write the
     *         feature to the backing store because it either does not exist
     *         or the  backing store throws its own exception that indicates
     *         an error.
     */
    public synchronized void write() throws IOException {
        if ((features == null) || (features.size() == 0)) {
            throw new IOException("No feature to be written.");
        }

        SeConnection connection = null;

        try {
            Feature feature = (Feature) features.get(currentIndex);
            FeatureType featureType = feature.getFeatureType();
            AttributeType[] attributeTypes = featureType.getAttributeTypes();
            connection = getConnection();

            if (notInserted) {
                // We must insert the record into ArcSDE
                SeInsert insert = new SeInsert(connection);
                String[] cols = getColumns(attributeTypes, connection);
                insert.intoTable(layer.getQualifiedName(), cols);
                insert.setWriteMode(true);

                SeRow row = insert.getRowToSet();

                // Now set the values for the new row here...
                for (int i = 0; i < cols.length; i++) {
                    setRowValue(row, i,
                        feature.getAttribute(
                            mutableAttributeIndexes[i].intValue()));
                }

                // Now "commit" the changes.
                insert.execute();
                insert.close();
            } else {
                // The record is already inserted, so we will be updating
                // the values associated with the given record.
                SeUpdate update = new SeUpdate(connection);
                String[] cols = getColumns(attributeTypes, connection);
                String featureId = feature.getID().substring(feature.getID()
                                                                    .lastIndexOf('.')
                        + 1, feature.getID().length());
                update.toTable(layer.getQualifiedName(), cols,
                    spatialColumnName + " = " + featureId);
                update.setWriteMode(true);

                SeRow row = update.getRowToSet();

                // Set values on rows here.....
                for (int i = 0; i < cols.length; i++) {
                    Object value = feature.getAttribute(mutableAttributeIndexes[i]
                            .intValue());
                    setRowValue(row, i, value);
                }

                update.execute();
                update.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IOException(e.getMessage());
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
        int size = features.size();

        return ((features != null) && (size > 0) && ((currentIndex + 1) < size));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void close() throws IOException {
        dataStore = null;
        layer = null;
        features = null;
        currentIndex = 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param attributeTypes DOCUMENT ME!
     * @param connection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    private synchronized String[] getColumns(AttributeType[] attributeTypes,
        SeConnection connection) throws SeException {
        if (columnDefinitions == null) {
            SeTable table = new SeTable(connection, layer.getQualifiedName());

            // We are going to inspect the column defintions in order to 
            // determine which attributes are actually mutable...
            columnDefinitions = table.describe();

            ArrayList columnList = new ArrayList();
            ArrayList indexes = new ArrayList();

            for (int i = 0; i < attributeTypes.length; i++) {
                // We need to exclude read only types from the set of "mutable" 
                // column names.  See the ArcSDE documentation for the explanation
                // of "1", if they provided a symbolic constant I would use it...
                // As it is, I think this is easier to understand along with their
                // documentation.  1 indicates an ArcSDE managed field.
                if (columnDefinitions[i].getRowIdType() != 1) {
                    columnList.add(attributeTypes[i].getName().toUpperCase());
                    indexes.add(new Integer(i));
                }
            }

            columns = new String[columnList.size()];
            mutableAttributeIndexes = new Integer[indexes.size()];
            columnList.toArray(columns);
            indexes.toArray(mutableAttributeIndexes);
        }

        return columns;
    }

    /**
     * Used to set a value on an SeRow object.  The values is converted to the
     * appropriate type based on an inspection of the SeColumnDefintion
     * object.
     *
     * @param row
     * @param index
     * @param value
     *
     * @throws SeException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
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
                    SeShape shape = geometryBuilder.constructShape((Geometry) value,
                            layer.getCoordRef());
                    row.setShape(index, shape);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                    throw new IOException(e.getMessage());
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
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    private synchronized SeConnection getConnection()
        throws DataSourceException, UnavailableConnectionException {
        if (transactionState != null) {
            return transactionState.getConnection();
        } else {
            return dataStore.getConnectionPool().getConnection();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param connection
     */
    private synchronized void releaseConnection(SeConnection connection) {
        if (transactionState != null) {
            // NO-OP, the transactionState object will release the connection
            // after it commits or rollsback the operations.
        } else {
            dataStore.getConnectionPool().release(connection);
        }
    }
}
