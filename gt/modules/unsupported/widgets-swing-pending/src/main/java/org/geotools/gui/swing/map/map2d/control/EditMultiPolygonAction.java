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

import com.vividsolutions.jts.geom.MultiPolygon;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.geotools.data.FeatureStore;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditStateEvent;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
public class EditMultiPolygonAction extends AbstractAction {

    private Map2D map = null;
    private EditableMap2DListener listener = new EditableMap2DListener() {

        public void mapEditStateChanged(Map2DEditStateEvent event) {
        }

        public void mapEditLayerChanged(Map2DEditLayerEvent event) {
            checkLayer(event.getNewEditLayer());
        }
    };

    protected void checkLayer(MapLayer editionLayer) {

        if (editionLayer != null) {

            if (editionLayer.getFeatureSource() instanceof FeatureStore) {
                
                Class jtsClass = null;
                jtsClass = editionLayer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();

                if (jtsClass != null && jtsClass.equals(MultiPolygon.class)) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }
            } else {
                setEnabled(false);
            }

        }else{
            setEnabled(false);
        }

    }

    public void actionPerformed(ActionEvent arg0) {
        if (map != null && map instanceof EditableMap2D) {
            ((EditableMap2D) map).setEditState(MapConstants.EDIT_STATE.MULTI_POLYGON);
        }

    }

    public Map2D getMap() {
        return map;
    }

    public void setMap(Map2D map) {

        if (this.map != null && this.map instanceof EditableMap2D) {
            ((EditableMap2D) this.map).removeEditableMap2DListener(listener);
        }
        this.map = map;

        if (this.map != null && this.map instanceof EditableMap2D) {
            ((EditableMap2D) this.map).addEditableMap2DListener(listener);
            checkLayer(((EditableMap2D) this.map).getEditedMapLayer());
        } else {
            setEnabled(false);
        }

    }
}
