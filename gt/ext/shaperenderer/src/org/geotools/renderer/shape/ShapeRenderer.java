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
import java.awt.geom.AffineTransform;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileUtil;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.LabelCacheDefault;
import org.geotools.renderer.lite.ListenerList;
import org.geotools.renderer.lite.RenderListener;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.geotools.util.NumberRange;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A LiteRenderer Implementations that is optimized for shapefiles.
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class ShapeRenderer {
	static final Logger LOGGER = Logger
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

	/**
	 * This listener is added to the list of listeners automatically. It should
	 * be removed if the default logging is not needed.
	 */
	public static final DefaultRenderListener DEFAULT_LISTENER = new DefaultRenderListener();

	private double scaleDenominator;

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
			try {
				// extract the feature type stylers from the style object
				// and process them
				processStylers(graphics, currLayer, currLayer.getStyle()
						.getFeatureTypeStyles(), transform, context
						.getCoordinateReferenceSystem());
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

	private void processStylers(Graphics2D graphics, MapLayer currLayer,
			FeatureTypeStyle[] featureStylers, AffineTransform at,
			CoordinateReferenceSystem destinationCrs) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("processing " + featureStylers.length + " stylers");
		}

		for (int i = 0; i < featureStylers.length; i++) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("processing style " + i);
			}

			FeatureTypeStyle fts = featureStylers[i];

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
			// TODO: find a better way to declare the scale ranges so that we
			// get style caching also between multiple rendering runs
			NumberRange scaleRange = new NumberRange(scaleDenominator,
					scaleDenominator);

			ShapefileDataStore ds = ((ShapefileDataStore) currLayer
					.getFeatureSource().getDataStore());
			ShapefileReader shpreader = ShapefileUtil.getShpReader(ds);

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
						LOGGER.fine("trying to read Feature ...");
					}
					
					ShapefileReader.Record record=shpreader.nextRecord();
					
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
									fts.getFeatureTypeName()) || typeName
									.equalsIgnoreCase(fts.getFeatureTypeName()))) {
						// applicable rules
						for (Iterator it = ruleList.iterator(); it.hasNext();) {

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
								processSymbolizers(graphics, feature,
										symbolizers, scaleRange, at,
										destinationCrs);

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

							for (Iterator it = elseRuleList.iterator(); it
									.hasNext();) {
								Rule r = (Rule) it.next();
								Symbolizer[] symbolizers = r.getSymbolizers();

								if (LOGGER.isLoggable(Level.FINER)) {
									LOGGER.finer("processing Symobolizer ...");
								}

								processSymbolizers(graphics, feature,
										symbolizers, scaleRange, at,
										destinationCrs);

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
}
