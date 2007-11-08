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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;

import javax.swing.JPanel;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.column.OpacityComponent;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapLayer;



/**
 * @author johann sorel
 * Default popup control for visibility of MapLayer, use for JXMapContextTreePopup
 */
public class LayerVisiblePopupComponent2 extends JPanel implements PopupComponent{
    
    private MapLayer layer;
    private JCheckBox jck = new JCheckBox();
    private OpacityComponent opa = new OpacityComponent();
    
    
    /** Creates a new instance of LayerVisibleControl */
    public LayerVisiblePopupComponent2() {
        init();
    }
    
    public Component getComponent(Object[] obj, ContextTreeNode node[]) {
        layer = (MapLayer)obj[0];
        jck.setSelected(layer.isVisible());
        opa.parse(layer);
        
        return this;
    }
    
    private void init(){
        setLayout(new BorderLayout());
        
        
        setOpaque(false);
        opa.setOpaque(false);
        opa.setPreferredSize(new Dimension(30,20));
        
        jck.setOpaque(false);
        jck.setText( TextBundle.getResource().getString("visible"));
        jck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                layer.setVisible(jck.isSelected());
            }
        });
        
        add(BorderLayout.WEST,jck);
        add(BorderLayout.CENTER,opa);
    }
    
    public boolean isValid(Object[] objs) {
        
        if(objs.length == 1){
            return isValid(objs[0]);
        }        
        return false;        
    }
    
    private boolean isValid(Object obj) {
        return obj instanceof MapLayer;
    }
    
}
