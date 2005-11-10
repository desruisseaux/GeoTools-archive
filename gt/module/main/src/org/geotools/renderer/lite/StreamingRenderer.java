/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.geotools.renderer.lite;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.util.Range;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.crs.ForceCoordinateSystemFeatureReader;
import org.geotools.factory.Hints;
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
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.styling.ColorMapEntry;
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
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * A streaming implementation of the GTRenderer interface. 
 * <ul>
 * <li>The code is relatively simple to understand, so it can be used as a simple example of an SLD
 * compliant rendering code</li>
 * <li>Uses as little memory as possible</li>
 * </ul>
 * Use this class if you need a stateless renderer that provides low memory footprint and decent
 * rendering performance on the first call but don't need good optimal performance on subsequent
 * calls on the same data. Notice: for the time being, this class doesn't support GridCoverage
 * stylers, that will be rendered using the non geophisics version of the GridCoverage, if
 * available, with the geophisics one, otherwise.
 *
 * @author James Macgill
 * @author dblasby
 * @author jessie eichar
 * @author Andrea Aime
 *
 * @version $Id$
 */
public class StreamingRenderer implements GTRenderer {
    
    public HashMap symbolizerAssociationHT = new HashMap(); //associate a symbolizer with some data
    
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
    private final static CoordinateOperationFactory operationFactory;
    static {
        Hints hints=new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        operationFactory=FactoryFinder.getCoordinateOperationFactory(hints);
    }
    private final static MathTransformFactory mathTransformFactory;
    static {
        Hints hints=new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        mathTransformFactory=FactoryFinder.getMathTransformFactory(hints);
    }
    
    /**
     *  This is used to control what type of image the system will draw into.
     */
    public int defaultImageType = BufferedImage.TYPE_4BYTE_ABGR;
    
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
    
    
    private IndexedFeatureResults indexedFeatureResults;
    
    private ListenerList renderListeners= new ListenerList();
    
    private RenderingHints java2dHints;
    
    private boolean optimizedDataLoadingEnabledDEFAULT = false;
    private boolean memoryPreloadingEnabledDEFAULT = false;
    
    
    /**
     * Activates bbox and attribute filtering optimization, that works properly only if the input
     * feature sources really contain just one feature type. This may not be the case if the feature
     * source is based on a generic feature collection
     */
    /**
     * "optimizedDataLoadingEnabled" - Boolean  yes/no (see default optimizedDataLoadingEnabledDEFAULT)
     * "memoryPreloadingEnabled"     - Boolean  yes/no (see default memoryPreloadingEnabledDEFAULT)
     */
    private Map rendererHints = null;
    
    
    /**
     * Creates a new instance of LiteRenderer without a context. Use it only to gain access to
     * utility methods of this class or if you want to render random feature collections instead of
     * using the map context interface
     */
    public StreamingRenderer() {
        addRenderListener(DEFAULT_LISTENER);
    }
    
    
    /**
     * Sets the flag which controls behaviour for applying affine transformation to the graphics
     * object.
     *
     * @param flag If true then the transform will be concatenated to the existing transform. If
     *        false it will be replaced.
     */
    private void setConcatTransforms( boolean flag ) {
        concatTransforms = flag;
    }
    
    /**
     * Returns the amount of time the renderer waits for loading an external image before giving up
     * and examining the other images in the Graphic object
     *
     * @return the timeout in milliseconds
     */
    private static long getImageLoadingTimeout() {
        return ImageLoader.getTimeout();
    }
    
    /**
     * Sets the maximum time to wait for getting an external image. Set it to -1 to wait
     * undefinitely. The default value is 10 seconds
     *
     * @param newTimeout the new timeout value in milliseconds
     */
    private static void setImageLoadingTimeout( long newTimeout ) {
        ImageLoader.setTimeout(newTimeout);
    }
    
