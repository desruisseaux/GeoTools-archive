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


import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.jdbc.JDBC1DataStore;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;



/**
 * Provider for ContextTree to render TreeColumn
 * 
 * @author johann sorel
 */
public class TreeNodeProvider extends ComponentProvider<JLabel> {
        
    public static final ImageIcon ICON_LAYER_VISIBLE = IconBundle.getResource().getIcon("16_maplayer_visible");
    public static final ImageIcon ICON_LAYER_UNVISIBLE = IconBundle.getResource().getIcon("16_maplayer_unvisible");    
    public static final ImageIcon ICON_LAYER_FILE_VECTOR_VISIBLE = IconBundle.getResource().getIcon("JS16_layer_e_fv");
    public static final ImageIcon ICON_LAYER_FILE_VECTOR_UNVISIBLE = IconBundle.getResource().getIcon("JS16_layer_d_fv");
    public static final ImageIcon ICON_LAYER_FILE_RASTER_VISIBLE = IconBundle.getResource().getIcon("JS16_layer_e_fr");
    public static final ImageIcon ICON_LAYER_FILE_RASTER_UNVISIBLE = IconBundle.getResource().getIcon("JS16_layer_d_fr");
    public static final ImageIcon ICON_LAYER_DB_VISIBLE = IconBundle.getResource().getIcon("JS16_layer_e_db");
    public static final ImageIcon ICON_LAYER_DB_UNVISIBLE = IconBundle.getResource().getIcon("JS16_layer_d_db");    
    public static final ImageIcon ICON_CONTEXT_ACTIVE = IconBundle.getResource().getIcon("16_mapcontext_enable");
    public static final ImageIcon ICON_CONTEXT_DESACTIVE = IconBundle.getResource().getIcon("16_mapcontext_disable");

    private final JContextTree tree;
    
    
    /**
     * Provider for ContextTree to render TreeColumn
     * 
     * @param tree related JContextTree
     */
    public TreeNodeProvider(JContextTree tree) {
        this.tree = tree;
        rendererComponent = new JLabel();
    }
    
    /**
     * {@inheritDoc}
     * @param arg0 
     */
    @Override
    protected void configureState(CellContext arg0) {
    }
    
    /** 
     * {@inheritDoc}
     * @return 
     */
    @Override
    protected JLabel createRendererComponent() {        
        return new JLabel();
    }
    
    /**
     * {@inheritDoc}
     * @param arg0 
     */
    @Override
    protected void format(CellContext arg0) {
        ContextTreeNode node  = (ContextTreeNode) arg0.getValue();
        if(node.getUserObject() instanceof MapContext) {
            if(node.getUserObject().equals(tree.getTreeTableModel().getActiveContext())) {
                rendererComponent.setIcon( ICON_CONTEXT_ACTIVE );
                rendererComponent.setFont(new Font("Tahoma",Font.BOLD,10));
            } else{
                rendererComponent.setIcon( ICON_CONTEXT_DESACTIVE );
                rendererComponent.setFont(new Font("Tahoma",Font.PLAIN,10));
            }
            rendererComponent.setText( ((MapContext)node.getUserObject()).getTitle() );
        } else if(node.getUserObject() instanceof MapLayer){
            MapLayer layer = (MapLayer)node.getUserObject();
            
            rendererComponent.setFont(new Font("Arial",Font.PLAIN,9));
            
            //choose icon from datastoretype
            DataStore ds = layer.getFeatureSource().getDataStore();
            
            if ( layer.getFeatureSource().getSchema().getTypeName().equals("GridCoverage")){
                rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_FILE_RASTER_VISIBLE : ICON_LAYER_FILE_RASTER_UNVISIBLE );
            }else if(AbstractFileDataStore.class.isAssignableFrom( ds.getClass() )){
                rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_FILE_VECTOR_VISIBLE : ICON_LAYER_FILE_VECTOR_UNVISIBLE );
            }else if(JDBC1DataStore.class.isAssignableFrom( ds.getClass() )){
                rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_DB_VISIBLE : ICON_LAYER_DB_UNVISIBLE  );
            }else{
                rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_VISIBLE : ICON_LAYER_UNVISIBLE );
            }
            
            rendererComponent.setText( layer.getTitle() );
        } else {
            rendererComponent.setText(arg0.getValue().toString());
        }
        
    }
    
}
