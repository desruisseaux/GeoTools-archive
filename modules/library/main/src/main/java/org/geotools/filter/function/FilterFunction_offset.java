package org.geotools.filter.function;

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

//this was autogenerated and then hand modified to implement better support for geometry
// transformations in SLD
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;

public class FilterFunction_offset extends FunctionExpressionImpl implements FunctionExpression,
        GeometryTransformation {

    public FilterFunction_offset() {
        super("offset");
    }

    public int getArgCount() {
        return 3;
    }

    public Object evaluate(Object feature) {
        Geometry geom = (Geometry) getExpression(0).evaluate(feature, Geometry.class);
        double offsetX = getExpression(1).evaluate(feature, Double.class);
        double offsetY = getExpression(2).evaluate(feature, Double.class);

        if (geom != null) {
            Geometry offseted = (Geometry) geom.clone();
            offseted.apply(new OffsetOrdinateFilter(offsetX, offsetY));
            return offseted;
        } else {
            return null;
        }
    }

    /**
     * Returns an translated rendering envelope if the offsets are not using feature attributes. If
     * the offsets are feature dependent the user will have to expand the rendering area via the
     * renderer buffer parameter
     */
    public ReferencedEnvelope invert(ReferencedEnvelope renderingEnvelope) {
        Double offsetX = getExpression(1).evaluate(null, Double.class);
        Double offsetY = getExpression(2).evaluate(null, Double.class);

        if (offsetX != null && offsetY != null) {
            ReferencedEnvelope offseted = new ReferencedEnvelope(renderingEnvelope);
            offseted.translate(offsetX, offsetY);
            return offseted;
        } else {
            return null;
        }
    }

    /**
     * Applies an offset to the X and Y coordinates
     */
    public static class OffsetOrdinateFilter implements CoordinateSequenceFilter {
        double offsetX;
        double offsetY;

        public OffsetOrdinateFilter(double offsetX, double offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public void filter(CoordinateSequence seq, int i) {
            seq.setOrdinate(i, 0, seq.getOrdinate(i, 0) + offsetX);
            seq.setOrdinate(i, 1, seq.getOrdinate(i, 1) + offsetY);
        }

        public boolean isDone() {
            return false;
        }

        public boolean isGeometryChanged() {
            return true;
        }

    }

}
