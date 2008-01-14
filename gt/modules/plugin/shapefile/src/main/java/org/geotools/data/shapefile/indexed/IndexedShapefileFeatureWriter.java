package org.geotools.data.shapefile.indexed;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.FileWriter;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileFeatureWriter;
import org.geotools.data.shapefile.ShpFileType;
import org.geotools.data.shapefile.ShpFiles;
import org.opengis.feature.simple.SimpleFeature;

/**
 * A FeatureWriter for ShapefileDataStore. Uses a write and annotate technique
 * to avoid buffering attributes and geometries. Because the shape and dbf
 * require header information which can only be obtained by reading the entire
 * series of Features, the headers are updated after the initial write
 * completes.
 */
class IndexedShapefileFeatureWriter extends ShapefileFeatureWriter implements
        FileWriter {

    private IndexedShapefileDataStore indexedShapefileDataStore;
    private IndexedFidWriter fidWriter;

    private String currentFid;

    public IndexedShapefileFeatureWriter(String typeName, ShpFiles shpFiles,
            IndexedShapefileAttributeReader attsReader,
            FeatureReader featureReader, IndexedShapefileDataStore datastore)
            throws IOException {
        super(typeName, shpFiles, attsReader, featureReader);
        this.indexedShapefileDataStore = datastore;
        if (!datastore.indexUseable(ShpFileType.FIX)) {
            this.fidWriter = IndexedFidWriter.EMPTY_WRITER;
        } else {
            this.fidWriter = new IndexedFidWriter(shpFiles);
        }
    }

    @Override
    public SimpleFeature next() throws IOException {
        // closed already, error!
        if (featureReader == null) {
            throw new IOException("Writer closed");
        }

        // we have to write the current feature back into the stream
        if (currentFeature != null) {
            write();
        }

        long next = fidWriter.next();
        currentFid = getFeatureType().getTypeName() + "." + next;
        SimpleFeature feature = super.next();
        return feature;
    }

    @Override
    protected String nextFeatureId() {
        return currentFid;
    }

    @Override
    public void remove() throws IOException {
        fidWriter.remove();
        super.remove();
    }

    @Override
    public void write() throws IOException {
        fidWriter.write();
        super.write();
    }

    /**
     * Release resources and flush the header information.
     */
    public void close() throws IOException {
        super.close();

        try {
            fidWriter.close();
            if (shpFiles.isLocal()) {
                if (indexedShapefileDataStore.needsGeneration(ShpFileType.FIX)) {
                    FidIndexer.generate(shpFiles);
                }

                deleteFile(ShpFileType.GRX);
                deleteFile(ShpFileType.QIX);

                if (indexedShapefileDataStore.treeType == IndexType.QIX) {
                    indexedShapefileDataStore
                            .buildQuadTree(indexedShapefileDataStore.maxDepth);
                }
            }
        } catch (Throwable e) {
            indexedShapefileDataStore.treeType = IndexType.NONE;
            ShapefileDataStoreFactory.LOGGER.log(Level.WARNING,
                    "Error creating Spatial index", e);
        }
    }

    private void deleteFile(ShpFileType shpFileType) {
        URL url = shpFiles.acquireWrite(shpFileType, this);
        try {
            File toDelete = DataUtilities.urlToFile(url);

            if (toDelete.exists()) {
                toDelete.delete();
            }
        } finally {
            shpFiles.unlockWrite(url, this);
        }
    }

    public String id() {
        return getClass().getName();
    }
}