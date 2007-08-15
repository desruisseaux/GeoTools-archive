/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.builder.algorithm;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;


/**
 * Implementation of IDW Interpolation.
 *
 * @author jezekjan
 *
 */
public class IDWInterpolation extends AbstractInterpolation {
    /**
     *
     * @param positions HashMap containing {@link org.opengis.geometry.DirectPosition} as
     * key and value of general parameter as value
     * @param dx Value of step in x direction between generated cells
     * @param dy Value of step in y direction between generated cells
     * @param envelope Envelope that should be filled by generated grid
     */
    public IDWInterpolation(HashMap positions, double dx, double dy, Envelope envelope) {
        super(positions, dx, dy, envelope);
    }

    public IDWInterpolation(HashMap positions) {
        super(positions);
    }

    public float getGridValue(DirectPosition p) {
        return calculateValue(p);
    }

    /**
     * Computes nearest points.
     * @param p
     * @param maxdistance
     * @param number
     * @return
     *
     * @todo consider some indexing mechanism for finding the nearest positions
     */
    private HashMap getNearestPositions(DirectPosition p, double maxdistance) {
        HashMap nearest = new HashMap();

        DirectPosition source = null;
        double dist;

        for (Iterator i = this.getPositions().keySet().iterator(); i.hasNext();) {
            source = (DirectPosition) i.next();

            if ((source != null)
                    || source.getCoordinateReferenceSystem().getClass()
                                 .isAssignableFrom(DefaultGeographicCRS.class)) {
                dist = ((DefaultGeographicCRS) source.getCoordinateReferenceSystem()).distance(p
                        .getCoordinates(), source.getCoordinates()).doubleValue();
            } else {
                dist = ((Point2D) p).distance((Point2D) source);
            }

            if ((dist < maxdistance)) {
                nearest.put(source, new Double(dist));
            }
        }

        return nearest;
    }

    private float calculateValue(DirectPosition p) {
        double maxdist = 500000;

        HashMap nearest = getNearestPositions(p, maxdist);

        float value;
        double sumdValue = 0;
        double sumweight = 0;

        for (Iterator i = nearest.keySet().iterator(); i.hasNext();) {
            DirectPosition dp = (DirectPosition) i.next();
            double distance = ((Double) nearest.get(dp)).doubleValue();
            double weight = (1 / Math.pow(distance, 2));

            sumdValue = sumdValue + (float) ((Double) (this.getPositions().get(dp)) * weight);

            sumweight = sumweight + weight;
        }

        value = (float) (sumdValue / sumweight);

        return value;
    }
}
