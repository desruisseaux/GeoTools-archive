/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.map.map2d;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.MapLayer;

/**
 *
 * @author Johann Sorel
 */
public class DefaultSelectableMap2D extends DefaultNavigableMap2D implements SelectableMap2D{

    public void setSelectionEnable(boolean enable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSelectionEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSelectableLayer(MapLayer layer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSelectableLayer(MapLayer[] layer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MapLayer[] getSelectableLayer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void doSelection(double x, double y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void doSelection(Envelope env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSelection(FeatureCollection selection) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FeatureCollection getSelection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
