/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */

package org.geotools.swing.tool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import javax.measure.unit.Unit;
import javax.swing.ImageIcon;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.geotools.swing.JTextReporter;
import org.geotools.swing.TextReporterListener;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.utils.MapLayerUtils;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 * A cursor tool to retrieve information about features that the user clicks
 * on with the mouse.
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $Id$
 * @version $URL$
 */
public class InfoTool extends CursorTool implements TextReporterListener {

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/Text");

    /** The tool name */
    public static final String TOOL_NAME = stringRes.getString("tool_name_info");
    /** Tool tip text */
    public static final String TOOL_TIP = stringRes.getString("tool_tip_info");
    /** Cursor */
    public static final String CURSOR_IMAGE = "/org/geotools/swing/icons/mActionIdentify.png";
    /** Cursor hotspot coordinates */
    public static final Point CURSOR_HOTSPOT = new Point(0, 0);
    /** Icon for the control */
    public static final String ICON_IMAGE = "/org/geotools/swing/icons/mActionIdentify.png";

    /**
     * Default distance fraction. When the user clicks on the map, this tool
     * searches for point and line features that are within a given distance
     * (in world units) of the mouse location. That threshold distance is set
     * as the maximum map side length multiplied by the distance fraction.
     */
    public static final double DEFAULT_DISTANCE_FRACTION = 0.04d;

    private Cursor cursor;
    private FilterFactory2 filterFactory;
    private GeometryFactory geomFactory;

    private JTextReporter reporter;

    /**
     * Constructor
     */
    public InfoTool() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon cursorIcon = new ImageIcon(getClass().getResource(CURSOR_IMAGE));

        int iconWidth = cursorIcon.getIconWidth();
        int iconHeight = cursorIcon.getIconHeight();

        Dimension bestCursorSize = tk.getBestCursorSize(cursorIcon.getIconWidth(), cursorIcon.getIconHeight());

