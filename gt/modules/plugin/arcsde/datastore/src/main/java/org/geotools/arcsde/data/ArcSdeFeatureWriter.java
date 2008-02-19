package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.jdbc.MutableFIDFeature;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;
import com.esri.sde.sdk.client.SeTable.SeTableIdRange;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

abstract class ArcSdeFeatureWriter implements FeatureWriter {

    protected static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");
    /**
     * Fid prefix used for just created and not yet committed features
     */
    private static final String NEW_FID_PREFIX = "@NEW_";
    /**
     * Complete feature type this writer acts upon
     */
    protected final SimpleFeatureType featureType;
    /**
     * Connection to hold while this feature writer is alive.
     */
    protected ArcSDEPooledConnection connection;
    /**
     * Reader for streamed access to filtered content this writer acts upon.
     */
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> filteredContent;
    /**
     * Builder for new Features this writer creates when next() is called and
     * hasNext() == false
     */
    protected final SimpleFeatureBuilder featureBuilder;
    /**
     * Map of {row index/mutable column names} in the SeTable structure. Not to
     * be accessed directly, but through
     * {@link #getMutableColumnNames(ArcSDEPooledConnection)}
     */
    private LinkedHashMap<Integer, String> mutableColumnNames;
    /**
     * Not to be accessed directly, but through {@link #getLayer()}
     */
    private SeLayer cachedLayer;
    /**
     * Not to be accessed directly, but through {@link #getTable()}
     */
    private SeTable cachedTable;
    /**
     * The feature at the current index. No need to maintain any sort of
     * collection of features as this writer works a feature at a time.
     */
    protected SimpleFeature feature;
    /**
     * Provides row_id column index
     */
    protected final FIDReader fidReader;

    protected final FeatureListenerManager listenerManager;

    public ArcSdeFeatureWriter(final FIDReader fidReader, final SimpleFeatureType featureType,
            final  FeatureReader<SimpleFeatureType, SimpleFeature> filteredContent, final ArcSDEPooledConnection connection,
            final FeatureListenerManager listenerManager) {
        assert fidReader != null;
        assert featureType != null;
        assert filteredContent != null;
        assert connection != null;

        this.fidReader = fidReader;
        this.featureType = featureType;
        this.filteredContent = filteredContent;
        this.connection = connection;
        this.listenerManager = listenerManager;
        this.featureBuilder = new SimpleFeatureBuilder(featureType);
    }

