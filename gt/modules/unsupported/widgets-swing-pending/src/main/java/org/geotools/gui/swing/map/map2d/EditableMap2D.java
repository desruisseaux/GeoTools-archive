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
package org.geotools.gui.swing.map.map2d;

import org.geotools.gui.swing.map.map2d.handler.EditionHandler;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;

/**
 * interface for map2d widget how handle Edition
 * @author Johann Sorel
 */
public interface EditableMap2D extends SelectableMap2D {

    public void setMemoryLayers(MapLayer[] layers);
            
    public void repaintMemoryDecoration();

    public Style createPointStyle();

    public Style createStyle();

    public void setEditionHandler(EditionHandler handler);
    
    public EditionHandler getEditionHandler();
    
    /**
     * set the MapLayer to edit
     * @param layer : MapLayer to edit
     */
    public void setEditedMapLayer(MapLayer layer);

    /**
     * get the edited MapLayer
     * @return edited MapLayer
     */
    public MapLayer getEditedMapLayer();

    /**
     * add an EditableMap2DListener
     * @param listener : EditableMap2DListener to add
     */
    public void addEditableMap2DListener(EditableMap2DListener listener);

    /**
     * remove an EditableMap2DListener
     * @param listener : EditableMap2DListener to remove
     */
    public void removeEditableMap2DListener(EditableMap2DListener listener);

    /**
     * get an array of EditableMap2DListener
     * @return array of EditableMap2DListener
     */
    public EditableMap2DListener[] getEditableMap2DListeners();
}
