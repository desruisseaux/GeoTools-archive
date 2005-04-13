/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.geotools.renderer.lite;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.util.Range;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.JTS;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.operation.CoordinateOperationFactory;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.renderer.Renderer;
import org.geotools.renderer.Renderer2D;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleAttributeExtractor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * A lite implementation of the Renderer and Renderer2D interfaces. Lite means that:
 * <ul>
 * <li>The code is relatively simple to understand, so it can be used as a simple example of an SLD
 * compliant rendering code</li>
 * <li>Uses as few memory as possible</li>
 * </ul>
 * Use this class if you need a stateless renderer that provides low memory footprint and decent
 * rendering performance on the first call but don't need good optimal performance on subsequent
 * calls on the same data. Notice: for the time being, this class doesn't support GridCoverage
 * stylers, that will be rendered using the non geophisics version of the GridCoverage, if
 * available, with the geophisics one, otherwise.
 * 
 * @author James Macgill
 * @author Andrea Aime
 * @version $Id$
 */
public class LiteRenderer2 implements Renderer, Renderer2D {
    /** Tolerance used to compare doubles for equality */
    private static final double TOLERANCE = 1e-6;

    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    int error = 0;
    
    /** 
     * This listener is added to the list of listeners automatically.  It should be removed
     * if the default logging is not needed.
     */
    public static final DefaultRenderListener DEFAULT_LISTENER=new DefaultRenderListener();

    /** Filter factory for creating bounding box filters */
    private FilterFactory filterFactory = FilterFactory.createFilterFactory();
    CoordinateOperationFactory operationFactory = new CoordinateOperationFactory();

    /**
     * Context which contains the layers and the bouning box which needs to be rendered.
     */
    private MapContext context;

    /**
     * Flag which determines if the renderer is interactive or not. An interactive renderer will
     * return rather than waiting for time consuming operations to complete (e.g. Image Loading). A
     * non-interactive renderer (e.g. a SVG or PDF renderer) will block for these operations.
     */
    private boolean interactive = true;

    /**
     * Flag which controls behaviour for applying affine transformation to the graphics object. If
     * true then the transform will be concatenated to the existing transform. If false it will be
     * replaced.
     */
    private boolean concatTransforms = false;

    /** Geographic map extent */
    private Envelope mapExtent = null;

    /** Graphics object to be rendered to. Controlled by set output. */
    private Graphics2D outputGraphics;

    /** The size of the output area in output units. */
    private Rectangle screenSize;

    /**
     * Activates bbox and attribute filtering optimization, that works properly only if the input
     * feature sources really contain just one feature type. This may not be the case if the feature
     * source is based on a generic feature collection
     */
    private boolean optimizedDataLoadingEnabled;

    /**
     * This flag is set to false when starting rendering, and will be checked during the rendering
     * loop in order to make it stop forcefully
     */
    private boolean renderingStopRequested;

    /**
     * The ratio required to scale the features to be rendered so that they fit into the output
     * space.
     */
    private double scaleDenominator;

    /** Maximun displacement for generalization during rendering */
    private double generalizationDistance = 1.0;

    /** Factory that will resolve symbolizers into rendered styles */
    private SLDStyleFactory styleFactory = new SLDStyleFactory();

    LabelCache labelCache=new LabelCacheDefault();
    
    /** The painter class we use to depict shapes onto the screen */
    private StyledShapePainter painter = new StyledShapePainter(labelCache);

    /** The math transform cache */
    private HashMap transformMap = new HashMap();

    /** Set to false if the reprojection fails */
    private boolean canTransform = true;

    private boolean memoryPreloadingEnabled;

    private IndexedFeatureResults indexedFeatureResults;
    
    private ListenerList renderListeners= new ListenerList();

    private RenderingHints hints;
    

    /**
     * Creates a new instance of LiteRenderer without a context. Use it only to gain access to
     * utility methods of this class or if you want to render random feature collections instead of
     * using the map context interface
     */
    public LiteRenderer2() {
        addRenderListener(DEFAULT_LISTENER);
    }

    /**
     * Creates a new instance of Java2DRenderer.
     * 
     * @param context Contains pointers to layers, bounding box, and style required for rendering.
     */
    public LiteRenderer2( MapContext context ) {
        this();
        this.context = context;
    }

    /**
     * Sets the flag which controls behaviour for applying affine transformation to the graphics
     * object.
     * 
     * @param flag If true then the transform will be concatenated to the existing transform. If
     *        false it will be replaced.
     */
    public void setConcatTransforms( boolean flag ) {
        concatTransforms = flag;
    }