    /**
     * @see FeatureWriter#close()
     */
    public void close() throws IOException {

        if (filteredContent != null) {
            filteredContent.close();
            filteredContent = null;
        }

        // let repeatedly calling close() be inoffensive
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * @see FeatureWriter#getFeatureType()
     */
    public final SimpleFeatureType getFeatureType() {
        return featureType;
    }

    /**
     * @see FeatureWriter#hasNext()
     */
    public final boolean hasNext() throws IOException {
        // filteredContent may be null because we
        // took the precaution of closing it in a previous call
        // to this method
        final boolean hasNext = filteredContent != null && filteredContent.hasNext();
        // be cautious of badly coded clients
        if (!hasNext && filteredContent != null) {
            filteredContent.close();
            filteredContent = null;
        }
        return hasNext;
    }

    /**
     * @see FeatureWriter#next()
     */
    public final SimpleFeature next() throws IOException {
        if (hasNext()) {
            feature = filteredContent.next();
        } else {
            final String newFid = newFid();
            final SimpleFeature newFeature = featureBuilder.buildFeature(newFid);
            final List properties = (List) newFeature.getProperties();
            feature = new MutableFIDFeature(properties, featureType, newFid);
        }
        return feature;
    }

    /**
     * @see FeatureWriter#remove()
     */
    public void remove() throws IOException {
        if (isNewlyCreated(feature)) {
            // we're in auto commit, no need to remove anything
            return;
        }
        // deletes are executed immediately. We set up a transaction
        // if in autocommit mode to be committed or rolled back on this same
        // method if something happens bellow.
        final boolean handleTransaction = !connection.isTransactionActive();
        if (handleTransaction) {
            try {
                connection.startTransaction();
            } catch (SeException e) {
                throw new ArcSdeException("Can't initiate delete transaction", e);
            }
        }

        final String id = feature.getID();
        final long featureId = ArcSDEAdapter.getNumericFid(id);
        final SeObjectId objectID = new SeObjectId(featureId);
        final String qualifiedName = featureType.getTypeName();
        SeDelete seDelete = null;
        try {
            seDelete = new SeDelete(connection);
            // A call to SeDelete.byId immediately deletes the row from the
            // database. The application does not need to call execute()
            seDelete.byId(qualifiedName, objectID);
            if (handleTransaction) {
                connection.commitTransaction();
            }
            fireRemoved(feature);
        } catch (SeException e) {
            if (handleTransaction) {
                try {
                    connection.rollbackTransaction();
                } catch (SeException e1) {
                    LOGGER.log(Level.SEVERE, "Unrecoverable error rolling back delete transaction",
                            new ArcSdeException("Unable to rollback", e));
                }
            }
            throw new ArcSdeException("Error deleting feature id:" + featureId, e);
        } finally {
            if (seDelete != null) {
                try {
                    seDelete.close();
                } catch (SeException e) {
                    LOGGER.log(Level.SEVERE, "Unrecoverable error rolling back delete transaction",
                            e);
                }
            }
        }
    }

    private void fireAdded(final SimpleFeature feature) {
        final String typeName = featureType.getTypeName();
        final BoundingBox bounds = feature.getBounds();
        final ReferencedEnvelope referencedEnvelope;
        if (bounds instanceof ReferencedEnvelope) {
            referencedEnvelope = (ReferencedEnvelope) bounds;
        } else {
            referencedEnvelope = new ReferencedEnvelope(bounds);
        }
        doFireFeaturesAdded(typeName, referencedEnvelope);
    }

    private void fireChanged(final SimpleFeature feature) {
        final String typeName = featureType.getTypeName();
        final BoundingBox bounds = feature.getBounds();
        final ReferencedEnvelope referencedEnvelope;
        if (bounds instanceof ReferencedEnvelope) {
            referencedEnvelope = (ReferencedEnvelope) bounds;
        } else {
            referencedEnvelope = new ReferencedEnvelope(bounds);
        }
        doFireFeaturesChanged(typeName, referencedEnvelope);
    }

    private void fireRemoved(final SimpleFeature feature) {
        final String typeName = featureType.getTypeName();
        final BoundingBox bounds = feature.getBounds();
        final ReferencedEnvelope referencedEnvelope;
        if (bounds instanceof ReferencedEnvelope) {
            referencedEnvelope = (ReferencedEnvelope) bounds;
        } else {
            referencedEnvelope = new ReferencedEnvelope(bounds);
        }
        doFireFeaturesRemoved(typeName, referencedEnvelope);
    }

    protected abstract void doFireFeaturesAdded(String typeName, ReferencedEnvelope bounds);

    protected abstract void doFireFeaturesChanged(String typeName, ReferencedEnvelope bounds);

    protected abstract void doFireFeaturesRemoved(String typeName, ReferencedEnvelope bounds);

    /**
     * @see FeatureWriter#write()
     */
    public void write() throws IOException {
        // final ArcSDEPooledConnection connection = getConnection();
        if (isNewlyCreated(feature)) {
            try {
                Number newId = insertSeRow(feature, getLayer(), connection);
                MutableFIDFeature mutableFidFeature = (MutableFIDFeature) feature;
                String id = featureType.getTypeName() + "." + newId.longValue();
                mutableFidFeature.setID(id);
                fireAdded(mutableFidFeature);
            } catch (SeException e) {
                ArcSdeException sdeEx = new ArcSdeException(e);
                LOGGER.log(Level.WARNING, "Error inserting " + feature + ": " + sdeEx.getMessage(),
                        e);
                throw sdeEx;
            }
        } else {
            try {
                updateRow(feature, getLayer(), connection);
                fireChanged(feature);
            } catch (SeException e) {
                ArcSdeException sdeEx = new ArcSdeException(e);
                LOGGER.log(Level.WARNING, "Error updating " + feature + ": " + sdeEx.getMessage(),
                        e);
                throw sdeEx;
            }
        }
    }

    /**
     * Inserts a feature into an SeLayer.
     * 
     * @param feature
     *            the newly create Feature to insert.
     * @param layer
     *            the layer where to insert the feature.
     * @param connection
     *            the connection to use for the insert operation. Its auto
     *            commit mode determines whether the operation takes effect
     *            immediately or not.
     * @throws SeException
     *             if thrown by any sde stream method
     * @throws IOException
     */
    private void updateRow(final SimpleFeature feature, final SeLayer layer,
            final ArcSDEPooledConnection connection) throws SeException, NoSuchElementException,
            IOException {
        final SeUpdate updateStream = new SeUpdate(connection);
        // updateStream.setWriteMode(true);

        final SeCoordinateReference seCoordRef = layer.getCoordRef();

        final SeRow row;
        final LinkedHashMap<Integer, String> mutableColumns = getMutableColumnNames(connection);
        {
            String[] rowColumnNames = new ArrayList<String>(mutableColumns.values())
                    .toArray(new String[0]);
            SimpleFeatureType featureType = feature.getFeatureType();
            String typeName = featureType.getTypeName();

            final String fid = feature.getID();
            final long numericFid = ArcSDEAdapter.getNumericFid(fid);
            final SeObjectId seObjectId = new SeObjectId(numericFid);
            row = updateStream.singleRow(seObjectId, typeName, rowColumnNames);
        }

        setRowProperties(feature, seCoordRef, mutableColumns, row);
        updateStream.execute();
        // updateStream.flushBufferedWrites();
        updateStream.close();
    }

    /**
     * Inserts a feature into an SeLayer.
     * 
     * @param feature
     *            the newly create Feature to insert.
     * @param layer
     *            the layer where to insert the feature.
     * @param connection
     *            the connection to use for the insert operation. Its auto
     *            commit mode determines whether the operation takes effect
     *            immediately or not.
     * @throws SeException
     *             if thrown by any sde stream method
     * @throws IOException
     */
    private Number insertSeRow(final SimpleFeature feature, final SeLayer layer,
            final ArcSDEPooledConnection connection) throws SeException, IOException {

        final SeCoordinateReference seCoordRef = layer.getCoordRef();

        Number newId = null;

        // this returns only the mutable attributes
        LinkedHashMap<Integer, String> mutableColumns = getMutableColumnNames(connection);
        if (fidReader instanceof FIDReader.UserManagedFidReader) {
            newId = getNextAvailableUserManagedId();
            // set the userId value on the feature so its grabbed from it at
            // setRowProperties
            feature.setAttribute(fidReader.getFidColumn(), newId);
        }
        SeInsert insertStream = new SeInsert(connection);
        {
            String[] rowColumnNames = new ArrayList<String>(mutableColumns.values())
                    .toArray(new String[0]);

            String typeName = featureType.getTypeName();
            insertStream.intoTable(typeName, rowColumnNames);
            insertStream.setWriteMode(true);
        }

        final SeRow row = insertStream.getRowToSet();

        setRowProperties(feature, seCoordRef, mutableColumns, row);

        insertStream.execute();

        if (fidReader instanceof FIDReader.SdeManagedFidReader) {
            SeObjectId newRowId = insertStream.lastInsertedRowId();
            newId = Long.valueOf(newRowId.longValue());
        }

        insertStream.flushBufferedWrites();
        insertStream.close();

        // TODO: handle SHAPE fid strategy (actually such a table shouldn't be
        // editable)
        return newId;
    }

    /**
     * Sets the SeRow property values by index, taking the index from the
     * mutableColumns keys and the values from <code>feature</code>, using
     * the mutableColumns values to get the feature properties by name.
     * 
     * @param feature
     *            the Feature where to get the property values from
     * @param seCoordRef
     * @param mutableColumns
     * @param row
     * @throws SeException
     * @throws IOException
     */
    private void setRowProperties(final SimpleFeature feature,
            final SeCoordinateReference seCoordRef, Map<Integer, String> mutableColumns,
            final SeRow row) throws SeException, IOException {

        // Now set the values for the new row here...
        int seRowIndex;
        String attName;
        Object value;
        for (Map.Entry<Integer, String> entry : mutableColumns.entrySet()) {
            seRowIndex = entry.getKey().intValue();
            attName = entry.getValue();
            value = feature.getAttribute(attName);
            setRowValue(row, seRowIndex, value, seCoordRef, attName, feature.getType()
                    .getTypeName(), feature.getID());
        }
    }

    /**
     * Called when the layer row id is user managed to ask ArcSDE for the next
     * available ID.
     * 
     * @return
     * @throws IOException
     * @throws SeException
     */
    private Number getNextAvailableUserManagedId() throws IOException, SeException {

        // TODO: refactor, this is expensive to do for each row to insert
        // TODO: refactor to some sort of strategy object like done for
        // FIDReader
        final SeTable table = getTable();
        // ArcSDE JavaDoc only says: "Returns a range of row id values"
        // http://edndoc.esri.com/arcsde/9.1/java_api/docs/com/esri/sde/sdk/client/setable.html#getIds(int)
        // I've checked empirically it is to return a range of available ids
        final SeTableIdRange ids = table.getIds(1);
        final SeObjectId startId = ids.getStartId();
        final long id = startId.longValue();
        final Long newId = Long.valueOf(id);

        final AttributeDescriptor rowIdAtt = featureType.getAttribute(fidReader.getFidColumn());
        final Class binding = rowIdAtt.getType().getBinding();
        final Number userFidValue;
        if (Long.class == binding) {
            userFidValue = newId;
        } else if (Integer.class == binding) {
            userFidValue = Integer.valueOf(newId.intValue());
        } else if (Double.class == binding) {
            userFidValue = new Double(newId.doubleValue());
        } else if (Float.class == binding) {
            userFidValue = new Float(newId.floatValue());
        } else {
            throw new IllegalArgumentException("Can't handle a user managed row id of type "
                    + binding);
        }

        return userFidValue;
    }

    /**
     * Used to set a value on an SeRow object. The values is converted to the
     * appropriate type based on an inspection of the SeColumnDefintion object.
     * 
     * @param row
     * @param index
     * @param value
     * @param coordRef
     * 
     * @throws SeException
     *             DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     */
    private void setRowValue(final SeRow row, final int index, Object value,
            final SeCoordinateReference coordRef, final String attName, final String typeName,
            final String fid) throws SeException, IOException {

        final SeColumnDefinition seColumnDefinition = row.getColumnDef(index);

        final int colType = seColumnDefinition.getType();
        if (colType == SeColumnDefinition.TYPE_INT16) {
            value = Converters.convert(value, Short.class);
            row.setShort(index, (Short) value);
        } else if (colType == SeColumnDefinition.TYPE_INT32) {
            value = Converters.convert(value, Integer.class);
            row.setInteger(index, (Integer) value);
        } else if (colType == SeColumnDefinition.TYPE_INT64) {
            value = Converters.convert(value, Long.class);
            row.setLong(index, (Long) value);
        } else if (colType == SeColumnDefinition.TYPE_FLOAT32) {
            value = Converters.convert(value, Float.class);
            row.setFloat(index, (Float) value);
        } else if (colType == SeColumnDefinition.TYPE_FLOAT64) {
            value = Converters.convert(value, Double.class);
            row.setDouble(index, (Double) value);
        } else if (colType == SeColumnDefinition.TYPE_STRING
                || colType == SeColumnDefinition.TYPE_NSTRING
                || colType == SeColumnDefinition.TYPE_CLOB
                || colType == SeColumnDefinition.TYPE_NCLOB) {
            value = Converters.convert(value, String.class);
            row.setString(index, (String) value);
        } else if (colType == SeColumnDefinition.TYPE_DATE) {
            // REVISIT: is converters already ready for date->calendar?
            if (value != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) value);
                row.setTime(index, calendar);
            } else {
                row.setTime(index, null);
            }
        } else if (colType == SeColumnDefinition.TYPE_SHAPE) {
            if (value != null) {
                final Geometry geom = (Geometry) value;
                IsValidOp validator = new IsValidOp(geom);
                if (!validator.isValid()) {
                    TopologyValidationError validationError = validator.getValidationError();
                    String validationErrorMessage = validationError.getMessage();
                    Coordinate coordinate = validationError.getCoordinate();
                    String errorMessage = "Topology validation error at or near point "
                            + coordinate + ": " + validationErrorMessage;
                    throw new DataSourceException("Invalid geometry passed to " + typeName + "."
                            + attName + "\n Geomerty: " + geom + "\n" + errorMessage);
                }
                ArcSDEGeometryBuilder geometryBuilder;
                geometryBuilder = ArcSDEGeometryBuilder.builderFor(geom.getClass());
                SeShape shape = geometryBuilder.constructShape(geom, coordRef);
                row.setShape(index, shape);
            } else {
                row.setShape(index, null);
            }
        }
    }

    /**
     * Returns the row index and column names for all the mutable properties in
     * the sde layer. That is, those properties whose type is not
     * {@link SeRegistration#SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE}, which are
     * used as row id columns managed by arcsde.
     * 
     * @param connection
     * @return a map keyed by mutable column name and valued by the index of the
     *         mutable column name in the SeTable structure
     * @throws IOException
     * @throws NoSuchElementException
     * @throws SeException
     */
    private LinkedHashMap<Integer, String> getMutableColumnNames(ArcSDEPooledConnection connection)
            throws NoSuchElementException, IOException, SeException {
        if (mutableColumnNames == null) {
            // We are going to inspect the column defintions in order to
            // determine which attributes are actually mutable...
            final SeColumnDefinition[] columnDefinitions = getTable().describe();
            final String shapeAttributeName;
            shapeAttributeName = getLayer().getShapeAttributeName(SeLayer.SE_SHAPE_ATTRIBUTE_FID);

            // use LinkedHashMap to respect column order
            LinkedHashMap<Integer, String> columnList = new LinkedHashMap<Integer, String>();

            SeColumnDefinition columnDefinition;
            String columnName;
            int usedIndex = 0;
            for (int actualIndex = 0; actualIndex < columnDefinitions.length; actualIndex++) {
                columnDefinition = columnDefinitions[actualIndex];
                columnName = columnDefinition.getName();
                // this is an attribute added to the featuretype
                // solely to support FIDs. It isn't an actual attribute
                // on the underlying SDE table, and as such it can't
                // be written to. Skip it!
                if (columnName.equals(shapeAttributeName)) {
                    continue;
                }

                // We need to exclude read only types from the set of "mutable"
                // column names.
                final short rowIdType = columnDefinition.getRowIdType();
                if (SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE != rowIdType) {
                    columnList.put(Integer.valueOf(usedIndex), columnName);
                    // only increment usedIndex if we added a mutable column to
                    // the list
                    usedIndex++;
                }
            }
            this.mutableColumnNames = columnList;
        }

        return this.mutableColumnNames;
    }

    private SeTable getTable() throws DataSourceException {
        if (this.cachedTable == null) {
            // final ArcSDEPooledConnection connection = getConnection();
            final String typeName = this.featureType.getTypeName();
            final SeTable table = connection.getTable(typeName);
            this.cachedTable = table;
        }
        return this.cachedTable;
    }

    private SeLayer getLayer() throws DataSourceException {
        if (this.cachedLayer == null) {
            // final ArcSDEPooledConnection connection = getConnection();
            final String typeName = this.featureType.getTypeName();
            final SeLayer layer = connection.getLayer(typeName);
            this.cachedLayer = layer;
        }
        return this.cachedLayer;
    }

    /**
     * Creates a feature id for a new feature; the feature id is compound of the
     * {@value #NEW_FID_PREFIX} plus a UUID.
     * 
     * @return
     */
    private String newFid() {
        return NEW_FID_PREFIX + UUID.randomUUID();
    }

    /**
     * Checks if <code>feature</code> has been created by this writer
     * 
     * @param feature
     * @return
     */
    private final boolean isNewlyCreated(SimpleFeature feature) {
        final String id = feature.getID();
        return id.startsWith(NEW_FID_PREFIX);
    }
}