/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.lite;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.util.Range;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Source;
import org.geotools.data.collection.ResourceCollection;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.Decimator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.LiteShape2;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.BufferedCoordinateOperationFactory;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleAttributeExtractor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * A streaming implementation of the GTRenderer interface.
 * <ul>
 * <li>The code is relatively simple to understand, so it can be used as a
 * simple example of an SLD compliant rendering code</li>
 * <li>Uses as little memory as possible</li>
 * </ul>
 * Use this class if you need a stateless renderer that provides low memory
 * footprint and decent rendering performance on the first call but don't need
 * good optimal performance on subsequent calls on the same data. Notice: for
 * the time being, this class doesn't support GridCoverage stylers, that will be
 * rendered using the non geophisics version of the GridCoverage, if available,
 * with the geophisics one, otherwise.
 * 
 * <p>
 * At the moment the streaming renderer is not thread safe
 * 
 * @author James Macgill
 * @author dblasby
 * @author jessie eichar
 * @author Simone Giannecchini
 * @author Andrea Aime
 * @author Alessio Fabiani
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/render/src/org/geotools/renderer/lite/StreamingRenderer.java $
 * @version $Id$
 */
public final class StreamingRenderer implements GTRenderer {

	private final static int defaultMaxFiltersToSendToDatastore = 5; // default
    
    /**
     * Computes the scale as the ratio between map distances and real world distances,
     * assuming 90dpi and taking into consideration projection deformations and actual
     * earth shape. <br>
     * Use this method only when in need of accurate computation. Will break if the
     * data extent is outside of the currenct projection definition area. 
     */
    public static final String SCALE_ACCURATE = "ACCURATE";
    
    /**
     * Very simple and lenient scale computation method that conforms to the OGC SLD 
     * specification 1.0, page 26. <br>This method is quite approximative, but should
     * never break and ensure constant scale even on lat/lon unprojected maps (because
     * in that case scale is computed as if the area was along the equator no matter
     * what the real position is).
     */
    public static final String SCALE_OGC = "OGC";
    
	public IdentityHashMap symbolizerAssociationHT = new IdentityHashMap(); // associate a value

	/** Tolerance used to compare doubles for equality */
	private static final double TOLERANCE = 1e-6;

	/** The logger for the rendering module. */
	private static final Logger LOGGER = Logger
			.getLogger("org.geotools.rendering");

	int error = 0;

	/** Filter factory for creating bounding box filters */
	private final static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
	
	private final static PropertyName gridPropertyName= filterFactory.property("grid");
	
    private final static PropertyName defaultGeometryPropertyName= filterFactory.property("");
	