    /**
     * Returns the amount of time the renderer waits for loading an external image before giving up
     * and examining the other images in the Graphic object
     * 
     * @return the timeout in milliseconds
     */
    public static long getImageLoadingTimeout() {
        return ImageLoader.getTimeout();
    }

    /**
     * Sets the maximum time to wait for getting an external image. Set it to -1 to wait
     * undefinitely. The default value is 10 seconds
     * 
     * @param newTimeout the new timeout value in milliseconds
     */
    public static void setImageLoadingTimeout( long newTimeout ) {
        ImageLoader.setTimeout(newTimeout);
    }

    /**
     * Flag which controls behaviour for applying affine transformation to the graphics object.
     * 
     * @return a boolean flag. If true then the transform will be concatenated to the existing
     *         transform. If false it will be replaced.
     */
    public boolean getConcatTransforms() {
        return concatTransforms;
    }

    /**
     * Called before {@link render}, this sets where any output will be sent.
     * 
     * @param g A graphics object for future rendering to be sent to. Note: must be an instance of
     *        lite renderer.
     * @param bounds The size of the output area, required so that scale can be calculated.
     * @deprecated Graphics and bounds is to be set in renderer().
     */
    public void setOutput( Graphics g, Rectangle bounds ) {
        outputGraphics = (Graphics2D) g;
        screenSize = bounds;
    }
    /**
     * adds a listener that responds to error events of feature rendered events.
     * 
     * @see RenderListener
     * 
     * @param listener the listener to add.
     */
    public void addRenderListener(RenderListener listener){
        renderListeners.add(listener);
    }
    /**
     * Removes a render listener.
     * 
     * @see RenderListener
     * 
     * @param listener the listener to remove.
     */
    public void removeRenderListener(RenderListener listener){
        renderListeners.remove(listener);
    }
    
    private void fireFeatureRenderedEvent(Feature feature) {
        Object[] objects=renderListeners.getListeners();
        for( int i = 0; i < objects.length; i++ ) {
            RenderListener listener=(RenderListener) objects[i];
            listener.featureRenderer(feature);
        }
    }

