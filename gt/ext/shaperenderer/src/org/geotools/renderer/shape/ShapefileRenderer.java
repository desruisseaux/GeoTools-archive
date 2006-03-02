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
package org.geotools.renderer.shape;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.util.Range;

import org.geotools.data.DataStore;
import org.geotools.data.FIDReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.TransactionStateDiff;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeature;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.geometry.JTS;
import org.geotools.index.quadtree.StoreException;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.Decimator;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.LabelCacheDefault;
import org.geotools.renderer.lite.ListenerList;
import org.geotools.renderer.lite.LiteCoordinateSequence;
import org.geotools.renderer.lite.LiteCoordinateSequenceFactory;
import org.geotools.renderer.lite.LiteShape2;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleAttributeExtractor;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.StyleVisitor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.visitor.DuplicatorStyleVisitor;
import org.geotools.util.NumberRange;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A LiteRenderer Implementations that is optimized for shapefiles.
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.2.x/ext/shaperenderer/src/org/geotools/renderer/shape/ShapefileRenderer.java $
 */
public class ShapefileRenderer implements GTRenderer {
    public static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.shape");

    /** Tolerance used to compare doubles for equality */
    private static final double TOLERANCE = 1e-6;
    private static final GeometryFactory geomFactory = new GeometryFactory(
            new LiteCoordinateSequenceFactory());
    private static final Coordinate[] COORDS;
    private static final MultiPolygon MULTI_POLYGON_GEOM;
    private static final Polygon POLYGON_GEOM;
    private static final LinearRing LINE_GEOM;
    private static final MultiLineString MULTI_LINE_GEOM;
    private static final Point POINT_GEOM;
    private static final MultiPoint MULTI_POINT_GEOM;

    static {
        COORDS = new Coordinate[5];
        COORDS[0] = new Coordinate(0.0, 0.0);
        COORDS[1] = new Coordinate(5.0, 0.0);
        COORDS[2] = new Coordinate(5.0, 5.0);
        COORDS[3] = new Coordinate(0.0, 5.0);
        COORDS[4] = new Coordinate(0.0, 0.0);
        LINE_GEOM = geomFactory.createLinearRing(COORDS);
        MULTI_LINE_GEOM = geomFactory.createMultiLineString(new LineString[]{LINE_GEOM});
        POLYGON_GEOM = geomFactory.createPolygon(LINE_GEOM, new LinearRing[0]);
        MULTI_POLYGON_GEOM = geomFactory.createMultiPolygon(new Polygon[]{POLYGON_GEOM});
        POINT_GEOM = geomFactory.createPoint(COORDS[2]);
        MULTI_POINT_GEOM = geomFactory.createMultiPoint(COORDS);
    }

    /**
     * This listener is added to the list of listeners automatically. It should be removed if the
     * default logging is not needed.
     */
    public static final DefaultRenderListener DEFAULT_LISTENER = new DefaultRenderListener();
    static int NUM_SAMPLES = 200;
    private RenderingHints hints;

    /** Factory that will resolve symbolizers into rendered styles */
    private SLDStyleFactory styleFactory = new SLDStyleFactory();
    private boolean renderingStopRequested;
    private boolean concatTransforms;
    private MapContext context;
    LabelCache labelCache = new LabelCacheDefault();
    private ListenerList renderListeners = new ListenerList();
    List geometryCache = new LinkedList();
    List featureCache = new LinkedList();
    boolean caching = false;
    private double scaleDenominator;
    DbaseFileHeader dbfheader;
    private Object defaultGeom;
    IndexInfo[] layerIndexInfo;

    /**
     * Maps between the AttributeType index of the new generated FeatureType and the real
     * attributeType
     */
    int[] attributeIndexing;

    /** The painter class we use to depict shapes onto the screen */
    private StyledShapePainter painter = new StyledShapePainter(labelCache);
    private Map decimators = new HashMap();

    private Map rendererHints;

    private Graphics2D outputGraphics;

    public ShapefileRenderer( MapContext context ) {
        setContext(context);
    }

    public ShapefileRenderer() {
    }

    public void paint( Graphics2D graphics, Rectangle paintArea, Envelope mapArea ) {
        if (mapArea == null || paintArea == null) {
            LOGGER.info("renderer passed null arguments");
            return;
        } // Other arguments get checked later
        paint(graphics, paintArea, mapArea, RendererUtilities.worldToScreenTransform(mapArea,
                paintArea));
    }

