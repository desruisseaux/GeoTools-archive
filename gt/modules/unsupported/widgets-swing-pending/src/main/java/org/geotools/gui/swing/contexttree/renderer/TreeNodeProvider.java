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

package org.geotools.gui.swing.contexttree.renderer;


import org.geotools.gui.swing.icon.IconBundle;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.JContextTree;

import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;



/**
 * 
 * @author johann sorel
 */
public class TreeNodeProvider extends ComponentProvider<JLabel> {
    
    private JContextTree tree;
    public static final ImageIcon ICON_VECTEUR_VISIBLE = IconBundle.getResource().getIcon("16_maplayer_visible");
    public static final ImageIcon ICON_VECTEUR_UNVISIBLE = IconBundle.getResource().getIcon("16_maplayer_unvisible");
    public static final ImageIcon ICON_CONTEXT_ACTIVE = IconBundle.getResource().getIcon("16_mapcontext_enable");
    public static final ImageIcon ICON_CONTEXT_DESACTIVE = IconBundle.getResource().getIcon("16_mapcontext_disable");

    
    public TreeNodeProvider(JContextTree tree) {
        this.tree = tree;
        rendererComponent = new JLabel();
    }
    
    @Override
    protected void configureState(CellContext arg0) {
    }
    
    @Override
    protected JLabel createRendererComponent() {
        
        return new JLabel();
    }
    
    @Override
    protected void format(CellContext arg0) {
        ContextTreeNode node  = (ContextTreeNode) arg0.getValue();
        if(node.getUserObject() instanceof MapContext) {
            if(node.getUserObject().equals(tree.getActiveContext())) {
                rendererComponent.setIcon( ICON_CONTEXT_ACTIVE );
                rendererComponent.setFont(new Font("Tahoma",Font.BOLD,10));
            } else{
                rendererComponent.setIcon( ICON_CONTEXT_DESACTIVE );
                rendererComponent.setFont(new Font("Tahoma",Font.PLAIN,10));
            }
            rendererComponent.setText( ((MapContext)node.getUserObject()).getTitle() );
        } else if(node.getUserObject() instanceof MapLayer){
            rendererComponent.setFont(new Font("Arial",Font.PLAIN,9));
            rendererComponent.setIcon( (((MapLayer)node.getUserObject()).isVisible()) ? ICON_VECTEUR_VISIBLE : ICON_VECTEUR_UNVISIBLE );
            rendererComponent.setText( ((MapLayer)node.getUserObject()).getTitle() );
        } else {
            rendererComponent.setText(arg0.getValue().toString());
        }
        
    }
    
}