    private void fireErrorEvent(Exception e) {
        Object[] objects=renderListeners.getListeners();
        for( int i = 0; i < objects.length; i++ ) {
            RenderListener listener=(RenderListener) objects[i];
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
    }

    /**
     * Render features based on the LayerList, BoundBox and Style specified in this.context. Don't
     * mix calls to paint and setOutput, when calling this method the graphics set in the setOutput
     * method is discarded.
     * 
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg: pixels).
     * @param transform A transform which converts World coordinates to Screen coordinates.
     * @task Need to check if the Layer CoordinateSystem is different to the BoundingBox rendering
     *       CoordinateSystem and if so, then transform the coordinates.
     */
    public void paint( Graphics2D graphics, Rectangle paintArea, AffineTransform transform ) {
        // Consider the geometries, they should lay inside or
        // overlap with the bbox indicated by the painting area.
        // First, create the bbox in real world coordinates
        AffineTransform pixelToWorld = null;

        try {
            pixelToWorld = transform.createInverse();
        } catch (NoninvertibleTransformException e) {
            fireErrorEvent(new Exception("Can't create pixel to world transform", e));
        }

        Point2D p1 = new Point2D.Double();
        Point2D p2 = new Point2D.Double();
        pixelToWorld.transform(new Point2D.Double(paintArea.getMinX(), paintArea.getMinY()), p1);
        pixelToWorld.transform(new Point2D.Double(paintArea.getMaxX(), paintArea.getMaxY()), p2);

        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        Envelope envelope = new Envelope(Math.min(x1, x2), Math.max(x1, x2), Math.min(y1, y2), Math
                .max(y1, y2));


        paint(graphics, paintArea, envelope);
    }

    /**
     * TODO summary sentence for paint ...
     * 
     * @param graphics
     * @param paintArea
     * @param transform
     * @param envelope
     */
    public void paint( Graphics2D graphics, Rectangle paintArea, Envelope envelope ) {
        AffineTransform transform=worldToScreenTransform(envelope, paintArea);
        error = 0;
        if ( hints != null )
            graphics.setRenderingHints(hints);
        if ((graphics == null) || (paintArea == null)) {
            LOGGER.info("renderer passed null arguments");

            return;
        }
        // reset the abort flag
        renderingStopRequested = false;
        AffineTransform at = transform;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Affine Transform is " + at);
        }
        /*
         * If we are rendering to a component which has already set up some form of transformation
         * then we can concatenate our transformation to it. An example of this is the ZoomPane
         * component of the swinggui module.
         */
        if (concatTransforms) {
            AffineTransform atg = graphics.getTransform();
            atg.concatenate(at);
            at = atg;
        }
        // graphics.setTransform(at);
        setScaleDenominator(1 / at.getScaleX());
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
            try {
                // mapExtent = this.context.getAreaOfInterest();
                FeatureResults results = queryLayer(currLayer, envelope, destinationCrs);

                // extract the feature type stylers from the style object
                // and process them
                processStylers(graphics, results, currLayer.getStyle().getFeatureTypeStyles(), at,
                        context.getCoordinateReferenceSystem());
            } catch (Exception exception) {
                fireErrorEvent(new Exception("Exception rendering layer " + currLayer,exception));
            }

            labelCache.endLayer(graphics, screenSize);
        }
        labelCache.end(graphics, paintArea);
        LOGGER.fine("Style cache hit ratio: " + styleFactory.getHitRatio() + " , hits "
                + styleFactory.getHits() + ", requests " + styleFactory.getRequests());
        if (error > 0) {
            LOGGER.warning("Number of Errors during paint(Graphics2D, AffineTransform) = " + error);
        }
    }

    /**
     * Queries a given layer's features to be rendered based on the target rendering bounding box.
     * <p>
     * If <code>optimizedDataLoadingEnabled</code> attribute has been set to <code>true</code>,
     * the following optimization will be performed in order to limit the number of features
     * returned:
     * <ul>
     * <li>Just the features whose geometric attributes lies within <code>envelope</code> will be
     * queried</li>
     * <li>The queried attributes will be limited to just those needed to perform the rendering,
     * based on the requiered geometric and non geometric attributes found in the Layer's style
     * rules</li>
     * <li>If a <code>Query</code> has been set to limit the resulting layer's features, the
     * final filter to obtain them will respect it. This means that the bounding box filter and the
     * Query filter will be combined, also including maxFeatures from Query</li>
     * <li>At least that the layer's definition query explicitly says to retrieve some attribute,
     * no attributes will be requested from it, for performance reassons. So it is desirable to not
     * use a Query for filtering a layer wich includes attributes. Note that including the
     * attributes in the result is not necessary for the query's filter to get properly processed.
     * </li>
     * </ul>
     * </p>
     * <p>
     * <b>NOTE </b>: This is an internal method and should only be called by
     * <code>paint(Graphics2D, Rectangle, AffineTransform)</code>. It is package protected just
     * to allow unit testing it.
     * </p>
     * 
     * @param currLayer the actually processing layer for renderition
     * @param envelope the spatial extent wich is the target area fo the rendering process
     * @param destinationCrs DOCUMENT ME!
     * @return the set of features resulting from <code>currLayer</code> after quering its feature
     *         source
     * @throws IllegalFilterException if something goes wrong constructing the bbox filter
     * @throws IOException
     * @throws IllegalAttributeException
     * @see MapLayer#setQuery(org.geotools.data.Query)
     */
    FeatureResults queryLayer( MapLayer currLayer, Envelope envelope,
            CoordinateReferenceSystem destinationCrs ) throws IllegalFilterException, IOException,
            IllegalAttributeException {
        FeatureResults results = null;
        FeatureSource featureSource = currLayer.getFeatureSource();
        FeatureType schema = featureSource.getSchema();
        Query query = Query.ALL;

        if (optimizedDataLoadingEnabled) {
            // see what attributes we really need by exploring the styles
            String[] attributes = findStyleAttributes(currLayer, schema);

            try {
                // Then create the geometry filters. We have to create one for each
                // geometric
                // attribute used during the rendering as the feature may have more
                // than one
                // and the styles could use non default geometric ones
                CoordinateReferenceSystem sourceCrs = currLayer.getFeatureSource().getSchema()
                        .getDefaultGeometry().getCoordinateSystem();

                if (sourceCrs != null && !sourceCrs.equals(destinationCrs)) {
                    // get an unprojected envelope since the feature source is operating on
                    // unprojected geometries
                    MathTransform transform = operationFactory.createOperation(destinationCrs,
                            sourceCrs).getMathTransform();
                    if (transform != null && !transform.isIdentity())
                        envelope = JTS.transform(envelope, transform);
                }

                Filter filter = null;
                if (!memoryPreloadingEnabled) {
                    BBoxExpression rightBBox = filterFactory.createBBoxExpression(envelope);
                    filter = createBBoxFilters(schema, attributes, rightBBox);
                } else {
                    filter = Filter.NONE;
                }

                // now build the query using only the attributes and the bounding
                // box needed
                DefaultQuery q = new DefaultQuery(schema.getTypeName());
                q.setFilter(filter);
                q.setPropertyNames(attributes);
                query = q;
            } catch (Exception e) {
                fireErrorEvent(new Exception("Error transforming bbox",e));
                canTransform = false;
                DefaultQuery q = new DefaultQuery(schema.getTypeName());
                q.setPropertyNames(attributes);
                if( envelope.intersects(featureSource.getBounds())){
                    LOGGER.fine("Got a tranform exception while trying to de-project the current "
                            + "envelope, bboxs intersect therefore using envelope)");
                    Filter filter = null;
                    BBoxExpression rightBBox = filterFactory.createBBoxExpression(envelope);
                    filter = createBBoxFilters(schema, attributes, rightBBox);
                    q.setFilter(filter);
                }else{
                    LOGGER.fine("Got a tranform exception while trying to de-project the current "
                            + "envelope, falling back on full data loading (no bbox query)");
                }
                query = q;
            }
        }

        // now, if a definition query has been established for this layer, be
        // sure to respect it by combining it with the bounding box one.
        Query definitionQuery = currLayer.getQuery();

        if (definitionQuery != Query.ALL) {
            if (query == Query.ALL) {
                query = definitionQuery;
            } else {
                query = DataUtilities.mixQueries(definitionQuery, query, "liteRenderer");
            }
        }

        if (memoryPreloadingEnabled) {
            // TODO: attache a feature listener, we must erase the memory cache if
            // anything changes in the data store
            if (indexedFeatureResults == null) {
                indexedFeatureResults = new IndexedFeatureResults(featureSource.getFeatures(query));
            }
            indexedFeatureResults.setQueryBounds(envelope);
            results = indexedFeatureResults;
        } else {
            results = featureSource.getFeatures(query);
        }

        return results;
    }

    /**
     * Inspects the <code>MapLayer</code>'s style and retrieves it's needed attribute names,
     * returning at least the default geometry attribute name.
     * 
     * @param layer the <code>MapLayer</code> to determine the needed attributes from
     * @param schema the <code>layer</code>'s featuresource schema
     * @return the minimun set of attribute names needed to render <code>layer</code>
     */
    private String[] findStyleAttributes( MapLayer layer, FeatureType schema ) {
        StyleAttributeExtractor sae = new StyleAttributeExtractor();
        sae.visit(layer.getStyle());

        String[] ftsAttributes = sae.getAttributeNames();

        /*
         * GR: if as result of sae.getAttributeNames() ftsAttributes already contains geometry
         * attribue names, they gets duplicated, wich produces an error in AbstracDatastore when
         * trying to create a derivate FeatureType. So I'll add the default geometry only if it is
         * not already present, but: should all the geometric attributes be added by default? I will
         * add them, but don't really know what's the expected behavior
         */
        List atts = new LinkedList(Arrays.asList(ftsAttributes));
        AttributeType[] attTypes = schema.getAttributeTypes();
        String attName;

        for( int i = 0; i < attTypes.length; i++ ) {
            attName = attTypes[i].getName();

            //if (attTypes[i].isGeometry() && !atts.contains(attName)) {
            if (!atts.contains(attName)) {
                atts.add(attName);
                LOGGER.fine("added attribute " + attName);
            }
        }

        ftsAttributes = new String[atts.size()];
        atts.toArray(ftsAttributes);

        return ftsAttributes;
    }

    /**
     * Creates the bounding box filters (one for each geometric attribute) needed to query a
     * <code>MapLayer</code>'s feature source to return just the features for the target
     * rendering extent
     * 
     * @param schema the layer's feature source schema
     * @param attributes set of needed attributes
     * @param bbox the expression holding the target rendering bounding box
     * @return an or'ed list of bbox filters, one for each geometric attribute in
     *         <code>attributes</code>. If there are just one geometric attribute, just returns
     *         its corresponding <code>GeometryFilter</code>.
     * @throws IllegalFilterException if something goes wrong creating the filter
     */
    private Filter createBBoxFilters( FeatureType schema, String[] attributes, BBoxExpression bbox )
            throws IllegalFilterException {
        Filter filter = null;

        for( int j = 0; j < attributes.length; j++ ) {
            AttributeType attType = schema.getAttributeType(attributes[j]);
            
            //DJB: added this for better error messages!
            if (attType == null)
            	throw new IllegalFilterException("Could not find '"+attributes[j]+"' in the FeatureType ("+schema.getTypeName()+")");

            if (attType.isGeometry()) {
                GeometryFilter gfilter = filterFactory.createGeometryFilter(Filter.GEOMETRY_BBOX);

                // TODO: how do I get the full xpath of an attribute should
                // feature composition be used?
                Expression left = filterFactory
                        .createAttributeExpression(schema, attType.getName());
                gfilter.addLeftGeometry(left);
                gfilter.addRightGeometry(bbox);

                if (filter == null) {
                    filter = gfilter;
                } else {
                    filter = filter.or(gfilter);
                }
            }
        }

        return filter;
    }

    /**
     * Performs the actual rendering process to the graphics context set in setOutput.
     * <p>
     * The style parameter controls the appearance features. Rules within the style object may cause
     * some features to be rendered multiple times or not at all.
     * </p>
     * 
     * @param features the feature collection to be rendered
     * @param map Controls the full extent of the input space. Used in the calculation of scale.
     * @param s A style object. Contains a set of FeatureTypeStylers that are to be applied in order
     *        to control the rendering process.
     */
    public void render( FeatureCollection features, Envelope map, Style s ) {
        if (outputGraphics == null) {
            LOGGER.info("renderer passed null graphics");

            return;
        }

        // reset the abort flag
        renderingStopRequested = false;

        long startTime = 0;

        if (LOGGER.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
        }

        mapExtent = map;

        // set up the affine transform and calculate scale values
        AffineTransform at = worldToScreenTransform(mapExtent, screenSize);

        /*
         * If we are rendering to a component which has already set up some form of transformation
         * then we can concatenate our transformation to it. An example of this is the ZoomPane
         * component of the swinggui module.
         */
        // if (concatTransforms) {
        // outputGraphics.getTransform().concatenate(at);
        // } else {
        // outputGraphics.setTransform(at);
        // }
        scaleDenominator = 1 / outputGraphics.getTransform().getScaleX();

        // extract the feature type stylers from the style object and process them
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();

        try {
            processStylers(outputGraphics, DataUtilities.results(features), featureStylers, at,
                    null);
        } catch (IOException ioe) {
            fireErrorEvent(new Exception("I/O error while rendering the layer" ,ioe));
        } catch (IllegalAttributeException iae) {
            fireErrorEvent(new Exception("Illegal attribute exception while rendering the layer" ,iae));
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            long endTime = System.currentTimeMillis();
            double elapsed = (endTime - startTime) / 1000.0;
        }
    }

    /**
     * Sets up the affine transform
     * 
     * @param mapExtent the map extent
     * @param screenSize the screen size
     * @return a transform that maps from real world coordinates to the screen
     */
    public AffineTransform worldToScreenTransform( Envelope mapExtent, Rectangle screenSize ) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX;
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();

        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);

        return at;
    }

    /**
     * Converts a coordinate expressed on the device space back to real world coordinates
     * 
     * @param x horizontal coordinate on device space
     * @param y vertical coordinate on device space
     * @param map The map extent
     * @return The correspondent real world coordinate
     */
    public Coordinate pixelToWorld( int x, int y, Envelope map ) {
        if (outputGraphics == null) {
            LOGGER.info("no graphics yet deffined");

            return null;
        }

        // set up the affine transform and calculate scale values
        AffineTransform at = worldToScreenTransform(map, screenSize);

        /*
         * If we are rendering to a component which has already set up some form of transformation
         * then we can concatenate our transformation to it. An example of this is the ZoomPane
         * component of the swinggui module.
         */
        if (concatTransforms) {
            outputGraphics.getTransform().concatenate(at);
        } else {
            outputGraphics.setTransform(at);
        }

        try {
            Point2D result = at.inverseTransform(new java.awt.geom.Point2D.Double(x, y),
                    new java.awt.geom.Point2D.Double());
            Coordinate c = new Coordinate(result.getX(), result.getY());

            return c;
        } catch (Exception e) {
            fireErrorEvent(e);
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
     * Applies each feature type styler in turn to all of the features. This perhaps needs some
     * explanation to make it absolutely clear. featureStylers[0] is applied to all features before
     * featureStylers[1] is applied. This can have important consequences as regards the painting
     * order.
     * <p>
     * In most cases, this is the desired effect. For example, all line features may be rendered
     * with a fat line and then a thin line. This produces a 'cased' effect without any strange
     * overlaps.
     * </p>
     * <p>
     * This method is internal and should only be called by render.
     * </p>
     * <p>
     * </p>
     * 
     * @param graphics DOCUMENT ME!
     * @param features An array of features to be rendered
     * @param featureStylers An array of feature stylers to be applied
     * @param at DOCUMENT ME!
     * @param destinationCrs - The destination CRS, or null if no reprojection is required
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    private void processStylers( final Graphics2D graphics, final FeatureResults features,
            final FeatureTypeStyle[] featureStylers, AffineTransform at,
            CoordinateReferenceSystem destinationCrs ) throws IOException,
            IllegalAttributeException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("processing " + featureStylers.length + " stylers");
        }

        LiteShape2 shape = createPath(null, at);
        transformMap = new HashMap();

        for( int i = 0; i < featureStylers.length; i++ ) {
		        if (LOGGER.isLoggable(Level.FINE)) {
		            LOGGER.fine("processing style " + i);
		        }

            FeatureTypeStyle fts = featureStylers[i];

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
            // TODO: find a better way to declare the scale ranges so that we
            // get style caching also between multiple rendering runs
            NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
            FeatureReader reader = features.reader();
            while( true ) {
                try {

                    if (renderingStopRequested) {
                        break;
                    }
                    
                    if (!reader.hasNext()) {
                        break;
                    }

                    boolean doElse = true;

						        if (LOGGER.isLoggable(Level.FINER)) {
						            LOGGER.fine("trying to read Feature ...");
						        }

                    Feature feature = reader.next();

						        if (LOGGER.isLoggable(Level.FINEST)) {
						            LOGGER.finest("... done: " + feature.toString());
						        }

                    String typeName = feature.getFeatureType().getTypeName();

						        if (LOGGER.isLoggable(Level.FINER)) {
						            LOGGER.fine("... done: " + typeName);
						        }

                    if ((typeName != null)
                            && (feature.getFeatureType().isDescendedFrom(null,
                                    fts.getFeatureTypeName()) || typeName.equalsIgnoreCase(fts
                                    .getFeatureTypeName()))) {
                        // applicable rules
                        for( Iterator it = ruleList.iterator(); it.hasNext(); ) {

                            Rule r = (Rule) it.next();

										        if (LOGGER.isLoggable(Level.FINER)) {
										            LOGGER.finer("applying rule: " + r.toString());
										        }

//                            // if this rule applies
//                            if (isWithInScale(r) && !r.hasElseFilter()) {
												        if (LOGGER.isLoggable(Level.FINER)) {
												            LOGGER.finer("this rule applies ...");
												        }
                            	
                            		// if( r != null ) {
		                                Filter filter = r.getFilter();
		
		                                if ((filter == null) || filter.contains(feature)) {
		                                    doElse = false;

																        if (LOGGER.isLoggable(Level.FINER)) {
																            LOGGER.finer("processing Symobolizer ...");
																        }
		
		                                    Symbolizer[] symbolizers = r.getSymbolizers();
		                                    processSymbolizers(graphics, feature, symbolizers, scaleRange,
		                                            at, destinationCrs);
		
																        if (LOGGER.isLoggable(Level.FINER)) {
																            LOGGER.finer("... done!");
																        }
		                                }
                            		// }
//                            }
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

                                processSymbolizers(graphics, feature, symbolizers, scaleRange,
                                        at, destinationCrs);

												        if (LOGGER.isLoggable(Level.FINER)) {
												            LOGGER.finer("... done!");
												        }
                            }
                        }
                    }

						        if (LOGGER.isLoggable(Level.FINER)) {
						            LOGGER.finer("feature rendered event ...");
						        }

                    fireFeatureRenderedEvent(feature);
                } catch (Exception e) {
                    fireErrorEvent(e);
                }
            }

            reader.close();
            
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
     * @param shape
     * @param destinationCrs
     * @throws TransformException
     * @throws FactoryException 
     */
    private void processSymbolizers( final Graphics2D graphics, final Feature feature,
            final Symbolizer[] symbolizers, Range scaleRange, AffineTransform at,
            CoordinateReferenceSystem destinationCrs ) throws TransformException, FactoryException {

    	LiteShape2 shape=createPath(null, at);
        for( int m = 0; m < symbolizers.length; m++ ) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("applying symbolizer " + symbolizers[m]);
            }

            if (symbolizers[m] instanceof RasterSymbolizer) {
                AffineTransform tempTransform = graphics.getTransform();
                graphics.setTransform(shape.getAffineTransform());
                renderRaster(graphics, feature, (RasterSymbolizer) symbolizers[m]);
                graphics.setTransform(tempTransform);
            } else{
                Geometry g = findGeometry(feature, symbolizers[m]);
                CoordinateReferenceSystem crs = findGeometryCS(feature, symbolizers[m]);
                MathTransform2D transform = null;

                if (canTransform) {
                    try {
                        transform = getMathTransform(crs, destinationCrs, shape
                                .getAffineTransform());
                    } catch (Exception e) {
                        // fall through
                    }
                }

                if (transform != null) {
                	shape = getTransformedShape(g, transform);
                } else {
                    shape.setGeometry(g);
                }
                if( symbolizers[m] instanceof TextSymbolizer ){
                	labelCache.put((TextSymbolizer) symbolizers[m], feature, shape, scaleRange);
                }
                else{
                    Style2D style = styleFactory.createStyle(feature, symbolizers[m], scaleRange);
                    painter.paint(graphics, shape, style, scaleDenominator);
                }

            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param g
     * @param transform
     * @return
     * @throws TransformException
     * @throws FactoryException 
     */
    private LiteShape2 getTransformedShape( Geometry g, MathTransform2D transform )
            throws TransformException, FactoryException {
        LiteShape2 shape = new LiteShape2(g, null, transform, false);
        return shape;
    }

    /**
     * Computes the math transform from the source CRS to the destination CRS. Since this is
     * expensive, we keep a cache of coordinate transformations during the rendering process
     * 
     * @param sourceCrs
     * @param destinationCrs
     * @param at DOCUMENT ME!
     * @return
     * @throws CannotCreateTransformException
     * @throws FactoryException
     * @throws OperationNotFoundException
     */
    private MathTransform2D getMathTransform( CoordinateReferenceSystem sourceCrs,
            CoordinateReferenceSystem destinationCrs, AffineTransform at )
            throws OperationNotFoundException, FactoryException {
        MathTransform2D transform = (MathTransform2D) transformMap.get(sourceCrs);

        if (transform != null) {
            return transform;
        }

        if ((sourceCrs == null) || (destinationCrs == null)) { // no transformation possible

            return null;
        }

        transform = (MathTransform2D) operationFactory.createOperation(sourceCrs, destinationCrs)
                .getMathTransform();

        if (transform != null) {
            transform = (MathTransform2D) operationFactory.getMathTransformFactory()
                    .createConcatenatedTransform(
                            transform,
                            operationFactory.getMathTransformFactory().createAffineTransform(
                                    new GeneralMatrix(at)));
        } else {
            transform = (MathTransform2D) operationFactory.getMathTransformFactory()
                    .createAffineTransform(new GeneralMatrix(at));
        }

        transformMap.put(sourceCrs, transform);

        return transform;
    }

    /**
     * Renders a grid coverage on the device. At the time being, the symbolizer is ignored and the
     * renderer tries to depict the non geophysics version of the grid coverage on the device, and
     * falls back on the geophysics one if the former fails
     * 
     * @param graphics DOCUMENT ME!
     * @param feature the feature that contains the GridCoverage. The grid coverage must be
     *        contained in the "grid" attribute
     * @param symbolizer The raster symbolizer
     * @task make it follow the symbolizer
     */
    private void renderRaster( Graphics2D graphics, Feature feature, RasterSymbolizer symbolizer ) {
        LOGGER.fine("rendering Raster for feature " + feature.toString() + " - " + feature.getAttribute("grid") );
        GridCoverage grid = (GridCoverage) feature.getAttribute("grid");
        GridCoverageRenderer gcr = new GridCoverageRenderer(grid);
        gcr.paint(graphics);
        LOGGER.fine("Raster rendered");
    }

    /**
     * Finds the geometric attribute requested by the symbolizer
     * 
     * @param f The feature
     * @param s The symbolizer
     * @return The geometry requested in the symbolizer, or the default geometry if none is
     *         specified
     */
    private com.vividsolutions.jts.geom.Geometry findGeometry( Feature f, Symbolizer s ) {
        String geomName = getGeometryPropertyName(s);

        // get the geometry
        Geometry geom;

        if (geomName == null) {
            geom = f.getDefaultGeometry();
        } else {
            geom = (com.vividsolutions.jts.geom.Geometry) f.getAttribute(geomName);
        }

        // if the symbolizer is a point or text symbolizer generate a suitable location to place the
        // point in order to avoid recomputing that location at each rendering step
        if ((s instanceof PointSymbolizer 
//        		|| s instanceof TextSymbolizer
        		)
                && !((geom instanceof Point) || (geom instanceof MultiPoint))) {
            if (geom instanceof LineString && !(geom instanceof LinearRing)) {
                // use the mid point to represent the point/text symbolizer anchor
                Coordinate[] coordinates = geom.getCoordinates();
                Coordinate start = coordinates[0];
                Coordinate end = coordinates[1];
                Coordinate mid = new Coordinate((start.x + end.x) / 2, (start.y + end.y) / 2);
                geom = geom.getFactory().createPoint(mid);
            } else {
                // otherwise use the centroid of the polygon
                geom = geom.getCentroid();
            }
        }

        return geom;
    }

    /**
     * Finds the geometric attribute coordinate reference system
     * 
     * @param f The feature
     * @param s The symbolizer
     * @return The geometry requested in the symbolizer, or the default geometry if none is
     *         specified
     */
    private org.opengis.referencing.crs.CoordinateReferenceSystem findGeometryCS( Feature f,
            Symbolizer s ) {
        String geomName = getGeometryPropertyName(s);

        if (geomName != null) {
            return ((GeometryAttributeType) f.getFeatureType().getAttributeType(geomName))
                    .getCoordinateSystem();
        } else {
            return ((GeometryAttributeType) f.getFeatureType().getDefaultGeometry())
                    .getCoordinateSystem();
        }
    }

    private String getGeometryPropertyName( Symbolizer s ) {
        String geomName = null;

        // TODO: fix the styles, the getGeometryPropertyName should probably be moved into an
        // interface...
        if (s instanceof PolygonSymbolizer) {
            geomName = ((PolygonSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof PointSymbolizer) {
            geomName = ((PointSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof LineSymbolizer) {
            geomName = ((LineSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof TextSymbolizer) {
            geomName = ((TextSymbolizer) s).getGeometryPropertyName();
        }

        return geomName;
    }

    /**
     * Convenience method. Converts a Geometry object into a Shape
     * 
     * @param geom The Geometry object to convert
     * @param at DOCUMENT ME!
     * @return A GeneralPath that is equivalent to geom
     */
    private LiteShape2 createPath( final Geometry geom, final AffineTransform at ) {
	    	if (generalizationDistance > 0) {
	            return new LiteShape2(geom, at, true, generalizationDistance);
	        } else {
	            return new LiteShape2(geom, at, false);
	        }
    }

    /**
     * Getter for property interactive.
     * 
     * @return Value of property interactive.
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Sets the interactive status of the renderer. An interactive renderer won't wait for long
     * image loading, preferring an alternative mark instead
     * 
     * @param interactive new value for the interactive property
     */
    public void setInteractive( boolean interactive ) {
        this.interactive = interactive;
    }

    /**
     * <p>
     * Returns true if the optimized data loading is enabled, false otherwise.
     * </p>
     * <p>
     * When optimized data loading is enabled, lite renderer will try to load only the needed
     * feature attributes (according to styles) and to load only the features that are in (or
     * overlaps with)the bounding box requested for painting
     * </p>
     * 
     * @return
     */
    public boolean isOptimizedDataLoadingEnabled() {
        return optimizedDataLoadingEnabled;
    }

    /**
     * Enables/disable optimized data loading
     * 
     * @param b
     */
    public void setOptimizedDataLoadingEnabled( boolean b ) {
        optimizedDataLoadingEnabled = b;
    }

    /**
     * Returns the generalization distance in the screen space.
     * 
     * @return
     */
    public double getGeneralizationDistance() {
        return generalizationDistance;
    }

    /**
     * <p>
     * Sets the generalizazion distance in the screen space.
     * </p>
     * <p>
     * Default value is 1, meaning that two subsequent points are collapsed to one if their on
     * screen distance is less than one pixel
     * </p>
     * <p>
     * Set the distance to 0 if you don't want any kind of generalization
     * </p>
     * 
     * @param d
     */
    public void setGeneralizationDistance( double d ) {
        generalizationDistance = d;
    }

    /**
     * @param enabled
     */
    public void setMemoryPreloadingEnabled( boolean enabled ) {
        this.memoryPreloadingEnabled = enabled;
        if( !enabled )
            indexedFeatureResults=null;
    }
    
    public void setRenderingHints(RenderingHints hints){
        this.hints=hints;
    }
    
    
    public void setRenderingHint(RenderingHints.Key key, Object value){
        if( hints==null )
            hints=new RenderingHints( key, value);
        else
            hints.put(key, value);
    }
    
    /**
     * By default ignores all feature renderered events and logs all exceptions as severe.
     */
    private static class DefaultRenderListener implements  RenderListener{
        /**
         * @see org.geotools.renderer.lite.RenderListener#featureRenderer(org.geotools.feature.Feature)
         */
        public void featureRenderer( Feature feature ) {
            //do nothing.
        }

        /**
         * @see org.geotools.renderer.lite.RenderListener#errorOccurred(java.lang.Exception)
         */
        public void errorOccurred( Exception e ) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        
    }
}
