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

package org.geotools.gui.swing.map.map2d.listener;

import com.vividsolutions.jts.geom.Envelope;
import java.util.EventObject;
import org.geotools.gui.swing.map.map2d.Map2D;

/**
 *
 * @author Johann Sorel
 */
public class Map2DMapAreaEvent extends EventObject{

    private Envelope oldEnvelope = null;
    private Envelope newEnvelope = null;
    
    
    public Map2DMapAreaEvent(Map2D map, Envelope oldone, Envelope newone){
        super(map);
        oldEnvelope = oldone;
        newEnvelope = newone;
    }

    public Envelope getPreviousMapArea() {
        return oldEnvelope;
    }

    public Envelope getNewMapArea() {
        return newEnvelope;
    }
}
