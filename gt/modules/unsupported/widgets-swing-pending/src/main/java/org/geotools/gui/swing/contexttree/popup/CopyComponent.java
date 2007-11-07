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
import javax.swing.JMenuItem;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.TreeTable;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;

/**
 *
 * @author johann sorel
 */
public class CopyComponent implements PopupComponent{

    private TreeTable tree = null;
    private JMenuItem copyitem = null;
    
    public CopyComponent(final TreeTable tree){
        this.tree = tree;
        
        copyitem = new JMenuItem(TextBundle.getResource().getString("copy"));
        copyitem.setIcon( IconBundle.getResource().getIcon("16_copy") );
        
        copyitem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                tree.copySelectionInBuffer();
            }
        });
        
    }
    
    public boolean isValid(Object[] obj) {
        return true;
    }

    public Component getComponent(Object[] obj, ContextTreeNode node[]) {
        copyitem.setEnabled( tree.hasSelection());
        return copyitem;
    }

}