    /**
     * Flag which controls behaviour for applying affine transformation to the graphics object.
     *
     * @return a boolean flag. If true then the transform will be concatenated to the existing
     *         transform. If false it will be replaced.
     */
    private boolean getConcatTransforms() {
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
    private void setOutput( Graphics g, Rectangle bounds ) {
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
    private void setScaleDenominator( double scaleDenominator ) {
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
    
    /** Renders features based on the map layers and their styles as specified
     * in the map context using <code>setContext</code>.
     * <p/>
     * This version of the method assumes that the size of the output area
     * and the transformation from coordinates to pixels are known.
     * The latter determines the map scale. The viewport (the visible
     * part of the map) will be calculated internally.
     *
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg: pixels).
     * @param worldToScreen A transform which converts World coordinates to Screen coordinates.
     * @task Need to check if the Layer CoordinateSystem is different to the BoundingBox rendering
     *       CoordinateSystem and if so, then transform the coordinates.
     */
    public void paint( Graphics2D graphics, Rectangle paintArea, AffineTransform worldToScreen ) {
        if (worldToScreen == null || paintArea == null) {
            LOGGER.info("renderer passed null arguments");
            return;
        } //Other arguments get checked later
        // First, create the bbox in real world coordinates
        Envelope mapArea;
        try {
            mapArea = RendererUtilities.createMapEnvelope(paintArea, worldToScreen);
            this.outputGraphics = graphics;
            paint(graphics, paintArea, mapArea, worldToScreen);
        } catch (NoninvertibleTransformException e) {
            fireErrorEvent(new Exception("Can't create pixel to world transform", e));
        }
    }
    
    /** Renders features based on the map layers and their styles as specified
     * in the map context using <code>setContext</code>.
     * <p/>
     * This version of the method assumes that the area of the visible part
     * of the map and the size of the output area are known. The transform
     * between the two is calculated internally.
     *
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg: pixels).
     * @param envelope the map's visible area (viewport) in map coordinates.
     */
    public void paint( Graphics2D graphics, Rectangle paintArea, Envelope mapArea ) {
        if (mapArea == null || paintArea == null) {
            LOGGER.info("renderer passed null arguments");
            return;
        } //Other arguments get checked later
        paint(graphics, paintArea, mapArea, RendererUtilities.worldToScreenTransform(mapArea, paintArea));
    }
    
    /**
     * Renders features based on the map layers and their styles as specified
     * in the map context using <code>setContext</code>.
     * <p/>
     * This version of the method assumes that paint area, enelope and
     * worldToScreen transform are already computed. Use this method to
     * avoid recomputation. <b>Note however that no check is performed that
     * they are really in sync!<b/>
     *
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg: pixels).
     * @param envelope the map's visible area (viewport) in map coordinates.
     * @param worldToScreen A transform which converts World coordinates to Screen coordinates.
     */
    public void paint( Graphics2D graphics, Rectangle paintArea,
            Envelope mapArea, AffineTransform worldToScreen) {
        //Check for null arguments, recompute missing ones if possible
        if (graphics == null || paintArea == null) {
            LOGGER.info("renderer passed null arguments");
            return;
        } else if (mapArea == null && paintArea == null) {
            LOGGER.info("renderer passed null arguments");
            return;
        } else if (mapArea == null){
            try{
                mapArea = RendererUtilities.createMapEnvelope(
                        paintArea, worldToScreen);
            } catch (NoninvertibleTransformException e) {
                //TODO: Throw error here as in the other paint method?
                LOGGER.info("renderer passed null arguments");
                return;
            }
        } else if (worldToScreen == null){
            worldToScreen = RendererUtilities.worldToScreenTransform(
                    mapArea, paintArea);
        }
        
        error = 0;
        if ( java2dHints != null )
            graphics.setRenderingHints(java2dHints);
        
        // reset the abort flag
        renderingStopRequested = false;
        AffineTransform at = worldToScreen;
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
        
        //setScaleDenominator(1 / at.getScaleX()); //DJB old method
        
        try{
            setScaleDenominator(  RendererUtilities.calculateScale(mapArea,context.getCoordinateReferenceSystem(),paintArea.width,paintArea.height,90));// 90 = OGC standard DPI (see SLD spec page 37)
        } catch (Exception e) // probably either (1) no CRS (2) error xforming
        {
            setScaleDenominator(1 / at.getScaleX()); //DJB old method - the best we can do
        }
        
        MapLayer[] layers = context.getLayers();
        // get detstination CRS
        CoordinateReferenceSystem destinationCrs = context.getCoordinateReferenceSystem();
        
        labelCache.start();
        for( int i = 0; i < layers.length; i++ ) //DJB: for each layer (ie. one SLD or one LAYER= STYLE= for WMS request)
        {
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
                // DJB: get a featureresults (so you can get a feature reader) for the data
                FeatureResults results = queryLayer(currLayer, mapArea, destinationCrs);
                
                // extract the feature type stylers from the style object
                // and process them
                this.screenSize = paintArea;
                processStylers(graphics, results, currLayer.getStyle().getFeatureTypeStyles(), at,
                        context.getCoordinateReferenceSystem(),
                        currLayer.getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem()); //src CRS
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
    /*
     * Default visibility for testing purposes
     */
    FeatureResults queryLayer( MapLayer currLayer, Envelope envelope,
            CoordinateReferenceSystem destinationCrs ) throws IllegalFilterException, IOException,
            IllegalAttributeException {
        FeatureResults results = null;
        FeatureSource featureSource = currLayer.getFeatureSource();
        FeatureType schema = featureSource.getSchema();
        Query query = Query.ALL;
        MathTransform transform =null;
        
        if (isOptimizedDataLoadingEnabled()) {
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
                    transform = operationFactory.createOperation(destinationCrs,sourceCrs).getMathTransform();
                    if (transform != null && !transform.isIdentity()) {
                        // Envelope eee=  JTS.transform(envelope, transform);// this is the old way
                        //10 = make 10 points on each side of the bbox & transform the polygon
                        envelope = JTS.transform(envelope, transform,10); // this will usually be a "bigger" bbox
                    } else
                        transform = null; //reset transform
                }
                
                Filter filter = null;
                if (!isMemoryPreloadingEnabled()) {
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
                    q.setFilter( Filter.NONE );
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
        
        if (!(query instanceof DefaultQuery))
            query = new DefaultQuery(query);
        
        ((DefaultQuery)query).setCoordinateSystem(
                currLayer.getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem());
        
        
        if (isMemoryPreloadingEnabled()) {
            // TODO: attache a feature listener, we must erase the memory cache if
            // anything changes in the data store
            if (indexedFeatureResults == null) {
                indexedFeatureResults = new IndexedFeatureResults(featureSource.getFeatures(query));
            }
            indexedFeatureResults.setQueryBounds(envelope);
            results = indexedFeatureResults;
        } else {   // insert a debug point here to check your query
            results = featureSource.getFeatures(query);
        }
        
        return results;
    }
    
    /**
     * @return
     */
    private boolean isMemoryPreloadingEnabled() {
        if (rendererHints == null)
            return memoryPreloadingEnabledDEFAULT;
        Boolean result = (Boolean) rendererHints.get("memoryPreloadingEnabled");
        if (result == null)
            return memoryPreloadingEnabledDEFAULT;
        return result.booleanValue();
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
            
            //DJB: This geometry check was commented out.  I think it should actually be back in or
            //     you get ALL the attributes back, which isnt what you want.
            //ALX: For rasters I need even the "grid" attribute.
            if ((attTypes[i] instanceof GeometryAttributeType || attTypes[i].getName().equalsIgnoreCase("grid")) && !atts.contains(attName)) {
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
            
            if (attType instanceof GeometryAttributeType) {
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
    private void render( FeatureCollection features, Envelope map, Style s ) {
        if (outputGraphics == null) {
            LOGGER.info("renderer passed null graphics");
            
            return;
        }
        
        // reset the abort flag
        renderingStopRequested = false;
        
        
        mapExtent = map;
        
        // set up the affine transform and calculate scale values
        AffineTransform at = RendererUtilities.worldToScreenTransform(mapExtent, screenSize);
        
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
                    null,null);
        } catch (IOException ioe) {
            fireErrorEvent(new Exception("I/O error while rendering the layer" ,ioe));
        } catch (IllegalAttributeException iae) {
            fireErrorEvent(new Exception("Illegal attribute exception while rendering the layer" ,iae));
        }
    }
       
    /**
     * Converts a coordinate expressed on the device space back to real world coordinates
     *
     * @param x horizontal coordinate on device space
     * @param y vertical coordinate on device space
     * @param map The map extent
     * @return The correspondent real world coordinate
     */
    private Coordinate pixelToWorld( int x, int y, Envelope map ) {
        if (outputGraphics == null) {
            LOGGER.info("no graphics yet deffined");
            
            return null;
        }
        
        // set up the affine transform and calculate scale values
        AffineTransform at = RendererUtilities.worldToScreenTransform(map, screenSize);
        
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
     * creates a list of LiteFeatureTypeStyles
     *   a) out-of-scale rules removed
     *   b) incompatible FeatureTypeStyles removed
     *
     *
     * @param featureStylers
     * @param features
     * @return
     * @throws Exception
     */
    private ArrayList createLiteFeatureTypeStyles( FeatureTypeStyle[] featureStylers,FeatureResults features, Graphics2D graphics) throws IOException {
        ArrayList result  = new ArrayList();
        
        int itemNumber =0;
        
        for( int i = 0; i < featureStylers.length; i++ ) //DJB: for each FeatureTypeStyle in the SLD (each on is drawn indpendently)
        {
            FeatureTypeStyle fts = featureStylers[i];
            String typeName = features.getSchema().getTypeName();
            
            if ((typeName != null)
            && (features.getSchema().isDescendedFrom(null,
                    fts.getFeatureTypeName()) || typeName.equalsIgnoreCase(fts
                    .getFeatureTypeName()))) {
                //DJB: this FTS is compatible with this FT.
                
                // get applicable rules at the current scale
                Rule[] rules = fts.getRules();
                ArrayList ruleList = new ArrayList();
                ArrayList elseRuleList = new ArrayList();
                
                for( int j = 0; j < rules.length; j++ ) {
                    Rule r = rules[j];
                    
                    if (isWithInScale(r)) {
                        if (r.hasElseFilter()) {
                            elseRuleList.add(r);
                        } else {
                            ruleList.add(r);
                        }
                    }
                }
                if ( (ruleList.size() == 0) && (elseRuleList.size()==0) )
                    continue;  //DJB: optimization - nothing to render, dont do anything!!
                
                LiteFeatureTypeStyle lfts =null;
                if (itemNumber == 0) //we can optimize this one!
                {
                    lfts = new LiteFeatureTypeStyle(graphics,ruleList,elseRuleList);
                } else {
                    BufferedImage image = new BufferedImage(screenSize.width, screenSize.height,defaultImageType);
                    lfts = new LiteFeatureTypeStyle(image,graphics.getTransform(),ruleList,elseRuleList,java2dHints);
                }
                result.add(lfts);
                itemNumber++;
                
            }
        }
        
        return result;
    }
    
    private FeatureReader getReader(FeatureResults features,CoordinateReferenceSystem sourceCrs) throws IOException {
        FeatureReader reader = features.reader();
        
        //DJB: dont do reprojection here - do it after decimation
        //     but we ensure that the reader is producing geometries with the correct CRS
        //NOTE: it, by default, produces ones that are are tagged with the CRS of the datastore, which
        //      maybe incorrect.
        //      The correct value is in sourceCrs.
        
        // this is the reader's CRS
        CoordinateReferenceSystem rCS = reader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
        
        // sourceCrs == source's real SRS
        
        //if we need to recode the incoming geometries
        
        if (rCS != sourceCrs)  //not both null or both EXACTLY the same CRS object
        {
            if (sourceCrs != null) //dont re-tag to null, keep the DataStore's CRS (this shouldnt really happen)
            {
                // if the datastore is producing null CRS, we recode.
                // if the datastore's CRS != real CRS, then we recode
                if ( ( rCS ==null) || (!rCS .equals( sourceCrs) )) {
                    //need to retag the features
                    try{
                        reader = new ForceCoordinateSystemFeatureReader(reader,sourceCrs );
                    } catch(Exception ee) {
                        ee.printStackTrace(); // do nothing but warn user
                    }
                }
            }
        }
        return reader;
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
    private void processStylers(final Graphics2D graphics,
            final FeatureResults features,
            final FeatureTypeStyle[] featureStylers,
            AffineTransform at,
            CoordinateReferenceSystem destinationCrs,
            CoordinateReferenceSystem sourceCrs
            )
            throws IOException,  IllegalAttributeException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("processing " + featureStylers.length + " stylers for "+features.getSchema().getTypeName());
        }
        
        transformMap = new HashMap();
        NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
        
        
        symbolizerAssociationHT = new HashMap();
        ArrayList lfts= createLiteFeatureTypeStyles(featureStylers,features,graphics);
        if (lfts.size() ==0)
            return; // nothing to do
        FeatureReader reader = getReader(features,sourceCrs);
        int n_lfts = lfts.size();
        LiteFeatureTypeStyle[]  fts_array = (LiteFeatureTypeStyle[]) lfts.toArray( new LiteFeatureTypeStyle[n_lfts] );
        
        
        try{
            
            while( true ) {
                try {
                    if (renderingStopRequested) {
                        break;
                    }
                    if (!reader.hasNext()) {
                        break;
                    }
                    Feature feature = reader.next(); // read the feature
                    for (int t=0;t<n_lfts;t++) {
                        process(feature,fts_array[t],scaleRange,at,destinationCrs);  //draw the feature on the image(s)
                    }
                } catch (Exception e) {
                    fireErrorEvent(e);
                }
            }
            
        } finally {
            reader.close();
        }
        //have to re-form the image now.
        // graphics.setTransform( new AffineTransform() );
        for (int t=0;t<n_lfts;t++) {
            if (fts_array[t].myImage != null) // this is the case for the first one (ie. fts_array[t].graphics == graphics)
            {
                graphics.drawImage(fts_array[t].myImage,0,0,null);
                fts_array[t].graphics.dispose();
            }
        }
        
        
    }
    
    /**
     * @param feature
     * @param style
     */
    final private void process(Feature feature, LiteFeatureTypeStyle style,Range scaleRange,AffineTransform at, CoordinateReferenceSystem destinationCrs) throws TransformException,FactoryException {
        boolean doElse = true;
        Rule[] elseRuleList = style.elseRules;
        Rule[] ruleList = style.ruleList;
        
        Graphics2D graphics = style.graphics;
        
        // applicable rules
        for( int t=0;t<ruleList.length;t++ ) {
            Rule r = ruleList[t];
            Filter filter = r.getFilter();
            
            if ((filter == null) || filter.contains(feature)) {
                doElse = false;
                Symbolizer[] symbolizers = r.getSymbolizers();
                processSymbolizers(graphics, feature, symbolizers, scaleRange,
                        at, destinationCrs);
            }
        }
        
        if (doElse) {
            for( int tt=0;tt<elseRuleList.length;tt++ ) {
                Rule r = elseRuleList[tt];
                Symbolizer[] symbolizers = r.getSymbolizers();
                
                processSymbolizers(graphics, feature, symbolizers, scaleRange,
                        at, destinationCrs);
                
            }
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
        LiteShape2 shape;
        
        for( int m = 0; m < symbolizers.length; m++ ) {
            
            
            if (symbolizers[m] instanceof RasterSymbolizer) {
                AffineTransform tempTransform = graphics.getTransform();
                graphics.setTransform(at);
                renderRaster(graphics, feature, (RasterSymbolizer) symbolizers[m], destinationCrs);
                graphics.setTransform(tempTransform);
            } else {
                Geometry g = findGeometry(feature, symbolizers[m]); //pulls the geometry
                
                SymbolizerAssociation sa = (SymbolizerAssociation) symbolizerAssociationHT.get(symbolizers[m]);
                if (sa==null) {
                    sa= new SymbolizerAssociation();
                    sa.setCRS(findGeometryCS(feature, symbolizers[m]));
                    MathTransform2D transform = null;
                    try {
                        // DJB: this should never be necessary since we've already taken care to make sure the reader is
                        // producing the correct coordinate system
                        transform = getMathTransform(sa.crs, destinationCrs);
                        if (transform != null) {
                            transform = (MathTransform2D) mathTransformFactory
                                    .createConcatenatedTransform(
                                    transform,
                                    mathTransformFactory.createAffineTransform(
                                    new GeneralMatrix(at)));
                        } else {
                            transform = (MathTransform2D) mathTransformFactory
                                    .createAffineTransform(new GeneralMatrix(at));
                        }
                    } catch (Exception e) {
                        // fall through
                    }
                    sa.setXform(transform);
                    symbolizerAssociationHT.put(symbolizers[m],sa);
                }
                
                
                MathTransform2D transform = sa.getXform();
                
                
                
                shape = getTransformedShape(g, transform);
                
                if( symbolizers[m] instanceof TextSymbolizer ){
                    labelCache.put((TextSymbolizer) symbolizers[m], feature, shape, scaleRange);
                } else{
                    Style2D style = styleFactory.createStyle(feature, symbolizers[m], scaleRange);
                    painter.paint(graphics, shape, style, scaleDenominator);
                }
                
            }
        }
        
        fireFeatureRenderedEvent(feature);
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
        
        LiteShape2 shape = new LiteShape2(g, transform, getDecimator(transform), false);
        return shape;
    }
    
    HashMap decimators=new HashMap();
    /**
     * @return
     * @throws org.opengis.referencing.operation.NoninvertibleTransformException
     */
    private Decimator getDecimator(MathTransform2D mathTransform) throws org.opengis.referencing.operation.NoninvertibleTransformException {
        Decimator decimator=(Decimator) decimators.get(mathTransform);
        if( decimator==null ){
            if (mathTransform != null && !mathTransform.isIdentity())
                decimator=new Decimator(mathTransform.inverse());
            else
                decimator=new Decimator(null);
            
            decimators.put(mathTransform, decimator);
        }
        return decimator;
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
            CoordinateReferenceSystem destinationCrs)
            throws OperationNotFoundException, FactoryException {
        MathTransform2D transform = (MathTransform2D) transformMap.get(sourceCrs);
        
        if (transform != null) {
            return transform;
        }
        
        if (((sourceCrs == null) || (destinationCrs == null)) ) { // no transformation possible
            
            return null;
        }
        
        transform = (MathTransform2D) operationFactory.createOperation(sourceCrs, destinationCrs)
        .getMathTransform();
        
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
    private void renderRaster( Graphics2D graphics, Feature feature, RasterSymbolizer symbolizer, CoordinateReferenceSystem destinationCRS ) {
        LOGGER.fine("rendering Raster for feature " + feature.toString() + " - " + feature.getAttribute("grid") );

        GridCoverage2D grid = (GridCoverage2D) feature.getAttribute("grid");
        
        final WritableRaster raster				= grid.getRenderedImage().copyData(null);
        final int numBands						= raster.getNumBands();
        
        final float alpha = getOpacity(symbolizer);
        graphics.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha));
        
        final GridSampleDimension[] targetBands = new GridSampleDimension[numBands];

        final Map[] colorMaps = new Map[numBands];
        for(int band=0; band<numBands; band++) { //TODO get seprated R,G,B colorMaps from symbolizer
            final Map categories 		= getCategories(symbolizer, grid, band);
            colorMaps[band] = categories;

            /**
             * Temporary solution, until the Recolor Operation is ported ...
             */
            targetBands[band] = (GridSampleDimension) transformColormap(
            		band,
					grid.getSampleDimension(band),
					colorMaps
            );
        }

        grid = new GridCoverage2D(
        		grid.getName(), 
				grid.getRenderedImage(), 
				grid.getCoordinateReferenceSystem(), 
				grid.getEnvelope(), 
				targetBands, 
				new GridCoverage[] {grid}, 
				null
		);
        GridCoverageRenderer gcr = new GridCoverageRenderer(grid, destinationCRS);
        gcr.paint(graphics);
        LOGGER.fine("Raster rendered");
    }
    
    private float getOpacity(final RasterSymbolizer sym) {
        float alpha = 1.0f;
        Expression exp = sym.getOpacity();
        if(exp == null) return alpha;
        Object obj = exp.getValue(null);
        if(obj == null) return alpha;
        Number num = null;
        if(obj instanceof Number) num = (Number)obj;
        if(num == null) return alpha;
        return num.floatValue();
    }
    
    private Map getCategories(final RasterSymbolizer sym, final GridCoverage2D grid, final int band) {
        final String[] labels			= getLabels(sym, band);
        final Double[] quantities		= getQuantities(sym, band);
    	final Color[] colors			= getColors(sym, band);

    	final Map categories			= new HashMap();
    	
    	/**
    	 * Checking Categories
    	 */
    	for(int i=0; i<labels.length; i++) {
    		if( !categories.containsKey(labels[i]) ) {
    			categories.put(labels[i], new Color[] {colors[i]});
    		} else {
    			final Color[] oldCmap = (Color[]) categories.get(labels[i]);
    			final int length = oldCmap.length;
    			final Color[] newCmap = new Color[length + 1];
    			System.arraycopy(oldCmap, 0, newCmap, 0, length);
    			newCmap[length] = colors[i];
    			categories.put(labels[i], newCmap);
    		}
    	}

    	return categories;
    }
    
    private String[] getLabels(final RasterSymbolizer sym, final int band) {
    	String[] labels = null;
    	if(sym.getColorMap() != null) {
    		final ColorMapEntry[] colors = sym.getColorMap().getColorMapEntries();
    		final int numColors = colors.length;
    		labels = new String[numColors];
    		for(int ci=0;ci<numColors;ci++) {
    			labels[ci] = colors[ci].getLabel();
    		}
    	}
    	
    	return labels;
    }
    
    private Double[] getQuantities(final RasterSymbolizer sym, final int band) {
    	Double[] quantities = null;
    	if(sym.getColorMap() != null) {
    		final ColorMapEntry[] colors = sym.getColorMap().getColorMapEntries();
    		final int numColors = colors.length;
    		quantities = new Double[numColors];
    		for(int ci=0;ci<numColors;ci++) {
    			Expression exp = colors[ci].getQuantity();
    			if(exp == null) return null;
    	        Object obj = exp.getValue(null);
    	        if(obj == null) return null;
    	        if(obj instanceof String)
    	        	quantities[ci] = Double.valueOf((String)obj);
    	        else if(obj instanceof Double)
    	        	quantities[ci] = (Double)obj;
    	        if(quantities[ci] == null) return null;
    		}
    	}
    	
    	return quantities;
    }
    
    private Color[] getColors(final RasterSymbolizer sym, final int band) {
    	Color[] colorTable = null;
    	if(sym.getColorMap() != null) {
    		final ColorMapEntry[] colors = sym.getColorMap().getColorMapEntries();
    		final int numColors = colors.length;
    		colorTable = new Color[numColors];
    		for(int ci=0;ci<numColors;ci++) {
    			Expression exp = colors[ci].getColor();
    	        if(exp == null) return null;
    	        Object obj = exp.getValue(null);
    	        if(obj == null) return null;
    	        final Double opacity = (colors[ci].getOpacity() != null ?  
    	        		(colors[ci].getOpacity().getValue(null) instanceof String ? 
    	        				Double.valueOf((String)colors[ci].getOpacity().getValue(null)) :
    	        				(Double) colors[ci].getOpacity().getValue(null)) : 
    	        		new Double(1.0));
    	        final Integer intval = Integer.decode((String)obj);
    	    	final int i = intval.intValue();
    	        colorTable[ci] = new Color(
    	        		(i >> 16) & 0xFF, 
						(i >> 8) & 0xFF, 
						i & 0xFF, 
    	        		new Double(Math.ceil(255.0 * opacity.floatValue())).intValue()
				);
    	        if(colorTable[ci] == null) return null;
    		}
    	}
    	
    	return colorTable;
    }
    
    private double[] getExtrema(final RenderedImage image, final int band, final Double NaN) {
        final double[] extrema = new double[2];

        final int nX = image.getWidth();
        final int nY = image.getHeight();
    	double[] aMask = new double[nY * nX]; 
    	image.getData().getSamples(0, 0, nX, nY, band, aMask);
    	DataBufferDouble mbuffer = new DataBufferDouble(aMask, nY * nX);
    	SampleModel mSampleModel = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_DOUBLE, nX, nY, 1);
        SampleModel iSampleModel = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_DOUBLE, nX, nY, 1);
		ColorModel mColorModel =
			PlanarImage.createColorModel(mSampleModel);
        ColorModel iColorModel =
			PlanarImage.createColorModel(iSampleModel);
		Raster mRaster = RasterFactory.createWritableRaster(mSampleModel,
				mbuffer,
				new Point(0,0));
		TiledImage mask = new TiledImage(0, 0, nX, nY, 0, 0,
				mSampleModel, mColorModel);
		mask.setData(mRaster);
		TiledImage img = new TiledImage(0, 0, nX, nY, 0, 0,
				iSampleModel, iColorModel);
		img.setData(mRaster);

		ParameterBlock ePb = new ParameterBlock();
		ePb.addSource(img);
		if( NaN != null )
			ePb.add( new ROI(mask, (new Double(Math.floor(NaN.doubleValue())).intValue()) ) ); //ROI
		RenderedOp op = JAI.create("Extrema", ePb);
		double[][] extremas = (double[][]) op.getProperty("extrema");
		extrema[0] = (!Double.isInfinite(extremas[0][0]) && !Double.isNaN(extremas[0][0]) ? extremas[0][0] : -Double.MAX_VALUE);
		extrema[1] = (!Double.isInfinite(extremas[1][0]) && !Double.isNaN(extremas[1][0]) ? extremas[1][0] : Double.MAX_VALUE);
		
		return extrema;
    }
    
    /**
     * Transform the supplied RGB colors.
     */
    protected SampleDimension transformColormap(final int   band,
                                                SampleDimension dimension,
                                                final Map[] colorMaps)
    {
        if (colorMaps==null || colorMaps.length==0) {
            return dimension;
        }
        boolean changed = false;
        final Map colorMap = colorMaps[Math.min(band, colorMaps.length-1)];
        final List categoryList = ((GridSampleDimension) dimension).getCategories();
        if (categoryList == null) {
            return dimension;
        }
        final Category categories[] = (Category[]) categoryList.toArray();
        for (int j=categories.length; --j>=0;) {
            Category category = categories[j];
            Color[] colors = (Color[]) colorMap.get(category.getName().toString());
            if (colors == null) {
                if (!category.isQuantitative()) {
                    continue;
                }
                colors = (Color[]) colorMap.get(null);
                if (colors == null) {
                    continue;
                }
            }
            final Range range = category.getRange();
            int lower = ((Number) range.getMinValue()).intValue();
            int upper = ((Number) range.getMaxValue()).intValue();
            if (!range.isMinIncluded()) lower++;
            if ( range.isMaxIncluded()) upper++;
            category = category.recolor(colors);
            if (!categories[j].equals(category)) {
                categories[j] = category;
                changed = true;
            }
        }
        return changed ? new GridSampleDimension(categories, dimension.getUnits())
                       : dimension;
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
        
        // if the symbolizer is a point symbolizer generate a suitable location to place the
        // point in order to avoid recomputing that location at each rendering step
        if (s instanceof PointSymbolizer)
            geom = getCentroid(geom); // djb: major simpificatioN
        
        return geom;
    }
    
    /**
     *  Finds the centroid of the input geometry
     *    if input = point, line, polygon  --> return a point that represents the centroid of that geom
     *    if input = geometry collection --> return a multipoint that represents the centoid of each sub-geom
     * @param g
     * @return
     */
    private Geometry getCentroid(Geometry g) {
        if (g instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) g;
            Coordinate[] pts = new Coordinate[gc.getNumGeometries()];
            for (int t=0;t<gc.getNumGeometries();t++) {
                pts[t] = gc.getGeometryN(t).getCentroid().getCoordinate();
            }
            return g.getFactory().createMultiPoint(pts);
        } else {
            return g.getCentroid();
        }
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
     * Getter for property interactive.
     *
     * @return Value of property interactive.
     */
    private boolean isInteractive() {
        return interactive;
    }
    
    /**
     * Sets the interactive status of the renderer. An interactive renderer won't wait for long
     * image loading, preferring an alternative mark instead
     *
     * @param interactive new value for the interactive property
     */
    private void setInteractive( boolean interactive ) {
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
    private boolean isOptimizedDataLoadingEnabled() {
        if (rendererHints == null)
            return optimizedDataLoadingEnabledDEFAULT;
        Boolean result = (Boolean) rendererHints.get("optimizedDataLoadingEnabled");
        if (result == null)
            return optimizedDataLoadingEnabledDEFAULT;
        return result.booleanValue();
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
    private void setGeneralizationDistance( double d ) {
        generalizationDistance = d;
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
    
        /* (non-Javadoc)
         * @see org.geotools.renderer.GTRenderer#setJava2DHints(java.awt.RenderingHints)
         */
    public void setJava2DHints(RenderingHints hints) {
        this.java2dHints = hints;
    }
    
        /* (non-Javadoc)
         * @see org.geotools.renderer.GTRenderer#getJava2DHints()
         */
    public RenderingHints getJava2DHints() {
        return java2dHints;
    }
    
        /* (non-Javadoc)
         * @see org.geotools.renderer.GTRenderer#setRendererHints(java.util.Map)
         */
    public void setRendererHints(Map hints) {
        rendererHints = hints;
    }
    
        /* (non-Javadoc)
         * @see org.geotools.renderer.GTRenderer#getRendererHints()
         */
    public Map getRendererHints() {
        return rendererHints;
    }
    
        /* (non-Javadoc)
         * @see org.geotools.renderer.GTRenderer#setContext(org.geotools.map.MapContext)
         */
    public void setContext(MapContext context) {
        this.context = context;
    }
    
        /* (non-Javadoc)
         * @see org.geotools.renderer.GTRenderer#getContext()
         */
    public MapContext getContext() {
        return context;
    }
}
