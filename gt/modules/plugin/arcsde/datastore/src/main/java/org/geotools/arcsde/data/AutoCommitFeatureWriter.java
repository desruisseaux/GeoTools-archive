package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

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
import com.vividsolutions.jts.geom.Geometry;

/**
 * A FeatureWriter for auto commit mode.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/AutoCommitFeatureWriter.java $
 */
class AutoCommitFeatureWriter implements FeatureWriter {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    /**
     * Fid prefix used for just created and not yet commited features
     */
    private static final String NEW_FID_PREFIX = "@NEW_";

    /**
     * Complete feature type this writer acts upon
     */
    protected SimpleFeatureType featureType;

    /**
     * Connection to hold while this feature writer is alive. Not to be accessed
     * directly, but through {@link #getConnection()}
     */
    private ArcSDEPooledConnection connection;

    /**
     * Reader for streamed access to filtered content this writer acts upon.
     */
    protected FeatureReader filteredContent;

    /**
     * Builder for new Features this writer creates when next() is called and
     * hasNext() == false
     */
    private SimpleFeatureBuilder featureBuilder;

    /**
     * Map of mutable column names/index in the SeTable structure. Not to be
     * accessed directly, but through
     * {@link #getMutableColumnNames(ArcSDEPooledConnection)}
     */
    private Map<String, Integer> mutableColumnNames;

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
    private SimpleFeature feature;

