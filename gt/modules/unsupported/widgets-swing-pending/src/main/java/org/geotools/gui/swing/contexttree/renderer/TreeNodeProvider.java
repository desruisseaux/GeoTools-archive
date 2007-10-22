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

import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.icon.IconBundle;
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
            MapLayer layer = (MapLayer)node.getUserObject();
            
            rendererComponent.setFont(new Font("Arial",Font.PLAIN,9));
            rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_VISIBLE : ICON_LAYER_UNVISIBLE );
            
            //choose icon from datastoretype
            FeatureSource fs = layer.getFeatureSource();
            
//            System.out.println( layer.getFeatureSource().getClass().getSimpleName());
//            FeatureSource fs = layer.getFeatureSource();
//            if( fs != null){
//                System.out.println( fs.getClass().equals(IndexedShapefileDataStore.class ));
//                System.out.println( fs instanceof IndexedShapefileDataStore);
//                //System.out.println( fs instanceof IndexedShapefileDataStore.class);
//                System.out.println( IndexedShapefileDataStore.class.isInstance(fs.getClass()));
//                System.out.println( fs.getClass().isInstance(IndexedShapefileDataStore.class ));
//                //System.out.println( fs.isInstance(IndexedShapefileDataStore.class));
//                //IndexedShapefileDataStore.class.
//                
//                if( fs.getClass().getSimpleName().equals( "IndexedShapefileDataStore" ) ){
//                    rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_FILE_VECTOR_VISIBLE : ICON_LAYER_FILE_VECTOR_UNVISIBLE );
//                }else if(fs instanceof GridCoverageFactory){
//                    rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_FILE_RASTER_VISIBLE : ICON_LAYER_FILE_RASTER_UNVISIBLE );
//                }else if(fs instanceof PostgisDataStore){
//                    rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_DB_VISIBLE : ICON_LAYER_DB_UNVISIBLE );
//                }else{
//                    rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_VISIBLE : ICON_LAYER_UNVISIBLE );
//                }
//                
//                
//            }else{            
//                rendererComponent.setIcon( (layer.isVisible()) ? ICON_LAYER_VISIBLE : ICON_LAYER_UNVISIBLE );
//            }
            rendererComponent.setText( layer.getTitle() );
        } else {
            rendererComponent.setText(arg0.getValue().toString());
        }
        
    }
    
}