	private final static CoordinateOperationFactory operationFactory = new BufferedCoordinateOperationFactory(
			new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));

	/**
	 * Context which contains the layers and the bouning box which needs to be
	 * rendered.
	 */
	private MapContext context;

	/**
	 * Flag which determines if the renderer is interactive or not. An
	 * interactive renderer will return rather than waiting for time consuming
	 * operations to complete (e.g. Image Loading). A non-interactive renderer
	 * (e.g. a SVG or PDF renderer) will block for these operations.
	 */
	private boolean interactive = true;

	/**
	 * Flag which controls behaviour for applying affine transformation to the
	 * graphics object. If true then the transform will be concatenated to the
	 * existing transform. If false it will be replaced.
	 */
	private boolean concatTransforms = false;

	/** Geographic map extent */
	private ReferencedEnvelope mapExtent;

	/** The size of the output area in output units. */
	private Rectangle screenSize;

	/**
	 * This flag is set to false when starting rendering, and will be checked
	 * during the rendering loop in order to make it stop forcefully
	 */
	private boolean renderingStopRequested = false;

	/**
	 * The ratio required to scale the features to be rendered so that they fit
	 * into the output space.
	 */
	private double scaleDenominator;

	/** Maximun displacement for generalization during rendering */
	private double generalizationDistance = 1.0;

	/** Factory that will resolve symbolizers into rendered styles */
	private SLDStyleFactory styleFactory = new SLDStyleFactory();

	protected LabelCache labelCache = new LabelCacheDefault();

	/** The painter class we use to depict shapes onto the screen */
	private StyledShapePainter painter = new StyledShapePainter(labelCache);

	private IndexedFeatureResults indexedFeatureResults;

	private ListenerList renderListeners = new ListenerList();

	private RenderingHints java2dHints;

	private boolean optimizedDataLoadingEnabledDEFAULT = false;

	private boolean memoryPreloadingEnabledDEFAULT = false;
    
    private int renderingBufferDEFAULT = 0;
    
    private String scaleComputationMethodDEFAULT = SCALE_OGC;

	public static final String LABEL_CACHE_KEY = "labelCache";
	public static final String FORCE_CRS_KEY = "forceCRS";
	public static final String DPI_KEY = "dpi";
	public static final String DECLARED_SCALE_DENOM_KEY = "declaredScaleDenominator";
	public static final String MEMORY_PRE_LOADING_KEY = "memoryPreloadingEnabled";
	public static final String OPTIMIZED_DATA_LOADING_KEY = "optimizedDataLoadingEnabled";
	public static final String SCALE_COMPUTATION_METHOD_KEY = "scaleComputationMethod";
    
    
    /**
     * "optimizedDataLoadingEnabled" - Boolean  yes/no (see default optimizedDataLoadingEnabledDEFAULT)
     * "memoryPreloadingEnabled"     - Boolean  yes/no (see default memoryPreloadingEnabledDEFAULT)
     * "declaredScaleDenominator"    - Double   the value of the scale denominator to use by the renderer.  
     *                                          by default the value is calculated based on the screen size 
     *                                          and the displayed area of the map.
     *  "dpi"                        - Integer  number of dots per inch of the display 90 DPI is the default (as declared by OGC)      
     *  "forceCRS"                   - CoordinateReferenceSystem declares to the renderer that all layers are of the CRS declared in this hint                               
     *  "labelCache"                 - Declares the label cache that will be used by the renderer.                               
     */
    private Map rendererHints = null;
    
	private AffineTransform worldToScreenTransform = null;

	private CoordinateReferenceSystem destinationCrs;

	private boolean canTransform;

	private String layerId;

	/**
	 * Creates a new instance of LiteRenderer without a context. Use it only to
	 * gain access to utility methods of this class or if you want to render
	 * random feature collections instead of using the map context interface
	 */
	public StreamingRenderer() {

	}

	/**
	 * Sets the flag which controls behaviour for applying affine transformation
	 * to the graphics object.
	 * 
	 * @param flag
	 *            If true then the transform will be concatenated to the
	 *            existing transform. If false it will be replaced.
	 */
	public void setConcatTransforms(boolean flag) {
		concatTransforms = flag;
	}

	/**
	 * Flag which controls behaviour for applying affine transformation to the
	 * graphics object.
	 * 
	 * @return a boolean flag. If true then the transform will be concatenated
	 *         to the existing transform. If false it will be replaced.
	 */
	public boolean getConcatTransforms() {
		return concatTransforms;
	}

	/**
	 * adds a listener that responds to error events of feature rendered events.
	 * 
	 * @see RenderListener
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addRenderListener(RenderListener listener) {
		renderListeners.add(listener);
	}

	/**
	 * Removes a render listener.
	 * 
	 * @see RenderListener
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removeRenderListener(RenderListener listener) {
		renderListeners.remove(listener);
	}

	private void fireFeatureRenderedEvent(Object feature) {
        if( !(feature instanceof Feature)){
            return;
        }
		final Object[] objects = renderListeners.getListeners();
		final int length = objects.length;
		RenderListener listener;
		for (int i = 0; i < length; i++) {
			listener = (RenderListener) objects[i];
			listener.featureRenderer((Feature)feature);
		}
	}

	private void fireErrorEvent(Exception e) {
		Object[] objects = renderListeners.getListeners();
		final int length = objects.length;
		RenderListener listener;
		for (int i = 0; i < length; i++) {
			listener = (RenderListener) objects[i];
			listener.errorOccurred(e);
		}
	}

	/**
	 * If you call this method from another thread than the one that called
	 * <code>paint</code> or <code>render</code> the rendering will be
	 * forcefully stopped before termination
	 */
	public void stopRendering() {
		renderingStopRequested = true;
		labelCache.stop();
	}

	/**
	 * Renders features based on the map layers and their styles as specified in
	 * the map context using <code>setContext</code>. <p/> This version of
	 * the method assumes that the size of the output area and the
	 * transformation from coordinates to pixels are known. The latter
	 * determines the map scale. The viewport (the visible part of the map) will
	 * be calculated internally.
	 * 
	 * @param graphics
	 *            The graphics object to draw to.
	 * @param paintArea
	 *            The size of the output area in output units (eg: pixels).
	 * @param worldToScreen
	 *            A transform which converts World coordinates to Screen
	 *            coordinates.
	 * @task Need to check if the Layer CoordinateSystem is different to the
	 *       BoundingBox rendering CoordinateSystem and if so, then transform
	 *       the coordinates.
	 * @deprecated Use paint(Graphics2D graphics, Rectangle paintArea,
	 *             ReferencedEnvelope mapArea) or paint(Graphics2D graphics,
	 *             Rectangle paintArea, ReferencedEnvelope mapArea,
	 *             AffineTransform worldToScreen) instead.
	 */
	public void paint(Graphics2D graphics, Rectangle paintArea,
			AffineTransform worldToScreen) {
		if (worldToScreen == null || paintArea == null) {
			LOGGER.info("renderer passed null arguments");
			return;
		} // Other arguments get checked later
		// First, create the bbox in real world coordinates
		Envelope mapArea;
		try {
			mapArea = RendererUtilities.createMapEnvelope(paintArea,
					worldToScreen);
			paint(graphics, paintArea, mapArea, worldToScreen);
		} catch (NoninvertibleTransformException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			fireErrorEvent(new Exception(
					"Can't create pixel to world transform", e));
		}
	}

	/**
	 * Renders features based on the map layers and their styles as specified in
	 * the map context using <code>setContext</code>. <p/> This version of
	 * the method assumes that the area of the visible part of the map and the
	 * size of the output area are known. The transform between the two is
	 * calculated internally.
	 * 
	 * @param graphics
	 *            The graphics object to draw to.
	 * @param paintArea
	 *            The size of the output area in output units (eg: pixels).
	 * @param mapArea
	 *            the map's visible area (viewport) in map coordinates.
	 * @deprecated Use paint(Graphics2D graphics, Rectangle paintArea,
	 *             ReferencedEnvelope mapArea) or paint(Graphics2D graphics,
	 *             Rectangle paintArea, ReferencedEnvelope mapArea,
	 *             AffineTransform worldToScreen) instead.
	 */
	public void paint(Graphics2D graphics, Rectangle paintArea, Envelope mapArea) {
		if (mapArea == null || paintArea == null) {
			LOGGER.info("renderer passed null arguments");
			return;
		} // Other arguments get checked later
		paint(graphics, paintArea, mapArea, RendererUtilities
				.worldToScreenTransform(mapArea, paintArea));
	}

	/**
	 * Renders features based on the map layers and their styles as specified in
	 * the map context using <code>setContext</code>. <p/> This version of
	 * the method assumes that the area of the visible part of the map and the
	 * size of the output area are known. The transform between the two is
	 * calculated internally.
	 * 
	 * @param graphics
	 *            The graphics object to draw to.
	 * @param paintArea
	 *            The size of the output area in output units (eg: pixels).
	 * @param mapArea
	 *            the map's visible area (viewport) in map coordinates.
	 */
	public void paint(Graphics2D graphics, Rectangle paintArea,
			ReferencedEnvelope mapArea) {
		if (mapArea == null || paintArea == null) {
			LOGGER.info("renderer passed null arguments");
			return;
		} // Other arguments get checked later
		paint(graphics, paintArea, mapArea, RendererUtilities
				.worldToScreenTransform(mapArea, paintArea));
	}

	/**
	 * Renders features based on the map layers and their styles as specified in
	 * the map context using <code>setContext</code>. <p/> This version of
	 * the method assumes that paint area, enelope and worldToScreen transform
	 * are already computed. Use this method to avoid recomputation. <b>Note
	 * however that no check is performed that they are really in sync!<b/>
	 * 
	 * @param graphics
	 *            The graphics object to draw to.
	 * @param paintArea
	 *            The size of the output area in output units (eg: pixels).
	 * @param mapArea
	 *            the map's visible area (viewport) in map coordinates.
	 * @param worldToScreen
	 *            A transform which converts World coordinates to Screen
	 *            coordinates.
	 * @deprecated Use paint(Graphics2D graphics, Rectangle paintArea,
	 *             ReferencedEnvelope mapArea) or paint(Graphics2D graphics,
	 *             Rectangle paintArea, ReferencedEnvelope mapArea,
	 *             AffineTransform worldToScreen) instead.
	 */
	public void paint(Graphics2D graphics, Rectangle paintArea,
			Envelope mapArea, AffineTransform worldToScreen) {
		paint( graphics, paintArea, new ReferencedEnvelope(mapArea, context.getCoordinateReferenceSystem()),
				worldToScreen);
	}

	private double computeScale(ReferencedEnvelope envelope, Rectangle paintArea, Map hints) {
        if(getScaleComputationMethod().equals(SCALE_ACCURATE)) {
            try {
               return RendererUtilities.calculateScale(envelope,
                    paintArea.width, paintArea.height, hints);
            } catch (Exception e) // probably either (1) no CRS (2) error xforming
            {
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        } 
        return RendererUtilities.calculateOGCScale(envelope, paintArea.width, hints);
    }

    /**
	 * Renders features based on the map layers and their styles as specified in
	 * the map context using <code>setContext</code>. <p/> This version of
	 * the method assumes that paint area, enelope and worldToScreen transform
	 * are already computed. Use this method to avoid recomputation. <b>Note
	 * however that no check is performed that they are really in sync!<b/>
	 * 
	 * @param graphics
	 *            The graphics object to draw to.
	 * @param paintArea
	 *            The size of the output area in output units (eg: pixels).
	 * @param mapArea
	 *            the map's visible area (viewport) in map coordinates. Its
	 *            associate CRS is ALWAYS 2D
	 * @param worldToScreen
	 *            A transform which converts World coordinates to Screen
	 *            coordinates.
	 */
	public void paint(Graphics2D graphics, Rectangle paintArea,
			ReferencedEnvelope mapArea, AffineTransform worldToScreen) {
		// ////////////////////////////////////////////////////////////////////
		// 
		// Check for null arguments, recompute missing ones if possible
		//
		// ////////////////////////////////////////////////////////////////////
		if (graphics == null || paintArea == null) {
			LOGGER.severe("renderer passed null arguments");
			throw new NullPointerException("renderer passed null arguments");
		} else if (mapArea == null && paintArea == null) {
			LOGGER.severe("renderer passed null arguments");
			throw new NullPointerException("renderer passed null arguments");
		} else if (mapArea == null) {

			LOGGER.severe("renderer passed null arguments");
			throw new NullPointerException("renderer passed null arguments");
		} else if (worldToScreen == null) {
			worldToScreen = RendererUtilities.worldToScreenTransform(mapArea,
					paintArea);
			if (worldToScreen == null)
				return;
		}

		// ////////////////////////////////////////////////////////////////////
		// 
		// Setting base information
		//
		// TODO the way this thing is built is a mess if you try to use it in a
		// multithreaded environment. I will fix this at the end.
		//
		// ////////////////////////////////////////////////////////////////////
		destinationCrs = mapArea.getCoordinateReferenceSystem();
		mapExtent = new ReferencedEnvelope(mapArea);
		this.screenSize = paintArea;
		this.worldToScreenTransform = worldToScreen;
		error = 0;
		if (java2dHints != null)
			graphics.setRenderingHints(java2dHints);
		// reset the abort flag
		renderingStopRequested = false;

		// ////////////////////////////////////////////////////////////////////
		//
		// Managing transformations , CRSs and scales
		//
		// If we are rendering to a component which has already set up some form
		// of transformation then we can concatenate our transformation to it.
		// An example of this is the ZoomPane component of the swinggui module.
		// ////////////////////////////////////////////////////////////////////
		if (concatTransforms) {
			AffineTransform atg = graphics.getTransform();
			atg.concatenate(worldToScreenTransform);
			worldToScreenTransform = atg;
			graphics.setTransform(worldToScreenTransform);
		}

        // compute scale according to the user specified method
        scaleDenominator = computeScale(mapArea, paintArea, rendererHints);
        
		//////////////////////////////////////////////////////////////////////
        //
        // Consider expanding the map extent so that a few more geometries
        // will be considered, in order to catch those outside of the rendering
        // bounds whose stroke is so thick that it countributes rendered area
        //
        //////////////////////////////////////////////////////////////////////
        int buffer = getRenderingBuffer();
        if(buffer > 0) {
            mapExtent = new ReferencedEnvelope(expandEnvelope(mapExtent, worldToScreen, buffer), 
                    mapExtent.getCoordinateReferenceSystem()); 
        }
        

		// ////////////////////////////////////////////////////////////////////
		//
		// Processing all the map layers in the context using the accompaining
		// styles
		//
		// ////////////////////////////////////////////////////////////////////
		final MapLayer[] layers = context.getLayers();
		labelCache.start();
		final int layersNumber = layers.length;
		MapLayer currLayer;
		for (int i = 0; i < layersNumber; i++) // DJB: for each layer (ie. one
		{
			currLayer = layers[i];

			if (!currLayer.isVisible()) {
				// Only render layer when layer is visible
				continue;
			}

			if (renderingStopRequested) {
				return;
			}
			labelCache.startLayer(i+"");
			try {

				// extract the feature type stylers from the style object
				// and process them
				processStylers(graphics, currLayer, worldToScreenTransform,
						destinationCrs, mapExtent, screenSize);
			} catch (Throwable t) {
				LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
				fireErrorEvent(new Exception(new StringBuffer(
						"Exception rendering layer ").append(currLayer)
						.toString(), t));
			}

			labelCache.endLayer(i+"", graphics, screenSize);
		}

		labelCache.end(graphics, paintArea);

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Style cache hit ratio: ").append(
					styleFactory.getHitRatio()).append(" , hits ").append(
					styleFactory.getHits()).append(", requests ").append(
					styleFactory.getRequests()).toString());
		if (error > 0) {
			LOGGER
					.warning(new StringBuffer(
							"Number of Errors during paint(Graphics2D, AffineTransform) = ")
							.append(error).toString());
		}
	}

	/**
	 * Extends the provided {@link Envelope} in order to add the number of pixels
	 * specified by <code>buffer</code> in every direction.
	 * 
	 * @param envelope to extend.
	 * @param worldToScreen by means  of which doing the extension.
	 * @param buffer to use for the extension.
	 * @return an extende version of the provided {@link Envelope}.
	 */
    private Envelope expandEnvelope(Envelope envelope, AffineTransform worldToScreen, int buffer) {
    	assert buffer>0;
        double bufferX =  Math.abs(buffer * 1.0 /  XAffineTransform.getScaleX0(worldToScreen));
        double bufferY =  Math.abs(buffer * 1.0 /  XAffineTransform.getScaleY0(worldToScreen));
        return new Envelope(envelope.getMinX() - bufferX, 
                envelope.getMaxX() + bufferX, envelope.getMinY() - bufferY, 
                envelope.getMaxY() + bufferY);
    }

	/**
	 * Queries a given layer's <code>Source</code> instance to be rendered. 
	 * <p>
	 * <em><strong>Note: This is proof-of-concept quality only!</strong> At 
	 * the moment the query is not filtered, that means all objects with all 
	 * fields are read from the datastore for every call to this method. This 
	 * method should work like 
	 * {@link #queryLayer(MapLayer, FeatureSource, FeatureType, LiteFeatureTypeStyle[], Envelope, CoordinateReferenceSystem, CoordinateReferenceSystem, Rectangle, GeometryAttributeType)} 
	 * and eventually replace it.</em>
	 * </p>
	 * 
	 * @param currLayer The actually processed layer for rendering
	 * @param source Source to read data from
	 */
	//TODO: Implement filtering for bbox and read in only the need attributes 
	Collection queryLayer(MapLayer currLayer, Source source) {
		
		Collection results = null;
		DefaultQuery query = new DefaultQuery(DefaultQuery.ALL);
		Query definitionQuery;

		definitionQuery = currLayer.getQuery();

		if (definitionQuery != Query.ALL) {
			if (query == Query.ALL) {
				query = new DefaultQuery(definitionQuery);
			} else {
				query = new DefaultQuery(DataUtilities.mixQueries(definitionQuery, query, "liteRenderer"));
			}
		}
		
		results = source.content(query.getFilter());

		return results;
	}
		
	
	/**
	 * Queries a given layer's features to be rendered based on the target
	 * rendering bounding box.
	 * <p>
	 * If <code>optimizedDataLoadingEnabled</code> attribute has been set to
	 * <code>true</code>, the following optimization will be performed in
	 * order to limit the number of features returned:
	 * <ul>
	 * <li>Just the features whose geometric attributes lies within
	 * <code>envelope</code> will be queried</li>
	 * <li>The queried attributes will be limited to just those needed to
	 * perform the rendering, based on the requiered geometric and non geometric
	 * attributes found in the Layer's style rules</li>
	 * <li>If a <code>Query</code> has been set to limit the resulting
	 * layer's features, the final filter to obtain them will respect it. This
	 * means that the bounding box filter and the Query filter will be combined,
	 * also including maxFeatures from Query</li>
	 * <li>At least that the layer's definition query explicitly says to
	 * retrieve some attribute, no attributes will be requested from it, for
	 * performance reassons. So it is desirable to not use a Query for filtering
	 * a layer wich includes attributes. Note that including the attributes in
	 * the result is not necessary for the query's filter to get properly
	 * processed. </li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>NOTE </b>: This is an internal method and should only be called by
	 * <code>paint(Graphics2D, Rectangle, AffineTransform)</code>. It is
	 * package protected just to allow unit testing it.
	 * </p>
	 * 
	 * @param currLayer
	 *            the actually processing layer for renderition
	 * @param schema
	 * @param source
	 * @param envelope
	 *            the spatial extent wich is the target area fo the rendering
	 *            process
	 * @param destinationCrs
	 *            DOCUMENT ME!
	 * @param sourceCrs
	 * @param screenSize
	 * @param geometryAttribute
	 * @return the set of features resulting from <code>currLayer</code> after
	 *         quering its feature source
	 * @throws IllegalFilterException
	 *             if something goes wrong constructing the bbox filter
	 * @throws IOException
	 * @throws IllegalAttributeException
	 * @see MapLayer#setQuery(org.geotools.data.Query)
	 */
	/*
	 * Default visibility for testing purposes
	 */
	
	FeatureCollection queryLayer(MapLayer currLayer, FeatureSource source,
			FeatureType schema, LiteFeatureTypeStyle[] styles,
			Envelope mapArea, CoordinateReferenceSystem mapCRS,
			CoordinateReferenceSystem featCrs, Rectangle screenSize,
			GeometryAttributeType geometryAttribute,
            AffineTransform worldToScreenTransform)
			throws IllegalFilterException, IOException,
			IllegalAttributeException {
		FeatureCollection results = null;
		DefaultQuery query = new DefaultQuery(DefaultQuery.ALL);
		Query definitionQuery;
		MathTransform transform = null;
		String[] attributes;
		AttributeType[] ats;
		final int length;
		Filter filter = null;
		
        // if map extent are not already expanded by a constant buffer, try to compute a layer
        // specific one based on stroke widths
        if(getRenderingBuffer() == 0) {
            int buffer = findRenderingBuffer(styles);
            if (buffer > 0) {
                mapArea = expandEnvelope(mapArea, worldToScreenTransform,
                        buffer);
                LOGGER.fine("Expanding rendering area by " + buffer 
                        + " pixels to consider stroke width");
            }
        }
		ReferencedEnvelope envelope = new ReferencedEnvelope(mapArea, mapCRS);
		if (isOptimizedDataLoadingEnabled()) {
			// see what attributes we really need by exploring the styles
			// for testing purposes we have a null case -->

			if (styles == null) {
				ats = schema.getAttributeTypes();
				length = ats.length;
				attributes = new String[length];
				for (int t = 0; t < length; t++) {
					attributes[t] = ats[t].getName();
				}
			} else {
				attributes = findStyleAttributes(styles, schema);
			}

			try {
				// Then create the geometry filters. We have to create one for
				// each geometric attribute used during the rendering as the
				// feature may have more than one and the styles could use non
				// default geometric ones
				if (mapCRS != null && featCrs != null
						&& !CRS.equalsIgnoreMetadata(featCrs, mapCRS)) {
					// get an unprojected envelope since the feature source is
					// operating on
					// unprojected geometries

					transform = StreamingRenderer.getMathTransform(mapCRS,
							featCrs);

					if (transform != null && !transform.isIdentity()) {
						// Envelope eee= JTS.transform(envelope, transform);//
						// this is the old way
						// 10 = make 10 points on each side of the bbox &
						// transform the polygon

						envelope = new ReferencedEnvelope(JTS.transform(
								mapArea, null, transform, 10), featCrs);

						// will usually be a "bigger" bbox
					} else
						transform = null; // reset transform
				}

				if (!isMemoryPreloadingEnabled()) {
					filter = createBBoxFilters(schema, attributes, envelope);
				} else {
					filter = Filter.INCLUDE;
				}

				// now build the query using only the attributes and the
				// bounding box needed
				query = new DefaultQuery(schema.getTypeName());
				query.setFilter(filter);
				query.setPropertyNames(attributes);
				processRuleForQuery(styles, query);

			} catch (Exception e) {
				fireErrorEvent(new Exception("Error transforming bbox", e));
				canTransform = false;
				query = new DefaultQuery(schema.getTypeName());
				query.setPropertyNames(attributes);
				if (envelope.intersects(source.getBounds())) {
					LOGGER
							.fine(new StringBuffer(
									"Got a tranform exception while trying to de-project the current ")
									.append(
											"envelope, bboxs intersect therefore using envelope)")
									.toString());
					filter = null;					
					filter = createBBoxFilters(schema, attributes, envelope);
					query.setFilter(filter);
				} else {
					LOGGER
							.fine(new StringBuffer(
									"Got a tranform exception while trying to de-project the current ")
									.append(
											"envelope, falling back on full data loading (no bbox query)")
									.toString());
					query.setFilter(Filter.INCLUDE);
				}
				processRuleForQuery(styles, query);

			}
		}

		// now, if a definition query has been established for this layer, be
		// sure to respect it by combining it with the bounding box one.
		definitionQuery = currLayer.getQuery();

		if (definitionQuery != Query.ALL) {
			if (query == Query.ALL) {
				query = new DefaultQuery(definitionQuery);
			} else {
				query = new DefaultQuery(DataUtilities.mixQueries(
						definitionQuery, query, "liteRenderer"));
			}
		}
		query.setCoordinateSystem(featCrs);

		if (isMemoryPreloadingEnabled()) {
			// TODO: attache a feature listener, we must erase the memory cache
			// if
			// anything changes in the data store
			if (indexedFeatureResults == null) {
				indexedFeatureResults = new IndexedFeatureResults(source
						.getFeatures(query));
			}
			indexedFeatureResults.setQueryBounds(envelope);
			results = indexedFeatureResults;
		} else { // insert a debug point here to check your query
			results = source.getFeatures(query);
		}

		// commenting this out for now, since it's causing connections to be
		// left open, since it's making a transaction that is never committed.
		// I think perhaps not getting FIDs should be set in client software
		// anyways See GEOS-631 and related issues. -ch
		/*
		 * if ((source instanceof FeatureStore) && (doesntHaveFIDFilter(query))) {
		 * try { FeatureStore fs = (FeatureStore) source;
		 * 
		 * if (fs.getTransaction() == Transaction.AUTO_COMMIT) { // play it
		 * safe, only update the transaction info if its an // auto_commit // it
		 * logically possible that someone could be using the // Transaction to
		 * do future (or past) processing. // We dont want to affect a future
		 * Query // thats not possible with an AUTO_COMMIT so its safe.
		 * Transaction t = new DefaultTransaction();
		 * t.putProperty("doNotGetFIDS", Boolean.TRUE); fs.setTransaction(t); } }
		 * catch (Exception e) { if (LOGGER.isLoggable(Level.WARNING))
		 * LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e); // we // can
		 * carry on, but report to // user } }
		 */
		return results;
	}

	/**
	 * JE: If there is a single rule "and" its filter together with the query's
	 * filter and send it off to datastore. This will allow as more processing
	 * to be done on the back end... Very useful if DataStore is a database.
	 * Problem is that worst case each filter is ran twice. Next we will modify
	 * it to find a "Common" filter between all rules and send that to the
	 * datastore.
	 * 
	 * DJB: trying to be smarter. If there are no "elseRules" and no rules w/o a
	 * filter, then it makes sense to send them off to the Datastore We limit
	 * the number of Filters sent off to the datastore, just because it could
	 * get a bit rediculous. In general, for a database, if you can limit 10% of
	 * the rows being returned you're probably doing quite well. The main
	 * problem is when your filters really mean you're secretly asking for all
	 * the data in which case sending the filters to the Datastore actually
	 * costs you. But, databases are *much* faster at processing the Filters
	 * than JAVA is and can use statistical analysis to do it.
	 * 
	 * @param styles
	 * @param q
	 */

	private void processRuleForQuery(LiteFeatureTypeStyle[] styles,
			DefaultQuery q) {
		try {

			// first we check to see if there are >
			// "getMaxFiltersToSendToDatastore" rules
			// if so, then we dont do anything since no matter what there's too
			// many to send down.
			// next we check for any else rules. If we find any --> dont send
			// anything to Datastore
			// next we check for rules w/o filters. If we find any --> dont send
			// anything to Datastore
			//
			// otherwise, we're gold and can "or" together all the fiters then
			// AND it with the original filter.
			// ie. SELECT * FROM ... WHERE (the_geom && BBOX) AND (filter1 OR
			// filter2 OR filter3);

			final int maxFilters = getMaxFiltersToSendToDatastore();
			final ArrayList filtersToDS = new ArrayList();
			final int actualFilters = 0;
			final int stylesLength = styles.length;
			int styleElseRulesLength;
			int styleRulesLength;
			LiteFeatureTypeStyle style;
			int u = 0;
			Rule r;
			if (stylesLength > maxFilters) // there's at least one per
				return;
			for (int t = 0; t < stylesLength; t++) // look at each
			// featuretypestyle
			{
				style = styles[t];
				styleElseRulesLength = style.elseRules.length;
				styleRulesLength = style.ruleList.length;
				if (styleElseRulesLength > 0) // uh-oh has elseRule
					return;
				for (u = 0; u < styleRulesLength; u++) // look at each
				// rule in the
				// featuretypestyle
				{
					r = style.ruleList[u];
					if (r.getFilter() == null)
						return; // uh-oh has no filter (want all rows)
					filtersToDS.add(r.getFilter());
				}
			}
			if (actualFilters > maxFilters)
				return;

			org.opengis.filter.Filter ruleFiltersCombined;
			Filter newFilter;
			// We're GOLD -- OR together all the Rule's Filters
			if (filtersToDS.size() == 1) // special case of 1 filter
			{
				ruleFiltersCombined = (Filter) filtersToDS.get(0);
			} else {
				// build it up
				ruleFiltersCombined = (Filter) filtersToDS.get(0);
				final int size = filtersToDS.size();
				for (int t = 1; t < size; t++) // NOTE: dont
				// redo 1st one
				{
					newFilter = (Filter) filtersToDS.get(t);
					ruleFiltersCombined = filterFactory.or(
							ruleFiltersCombined, newFilter );
				}
			}
			// combine with the geometry filter (preexisting)
			ruleFiltersCombined = filterFactory.and(
					q.getFilter(), ruleFiltersCombined);

			// set the actual filter
			q.setFilter(ruleFiltersCombined);
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.SEVERE,
						"Could not send rules to datastore due to: "
								+ e.getLocalizedMessage(), e);
		}
	}

	/**
	 * find out the maximum number of filters we're going to send off to the
	 * datastore. See processRuleForQuery() for details.
	 * 
	 */
	private int getMaxFiltersToSendToDatastore() {
		try {
			Integer result = (Integer) rendererHints
					.get("maxFiltersToSendToDatastore");
			if (result == null)
				return defaultMaxFiltersToSendToDatastore; // default if not
			// present in hints
			return result.intValue();

		} catch (Exception e) {
			return defaultMaxFiltersToSendToDatastore;
		}
	}

	/**
	 */
	private boolean isMemoryPreloadingEnabled() {
		if (rendererHints == null)
			return memoryPreloadingEnabledDEFAULT;
		Object result = null;
		try{
			result =  rendererHints.get("memoryPreloadingEnabled");
		}catch (ClassCastException e) {
			
		}
		if (result == null)
			return memoryPreloadingEnabledDEFAULT;
		return ((Boolean)result).booleanValue();
	}
    
    /**
     * Returns an estimate of the rendering buffer needed to properly display this
     * layer taking into consideration the constant stroke sizes in the feature type
     * styles.
     * 
     * @param styles
     *            the feature type styles to be applied to the layer
     * @return an estimate of the buffer that should be used to properly display a layer
     *         rendered with the specified styles 
     */
    private int findRenderingBuffer(LiteFeatureTypeStyle[] styles) {
        final MetaBufferEstimator rbe = new MetaBufferEstimator();

        for (int t = 0; t < styles.length; t++) {
            final LiteFeatureTypeStyle lfts = styles[t];
            Rule[] rules = lfts.elseRules;
            for (int j = 0; j < rules.length; j++) {
                rbe.visit(rules[j]);
            }
            rules = lfts.ruleList;
            for (int j = 0; j <  rules.length; j++) {
                rbe.visit(rules[j]);
            }
        }

        if(!rbe.isEstimateAccurate())
            LOGGER.warning("Assuming rendering buffer = " + rbe.getBuffer() 
                    + ", but estimation is not accurate, you may want to set a buffer manually");
        return rbe.getBuffer();
    }

	/**
	 * Inspects the <code>MapLayer</code>'s style and retrieves it's needed
	 * attribute names, returning at least the default geometry attribute name.
	 * 
	 * @param layer
	 *            the <code>MapLayer</code> to determine the needed attributes
	 *            from
	 * @param schema
	 *            the <code>layer</code>'s featuresource schema
	 * @return the minimun set of attribute names needed to render
	 *         <code>layer</code>
	 */
	private String[] findStyleAttributes(LiteFeatureTypeStyle[] styles,
			FeatureType schema) {
		final StyleAttributeExtractor sae = new StyleAttributeExtractor();

		LiteFeatureTypeStyle lfts;
		Rule[] rules;
		int rulesLength;
		final int length = styles.length;
		for (int t = 0; t < length; t++) {
			lfts = styles[t];
			rules = lfts.elseRules;
			rulesLength = rules.length;
			for (int j = 0; j < rulesLength; j++) {
				sae.visit(rules[j]);
			}
			rules = lfts.ruleList;
			rulesLength = rules.length;
			for (int j = 0; j < rulesLength; j++) {
				sae.visit(rules[j]);
			}
		}

		String[] ftsAttributes = sae.getAttributeNames();

		/*
		 * DJB: this is an old comment - erase it soon (see geos-469 and below) -
		 * we only add the default geometry if it was used.
		 * 
		 * GR: if as result of sae.getAttributeNames() ftsAttributes already
		 * contains geometry attribue names, they gets duplicated, wich produces
		 * an error in AbstracDatastore when trying to create a derivate
		 * FeatureType. So I'll add the default geometry only if it is not
		 * already present, but: should all the geometric attributes be added by
		 * default? I will add them, but don't really know what's the expected
		 * behavior
		 */
		List atts = new LinkedList(Arrays.asList(ftsAttributes));
		AttributeType[] attTypes = schema.getAttributeTypes();
		String attName;

		final int attTypesLength = attTypes.length;
		for (int i = 0; i < attTypesLength; i++) {
			attName = attTypes[i].getName();

			// DJB: This geometry check was commented out. I think it should
			// actually be back in or
			// you get ALL the attributes back, which isnt what you want.
			// ALX: For rasters I need even the "grid" attribute.

			// DJB:geos-469, we do not grab all the geometry columns.
			// for symbolizers, if a geometry is required it is either
			// explicitly named
			// ("<Geometry><PropertyName>the_geom</PropertyName></Geometry>")
			// or the default geometry is assumed (no <Geometry> element).
			// I've modified the style attribute extractor so it tracks if the
			// default geometry is used. So, we no longer add EVERY geometry
			// column to the query!!

			if ((attName.equalsIgnoreCase("grid"))
					&& !atts.contains(attName)||
					(attName.equalsIgnoreCase("params"))
					&& !atts.contains(attName)) {
				atts.add(attName);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("added attribute " + attName);
			}
		}

		try {
			// DJB:geos-469 if the default geometry was used in the style, we
			// need to grab it.
			if (sae.getDefaultGeometryUsed()
					&& (!atts.contains(schema.getDefaultGeometry().getName()))) {
				atts.add(schema.getDefaultGeometry().getName());
			}
		} catch (Exception e) {
			// might not be a geometry column. That will cause problems down the
			// road (why render a non-geometry layer)
		}

		ftsAttributes = new String[atts.size()];
		atts.toArray(ftsAttributes);

		return ftsAttributes;
	}

	/**
	 * Creates the bounding box filters (one for each geometric attribute)
	 * needed to query a <code>MapLayer</code>'s feature source to return
	 * just the features for the target rendering extent
	 * 
	 * @param schema
	 *            the layer's feature source schema
	 * @param attributes
	 *            set of needed attributes
	 * @param bbox
	 *            the expression holding the target rendering bounding box
	 * @return an or'ed list of bbox filters, one for each geometric attribute
	 *         in <code>attributes</code>. If there are just one geometric
	 *         attribute, just returns its corresponding
	 *         <code>GeometryFilter</code>.
	 * @throws IllegalFilterException
	 *             if something goes wrong creating the filter
	 */
	private Filter createBBoxFilters(FeatureType schema, String[] attributes,
			Envelope bbox) throws IllegalFilterException {
		Filter filter = null;
		final int length = attributes.length;
		AttributeType attType;
		
		for (int j = 0; j < length; j++) {
			attType = schema.getAttributeType(attributes[j]);

			// DJB: added this for better error messages!
			if (attType == null) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(new StringBuffer("Could not find '").append(
							attributes[j]).append("' in the FeatureType (")
							.append(schema.getTypeName()).append(")")
							.toString());
				throw new IllegalFilterException(new StringBuffer(
						"Could not find '").append(
						attributes[j] + "' in the FeatureType (").append(
						schema.getTypeName()).append(")").toString());
			}

			if (attType instanceof GeometryAttributeType) {                                
                BBOX gfilter = filterFactory.bbox( attType.getName(), bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), null );
                
				if (filter == null) {
					filter = gfilter;
				} else {
					filter = filterFactory.or( filter, gfilter );
				}
			}
		}

		return filter;
	}

	/**
	 * Checks if a rule can be triggered at the current scale level
	 * 
	 * @param r
	 *            The rule
	 * @return true if the scale is compatible with the rule settings
	 */
	private boolean isWithInScale(Rule r) {
		return ((r.getMinScaleDenominator() - TOLERANCE) <= scaleDenominator)
				&& ((r.getMaxScaleDenominator() + TOLERANCE) > scaleDenominator);
	}

	/**
	 * <p>Creates a list of <code>LiteFeatureTypeStyle</code>s with:
	 * <ol type="a">
	 * <li>out-of-scale rules removed</li>
	 * <li>incompatible FeatureTypeStyles removed</li>
	 * </ol>
	 * </p>
	 * 
	 * <p><em><strong>Note:</strong> This method has a lot of duplication with 
	 * {@link #createLiteFeatureTypeStyles(FeatureTypeStyle[], FeatureType, Graphics2D)}. 
	 * </em></p>
	 * 
	 * @param featureStyles Styles to process
	 * @param typeDescription The type description that has to be matched
	 * @return ArrayList<LiteFeatureTypeStyle>
	 */
	//TODO: Merge the two createLiteFeatureTypeStyles() methods
	private ArrayList createLiteFeatureTypeStyles(FeatureTypeStyle[] featureStyles, Object typeDescription, Graphics2D graphics) throws IOException {
		ArrayList result = new ArrayList();
		
		Rule[] rules;
		ArrayList ruleList = new ArrayList();
		ArrayList elseRuleList = new ArrayList();
		Rule r;
		LiteFeatureTypeStyle lfts;
		BufferedImage image;
		int numOfRules;		
		int itemNumber = 0;

		final int length = featureStyles.length;
		for (int i = 0; i < length; i++) {
			FeatureTypeStyle fts = featureStyles[i];
	
			if ( typeDescription == null || typeDescription.toString().indexOf( fts.getFeatureTypeName() ) == -1 ) continue; 
			
			// get applicable rules at the current scale
			rules = fts.getRules();
			ruleList = new ArrayList();
			elseRuleList = new ArrayList();

			numOfRules = rules.length;
			for (int j = 0; j < numOfRules; j++) {
				// getting rule
				r = rules[j];

				if (isWithInScale(r)) {
					if (r.hasElseFilter()) {
						elseRuleList.add(r);
					} else {
						ruleList.add(r);
					}
				}
			}
			if ((ruleList.size() == 0) && (elseRuleList.size() == 0))
				continue; // DJB: optimization - nothing to render, dont
			// do anything!!

			if (itemNumber == 0) // we can optimize this one!
			{
				lfts = new LiteFeatureTypeStyle(graphics, ruleList,
						elseRuleList);
			} else {
				image = graphics
						.getDeviceConfiguration()
						.createCompatibleImage(screenSize.width,
								screenSize.height, Transparency.TRANSLUCENT);
				lfts = new LiteFeatureTypeStyle(image, graphics
						.getTransform(), ruleList, elseRuleList,
						java2dHints);
			}
			result.add(lfts);
			itemNumber++;			
			
		}

		return result;
	}
	
	/**
	 * creates a list of LiteFeatureTypeStyles a) out-of-scale rules removed b)
	 * incompatible FeatureTypeStyles removed
	 * 
	 * 
	 * @param featureStylers
	 * @param features
	 * @throws Exception
	 * @return ArrayList<LiteFeatureTypeStyle>
	 */
	private ArrayList createLiteFeatureTypeStyles(
			FeatureTypeStyle[] featureStyles, FeatureType ftype,
			Graphics2D graphics) throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("creating rules for scale denominator - "
					+ NumberFormat.getNumberInstance().format(scaleDenominator));
		ArrayList result = new ArrayList();

		int itemNumber = 0;

		Rule[] rules;
		ArrayList ruleList = new ArrayList();
		ArrayList elseRuleList = new ArrayList();
		Rule r;
		LiteFeatureTypeStyle lfts;
		BufferedImage image;
		int numOfRules;
		FeatureTypeStyle fts;
		final String typeName = ftype.getTypeName();
		final int length = featureStyles.length;
		for (int i = 0; i < length; i++)
		// DJB: for each FeatureTypeStyle in the SLD (each on is drawn
		// indpendently)
		{
			// getting feature styles
			fts = featureStyles[i];

			if ((typeName != null)
					&& (ftype.isDescendedFrom(null, fts.getFeatureTypeName()) || typeName
							.equalsIgnoreCase(fts.getFeatureTypeName()))) {
				// DJB: this FTS is compatible with this FT.

				// get applicable rules at the current scale
				rules = fts.getRules();
				ruleList = new ArrayList();
				elseRuleList = new ArrayList();

				numOfRules = rules.length;
				for (int j = 0; j < numOfRules; j++) {
					// getting rule
					r = rules[j];

					if (isWithInScale(r)) {
						if (r.hasElseFilter()) {
							elseRuleList.add(r);
						} else {
							ruleList.add(r);
						}
					}
				}
				if ((ruleList.size() == 0) && (elseRuleList.size() == 0))
					continue; // DJB: optimization - nothing to render, dont
				// do anything!!

				if (itemNumber == 0) // we can optimize this one!
				{
					lfts = new LiteFeatureTypeStyle(graphics, ruleList,
							elseRuleList);
				} else {
					image = graphics
							.getDeviceConfiguration()
							.createCompatibleImage(screenSize.width,
									screenSize.height, Transparency.TRANSLUCENT);
					lfts = new LiteFeatureTypeStyle(image, graphics
							.getTransform(), ruleList, elseRuleList,
							java2dHints);
				}
				result.add(lfts);
				itemNumber++;

			}
		}

		return result;
	}

	private Collection prepCollection(Collection collection,
			CoordinateReferenceSystem sourceCrs) throws IOException {
        if( collection instanceof FeatureCollection){
            // will force content into correct CRS if needed
            return prepFeatureCollection( (FeatureCollection) collection, sourceCrs );
        }
        return collection;
	}

    /**
     * Prepair a FeatureCollection for display, this method formally ensured that a FeatureReader
     * produced the correct CRS and has now been updated to work with FeatureCollection.
     * <p>
     * What is really going on is the need to set up for reprojection; but *after* decimation has
     * occured.
     * </p>
     *  
     * @param features
     * @param sourceCrs
     * @return FeatureCollection that produces results with the correct CRS
     */
    private FeatureCollection prepFeatureCollection( FeatureCollection features, CoordinateReferenceSystem sourceCrs ) {
        // DJB: dont do reprojection here - do it after decimation
        // but we ensure that the reader is producing geometries with
        // the correct CRS
        // NOTE: it, by default, produces ones that are are tagged with
        // the CRS of the datastore, which
        // maybe incorrect.
        // The correct value is in sourceCrs.

        // this is the reader's CRS
        CoordinateReferenceSystem rCS = features.getSchema().getDefaultGeometry().getCoordinateSystem();

        // sourceCrs == source's real SRS
        // if we need to recode the incoming geometries

        if (rCS != sourceCrs) // not both null or both EXACTLY the
        // same CRS object
        {
            if (sourceCrs != null) // dont re-tag to null, keep the
            // DataStore's CRS (this shouldnt
            // really happen)
            {
                // if the datastore is producing null CRS, we recode.
                // if the datastore's CRS != real CRS, then we recode
                if ((rCS == null) || (!rCS.equals(sourceCrs))) {
                    // need to retag the features
                    try {
                        return new ForceCoordinateSystemFeatureResults( features, sourceCrs );
                    } catch (Exception ee) {
                        LOGGER.log(Level.WARNING, ee.getLocalizedMessage(), ee);
                    }
                }
            }
        }
        return features;
    }
    
	/**
	 * Applies each feature type styler in turn to all of the features. This
	 * perhaps needs some explanation to make it absolutely clear.
	 * featureStylers[0] is applied to all features before featureStylers[1] is
	 * applied. This can have important consequences as regards the painting
	 * order.
	 * <p>
	 * In most cases, this is the desired effect. For example, all line features
	 * may be rendered with a fat line and then a thin line. This produces a
	 * 'cased' effect without any strange overlaps.
	 * </p>
	 * <p>
	 * This method is internal and should only be called by render.
	 * </p>
	 * <p>
	 * </p>
	 * 
	 * @param graphics
	 *            DOCUMENT ME!
	 * @param features
	 *            An array of features to be rendered
	 * @param featureStylers
	 *            An array of feature stylers to be applied
	 * @param at
	 *            DOCUMENT ME!
	 * @param destinationCrs -
	 *            The destination CRS, or null if no reprojection is required
	 * @param screenSize
	 * @throws IOException
	 * @throws IllegalAttributeException
	 * @throws IllegalFilterException
	 */
	final private void processStylers(final Graphics2D graphics,
			MapLayer currLayer, AffineTransform at,
			CoordinateReferenceSystem destinationCrs, Envelope mapArea,
			Rectangle screenSize) throws IllegalFilterException, IOException,
			IllegalAttributeException {

		/*
		 * DJB: changed this a wee bit so that it now does the layer query AFTER
		 * it has evaluated the rules for scale inclusion. This makes it so that
		 * geometry columns (and other columns) will not be queried unless they
		 * are actually going to be required. see geos-469
		 */
		// /////////////////////////////////////////////////////////////////////
		//
		// Preparing feature information and styles
		//
		// /////////////////////////////////////////////////////////////////////
		final FeatureTypeStyle[] featureStylers = currLayer.getStyle().getFeatureTypeStyles();
		
        final FeatureSource featureSource = currLayer.getFeatureSource();
        
        final Collection result;
        final CoordinateReferenceSystem sourceCrs;
        final NumberRange scaleRange = new NumberRange(scaleDenominator,scaleDenominator);
        symbolizerAssociationHT = new IdentityHashMap();// TODO use clear? MT issues?
        final ArrayList lfts ;
        
        if ( featureSource != null ) {
			final FeatureType schema = featureSource.getSchema();
	        
			final GeometryAttributeType geometryAttribute = schema.getDefaultGeometry();
			sourceCrs = geometryAttribute.getCoordinateSystem();
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine(new StringBuffer("processing ").append(
						featureStylers.length).append(" stylers for ").append(
						currLayer.getFeatureSource().getSchema().getTypeName())
						.toString());
			}
			// transformMap = new HashMap();
			lfts = createLiteFeatureTypeStyles(featureStylers,schema, graphics);
	
	        LiteFeatureTypeStyle[] featureTypeStyleArray = (LiteFeatureTypeStyle[]) lfts.toArray(new LiteFeatureTypeStyle[lfts.size()]);
	        // /////////////////////////////////////////////////////////////////////
			//
			// DJB: get a featureresults (so you can get a feature reader) for the
			// data
			//
			// /////////////////////////////////////////////////////////////////////
        
        	result = queryLayer(currLayer, featureSource, schema,
				featureTypeStyleArray,
				mapArea, destinationCrs, sourceCrs, screenSize,
				geometryAttribute, at);
        } else {
        	Source source = currLayer.getSource();
        	result = queryLayer( currLayer, currLayer.getSource() );
        	sourceCrs = null;
        	lfts = createLiteFeatureTypeStyles( featureStylers, source.describe(), graphics );
        }

		if (lfts.size() == 0) return; // nothing to do

        final Collection collection = prepCollection(result, sourceCrs);
		final Iterator iterator = collection.iterator();        
		int n_lfts = lfts.size();
		final LiteFeatureTypeStyle[] fts_array = (LiteFeatureTypeStyle[]) lfts
				.toArray(new LiteFeatureTypeStyle[n_lfts]);

		try {
			Object feature;
			int t = 0;
			while (!renderingStopRequested) { // loop exit condition tested inside try catch
				try {
					if (!iterator.hasNext()) {
						break;
					}
					feature = iterator.next(); // read the content
					for (t = 0; t < n_lfts; t++) {
						process(currLayer, feature, fts_array[t], scaleRange, at, destinationCrs);
                        // draw the content on the image(s)
					}
				} catch (Throwable tr) {
					LOGGER.log(Level.SEVERE, tr.getLocalizedMessage(), tr);
					fireErrorEvent(new Exception("Error rendering feature", tr));
				}
			}
		} finally {
            if( collection instanceof ResourceCollection ){
                ResourceCollection resource = (ResourceCollection ) collection;
                resource.close( iterator );
            }
		}
		// have to re-form the image now.
		// graphics.setTransform( new AffineTransform() );
		for (int t = 0; t < n_lfts; t++) {
			if (fts_array[t].myImage != null) // this is the case for the
			// first one (ie.
			// fts_array[t].graphics ==
			// graphics)
			{
				graphics.drawImage(fts_array[t].myImage, 0, 0, null);
				fts_array[t].myImage.flush();
				fts_array[t].graphics.dispose();
			}
		}

	}

	/**
	 * @param content
	 * @param feature 
	 * @param style
	 */
	final private void process(MapLayer currLayer, Object content, LiteFeatureTypeStyle style,
			Range scaleRange, AffineTransform at,
			CoordinateReferenceSystem destinationCrs)
			throws TransformException, FactoryException {
		boolean doElse = true;
		Rule[] elseRuleList = style.elseRules;
		Rule[] ruleList = style.ruleList;
		Rule r;
		Filter filter;
		Symbolizer[] symbolizers;
		Graphics2D graphics = style.graphics;
		// applicable rules
		final int length = ruleList.length;
		for (int t = 0; t < length; t++) {
			r = ruleList[t];
			filter = r.getFilter();

			if ((filter == null) || filter.evaluate(content)) {
				doElse = false;
				symbolizers = r.getSymbolizers();
				processSymbolizers(currLayer, graphics, content, symbolizers, scaleRange,
						at, destinationCrs);
			}
		}

		if (doElse) {
			final int elseLength = elseRuleList.length;
			for (int tt = 0; tt < elseLength; tt++) {
				r = elseRuleList[tt];
				symbolizers = r.getSymbolizers();

				processSymbolizers(currLayer, graphics, content, symbolizers, scaleRange,
						at, destinationCrs);

			}
		}
	}

	/**
	 * Applies each of a set of symbolizers in turn to a given feature.
	 * <p>
	 * This is an internal method and should only be called by processStylers.
	 * </p>
	 * @param currLayer 
	 * 
	 * @param graphics
	 * @param drawMe
	 *            The feature to be rendered
	 * @param symbolizers
	 *            An array of symbolizers which actually perform the rendering.
	 * @param scaleRange
	 *            The scale range we are working on... provided in order to make
	 *            the style factory happy
	 * @param shape
	 * @param destinationCrs
	 * @throws TransformException
	 * @throws FactoryException
	 */
	final private void processSymbolizers(MapLayer currLayer, final Graphics2D graphics,
			final Object drawMe, final Symbolizer[] symbolizers,
			Range scaleRange, AffineTransform at,
			CoordinateReferenceSystem destinationCrs)
			throws TransformException, FactoryException {
		LiteShape2 shape;
		Geometry g;
		SymbolizerAssociation sa;
		MathTransform2D transform = null;
		final int length = symbolizers.length;
		for (int m = 0; m < length; m++) {

			// /////////////////////////////////////////////////////////////////
			//
			// RASTER
			//
			// /////////////////////////////////////////////////////////////////
			if (symbolizers[m] instanceof RasterSymbolizer) {
				renderRaster(graphics, drawMe,
						(RasterSymbolizer) symbolizers[m], destinationCrs,
						scaleRange);

			} else {

				// /////////////////////////////////////////////////////////////////
				//
				// FEATURE
				//
				// /////////////////////////////////////////////////////////////////
				g = findGeometry(drawMe, symbolizers[m]); // pulls the
				// geometry

				if ( g == null ) continue;
				
				sa = (SymbolizerAssociation) symbolizerAssociationHT
						.get(symbolizers[m]);
				if (sa == null) {
					sa = new SymbolizerAssociation();
					sa.setCRS(findGeometryCS(currLayer, drawMe, symbolizers[m]));
					try {
						// DJB: this should never be necessary since we've
						// already taken care to make sure the reader is
						// producing the correct coordinate system
						if (sa.crs == null || CRS.equalsIgnoreMetadata(sa.crs,
								destinationCrs))
							transform = null;
						else
							transform = (MathTransform2D) StreamingRenderer
									.getMathTransform(sa.crs, destinationCrs);
						if (transform != null && !transform.isIdentity()) {
							transform = (MathTransform2D) ConcatenatedTransform
									.create(transform, ProjectiveTransform
											.create(at));

						} else {
							transform = (MathTransform2D) ProjectiveTransform
									.create(at);
						}
					} catch (Exception e) {
						// fall through
						LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
					}
					sa.setXform(transform);
					symbolizerAssociationHT.put(symbolizers[m], sa);
				}

				// some shapes may be too close to projection boundaries to
				// get transformed, try to be lenient
				try {
					shape = getTransformedShape(g, sa.getXform());
				} catch (TransformException te) {
                                        LOGGER.log(Level.FINE, te.getLocalizedMessage(), te);
					fireErrorEvent(te);
					continue;
				} catch (AssertionError ae) {
                                        LOGGER.log(Level.FINE, ae.getLocalizedMessage(), ae);
					fireErrorEvent(new RuntimeException(ae));
					continue;
				}
				if (symbolizers[m] instanceof TextSymbolizer && drawMe instanceof Feature) {
                    // TODO: double back and make labelCache work with Objects
					labelCache.put(layerId, (TextSymbolizer) symbolizers[m], (Feature)drawMe,
							shape, scaleRange);
				} else {
					Style2D style = styleFactory.createStyle(drawMe,
							symbolizers[m], scaleRange);
					painter.paint(graphics, shape, style, scaleDenominator);
				}

			}
		}
		fireFeatureRenderedEvent(drawMe);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param g
	 * @param transform
	 * @throws TransformException
	 * @throws FactoryException
	 */
	private final LiteShape2 getTransformedShape(Geometry g,
			MathTransform2D transform) throws TransformException,
			FactoryException {

		return new LiteShape2(g, transform, getDecimator(transform), false);
	}

	private HashMap decimators = new HashMap();

	/**
	 * @throws org.opengis.referencing.operation.NoninvertibleTransformException
	 */
	private Decimator getDecimator(MathTransform2D mathTransform)
			throws org.opengis.referencing.operation.NoninvertibleTransformException {
		Decimator decimator = (Decimator) decimators.get(mathTransform);
		if (decimator == null) {
			if (mathTransform != null && !mathTransform.isIdentity())
				decimator = new Decimator(mathTransform.inverse(), screenSize);
			else
				decimator = new Decimator(null, screenSize);

			decimators.put(mathTransform, decimator);
		}
		return decimator;
	}

	/**
	 * Renders a grid coverage on the device.
	 * 
	 * @param graphics
	 *            DOCUMENT ME!
	 * @param drawMe
	 *            the feature that contains the GridCoverage. The grid coverage
	 *            must be contained in the "grid" attribute
	 * @param symbolizer
	 *            The raster symbolizer
	 * @param scaleRange
	 * @param world2Grid 
	 * @task make it follow the symbolizer
	 */
	private void renderRaster(Graphics2D graphics, Object drawMe,
			RasterSymbolizer symbolizer,
			CoordinateReferenceSystem destinationCRS, Range scaleRange) {
		final Object grid = gridPropertyName.evaluate( drawMe);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("rendering Raster for feature ")
					.append(drawMe.toString()).append(" - ").append(
							grid).toString());

		try {
			// /////////////////////////////////////////////////////////////////
			//
			// If the grid object is a reader we ask him to do its best for the
			// requested resolution, if it is a gridcoverage instead we have to
			// rely on the gridocerage renderer itself.
			//
			// /////////////////////////////////////////////////////////////////
//			METABUFFER SUPPORT			
//			final GeneralEnvelope metaBufferedEnvelope=handleTileBordersArtifacts(mapExtent,java2dHints,this.worldToScreenTransform);
			final GridCoverageRenderer gcr = new GridCoverageRenderer(
					destinationCRS, mapExtent, screenSize, java2dHints);

			// //
			// It is a grid coverage
			// //
			if (grid instanceof GridCoverage)
//				METABUFFER SUPPORT				
//				gcr.paint(graphics, (GridCoverage2D) grid, symbolizer, metaBufferedEnvelope);
				gcr.paint(graphics, (GridCoverage2D) grid, symbolizer);
			else if (grid instanceof AbstractGridCoverage2DReader) {
				// //
				// It is an AbstractGridCoverage2DReader, let's use parameters
				// if we have any supplied by a user.
				// //
				// first I created the correct ReadGeometry
				final Parameter readGG = new Parameter(
						AbstractGridFormat.READ_GRIDGEOMETRY2D);
//	METABUFFER SUPPORT
//				readGG.setValue(new GridGeometry2D(new GeneralGridRange(
//						screenSize), metaBufferedEnvelope));
				readGG.setValue(new GridGeometry2D(new GeneralGridRange(
						screenSize), mapExtent));
				final AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) grid;
				// then I try to get read parameters associated with this
				// coverage if there are any.
				final Object params =gridPropertyName.evaluate("params");
				final GridCoverage2D coverage;
				if (params != null) {
					// //
					//
					// Getting parameters to control how to read this coverage.
					// Remember to check to actually have them before forwarding
					// them to the reader.
					//
					// //
					GeneralParameterValue[] readParams = (GeneralParameterValue[]) params;
					final int length = readParams.length;
					if (length > 0) {
						// we have a valid number of parameters, let's check if
						// also have a READ_GRIDGEOMETRY2D. In such case we just
						// override it with the one we just build for this
						// request.
						final String name = AbstractGridFormat.READ_GRIDGEOMETRY2D
								.getName().toString();
						int i = 0;
						for (; i < length; i++)
							if (readParams[i].getDescriptor().getName()
									.toString().equalsIgnoreCase(name))
								break;
						// did we find anything?
						if (i < length) {
							//we found another READ_GRIDGEOMETRY2D, let's override it.
							((Parameter) readParams[i]).setValue(readGG);
							coverage = (GridCoverage2D) reader
							.read(readParams);
						} else {
							// add the correct read geometry to the supplied
							// params since we did not find anything
							GeneralParameterValue[] readParams2 = new GeneralParameterValue[length + 1];
							System.arraycopy(readParams, 0, readParams2, 0,
									length);
							readParams2[length] = readGG;
							coverage = (GridCoverage2D) reader
									.read(readParams2);
						}
					} else
						// we have no parameters hence we just use the read grid
						// geometry to get a coverage
						coverage = (GridCoverage2D) reader
								.read(new GeneralParameterValue[] { readGG });
				} else {
					coverage = (GridCoverage2D) reader
							.read(new GeneralParameterValue[] { readGG });
				}
//				METABUFFER SUPPORT
//				gcr.paint(graphics, coverage, symbolizer,metaBufferedEnvelope);
				gcr.paint(graphics, coverage, symbolizer);
			}
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Raster rendered");

		} catch (FactoryException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			fireErrorEvent(e);
		} catch (TransformException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			fireErrorEvent(e);
		} catch (NoninvertibleTransformException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			fireErrorEvent(e);
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			fireErrorEvent(e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			fireErrorEvent(e);
		}

	}

	/**
	 * This method captures a first attempt to handle in a coherent way the problem of having artifacts at the 
	 * borders of drown tiles when using Tiling Clients like OpenLayers with higher order interpolation.
	 * 
	 * @param mapExtent to draw on.
	 * @param java2dHints to control the drawing process.
	 * @param worldToScreen transformation that maps onto the screen.
	 * @return a {@link GeneralEnvelope} that might be expanded in order to avoid
	 * 		   artifacts at the borders
	 */
	private GeneralEnvelope handleTileBordersArtifacts(
			ReferencedEnvelope mapExtent, RenderingHints java2dHints,
			AffineTransform worldToScreen) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Check if interpolation is required.
		//
		// /////////////////////////////////////////////////////////////////////
		if (!java2dHints.containsKey(JAI.KEY_INTERPOLATION)) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Unable to find interpolation for this request.");
			return new GeneralEnvelope(mapExtent);
		}
		final Interpolation interp = (Interpolation) java2dHints
				.get(JAI.KEY_INTERPOLATION);

		// /////////////////////////////////////////////////////////////////////
		//
		// Ok, we have an interpolation, let's decide if we have to extend the
		// requested area accordingly.
		//
		// /////////////////////////////////////////////////////////////////////
		int buffer = 0;
		if (interp instanceof InterpolationNearest) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Interpolation Nearest no need for extending.");
			return new GeneralEnvelope(mapExtent);

		}
		if (interp instanceof InterpolationBilinear)
		{
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Interpolation Bilinear  extending.by 1 pixel at least");
			buffer = 10;
		}
		else if (interp instanceof InterpolationBicubic)
		{
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Interpolation Bicubic  extending.by 2 pixel at least");
			buffer = 30;
		}
		if (buffer <= 0) {
			return new GeneralEnvelope(mapExtent);
		}
		
		// /////////////////////////////////////////////////////////////////////
		//
		// Doing the extension
		//
		// /////////////////////////////////////////////////////////////////////
		final Envelope tempEnv =expandEnvelope(mapExtent, worldToScreen, buffer);
		final GeneralEnvelope newEnv = new GeneralEnvelope(
				new double[] { tempEnv.getMinX(),
						tempEnv.getMinY() , }, new double[] {
						tempEnv.getMaxX() ,
						tempEnv.getMaxY()  });
		newEnv.setCoordinateReferenceSystem(mapExtent
				.getCoordinateReferenceSystem());
		return newEnv;

	}

	/**
	 * Finds the geometric attribute requested by the symbolizer
	 * 
	 * @param drawMe
	 *            The feature
	 * @param s
	 * 
	 * /** Finds the geometric attribute requested by the symbolizer
	 * 
	 * @param drawMe
	 *            The feature
	 * @param s
	 *            The symbolizer
	 * @return The geometry requested in the symbolizer, or the default geometry
	 *         if none is specified
	 */
	private com.vividsolutions.jts.geom.Geometry findGeometry(Object drawMe,
			Symbolizer s) {
		PropertyName geomName = getGeometryPropertyName(s);

		// get the geometry
		Geometry geom = (Geometry) geomName.evaluate( drawMe, Geometry.class );

		// if the symbolizer is a point symbolizer generate a suitable location
		// to place the
		// point in order to avoid recomputing that location at each rendering
		// step
		if (s instanceof PointSymbolizer){
			geom = getCentroid(geom); // djb: major simpificatioN
        }
		return geom;
	}

	/**
	 * Finds the centroid of the input geometry if input = point, line, polygon
	 * --> return a point that represents the centroid of that geom if input =
	 * geometry collection --> return a multipoint that represents the centoid
	 * of each sub-geom
	 * 
	 * @param g
	 */
	private Geometry getCentroid(Geometry g) {
		if (g instanceof GeometryCollection) {
			final GeometryCollection gc = (GeometryCollection) g;
			final Coordinate[] pts = new Coordinate[gc.getNumGeometries()];
			final int length = gc.getNumGeometries();
			for (int t = 0; t < length; t++) {
				pts[t] = gc.getGeometryN(t).getCentroid().getCoordinate();
			}
			return g.getFactory().createMultiPoint(pts);
		} else if (g != null) {
			return g.getCentroid();
		}
		return null;
	}

	/**
	 * Finds the geometric attribute coordinate reference system.
	 * @param drawMe2 
	 * 
	 * @param f The feature
	 * @param s The symbolizer
	 * @return The geometry requested in the symbolizer, or the default geometry if none is specified
	 */
	private org.opengis.referencing.crs.CoordinateReferenceSystem findGeometryCS(
			MapLayer currLayer, Object drawMe, Symbolizer s) {


        if( drawMe instanceof Feature ){
            Feature f = (Feature) drawMe;
        
            String geomName = getGeometryPropertyName(s).getPropertyName();        
            if (geomName == null || "".equals(geomName)) {
                FeatureType schema = f.getFeatureType();
                GeometryAttributeType geom = schema.getDefaultGeometry();
                return geom.getCoordinateSystem();
            } else {
                FeatureType schema = f.getFeatureType();
                GeometryAttributeType geom = (GeometryAttributeType) schema.getAttributeType( geomName );
                return geom.getCoordinateSystem();
            }
        }
        else if ( currLayer.getSource() != null ) {
        	return currLayer.getSource().getInfo().getCRS();
        }
        
        return null;
	}

	private PropertyName getGeometryPropertyName(Symbolizer s) {
		String geomName;

		// TODO: fix the styles, the getGeometryPropertyName should probably be
		// moved into an
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
        else {
            geomName = "";
        }
                
        if( geomName == null ){
            return defaultGeometryPropertyName;
        }
		return filterFactory.property(geomName);
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
	 * Sets the interactive status of the renderer. An interactive renderer
	 * won't wait for long image loading, preferring an alternative mark instead
	 * 
	 * @param interactive
	 *            new value for the interactive property
	 */
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	/**
	 * <p>
	 * Returns true if the optimized data loading is enabled, false otherwise.
	 * </p>
	 * <p>
	 * When optimized data loading is enabled, lite renderer will try to load
	 * only the needed feature attributes (according to styles) and to load only
	 * the features that are in (or overlaps with)the bounding box requested for
	 * painting
	 * </p>
	 * 
	 */
	private boolean isOptimizedDataLoadingEnabled() {
		if (rendererHints == null)
			return optimizedDataLoadingEnabledDEFAULT;
		Object result = null;
		try{
			result=rendererHints
					.get("optimizedDataLoadingEnabled");
		}catch (ClassCastException e) {
			
		}
		if (result == null)
			return optimizedDataLoadingEnabledDEFAULT;
		return ((Boolean)result).booleanValue();
	}
    
    /**
     * <p>
     * Returns the rendering buffer, a measure in pixels used to expand the geometry search area
     * enough to capture the geometries that do stay outside of the current rendering bounds but
     * do affect them because of their large strokes (labels and graphic symbols are handled 
     * differently, see the label chache).
     * </p>
     * 
     */
    private int getRenderingBuffer() {
        if (rendererHints == null)
            return renderingBufferDEFAULT;
        Number result = (Number) rendererHints.get("renderingBuffer");
        if (result == null)
            return renderingBufferDEFAULT;
        return result.intValue();
    }
    
    /**
     * <p>
     * Returns scale computation algorithm to be used. 
     * </p>
     * 
     */
    private String getScaleComputationMethod() {
        if (rendererHints == null)
            return scaleComputationMethodDEFAULT;
        String result = (String) rendererHints.get("scaleComputationMethod");
        if (result == null)
            return scaleComputationMethodDEFAULT;
        return result;
    }

	/**
	 * Returns the generalization distance in the screen space.
	 * 
	 */
	public double getGeneralizationDistance() {
		return generalizationDistance;
	}

	/**
	 * <p>
	 * Sets the generalizazion distance in the screen space.
	 * </p>
	 * <p>
	 * Default value is 1, meaning that two subsequent points are collapsed to
	 * one if their on screen distance is less than one pixel
	 * </p>
	 * <p>
	 * Set the distance to 0 if you don't want any kind of generalization
	 * </p>
	 * 
	 * @param d
	 */
	public void setGeneralizationDistance(double d) {
		generalizationDistance = d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.renderer.GTRenderer#setJava2DHints(java.awt.RenderingHints)
	 */
	public void setJava2DHints(RenderingHints hints) {
		this.java2dHints = hints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.renderer.GTRenderer#getJava2DHints()
	 */
	public RenderingHints getJava2DHints() {
		return java2dHints;
	}

    public void setRendererHints(Map hints) {
    	if( hints!=null && hints.containsKey(LABEL_CACHE_KEY) ){
    		LabelCache cache=(LabelCache) hints.get(LABEL_CACHE_KEY);
    		if( cache==null )
    			throw new NullPointerException("Label_Cache_Hint has a null value for the labelcache");
    		
    		this.labelCache=cache;
    		this.painter=new StyledShapePainter(cache);
    	}
        rendererHints = hints;
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.renderer.GTRenderer#getRendererHints()
	 */
	public Map getRendererHints() {
		return rendererHints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.renderer.GTRenderer#setContext(org.geotools.map.MapContext)
	 */
	public void setContext(MapContext context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.renderer.GTRenderer#getContext()
	 */
	public MapContext getContext() {
		return context;
	}

	public boolean isCanTransform() {
		return canTransform;
	}

	public static MathTransform getMathTransform(
			CoordinateReferenceSystem sourceCRS,
			CoordinateReferenceSystem destCRS) {
		try {
			CoordinateOperation op = operationFactory.createOperation(
					sourceCRS, destCRS);
			if (op != null)
				return op.getMathTransform();
		} catch (OperationNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

		} catch (FactoryException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return null;
	}
}