    public AutoCommitFeatureWriter(final SimpleFeatureType featureType,
            final FeatureReader filteredContent, final ArcSDEPooledConnection connection)
            throws NoSuchElementException, IOException {
        assert featureType != null;
        assert filteredContent != null;
        assert connection != null;

        this.featureType = featureType;
        this.filteredContent = filteredContent;
        this.connection = connection;
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
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    /**
     * @see FeatureWriter#hasNext()
     */
    public boolean hasNext() throws IOException {
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
    public SimpleFeature next() throws IOException {
        if (hasNext()) {
            feature = filteredContent.next();
        } else {
            feature = featureBuilder.buildFeature(newFid());
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
        final ArcSDEPooledConnection connection = getConnection();
        final boolean handleTransaction = !connection.isTransactionActive();
        if (handleTransaction) {
            try {
                connection.startTransaction();
            } catch (SeException e) {
                throw new DataSourceException("Can't initiate delete transaction", e);
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
        } catch (SeException e) {
            if (handleTransaction) {
                try {
                    connection.rollbackTransaction();
                } catch (SeException e1) {
                    LOGGER.log(Level.SEVERE, "Unrecoverable error rolling back delete transaction",
                            e);
                }
            }
            throw new DataSourceException("Error deleting " + featureId, e);
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

    /**
     * @see FeatureWriter#write()
     */
    public void write() throws IOException {
        try {
            final ArcSDEPooledConnection connection = getConnection();
            if (isNewlyCreated(feature)) {
                insertSeRow(feature, getLayer(), connection);
            } else {
                updateRow(feature, getLayer(), connection);
            }
        } catch (SeException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            if (LOGGER.isLoggable(Level.FINE)) {
                e.printStackTrace();
            }
            throw new DataSourceException(e.getMessage(), e);
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
    protected final void updateRow(final SimpleFeature feature, final SeLayer layer,
            final ArcSDEPooledConnection connection) throws SeException, NoSuchElementException,
            IOException {
        final SeUpdate update;
        final SeCoordinateReference seCoordRef = layer.getCoordRef();

        final SeRow row;
        Map<String, Integer> mutableColumns = getMutableColumnNames(connection);
        {
            String[] rowColumnNames = new ArrayList<String>(mutableColumns.keySet())
                    .toArray(new String[0]);
            SimpleFeatureType featureType = feature.getFeatureType();
            String typeName = featureType.getTypeName();

            update = new SeUpdate(connection);

            String fid = feature.getID();
            SeObjectId seObjectId = new SeObjectId(ArcSDEAdapter.getNumericFid(fid));
            row = update.singleRow(seObjectId, typeName, rowColumnNames);
            // update.setWriteMode(true);
        }

        setRowProperties(feature, seCoordRef, mutableColumns, row);

        // Now "commit" the changes.
        update.execute();
        update.close();
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
    protected final void insertSeRow(final SimpleFeature feature, final SeLayer layer,
            final ArcSDEPooledConnection connection) throws SeException, IOException {
        // insert the record into ArcSDE
        final SeInsert insert;
        final SeCoordinateReference seCoordRef = layer.getCoordRef();

        Map<String, Integer> mutableColumns = getMutableColumnNames(connection);
        {
            String[] rowColumnNames = new ArrayList<String>(mutableColumns.keySet())
                    .toArray(new String[0]);
            SimpleFeatureType featureType = feature.getFeatureType();
            String typeName = featureType.getTypeName();
            insert = new SeInsert(connection);
            insert.intoTable(typeName, rowColumnNames);
            insert.setWriteMode(true);
        }

        final SeRow row = insert.getRowToSet();

        setRowProperties(feature, seCoordRef, mutableColumns, row);

        // Now "commit" the changes.
        insert.execute();
        insert.close();
    }

    private void setRowProperties(final SimpleFeature feature,
            final SeCoordinateReference seCoordRef, Map<String, Integer> mutableColumns,
            final SeRow row) throws SeException, IOException {
        // Now set the values for the new row here...
        int seRowIndex;
        String attName;
        Object value;
        for (Map.Entry<String, Integer> entry : mutableColumns.entrySet()) {
            seRowIndex = entry.getValue().intValue();
            attName = entry.getKey();
            value = feature.getAttribute(attName);
            setRowValue(row, seRowIndex, value, seCoordRef);
        }
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
            final SeCoordinateReference coordRef) throws SeException, IOException {
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
                try {
                    ArcSDEGeometryBuilder geometryBuilder = ArcSDEGeometryBuilder.builderFor(value
                            .getClass());
                    Geometry geom = (Geometry) value;
                    SeShape shape = geometryBuilder.constructShape(geom, coordRef);
                    row.setShape(index, shape);
                } catch (Exception e) {
                    String msg = e instanceof SeException ? ((SeException) e).getSeError()
                            .getErrDesc() : e.getMessage();
                    LOGGER.log(Level.WARNING, msg, e);
                    throw new DataSourceException(msg, e);
                }
            } else {
                row.setShape(index, null);
            }
        }
    }

    /**
     * @param connection
     * @return a map keyed by mutable column name and valued by the index of the
     *         mutable column name in the SeTable structure
     * @throws IOException
     * @throws NoSuchElementException
     * @throws SeException
     */
    private Map<String, Integer> getMutableColumnNames(ArcSDEPooledConnection connection)
            throws NoSuchElementException, IOException, SeException {
        if (mutableColumnNames == null) {
            // We are going to inspect the column defintions in order to
            // determine which attributes are actually mutable...
            final SeColumnDefinition[] columnDefinitions = getTable().describe();
            final String shapeAttributeName;
            shapeAttributeName = getLayer().getShapeAttributeName(SeLayer.SE_SHAPE_ATTRIBUTE_FID);

            // use LinkedHashMap to respect column order
            Map<String, Integer> columnList = new LinkedHashMap<String, Integer>();

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
                    columnList.put(columnName, Integer.valueOf(usedIndex));
                    // only increment usedIndex if we added a mutable column to
                    // the list
                    usedIndex++;
                }
            }
            this.mutableColumnNames = Collections.unmodifiableMap(columnList);
        }

        return this.mutableColumnNames;
    }

    private SeTable getTable() throws DataSourceException {
        if (this.cachedTable == null) {
            final ArcSDEPooledConnection connection = getConnection();
            final String typeName = this.featureType.getTypeName();
            final SeTable table = connection.getTable(typeName);
            this.cachedTable = table;
        }
        return this.cachedTable;
    }

    private SeLayer getLayer() throws DataSourceException {
        if (this.cachedLayer == null) {
            final ArcSDEPooledConnection connection = getConnection();
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
    protected String newFid() {
        return NEW_FID_PREFIX + UUID.randomUUID();
    }

    /**
     * Checks if <code>feature</code> has been created by this writer
     * 
     * @param feature
     * @return
     */
    private boolean isNewlyCreated(SimpleFeature feature) {
        final String id = feature.getID();
        return id.startsWith(NEW_FID_PREFIX);
    }

    /**
     * Return connection in use. Subclasses may override to get the connection
     * from a transaction, for example.
     * 
     * @return
     * @throws DataSourceException
     */
    protected ArcSDEPooledConnection getConnection() throws DataSourceException {
        return connection;
    }

}
