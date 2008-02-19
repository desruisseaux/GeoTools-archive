/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.map.map2d.control;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.NavigableMap2D;
import org.geotools.map.MapContext;

/**
 *
 * @author johann sorel
 */
public class ZoomAllAction extends AbstractAction {

    private Map2D map = null;

    public void actionPerformed(ActionEvent arg0) {
        if (map != null) {


            MapContext context = map.getRenderingStrategy().getContext();

            try {
                ReferencedEnvelope env = context.getLayerBounds();

                if (env != null) {
                    map.getRenderingStrategy().setMapArea(env);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Map2D getMap() {
        return map;
    }

    public void setMap(Map2D map) {
        this.map = map;
        setEnabled(map != null);
    }
}