        cursor = tk.createCustomCursor(cursorIcon.getImage(), CURSOR_HOTSPOT, TOOL_TIP);
        filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        geomFactory = new GeometryFactory();
    }

    @Override
    public void onMouseClicked(MapMouseEvent ev) {
        DirectPosition2D pos = ev.getMapPosition();
        Unit<?> uom = getMapPane().getMapContext().getCoordinateReferenceSystem().getCoordinateSystem().getAxis(0).getUnit();

        ReferencedEnvelope mapEnv = getMapPane().getDisplayArea();
        double len = Math.max(mapEnv.getWidth(), mapEnv.getHeight());
        double thresholdDistance = len * DEFAULT_DISTANCE_FRACTION;
        String uomName = uom.toString();

        Geometry posGeom = geomFactory.createPoint(new Coordinate(pos.x, pos.y));

        report(pos);

        for (MapLayer layer : getMapPane().getMapContext().getLayers()) {
            FeatureIterator<? extends Feature> iter = null;
            if (layer.isSelected()) {
                String layerName = layer.getTitle();
                if (layerName == null || layerName.length() == 0) {
                    layerName = layer.getFeatureSource().getName().getLocalPart();
                }
                if (layerName == null || layerName.length() == 0) {
                    layerName = layer.getFeatureSource().getSchema().getName().getLocalPart();
                }

                try {
                    Map<String, Object> result = MapLayerUtils.isGridLayer(layer);
                    if ((Boolean)result.get(MapLayerUtils.IS_GRID_KEY)) {
                        /*
                         * For grid coverages we directly evaluate the band values
                         */
                        iter = layer.getFeatureSource().getFeatures().features();
                        String gridAttr = (String) result.get(MapLayerUtils.GRID_ATTR_KEY);
                        Object obj = iter.next().getProperty(gridAttr).getValue();
                        GridCoverage2D cov = null;

                        if ((Boolean)result.get(MapLayerUtils.IS_GRID_READER_KEY)) {
                            AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) obj;
                            cov = reader.read(null);
                        } else {
                            cov = (GridCoverage2D) obj;
                        }

                        try {
                            Object objArray = cov.evaluate(pos);
                            Number[] bandValues = asNumberArray(objArray);
                            report(layerName, bandValues);

                        } catch (CannotEvaluateException ex) {
                            // do nothing - point outside coverage
                        }

                    } else {
                        Filter filter = null;
                        GeometryDescriptor geomDesc = layer.getFeatureSource().getSchema().getGeometryDescriptor();
                        String attrName = geomDesc.getLocalName();
                        Class<?> geomClass = geomDesc.getType().getBinding();

                        if (Polygon.class.isAssignableFrom(geomClass) ||
                                MultiPolygon.class.isAssignableFrom(geomClass)) {
                            /*
                             * For polygons we test if they contain mouse location
                             */
                            filter = filterFactory.intersects(
                                    filterFactory.property(attrName),
                                    filterFactory.literal(posGeom));
                        } else {
                            /*
                             * For point and line features we test if the are near
                             * the mouse location
                             */
                            filter = filterFactory.dwithin(
                                    filterFactory.property(attrName),
                                    filterFactory.literal(posGeom),
                                    thresholdDistance, uomName);
                        }

                        FeatureCollection<? extends FeatureType, ? extends Feature> selectedFeatures =
                                layer.getFeatureSource().getFeatures(filter);

                        iter = selectedFeatures.features();
                        while (iter.hasNext()) {
                            report(layerName, iter.next());
                        }

                    }
                } catch (IOException ioEx) {
                } finally {
                    if (iter != null) {
                        iter.close();
                    }
                }
            }
        }
    }

    /**
     * Write the mouse click position to a {@code JTextReporter}
     *
     * @param pos mouse click position (world coords)
     */
    private void report(DirectPosition2D pos) {
        createReporter();

        reporter.append(String.format("Pos x=%.4f y=%.4f\n\n", pos.x, pos.y));
    }

    /**
     * Write the feature attribute names and values to a
     * {@code JTextReporter}
     *
     * @param layerName name of the map layer that contains this feature
     * @param feature the feature to report on
     */
    private void report(String layerName, Feature feature) {
        createReporter();

        Collection<Property> props = feature.getProperties();
        String valueStr = null;

        reporter.append(layerName);
        reporter.append("\n");

        for (Property prop : props) {
            String name = prop.getName().getLocalPart();
            Object value = prop.getValue();

            if (value instanceof Geometry) {
                name = "  Geometry";
                valueStr = value.getClass().getSimpleName();
            } else {
                valueStr = value.toString();
            }

            reporter.append(name + ": " + valueStr);
            reporter.append("\n");
        }
        reporter.append("\n");
    }

    /**
     * Write an array of grid coverage band values to a
     * {@code JTextReporter}
     *
     * @param layerName name of the map layer that contains the grid coverage
     * @param bandValues array of values
     */
    private void report(String layerName, Number[] bandValues) {
        createReporter();

        reporter.append(layerName);
        reporter.append("\n");

        for (int i = 0; i < bandValues.length; i++) {
            reporter.append(String.format("  Band %d: %s\n", (i+1), bandValues[i].toString()));
        }
        reporter.append("\n");
    }

    /**
     * Create and show a {@code JTextReporter} if one is not already active
     * for this tool
     */
    private void createReporter() {
        if (reporter == null) {
            reporter = new JTextReporter("Feature info");
            reporter.addListener(this);

            reporter.setSize(400, 400);
            reporter.setVisible(true);
        }
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public boolean drawDragBox() {
        return false;
    }

    /**
     * Called when a {@code JTextReporter} frame that was being used by this tool
     * is closed by the user
     *
     * @param ev event published by the {@code JTextReporter}
     */
    public void onReporterClosed(WindowEvent ev) {
        reporter = null;
    }

    /**
     * Convert the Object returned by {@linkplain GridCoverage2D#evaluate(DirectPosition)} into
     * an array of Numbers
     *
     * @param objArray an Object representing a primitive array
     *
     * @return a new array of Numbers
     */
    private Number[] asNumberArray(Object objArray) {
        Number[] numbers = null;

        if (objArray instanceof byte[]) {
            byte[] values = (byte[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = ((int)values[i]) & 0xff;
            }

        } else if (objArray instanceof int[]) {
            int[] values = (int[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = values[i];
            }

        } else if (objArray instanceof float[]) {
            float[] values = (float[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = values[i];
            }
        } else if (objArray instanceof double[]) {
            double[] values = (double[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = values[i];
            }
        }

        return numbers;
    }
}
