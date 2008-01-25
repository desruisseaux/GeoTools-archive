package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.FeatureReaderIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;

class WFSFeatureCollection extends DataFeatureCollection {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private Query query;

    private WFS110ProtocolHandler protocolHandler;

    private SimpleFeatureType contentType;

    /**
     * 
     * @param protocolHandler
     * @param query
     *            properly named query
     * @throws IOException
     */
    public WFSFeatureCollection(SimpleFeatureType contentType,
            WFS110ProtocolHandler protocolHandler, Query query) throws IOException {
        this.contentType = contentType;
        this.protocolHandler = protocolHandler;
        this.query = query;
    }

    @Override
    public SimpleFeatureType getSchema() {
        return contentType;
    }

    @Override
    public ReferencedEnvelope getBounds() {
        ReferencedEnvelope bounds = null;
        try {
            bounds = protocolHandler.getBounds(query);
            if (bounds == null) {
                // bad luck, do a full scan
                final Name defaultgeom = contentType.getDefaultGeometry().getName();
                final DefaultQuery geomQuery = new DefaultQuery(this.query);
                geomQuery.setPropertyNames(new String[] { defaultgeom.getLocalPart() });

                FeatureReader reader;
                reader = protocolHandler.getFeatureReader(contentType, geomQuery,
                        Transaction.AUTO_COMMIT);
                bounds = new ReferencedEnvelope(contentType.getCRS());
                try {
                    BoundingBox featureBounds;
                    while (reader.hasNext()) {
                        featureBounds = reader.next().getBounds();
                        bounds.expandToInclude(featureBounds.getMinX(), featureBounds.getMinY());
                        bounds.expandToInclude(featureBounds.getMaxX(), featureBounds.getMaxY());
                    }
                } finally {
                    reader.close();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Error getting bounds for " + query);
            bounds = new ReferencedEnvelope(getSchema().getCRS());
        }
        return bounds;
    }

    /**
     * @see DataFeatureCollection#getCount()
     */
    @Override
    public int getCount() throws IOException {
        return protocolHandler.getCount(query);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Iterator<SimpleFeature> openIterator() throws IOException {
        FeatureReader reader;
        reader = protocolHandler.getFeatureReader(contentType, query, Transaction.AUTO_COMMIT);
        return new FeatureReaderIterator(reader);
    }
}
