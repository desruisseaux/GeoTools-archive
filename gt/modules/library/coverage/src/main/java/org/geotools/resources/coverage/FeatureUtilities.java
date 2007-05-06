/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, Geotools Project Managment Committee (PMC)
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
package org.geotools.resources.coverage;

import java.awt.geom.Rectangle2D;

import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.resources.CRSUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * A set of utilities methods for interactions between {@link GridCoverage}
 * and {@link Feature}. Those methods are not really rigorous; must of them
 * should be seen as temporary implementations.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 */
public final class FeatureUtilities {
    /**
     * Do not allows instantiation of this class.
     */
    private FeatureUtilities() {
    }

    /**
     * Returns the polygon surrounding the specified rectangle.
     * Code lifted from ArcGridDataSource (temporary).
     */
    private static Polygon getPolygon(final Rectangle2D rect) {
        final PrecisionModel  pm = new PrecisionModel();
        final GeometryFactory gf = new GeometryFactory(pm, 0);
        final Coordinate[] coord = new Coordinate[] {
            new Coordinate(rect.getMinX(), rect.getMinY()),
            new Coordinate(rect.getMaxX(), rect.getMinY()),
            new Coordinate(rect.getMaxX(), rect.getMaxY()),
            new Coordinate(rect.getMinX(), rect.getMaxY()),
            new Coordinate(rect.getMinX(), rect.getMinY())
        };
        final LinearRing ring = gf.createLinearRing(coord);
        return new Polygon(ring, null, gf);
    }

    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     *
     * @param  coverage the grid coverage.
     * @return a feature with the grid coverage envelope as the geometry and the
     *         grid coverage itself in the "grid" attribute.
     */
    public static FeatureCollection wrapGridCoverage(final GridCoverage2D coverage)
            throws TransformException, SchemaException, IllegalAttributeException
    {
        final Polygon bounds = getPolygon(coverage.getEnvelope2D());
        final CoordinateReferenceSystem sourceCRS =
                CRSUtilities.getCRS2D(coverage.getCoordinateReferenceSystem());

        // create the feature type
        final GeometricAttributeType geom = new GeometricAttributeType("geom",
                        Polygon.class, true, 1, 1, null, sourceCRS, null);
        final AttributeType grid = AttributeTypeFactory.newAttributeType(
                        "grid", GridCoverage.class);

        final AttributeType[] attTypes = { geom, grid };
        // Fix the schema name
        final String typeName = "GridCoverage";
        final DefaultFeatureType schema = (DefaultFeatureType) FeatureTypeBuilder
                        .newFeatureType(attTypes, typeName);

        // create the feature
        Feature feature = schema.create(new Object[] { bounds, coverage });

        final FeatureCollection collection = FeatureCollections.newCollection();
        collection.add(feature);

        return collection;
    }

    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     *
     * @param  reader the grid coverage reader.
     * @return a feature with the grid coverage envelope as the geometry and the
     *         grid coverage itself in the "grid" attribute.
     */
    public static FeatureCollection wrapGridCoverageReader(final AbstractGridCoverage2DReader reader)
            throws TransformException, SchemaException, IllegalAttributeException
    {
        // create surrounding polygon
        final Polygon bounds = getPolygon(reader.getOriginalEnvelope().toRectangle2D());
        final CoordinateReferenceSystem sourceCRS = CRSUtilities.getCRS2D(reader.getCrs());

        // create the feature type
        final GeometricAttributeType geom = new GeometricAttributeType("geom",
                        Polygon.class, true, 1, 1, null, sourceCRS, null);
        final AttributeType grid = AttributeTypeFactory.newAttributeType(
                        "grid", AbstractGridCoverage2DReader.class);

        final AttributeType[] attTypes = { geom, grid };
        // Fix the schema name
        final String typeName = "GridCoverageReader";
        final DefaultFeatureType schema = (DefaultFeatureType) FeatureTypeBuilder
                        .newFeatureType(attTypes, typeName);

        // create the feature
        Feature feature = schema.create(new Object[] { bounds, reader });

        final FeatureCollection collection = FeatureCollections.newCollection();
        collection.add(feature);

        return collection;
    }
}
