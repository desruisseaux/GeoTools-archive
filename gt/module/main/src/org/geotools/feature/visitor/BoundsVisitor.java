/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.feature.visitor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.Feature;


/**
 * Calculates the extents (envelope) of the features it visits.
 *
 * @author Cory Horner, Refractions
 *
 * @since 2.2.M2
 */
public class BoundsVisitor implements FeatureCalc {
    Envelope bounds = new Envelope();

    public void visit(Feature feature) {
        Geometry geom = feature.getDefaultGeometry();
        Envelope bbox = geom.getEnvelopeInternal();

        bounds.expandToInclude(bbox);
    }

    public Envelope getBounds() {
        return bounds;
    }

    public void reset(Envelope bounds) {
        this.bounds = new Envelope();
    }

    public CalcResult getResult() {
        return new BoundsResult(bounds);
    }

    public static class BoundsResult extends AbstractCalcResult {
        private Envelope bbox;

        public BoundsResult(Envelope bbox) {
            this.bbox = bbox;
        }

        public Object getValue() {
            return new Envelope(bbox);
        }

        public boolean isCompatible(CalcResult targetResults) {
            //list each calculation result which can merge with this type of result
            if (targetResults instanceof BoundsResult) {
                return true;
            }

            return false;
        }

        public CalcResult merge(CalcResult resultsToAdd) {
            if (!isCompatible(resultsToAdd)) {
                throw new IllegalArgumentException(
                    "Parameter is not a compatible type");
            }

            if (resultsToAdd instanceof BoundsResult) {
                //add one set to the other (to create one big unique list)
                Envelope newBounds = new Envelope(bbox);
                newBounds.expandToInclude((Envelope) resultsToAdd.getValue());

                return new BoundsResult(newBounds);
            } else {
                throw new IllegalArgumentException(
                    "The CalcResults claim to be compatible, but the appropriate merge method has not been implemented.");
            }
        }
    }
}
