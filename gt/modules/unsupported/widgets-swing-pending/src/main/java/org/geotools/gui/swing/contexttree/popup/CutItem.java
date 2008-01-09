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
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.SelectionData;
import org.geotools.gui.swing.icon.IconBundle;

/**
 *
 * @author johann sorel
 */
public class CutItem implements TreePopupItem{

    private JContextTree tree = null;
    private JMenuItem cutitem = null;
    
    /**
     * cut item for jcontexttreepopup
     * @param tree
     */
    public CutItem(final JContextTree tree){
        this.tree = tree;
        
        cutitem = new JMenuItem(BUNDLE.getString("cut"));
        cutitem.setIcon( IconBundle.getResource().getIcon("16_cut") );
        cutitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        
        cutitem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                tree.cutSelectionInBuffer();
            }
        });
        
    }
    
    public boolean isValid(SelectionData[] selection) {
        return tree.containOnlyContexts(selection) || tree.containOnlyLayers(selection);
    }

    public Component getComponent(SelectionData[] selection) {
        cutitem.setEnabled( tree.canCutSelection() );
        return cutitem;
    }

}
