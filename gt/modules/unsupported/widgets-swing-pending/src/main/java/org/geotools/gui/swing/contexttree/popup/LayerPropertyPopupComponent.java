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

package org.geotools.gui.swing.contexttree.popup;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.i18n.TextBundle;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.LayerFeaturePropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerFilterPropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapLayer;


/**
 * @author johann sorel
 * Default popup control for property page of MapLayer, use for JXMapContextTreePopup
 * In Construction!
 */
public class LayerPropertyPopupComponent extends JMenuItem implements PopupComponent{
    
    private MapLayer layer;
    
    /** Creates a new instance of DefaultContextPropertyPop */
    public LayerPropertyPopupComponent() {
        super( TextBundle.getResource().getString("properties")  );
        init();
    }
    
    
    public Component getComponent(Object obj, ContextTreeNode node) {
        layer = (MapLayer)obj;
        return this;
    }
    
    private void init(){
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                ArrayList<PropertyPanel> lst = new ArrayList<PropertyPanel>();
                lst.add(new LayerFilterPropertyPanel());
                lst.add(new LayerStylePropertyPanel());
                lst.add(new LayerFeaturePropertyPanel());
                JPropertyDialog.showDialog(lst, layer);
                
            }
        }
        );
    }
    
    public boolean isValid(Object obj) {
        return obj instanceof MapLayer;
    }
    
}