    private DbaseFileHeader getDBFHeader( ShapefileDataStore ds ) {
        DbaseFileReader reader = null;

        try {
            reader = ShapefileRendererUtil.getDBFReader(ds);

            return reader.getHeader();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void processStylersNoCaching( Graphics2D graphics, ShapefileDataStore datastore,
            Query query, Envelope bbox, MathTransform mt, Style style, IndexInfo info,
            Transaction transaction ) throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("processing " + style.getFeatureTypeStyles().length + " stylers");
        }

        FeatureTypeStyle[] featureStylers = style.getFeatureTypeStyles();
        FeatureType type;

        try {
            type = createFeatureType(query, style, datastore.getSchema());
        } catch (Exception e) {
            fireErrorEvent(e);

            return;
        }

        for( int i = 0; i < featureStylers.length; i++ ) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("processing style " + i);
            }

            FeatureTypeStyle fts = featureStylers[i];
            String typeName = datastore.getSchema().getTypeName();

            if ((typeName != null)
                    && (datastore.getSchema().isDescendedFrom(null, fts.getFeatureTypeName()) || typeName
                            .equalsIgnoreCase(fts.getFeatureTypeName()))) {
                // get applicable rules at the current scale
                Rule[] rules = fts.getRules();
                List ruleList = new ArrayList();
                List elseRuleList = new ArrayList();

                for( int j = 0; j < rules.length; j++ ) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("processing rule " + j);
                    }

                    Rule r = rules[j];

