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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import java.util.List;
import javax.swing.JMenuItem;

import org.geotools.gui.swing.contexttree.SelectionData;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.LayerFilterPropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerGeneralPanel;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapLayer;

/**
 * @author johann sorel
 * Default popup control for property page of MapLayer, use for JContextTreePopup
 */
public class LayerPropertyItem extends JMenuItem implements TreePopupItem {

    private MapLayer layer;
    private List<PropertyPanel> lst = new ArrayList<PropertyPanel>();

    /** 
     * Creates a new instance of DefaultContextPropertyPop 
     */
    public LayerPropertyItem() {
        super(TextBundle.getResource().getString("properties"));
        init();
    }

    /**
     * set the list of PropertyPanel to use
     * @param liste
     */
    public void setPropertyPanels(List<PropertyPanel> liste) {
        lst.clear();
        lst.addAll(liste);
    }

    private void init() {
        lst.add(new LayerGeneralPanel());
        lst.add(new LayerFilterPropertyPanel());
        lst.add(new LayerStylePropertyPanel());

        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JPropertyDialog.showDialog(lst, layer);

            }
        });
    }

    public boolean isValid(SelectionData[] selection) {
        if (selection.length == 1) {
            return (selection[0].getLayer() != null && selection[0].getSubObject() == null);
        }
        return false;
    }

    public Component getComponent(SelectionData[] selection) {
        layer = selection[0].getLayer();
        return this;
    }
}
