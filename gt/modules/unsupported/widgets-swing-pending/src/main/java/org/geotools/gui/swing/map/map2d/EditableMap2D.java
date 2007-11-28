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

import org.geotools.gui.swing.map.MapConstants.EDIT_STATE;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.map.MapLayer;

/**
 *
 * @author Johann Sorel
 */
public interface EditableMap2D extends SelectableMap2D{

    public void setEditedMapLayer(MapLayer layer);    
    public MapLayer getEditedMapLayer();
    
    public void setEditState(EDIT_STATE state);
    public EDIT_STATE getEditState();
    
    
    public void addEditableMap2DListener(EditableMap2DListener listener);
    public void removeEditableMap2DListener(EditableMap2DListener listener);
    public EditableMap2DListener[] getEditableMap2DListeners();
}