                    if (isWithInScale(r)) {
                        if (r.hasElseFilter()) {
                            elseRuleList.add(r);
                        } else {
                            ruleList.add(r);
                        }
                    }
                }

                // process the features according to the rules
                // TODO: find a better way to declare the scale ranges so that
                // we
                // get style caching also between multiple rendering runs
                NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);

                Set modifiedFIDs = processTransaction(graphics, bbox, mt, datastore, transaction,
                        typeName, query, ruleList, elseRuleList, scaleRange);

                processShapefile(graphics, datastore, bbox, mt, info, type, query, ruleList,
                        elseRuleList, modifiedFIDs, scaleRange);
            }
        }
    }

    private Set processTransaction(Graphics2D graphics, Envelope bbox,
        MathTransform transform, DataStore ds, Transaction transaction,
        String typename, Query query, List ruleList, List elseRuleList,
        NumberRange scaleRange) {
        if (transaction == Transaction.AUTO_COMMIT) {
            return Collections.EMPTY_SET;
        }

        TransactionStateDiff state = (TransactionStateDiff) transaction.getState(ds);

        if (state == null) {
            return Collections.EMPTY_SET;
        }

        Set fids = new HashSet();
        Map diff = null;

        try {
            diff = state.diff(typename);
            fids = new HashSet(diff.keySet());
        } catch (IOException e) {
            fids = Collections.EMPTY_SET;
        }

        if (!diff.isEmpty()) {
            Feature feature;
            String fid;

            for( Iterator iter = fids.iterator(); iter.hasNext(); ) {
                if (renderingStopRequested) {
                    break;
                }

                boolean doElse = true;
                fid = (String) iter.next();
                feature = (Feature) diff.get(fid);

                if (!query.getFilter().contains(feature))
                    continue;

                if (feature != null) {
                    // applicable rules
                    for( Iterator it = ruleList.iterator(); it.hasNext(); ) {
                        Rule r = (Rule) it.next();

                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("applying rule: " + r.toString());
                        }

                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("this rule applies ...");
                        }

                        Filter filter = r.getFilter();

                        if ((filter == null) || filter.contains(feature)) {
                            doElse = false;

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("processing Symobolizer ...");
                            }

                            Symbolizer[] symbolizers = r.getSymbolizers();

                            try {
                                processSymbolizers(graphics, feature, symbolizers, scaleRange,
                                        transform);
                            } catch (Exception e) {
                                fireErrorEvent(e);

                                continue;
                            }

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("... done!");
                            }
                        }
                    }

                    if (doElse) {
                        // rules with an else filter
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("rules with an else filter");
                        }

                        for( Iterator it = elseRuleList.iterator(); it.hasNext(); ) {
                            Rule r = (Rule) it.next();
                            Symbolizer[] symbolizers = r.getSymbolizers();

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("processing Symobolizer ...");
                            }

                            try {
                                processSymbolizers(graphics, feature, symbolizers, scaleRange,
                                        transform);
                            } catch (Exception e) {
                                fireErrorEvent(e);

                                continue;
                            }

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("... done!");
                            }
                        }
                    }

                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("feature rendered event ...");
                    }
                }
            }
        }

        return fids;
    }

    private void processShapefile( Graphics2D graphics, ShapefileDataStore datastore,
            Envelope bbox, MathTransform mt, IndexInfo info, FeatureType type, Query query,
            List ruleList, List elseRuleList, Set modifiedFIDs, NumberRange scaleRange )
            throws IOException {
        DbaseFileReader dbfreader = null;

        try {
            dbfreader = ShapefileRendererUtil.getDBFReader(datastore);
        } catch (Exception e) {
            fireErrorEvent(e);
        }

        FIDReader fidReader = null;
        try {
            fidReader = ShapefileRendererUtil.getFidReader(datastore);
        } catch (Exception e) {
            fireErrorEvent(e);
            return;
        }

        OpacityFinder opacityFinder = new OpacityFinder(getAcceptableSymbolizers(type
                .getDefaultGeometry()));

        for( Iterator iter = ruleList.iterator(); iter.hasNext(); ) {
            Rule rule = (Rule) iter.next();
            rule.accept(opacityFinder);
        }

        IndexInfo.Reader shpreader = null;

        try {
            shpreader = new IndexInfo.Reader(info, ShapefileRendererUtil.getShpReader(datastore,
                    bbox, mt, opacityFinder.hasOpacity), bbox);
        } catch (Exception e) {
            fireErrorEvent(e);
            return;
        }

        try {
            while( true ) {
                try {
                    if (renderingStopRequested) {
                        break;
                    }

                    if (!shpreader.hasNext()) {
                        break;
                    }

                    boolean doElse = true;

                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.fine("trying to read geometry ...");
                    }

                    String nextFid = fidReader.next();
                    if (modifiedFIDs.contains(nextFid)) {
                        shpreader.next();
                        //Vitali Diatchkov. We should skip DBF record also. It
                        //is important if SLD is used based on attributes and some features
                        //are in transaction. - Features are styled with alien's attributes in this case - bug.
                        dbfreader.skip();

                        continue;
                    }

                    ShapefileReader.Record record = shpreader.next();

                    SimpleGeometry geom = (SimpleGeometry) record.shape();

                    if (geom == null) {
                        LOGGER.finest("skipping geometry");
                        dbfreader.skip();

                        continue;
                    }

                    Feature feature = createFeature(type, record, dbfreader, nextFid);
                    if (!query.getFilter().contains(feature))
                        continue;

                    if (renderingStopRequested) {
                        break;
                    }

                    if (caching) {
                        geometryCache.add(geom);
                        featureCache.add(feature);
                    }

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("... done: " + geom.toString());
                    }

                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.fine("... done: " + type.getTypeName());
                    }

                    // applicable rules
                    for( Iterator it = ruleList.iterator(); it.hasNext(); ) {
                        Rule r = (Rule) it.next();

                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("applying rule: " + r.toString());
                        }

                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("this rule applies ...");
                        }

                        Filter filter = r.getFilter();

                        if ((filter == null) || filter.contains(feature)) {
                            doElse = false;

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("processing Symobolizer ...");
                            }

                            Symbolizer[] symbolizers = r.getSymbolizers();

                            processSymbolizers(graphics, feature, geom, symbolizers, scaleRange);

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("... done!");
                            }
                        }
                    }

                    if (doElse) {
                        // rules with an else filter
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("rules with an else filter");
                        }

                        for( Iterator it = elseRuleList.iterator(); it.hasNext(); ) {
                            Rule r = (Rule) it.next();
                            Symbolizer[] symbolizers = r.getSymbolizers();

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("processing Symobolizer ...");
                            }

                            processSymbolizers(graphics, feature, geom, symbolizers, scaleRange);

                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("... done!");
                            }
                        }
                    }

                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("feature rendered event ...");
                    }
                } catch (Exception e) {
                    fireErrorEvent(e);
                }
            }
        } finally {
            try {
                if (dbfreader != null) {
                    dbfreader.close();
                }
            } finally {
                try {
                    if (shpreader != null) {
                        shpreader.close();
                    }
                } finally {
                    if (fidReader == null)
                        fidReader.close();
                }
            }
        }
    }

    private Class[] getAcceptableSymbolizers( GeometryAttributeType defaultGeometry ) {
        if (Polygon.class.isAssignableFrom(defaultGeometry.getType())
                || MultiPolygon.class.isAssignableFrom(defaultGeometry.getType())) {
            return new Class[]{PointSymbolizer.class, LineSymbolizer.class, PolygonSymbolizer.class};
        }

        return new Class[]{PointSymbolizer.class, LineSymbolizer.class};
    }

    /**
     * DOCUMENT ME!
     * 
     * @param type
     * @param record
     * @param dbfreader
     * @param id DOCUMENT ME!
     * @return
     * @throws Exception
     */
    Feature createFeature( FeatureType type, Record record, DbaseFileReader dbfreader, String id )
            throws Exception {
        if (type.getAttributeCount() == 1) {
            return type.create(new Object[1], id);
        } else {
            DbaseFileHeader header = dbfreader.getHeader();

            Object[] all = dbfreader.readEntry();
            Object[] values = new Object[type.getAttributeCount()];

            for( int i = 0; i < (values.length - 1); i++ ) {
                values[i] = all[attributeIndexing[i]];

                if (header.getFieldName(attributeIndexing[i]).equals(type.getAttributeType(i))) {
                    System.out.println("ok");
                }
            }

            values[values.length - 1] = getGeom(type.getDefaultGeometry());

            return type.create(values, id);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param defaultGeometry
     * @return
     */
    private Object getGeom( GeometryAttributeType defaultGeometry ) {
        if (defaultGeom == null) {
            if (MultiPolygon.class.isAssignableFrom(defaultGeometry.getType())) {
                defaultGeom = MULTI_POLYGON_GEOM;
            } else if (MultiLineString.class.isAssignableFrom(defaultGeometry.getType())) {
                defaultGeom = MULTI_LINE_GEOM;
            } else if (Point.class.isAssignableFrom(defaultGeometry.getType())) {
                defaultGeom = POINT_GEOM;
            } else if (MultiPoint.class.isAssignableFrom(defaultGeometry.getType())) {
                defaultGeom = MULTI_POINT_GEOM;
            }
        }

        return defaultGeom;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param query
     * @param style
     * @param schema DOCUMENT ME!
     * @return
     * @throws FactoryConfigurationError
     * @throws SchemaException
     */
    FeatureType createFeatureType( Query query, Style style, FeatureType schema )
            throws SchemaException {
        String[] attributes = findStyleAttributes((query == null) ? Query.ALL : query, style,
                schema);
        AttributeType[] types = new AttributeType[attributes.length];
        attributeIndexing = new int[attributes.length];

        for( int i = 0; i < types.length; i++ ) {
            types[i] = schema.getAttributeType(attributes[i]);

            for( int j = 0; j < dbfheader.getNumFields(); j++ ) {
                if (dbfheader.getFieldName(j).equals(attributes[i])) {
                    attributeIndexing[i] = j;

                    break;
                }
            }
        }

        FeatureType type = FeatureTypeBuilder.newFeatureType(types, schema.getTypeName(), schema
                .getNamespace(), false, null, schema.getDefaultGeometry());

        return type;
    }

    /**
     * Inspects the <code>MapLayer</code>'s style and retrieves it's needed attribute names,
     * returning at least the default geometry attribute name.
     * 
     * @param query DOCUMENT ME!
     * @param style the <code>Style</code> to determine the needed attributes from
     * @param schema the featuresource schema
     * @return the minimun set of attribute names needed to render <code>layer</code>
     */
    private String[] findStyleAttributes( final Query query, Style style, FeatureType schema ) {
        StyleAttributeExtractor sae = new StyleAttributeExtractor(){
            public void visit( Rule rule ) {

                DuplicatorStyleVisitor dupeStyleVisitor = new DuplicatorStyleVisitor(
                        StyleFactoryFinder.createStyleFactory(), FilterFactoryFinder
                                .createFilterFactory());
                dupeStyleVisitor.visit(rule);
                Rule clone = (Rule) dupeStyleVisitor.getCopy();

                // Rule clone=StyleFactoryFinder.createStyleFactory().createRule();
                // clone.setAbstract(rule.getAbstract());
                // clone.setFilter(rule.getFilter());
                // clone.setSymbolizers(rule.getSymbolizers());
                // clone.setIsElseFilter(rule.hasElseFilter());
                // clone.setLegendGraphic(rule.getLegendGraphic());
                // clone.setMaxScaleDenominator(rule.getMaxScaleDenominator());
                // clone.setMinScaleDenominator(rule.getMinScaleDenominator());
                // clone.setName(rule.getName());
                // clone.setTitle(rule.getTitle());
                //                    
                // if ((query != Query.ALL)
                // && !query.getFilter().equals(Filter.NONE)) {
                // if (clone.getFilter() == null) {
                // clone.setFilter(query.getFilter());
                // } else {
                // clone.setFilter(clone.getFilter().and(query.getFilter()));
                // }
                // }

                super.visit(clone);
            }
        };

        sae.visit(style);

        String[] ftsAttributes = sae.getAttributeNames();

        return ftsAttributes;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param graphics
     * @param feature DOCUMENT ME!
     * @param geom
     * @param symbolizers
     * @param scaleRange
     */
    private void processSymbolizers( Graphics2D graphics, Feature feature, SimpleGeometry geom,
            Symbolizer[] symbolizers, NumberRange scaleRange ) {
        for( int m = 0; m < symbolizers.length; m++ ) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying symbolizer " + symbolizers[m]);
            }

            if (renderingStopRequested) {
                break;
            }

            if (symbolizers[m] instanceof TextSymbolizer) {
                try {
                    labelCache.put((TextSymbolizer) symbolizers[m], feature, getLiteShape2(geom),
                            scaleRange);
                } catch (Exception e) {
                    fireErrorEvent(e);
                }
            } else {
                Style2D style = styleFactory.createStyle(feature, symbolizers[m], scaleRange);
                painter.paint(graphics, getShape(geom), style, scaleDenominator);

                // try {
                // style = styleFactory.createStyle(feature, getTestStyle(),
                // scaleRange);
                // painter.paint(graphics, getLiteShape2(geom), style,
                // scaleDenominator);
                // } catch (Exception e) {
                // fireErrorEvent(e);
                // }
            }

            fireFeatureRenderedEvent(feature);
        }
    }

    /**
     * Applies each of a set of symbolizers in turn to a given feature.
     * <p>
     * This is an internal method and should only be called by processStylers.
     * </p>
     * 
     * @param graphics
     * @param feature The feature to be rendered
     * @param symbolizers An array of symbolizers which actually perform the rendering.
     * @param scaleRange The scale range we are working on... provided in order to make the style
     *        factory happy
     * @param transform DOCUMENT ME!
     * @throws TransformException
     * @throws FactoryException
     */
    private void processSymbolizers( final Graphics2D graphics, final Feature feature,
            final Symbolizer[] symbolizers, Range scaleRange, MathTransform transform )
            throws TransformException, FactoryException {
        LiteShape2 shape;

        for( int m = 0; m < symbolizers.length; m++ ) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying symbolizer " + symbolizers[m]);
            }

            Geometry g = feature.getDefaultGeometry();
            shape = new LiteShape2(g, transform, getDecimator(transform), false);

            if (symbolizers[m] instanceof TextSymbolizer) {
                labelCache.put((TextSymbolizer) symbolizers[m], feature, shape, scaleRange);
            } else {
                Style2D style = styleFactory.createStyle(feature, symbolizers[m], scaleRange);
                painter.paint(graphics, shape, style, scaleDenominator);
            }
        }

        fireFeatureRenderedEvent(feature);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param mathTransform DOCUMENT ME!
     * @return
     * @throws org.opengis.referencing.operation.NoninvertibleTransformException
     */
    private Decimator getDecimator( MathTransform mathTransform )
            throws org.opengis.referencing.operation.NoninvertibleTransformException {
        Decimator decimator = (Decimator) decimators.get(mathTransform);

        if (decimator == null) {
            if ((mathTransform != null) && !mathTransform.isIdentity()) {
                decimator = new Decimator(mathTransform.inverse());
            } else {
                decimator = new Decimator(null);
            }

            decimators.put(mathTransform, decimator);
        }

        return decimator;
    }

    /**
     * Creates a JTS shape that is an approximation of the SImpleGeometry. This is ONLY use for
     * labelling and is only created if a text symbolizer is part of the current style.
     * 
     * @param geom the geometry to wrap
     * @return
     * @throws TransformException
     * @throws FactoryException
     * @throws RuntimeException DOCUMENT ME!
     */
    LiteShape2 getLiteShape2( SimpleGeometry geom ) throws TransformException, FactoryException {
        Geometry jtsGeom;
        if ((geom.type == ShapeType.POLYGON) || (geom.type == ShapeType.POLYGONM)
                || (geom.type == ShapeType.POLYGONZ)) {
            double[] points = getPointSample(geom, true);
            CoordinateSequence seq = new LiteCoordinateSequence(points);
            Polygon poly;

            try {
                poly = geomFactory.createPolygon(geomFactory.createLinearRing(seq),
                        new LinearRing[]{});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            jtsGeom = geomFactory.createMultiPolygon(new Polygon[]{poly});
        } else if ((geom.type == ShapeType.ARC) || (geom.type == ShapeType.ARCM)
                || (geom.type == ShapeType.ARCZ)) {
            double[] points = getPointSample(geom, false);
            CoordinateSequence seq = new LiteCoordinateSequence(points);
            jtsGeom = geomFactory.createMultiLineString(new LineString[]{geomFactory
                    .createLineString(seq)});
        } else if ((geom.type == ShapeType.MULTIPOINT) || (geom.type == ShapeType.MULTIPOINTM)
                || (geom.type == ShapeType.MULTIPOINTZ)) {
            double[] points = getPointSample(geom, false);
            CoordinateSequence seq = new LiteCoordinateSequence(points);
            jtsGeom = geomFactory.createMultiPoint(seq);
        } else {
            jtsGeom = geomFactory.createPoint(new Coordinate(geom.coords[0][0], geom.coords[0][1]));
        }

        LiteShape2 shape = new LiteShape2(jtsGeom, null, null, false);

        return shape;
    }

    /**
     * takes a random sampling from the geometry. Only uses the larges part of the geometry.
     * 
     * @param geom
     * @param isPolygon DOCUMENT ME!
     * @return
     */
    private double[] getPointSample( SimpleGeometry geom, boolean isPolygon ) {
        int largestPart = 0;

        for( int i = 0; i < geom.coords.length; i++ ) {
            if (geom.coords[i].length > geom.coords[largestPart].length) {
                largestPart = i;
            }
        }

        return geom.coords[largestPart];
    }

    /**
     * DOCUMENT ME!
     * 
     * @param geom
     * @return
     */
    private Shape getShape( SimpleGeometry geom ) {
        if ((geom.type == ShapeType.ARC) || (geom.type == ShapeType.ARCM)
                || (geom.type == ShapeType.ARCZ)) {
            return new MultiLineShape(geom);
        }

        if ((geom.type == ShapeType.POLYGON) || (geom.type == ShapeType.POLYGONM)
                || (geom.type == ShapeType.POLYGONZ)) {
            return new PolygonShape(geom);
        }

        if ((geom.type == ShapeType.POINT) || (geom.type == ShapeType.POINTM)
                || (geom.type == ShapeType.POINTZ) || (geom.type == ShapeType.MULTIPOINT)
                || (geom.type == ShapeType.MULTIPOINTM) || (geom.type == ShapeType.MULTIPOINTZ)) {
            return new MultiPointShape(geom);
        }

        return null;
    }

    /**
     * Checks if a rule can be triggered at the current scale level
     * 
     * @param r The rule
     * @return true if the scale is compatible with the rule settings
     */
    private boolean isWithInScale( Rule r ) {
        return ((r.getMinScaleDenominator() - TOLERANCE) <= scaleDenominator)
                && ((r.getMaxScaleDenominator() + TOLERANCE) > scaleDenominator);
    }

    /**
     * adds a listener that responds to error events of feature rendered events.
     * 
     * @param listener the listener to add.
     * @see RenderListener
     */
    public void addRenderListener( RenderListener listener ) {
        renderListeners.add(listener);
    }

    /**
     * Removes a render listener.
     * 
     * @param listener the listener to remove.
     * @see RenderListener
     */
    public void removeRenderListener( RenderListener listener ) {
        renderListeners.remove(listener);
    }

    private void fireFeatureRenderedEvent( Feature feature ) {
        Object[] objects = renderListeners.getListeners();

        for( int i = 0; i < objects.length; i++ ) {
            RenderListener listener = (RenderListener) objects[i];
            listener.featureRenderer(feature);
        }
    }

    private void fireErrorEvent( Exception e ) {
        Object[] objects = renderListeners.getListeners();

        for( int i = 0; i < objects.length; i++ ) {
            RenderListener listener = (RenderListener) objects[i];
            listener.errorOccurred(e);
        }
    }

    /**
     * Setter for property scaleDenominator.
     * 
     * @param scaleDenominator New value of property scaleDenominator.
     */
    protected void setScaleDenominator( double scaleDenominator ) {
        this.scaleDenominator = scaleDenominator;
    }

    /**
     * If you call this method from another thread than the one that called <code>paint</code> or
     * <code>render</code> the rendering will be forcefully stopped before termination
     */
    public void stopRendering() {
        renderingStopRequested = true;
        labelCache.stop();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return Returns the caching.
     */
    public boolean isCaching() {
        return caching;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param caching The caching to set.
     */
    public void setCaching( boolean caching ) {
        this.caching = caching;
    }

    public MapContext getContext() {
        return context;
    }

    public boolean isConcatTransforms() {
        return concatTransforms;
    }

    public void setConcatTransforms( boolean concatTransforms ) {
        this.concatTransforms = concatTransforms;
    }

    public IndexInfo useIndex( ShapefileDataStore ds ) throws IOException, StoreException {
        IndexInfo info;
        String filename = null;
        URL url = ShapefileRendererUtil.getshpURL(ds);

        if (url == null) {
            throw new NullPointerException("Null URL for ShapefileDataSource");
        }

        try {
            filename = java.net.URLDecoder.decode(url.toString(), "US-ASCII");
        } catch (java.io.UnsupportedEncodingException use) {
            throw new java.net.MalformedURLException("Unable to decode " + url + " cause "
                    + use.getMessage());
        }

        filename = filename.substring(0, filename.length() - 4);

        String grxext = ".grx";
        String qixext = ".qix";

        if (ds.isLocal()) {
            File grxTree = new File(new URL(filename + grxext).getPath());
            File qixTree = new File(new URL(filename + qixext).getPath());
            URL shx = new URL(filename + ".shx");

            if (!new File(shx.getPath()).exists()) {
                info = new IndexInfo(IndexInfo.TREE_NONE, null, null);
            } else if (!grxTree.exists() && qixTree.exists()) {
                info = new IndexInfo(IndexInfo.QUAD_TREE, new URL(filename + qixext), shx);
                LOGGER.fine("Using quad tree");
            } else if (grxTree.exists()) {
                info = new IndexInfo(IndexInfo.R_TREE, new URL(filename + grxext), shx);
                LOGGER.fine("Using r-tree");
            } else {
                info = new IndexInfo(IndexInfo.TREE_NONE, null, null);
                LOGGER.fine("No indexing");
            }
        } else {
            info = new IndexInfo(IndexInfo.TREE_NONE, null, null);
        }

        return info;
    }

    /**
     * By default ignores all feature renderered events and logs all exceptions as severe.
     */
    private static class DefaultRenderListener implements RenderListener {
        /**
         * @see org.geotools.renderer.lite.RenderListener#featureRenderer(org.geotools.feature.Feature)
         */
        public void featureRenderer( Feature feature ) {
            // do nothing.
        }

        /**
         * @see org.geotools.renderer.lite.RenderListener#errorOccurred(java.lang.Exception)
         */
        public void errorOccurred( Exception e ) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void setJava2DHints( RenderingHints hints ) {
        this.hints = hints;
    }

    public RenderingHints getJava2DHints() {
        return hints;
    }

    public void setRendererHints( Map hints ) {
        rendererHints = hints;
    }

    public Map getRendererHints() {
        return rendererHints;
    }

    public void setContext( MapContext context ) {
        if (context == null) {
            context = new DefaultMapContext();
        }

        this.context = context;

        MapLayer[] layers = context.getLayers();
        layerIndexInfo = new IndexInfo[layers.length];

        for( int i = 0; i < layers.length; i++ ) {
            DataStore ds = layers[i].getFeatureSource().getDataStore();
            assert (ds instanceof ShapefileDataStore);

            ShapefileDataStore sds = (ShapefileDataStore) ds;

            try {
                layerIndexInfo[i] = useIndex(sds);
            } catch (Exception e) {
                layerIndexInfo[i] = new IndexInfo(IndexInfo.TREE_NONE, null, null);
                LOGGER.fine("Exception while trying to use index" + e.getLocalizedMessage());
            }
        }
    }

    public void paint( Graphics2D graphics, Rectangle paintArea, AffineTransform worldToScreen ) {
        if (worldToScreen == null || paintArea == null) {
            LOGGER.info("renderer passed null arguments");
            return;
        } // Other arguments get checked later
        // First, create the bbox in real world coordinates
        Envelope mapArea;
        try {
            mapArea = RendererUtilities.createMapEnvelope(paintArea, worldToScreen);
            paint(graphics, paintArea, mapArea, worldToScreen);
        } catch (NoninvertibleTransformException e) {
            fireErrorEvent(new Exception("Can't create pixel to world transform", e));
        }
    }

    public void paint( Graphics2D graphics, Rectangle paintArea, Envelope envelope,
            AffineTransform transform ) {
        this.outputGraphics = graphics;

        if (hints != null) {
            graphics.setRenderingHints(hints);
        }

        if ((graphics == null) || (paintArea == null)) {
            LOGGER.info("renderer passed null arguments");

            return;
        }

        // reset the abort flag
        renderingStopRequested = false;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Affine Transform is " + transform);
        }

        /*
         * If we are rendering to a component which has already set up some form of transformation
         * then we can concatenate our transformation to it. An example of this is the ZoomPane
         * component of the swinggui module.
         */
        if (concatTransforms) {
            AffineTransform atg = graphics.getTransform();

            // graphics.setTransform(new AffineTransform());
            atg.concatenate(transform);
            transform = atg;
        }

        try {
            setScaleDenominator(RendererUtilities.calculateScale(envelope, context
                    .getCoordinateReferenceSystem(), paintArea.width, paintArea.height, 90)); // 90 =
            // OGC
            // standard
            // DPI
            // (see
            // SLD
            // spec
            // page
            // 37)
        } catch (Exception e) // probably either (1) no CRS (2) error xforming
        {
            setScaleDenominator(1 / transform.getScaleX()); // DJB old method - the best we can do
        }

        MapLayer[] layers = context.getLayers();

        // get detstination CRS
        CoordinateReferenceSystem destinationCrs = context.getCoordinateReferenceSystem();
        labelCache.start();

        for( int i = 0; i < layers.length; i++ ) {
            MapLayer currLayer = layers[i];

            if (!currLayer.isVisible()) {
                // Only render layer when layer is visible
                continue;
            }

            if (renderingStopRequested) {
                return;
            }

            labelCache.startLayer();

            Envelope bbox = envelope;

            try {
                ShapefileDataStore ds = (ShapefileDataStore) currLayer.getFeatureSource()
                        .getDataStore();
                CoordinateReferenceSystem dataCRS = ds.getSchema().getDefaultGeometry()
                        .getCoordinateSystem();
                MathTransform mt;

                try {
                    mt = CRS.transform(dataCRS, destinationCrs, true);
                    bbox = JTS.transform(bbox, mt.inverse(), 10);
                } catch (Exception e) {
                    mt = null;
                }

                MathTransform at = FactoryFinder.getMathTransformFactory(null)
                        .createAffineTransform(new GeneralMatrix(transform));

                if (mt == null) {
                    mt = at;
                } else {
                    mt = FactoryFinder.getMathTransformFactory(null).createConcatenatedTransform(
                            mt, at);
                }

                // dbfheader must be set so that the attributes required for theming can be read in.
                dbfheader = getDBFHeader(ds);

                // graphics.setTransform(transform);
                // extract the feature type stylers from the style object
                // and process them

                Transaction transaction = null;

                if (currLayer.getFeatureSource() instanceof FeatureStore) {
                    transaction = ((FeatureStore) currLayer.getFeatureSource()).getTransaction();
                }

                processStylersNoCaching(graphics, ds, currLayer.getQuery(), bbox, mt, currLayer
                        .getStyle(), layerIndexInfo[i], transaction);
            } catch (Exception exception) {
                fireErrorEvent(new Exception("Exception rendering layer " + currLayer, exception));
            }

            labelCache.endLayer(graphics, paintArea);
        }

        labelCache.end(graphics, paintArea);
        LOGGER.fine("Style cache hit ratio: " + styleFactory.getHitRatio() + " , hits "
                + styleFactory.getHits() + ", requests " + styleFactory.getRequests());
    }

}
