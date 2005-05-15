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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.geotools.factory.FactoryConfigurationError;
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
import org.geotools.geometry.JTS;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.LabelCacheDefault;
import org.geotools.renderer.lite.ListenerList;
import org.geotools.renderer.lite.LiteCoordinateSequenceFactory;
import org.geotools.renderer.lite.LiteShape2;
import org.geotools.renderer.lite.RenderListener;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleAttributeExtractor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
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
 */
public class ShapeRenderer {
	public static final Logger LOGGER = Logger
			.getLogger("org.geotools.renderer.shape");

	/** Tolerance used to compare doubles for equality */
	private static final double TOLERANCE = 1e-6;

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

	private static final GeometryFactory geomFactory = new GeometryFactory(new LiteCoordinateSequenceFactory());

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
		MULTI_LINE_GEOM = geomFactory
				.createMultiLineString(new LineString[] { LINE_GEOM });
		POLYGON_GEOM = geomFactory.createPolygon(LINE_GEOM, new LinearRing[0]);
		MULTI_POLYGON_GEOM = geomFactory
				.createMultiPolygon(new Polygon[] { POLYGON_GEOM });
		POINT_GEOM = geomFactory.createPoint(COORDS[2]);
		MULTI_POINT_GEOM = geomFactory.createMultiPoint(COORDS);
	}

	/**
	 * This listener is added to the list of listeners automatically. It should
	 * be removed if the default logging is not needed.
	 */
	public static final DefaultRenderListener DEFAULT_LISTENER = new DefaultRenderListener();

	private double scaleDenominator;

	DbaseFileHeader dbfheader;

	private Object defaultGeom;

	public ShapeRenderer(MapContext context) {
		this.context = context;
	}

	public void paint(Graphics2D graphics, Rectangle paintArea,
			Envelope envelope) {
		AffineTransform transform = worldToScreenTransform(envelope, paintArea);

		if (hints != null)
			graphics.setRenderingHints(hints);
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
		 * If we are rendering to a component which has already set up some form
		 * of transformation then we can concatenate our transformation to it.
		 * An example of this is the ZoomPane component of the swinggui module.
		 */
		if (concatTransforms) {
			AffineTransform atg = graphics.getTransform();
			graphics.setTransform(new AffineTransform());
			atg.concatenate(transform);
			transform = atg;
		}

		//      graphics.setTransform(at);
		setScaleDenominator(1 / transform.getScaleX());
		MapLayer[] layers = context.getLayers();
		// get detstination CRS
		CoordinateReferenceSystem destinationCrs = context
				.getCoordinateReferenceSystem();
		labelCache.start();
		for (int i = 0; i < layers.length; i++) {

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
				ShapefileDataStore ds = (ShapefileDataStore) currLayer
						.getFeatureSource().getDataStore();
				dbfheader = ShapefileRendererUtil.getDBFReader(ds).getHeader();
				CoordinateReferenceSystem dataCRS = ds.getSchema()
						.getDefaultGeometry().getCoordinateSystem();
				MathTransform mt;
				try {
					mt = CRS.transform(dataCRS, destinationCrs);
					bbox = JTS.transform(bbox, mt.inverse());
				} catch (Exception e) {
					mt = null;
				}

				MathTransform at = FactoryFinder.getMathTransformFactory(null)
						.createAffineTransform(new GeneralMatrix(transform));
				if (mt == null) {
					mt = at;
				} else {
					mt = FactoryFinder.getMathTransformFactory(null)
							.createConcatenatedTransform(mt, at);
				}

				//				graphics.setTransform(transform);

				// extract the feature type stylers from the style object
				// and process them
				if (isCaching() && geometryCache.size() > 0)
					processStylersCaching(graphics, ds, bbox, mt, currLayer
							.getStyle());
				else
					processStylersNoCaching(graphics, ds, bbox, mt, currLayer
							.getStyle());
			} catch (Exception exception) {
				fireErrorEvent(new Exception("Exception rendering layer "
						+ currLayer, exception));
			}

			labelCache.endLayer(graphics, paintArea);
		}
		labelCache.end(graphics, paintArea);
		LOGGER.fine("Style cache hit ratio: " + styleFactory.getHitRatio()
				+ " , hits " + styleFactory.getHits() + ", requests "
				+ styleFactory.getRequests());
	}

	private void processStylersNoCaching(Graphics2D graphics,
			ShapefileDataStore datastore, Envelope bbox, MathTransform mt,
			Style style) throws IOException {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("processing " + style.getFeatureTypeStyles().length
					+ " stylers");
		}
		FeatureTypeStyle[] featureStylers = style.getFeatureTypeStyles();
		FeatureType type;
		ShapefileReader shpreader = null;
		try {
			type = createFeatureType(style, datastore.getSchema());
			shpreader = ShapefileRendererUtil.getShpReader(datastore, bbox, mt);
		} catch (Exception e) {
			fireErrorEvent(e);
			return;
		}

		for (int i = 0; i < featureStylers.length; i++) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("processing style " + i);
			}

			FeatureTypeStyle fts = featureStylers[i];
			DbaseFileReader dbfreader = ShapefileRendererUtil
					.getDBFReader(datastore);
			String typeName = datastore.getSchema().getTypeName();

			if ((typeName != null)
					&& (datastore.getSchema().isDescendedFrom(null,
							fts.getFeatureTypeName()) || typeName
							.equalsIgnoreCase(fts.getFeatureTypeName()))) {
				// get applicable rules at the current scale
				Rule[] rules = fts.getRules();
				List ruleList = new ArrayList();
				List elseRuleList = new ArrayList();

				for (int j = 0; j < rules.length; j++) {
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
				NumberRange scaleRange = new NumberRange(scaleDenominator,
						scaleDenominator);

				int index = 1;
				try {
					while (true) {
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

							ShapefileReader.Record record = shpreader
									.nextRecord();

							SimpleGeometry geom = (SimpleGeometry) record.shape();
							if (geom == null) {
								dbfreader.skip();
								continue;
							}
							Feature feature = createFeature(type, record,
									dbfreader, typeName + index);
							index++;

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
								LOGGER.fine("... done: " + typeName);
							}

							//						 applicable rules
							for (Iterator it = ruleList.iterator(); it
									.hasNext();) {

								Rule r = (Rule) it.next();

								if (LOGGER.isLoggable(Level.FINER)) {
									LOGGER.finer("applying rule: "
											+ r.toString());
								}

								if (LOGGER.isLoggable(Level.FINER)) {
									LOGGER.finer("this rule applies ...");
								}

								Filter filter = r.getFilter();

								if ((filter == null)
										|| filter.contains(feature)) {
									doElse = false;

									if (LOGGER.isLoggable(Level.FINER)) {
										LOGGER
												.finer("processing Symobolizer ...");
									}

									Symbolizer[] symbolizers = r
											.getSymbolizers();

									processSymbolizers(graphics, feature, geom,
											symbolizers, scaleRange);

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

								for (Iterator it = elseRuleList.iterator(); it
										.hasNext();) {
									Rule r = (Rule) it.next();
									Symbolizer[] symbolizers = r
											.getSymbolizers();

									if (LOGGER.isLoggable(Level.FINER)) {
										LOGGER
												.finer("processing Symobolizer ...");
									}

									processSymbolizers(graphics, feature, geom,
											symbolizers, scaleRange);

									if (LOGGER.isLoggable(Level.FINER)) {
										LOGGER.finer("... done!");
									}
								}
							}
							if (LOGGER.isLoggable(Level.FINER)) {
								LOGGER.finer("feature rendered event ...");
							}

							fireFeatureRenderedEvent(null);
						} catch (Exception e) {
							fireErrorEvent(e);
						}
					}
				} finally {
					dbfreader.close();
					shpreader.close();
				}
			}
		}
	}

	private void processStylersCaching(Graphics2D graphics,
			ShapefileDataStore datastore, Envelope bbox, MathTransform mt,
			Style style) throws IOException {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("processing " + style.getFeatureTypeStyles().length
					+ " stylers");
		}
		FeatureTypeStyle[] featureStylers = style.getFeatureTypeStyles();
		FeatureType type;
		try {
			type = createFeatureType(style, datastore.getSchema());
		} catch (Exception e) {
			fireErrorEvent(e);
			return;
		}

		for (int i = 0; i < featureStylers.length; i++) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("processing style " + i);
			}

			FeatureTypeStyle fts = featureStylers[i];
			String typeName = datastore.getSchema().getTypeName();

			if ((typeName != null)
					&& (datastore.getSchema().isDescendedFrom(null,
							fts.getFeatureTypeName()) || typeName
							.equalsIgnoreCase(fts.getFeatureTypeName()))) {
				// get applicable rules at the current scale
				Rule[] rules = fts.getRules();
				List ruleList = new ArrayList();
				List elseRuleList = new ArrayList();

				for (int j = 0; j < rules.length; j++) {
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
				NumberRange scaleRange = new NumberRange(scaleDenominator,
						scaleDenominator);

				int index = 0;
				Iterator featureIter = featureCache.iterator();
				for (Iterator iter = geometryCache.iterator(); iter.hasNext();) {
					SimpleGeometry geom = (SimpleGeometry) iter.next();
					Feature feature = (Feature) featureIter.next();

					try {

						if (renderingStopRequested) {
							break;
						}

						boolean doElse = true;

						if (LOGGER.isLoggable(Level.FINER)) {
							LOGGER.fine("trying to read geometry ...");
						}

						if (LOGGER.isLoggable(Level.FINEST)) {
							LOGGER.finest("... done: " + geom.toString());
						}

						if (LOGGER.isLoggable(Level.FINER)) {
							LOGGER.fine("... done: " + typeName);
						}

						//						 applicable rules
						for (Iterator it = ruleList.iterator(); it.hasNext();) {

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

								processSymbolizers(graphics, feature, geom,
										symbolizers, scaleRange);

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

							for (Iterator it = elseRuleList.iterator(); it
									.hasNext();) {
								Rule r = (Rule) it.next();
								Symbolizer[] symbolizers = r.getSymbolizers();

								if (LOGGER.isLoggable(Level.FINER)) {
									LOGGER.finer("processing Symobolizer ...");
								}

								processSymbolizers(graphics, feature, geom,
										symbolizers, scaleRange);

								if (LOGGER.isLoggable(Level.FINER)) {
									LOGGER.finer("... done!");
								}
							}
						}
						if (LOGGER.isLoggable(Level.FINER)) {
							LOGGER.finer("feature rendered event ...");
						}

						fireFeatureRenderedEvent(null);
					} catch (Exception e) {
						fireErrorEvent(e);
					}
				}
			}
		}
	}

	/**
	 * @param type
	 * @param record
	 * @param dbfreader
	 * @return
	 * @throws Exception
	 */
	Feature createFeature(FeatureType type, Record record,
			DbaseFileReader dbfreader, String id) throws Exception {

		if (type.getAttributeCount() == 1) {
			return type.create(new Object[1], id);
		} else {
			DbaseFileHeader header = dbfreader.getHeader();

			Object[] all = dbfreader.readEntry();
			Object[] values = new Object[type.getAttributeCount()];
			for (int i = 0; i < values.length - 1; i++) {
				values[i] = all[attributeIndexing[i]];
				if (header.getFieldName(attributeIndexing[i]).equals(
						type.getAttributeType(i))) {
					System.out.println("ok");
				}
			}
			values[values.length - 1] = getGeom(type.getDefaultGeometry());
			return type.create(values, id);
		}
	}

	/**
	 * @param defaultGeometry
	 * @return
	 */
	private Object getGeom(GeometryAttributeType defaultGeometry) {
		if (defaultGeom == null) {
			if (MultiPolygon.class.isAssignableFrom(defaultGeometry.getType())) {
				defaultGeom = MULTI_POLYGON_GEOM;
			} else if (MultiLineString.class.isAssignableFrom(defaultGeometry
					.getType())) {
				defaultGeom = MULTI_LINE_GEOM;
			} else if (Point.class.isAssignableFrom(defaultGeometry.getType())) {
				defaultGeom = POINT_GEOM;
			} else if (MultiPoint.class.isAssignableFrom(defaultGeometry
					.getType())) {
				defaultGeom = MULTI_POINT_GEOM;
			}
		}

		return defaultGeom;
	}

	/**
	 * Maps between the AttributeType index of the new generated FeatureType and
	 * the real attributeType
	 */
	int[] attributeIndexing;

	/** The painter class we use to depict shapes onto the screen */
	private StyledShapePainter painter = new StyledShapePainter(labelCache);

	private static final int NUM_SAMPLES = 30;

	/**
	 * @param style
	 * @return
	 * @throws SchemaException
	 * @throws FactoryConfigurationError
	 */
	FeatureType createFeatureType(Style style, FeatureType schema)
			throws FactoryConfigurationError, SchemaException {
		String[] attributes = findStyleAttributes(style, schema);
		AttributeType[] types = new AttributeType[attributes.length];
		attributeIndexing = new int[attributes.length];

		for (int i = 0; i < types.length; i++) {
			types[i] = schema.getAttributeType(attributes[i]);
			for (int j = 0; j < dbfheader.getNumFields(); j++) {
				if (dbfheader.getFieldName(j).equals(attributes[i])) {
					attributeIndexing[i] = j;
					break;
				}
			}
		}

		FeatureType type = FeatureTypeBuilder.newFeatureType(types, schema
				.getTypeName(), schema.getNamespace(), false, null, schema
				.getDefaultGeometry());
		return type;
	}

	/**
	 * Inspects the <code>MapLayer</code>'s style and retrieves it's needed
	 * attribute names, returning at least the default geometry attribute name.
	 * 
	 * @param style
	 *            the <code>Style</code> to determine the needed attributes
	 *            from
	 * @param schema
	 *            the featuresource schema
	 * @return the minimun set of attribute names needed to render
	 *         <code>layer</code>
	 */
	private String[] findStyleAttributes(Style style, FeatureType schema) {
		StyleAttributeExtractor sae = new StyleAttributeExtractor();
		sae.visit(style);

		String[] ftsAttributes = sae.getAttributeNames();

		return ftsAttributes;
	}

	/**
	 * @param graphics
	 * @param geom
	 * @param symbolizers
	 * @param scaleRange
	 */
	private void processSymbolizers(Graphics2D graphics, Feature feature,
			SimpleGeometry geom, Symbolizer[] symbolizers, NumberRange scaleRange) {
		for (int m = 0; m < symbolizers.length; m++) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("applying symbolizer " + symbolizers[m]);
			}
			if (renderingStopRequested) {
				break;
			}

			if (symbolizers[m] instanceof TextSymbolizer) {
				try{
				labelCache.put((TextSymbolizer) symbolizers[m], feature, getLiteShape2(geom, feature),
						scaleRange);
				}catch (Exception e) {
					fireErrorEvent(e);
				}
			} else {
				Style2D style = styleFactory.createStyle(feature,
						symbolizers[m], scaleRange);
				painter
						.paint(graphics, getShape(geom), style,
								scaleDenominator);
			}

		}

	}

	/**
	 * Creates a JTS shape that is an approximation of the SImpleGeometry.  This is ONLY use
	 * for labelling and is only created if a text symbolizer is part of the current style.
	 * 
	 * @param geom the geometry to wrap
	 * @param feature the current feature.
	 * @return
	 * @throws FactoryException
	 * @throws TransformException
	 */
	private LiteShape2 getLiteShape2(SimpleGeometry geom, Feature feature) throws TransformException, FactoryException {
		Class geomType=feature.getFeatureType().getDefaultGeometry().getType();
		
		Geometry jtsGeom;
		LiteCoordinateSequenceFactory seqFactory=new LiteCoordinateSequenceFactory();
		
		if ( MultiPolygon.class.isAssignableFrom(geomType) ){
			double[] points=getPointSample(geom);
			CoordinateSequence seq=seqFactory.create(points);
			Polygon poly=geomFactory.createPolygon(geomFactory.createLinearRing(seq),
					new LinearRing[]{});
			jtsGeom=geomFactory.createMultiPolygon(new Polygon[]{poly});
		}else if( MultiLineString.class.isAssignableFrom(geomType)){
			double[] points=getPointSample(geom);
			CoordinateSequence seq=seqFactory.create(points);
			jtsGeom=geomFactory.createMultiLineString(new LineString[]{geomFactory.createLineString(seq)});
		}else if( MultiPoint.class.isAssignableFrom(geomType)){
			double[] points=getPointSample(geom);
			CoordinateSequence seq=seqFactory.create(points);
			jtsGeom=geomFactory.createMultiPoint(seq);
		}else{
			jtsGeom=geomFactory.createPoint(new Coordinate(geom.coords[0][0],geom.coords[0][1]));
		}
		LiteShape2 shape=new LiteShape2(jtsGeom,null,null,false);
		return shape;
	}

	/**
	 * takes a random sampling from the geometry.  Only uses the larges part of the geometry.
	 * @param geom
	 * @return
	 */
	private double[] getPointSample(SimpleGeometry geom) {
		int largestPart=0;
		for( int i=0; i< geom.coords.length; i++){
			if( geom.coords[i].length>geom.coords[largestPart].length){
				largestPart=i;
			}
		}
		int step=geom.coords[largestPart].length<NUM_SAMPLES?1:(int)(geom.coords[largestPart].length/NUM_SAMPLES);
		int size=Math.min(geom.coords[largestPart].length, NUM_SAMPLES);
		double[] coords=new double[size];
		int location = 0;
		for (int i=0; i < coords.length-1; i++, location+=step) {
			 coords[i]=geom.coords[largestPart][location];
		}
		coords[size-1]=geom.coords[largestPart][geom.coords[largestPart].length-1];
		return coords;
	}

	/**
	 * @param geom
	 * @return
	 */
	private Shape getShape(SimpleGeometry geom) {
		if (geom.type == ShapeType.ARC || geom.type == ShapeType.ARCM
				|| geom.type == ShapeType.ARCZ)
			return new MultiLineShape(geom);
		if (geom.type == ShapeType.POLYGON || geom.type == ShapeType.POLYGONM
				|| geom.type == ShapeType.POLYGONZ)
			return new PolygonShape(geom);
		return null;
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

	private void fireFeatureRenderedEvent(Feature feature) {
		Object[] objects = renderListeners.getListeners();
		for (int i = 0; i < objects.length; i++) {
			RenderListener listener = (RenderListener) objects[i];
			listener.featureRenderer(feature);
		}
	}

	private void fireErrorEvent(Exception e) {
		Object[] objects = renderListeners.getListeners();
		for (int i = 0; i < objects.length; i++) {
			RenderListener listener = (RenderListener) objects[i];
			listener.errorOccurred(e);
		}
	}

	/**
	 * Sets up the affine transform
	 * 
	 * @param mapExtent
	 *            the map extent
	 * @param screenSize
	 *            the screen size
	 * @return a transform that maps from real world coordinates to the screen
	 */
	public AffineTransform worldToScreenTransform(Envelope mapExtent,
			Rectangle screenSize) {
		double scaleX = screenSize.getWidth() / mapExtent.getWidth();
		double scaleY = screenSize.getHeight() / mapExtent.getHeight();

		double tx = -mapExtent.getMinX() * scaleX;
		double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();

		AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY,
				tx, ty);

		return at;
	}

	/**
	 * Setter for property scaleDenominator.
	 * 
	 * @param scaleDenominator
	 *            New value of property scaleDenominator.
	 */
	protected void setScaleDenominator(double scaleDenominator) {
		this.scaleDenominator = scaleDenominator;
	}

	/**
	 * If you call this method from another thread than the one that called
	 * <code>paint</code> or <code>render</code> the rendering will be
	 * forcefully stopped before termination
	 */
	public void stopRendering() {
		renderingStopRequested = true;
	}

	/**
	 * By default ignores all feature renderered events and logs all exceptions
	 * as severe.
	 */
	private static class DefaultRenderListener implements RenderListener {
		/**
		 * @see org.geotools.renderer.lite.RenderListener#featureRenderer(org.geotools.feature.Feature)
		 */
		public void featureRenderer(Feature feature) {
			//do nothing.
		}

		/**
		 * @see org.geotools.renderer.lite.RenderListener#errorOccurred(java.lang.Exception)
		 */
		public void errorOccurred(Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	/**
	 * @return Returns the caching.
	 */
	public boolean isCaching() {
		return caching;
	}

	/**
	 * @param caching
	 *            The caching to set.
	 */
	public void setCaching(boolean caching) {
		this.caching = caching;
	}

	MapContext getContext() {
		return context;
	}

	public RenderingHints getRenderHints() {
		return hints;
	}

	public void setRenderHints(RenderingHints hints) {
		this.hints = hints;
	}

	public void setRenderingHint(RenderingHints.Key key, Object value) {
		if (hints == null)
			hints = new RenderingHints(key, value);
		else
			hints.put(key, value);
	}

	private class NonValidatingFeature extends DefaultFeature {

		/**
		 * @param schema
		 * @param attributes
		 * @throws IllegalAttributeException
		 */
		protected NonValidatingFeature(DefaultFeatureType schema,
				Object[] attributes) throws IllegalAttributeException {
			super(schema, attributes);
			// TODO Auto-generated constructor stub
		}

		/**
		 * 
		 * @see org.geotools.feature.DefaultFeature#setAttributes(java.lang.Object[])
		 */
		public void setAttributes(Object[] attributes)
				throws IllegalAttributeException {
			for (int i = 0; i < attributes.length; i++) {
				setAttribute(i, attributes[i]);
			}
		}

		public void setAttribute(int position, Object val) {
			setAttributeValue(position, val);
		}

	}

	public boolean isConcatTransforms() {
		return concatTransforms;
	}

	public void setConcatTransforms(boolean concatTransforms) {
		this.concatTransforms = concatTransforms;
	}
}
