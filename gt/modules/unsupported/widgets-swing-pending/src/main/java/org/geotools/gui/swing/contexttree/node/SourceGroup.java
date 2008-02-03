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
package org.geotools.gui.swing.contexttree.node;

import javax.swing.Icon;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import javax.swing.ImageIcon;
import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.ServiceInfo;
import org.geotools.data.jdbc.JDBC1DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.gui.swing.contexttree.ContextTreeModel;
import org.geotools.gui.swing.contexttree.LightContextTreeModel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
public class SourceGroup implements SubNodeGroup {

    public boolean isValid(Object target) {
        return (target instanceof MapLayer);
    }

    public void createNodes(final LightContextTreeModel model, ContextTreeNode parentnode) {
        final MapLayer layer = (MapLayer) parentnode.getUserObject();
        final DataStore ds = layer.getFeatureSource().getDataStore();

        ContextTreeNode node = new ContextTreeNode(model) {

            @Override
            public ImageIcon getIcon() {
                return IconBundle.EMPTY_ICON;
            }

            @Override
            public boolean isEditable() {
                return false;
            }

            @Override
            public Object getValue() {

//                IndexedShapefileDataStore data = (IndexedShapefileDataStore) layer.getFeatureSource().getDataStore();
//                ServiceInfo si = data.getInfo();
//                try{
//                System.out.println(si.getDescription());
//                }catch(Exception e){}
//                try{
//                System.out.println(si.getIcon());
//                }catch(Exception e){}
//                try{
//                System.out.println(si.getKeywords());
//                }catch(Exception e){}
//                try{
//                System.out.println(si.getPublisher());
//                }catch(Exception e){}
//                try{
//                System.out.println(si.getSchema());
//                }catch(Exception e){}
//                try{
//                System.out.println(si.getSource());
//                }catch(Exception e){}
//                try{
//                System.out.println(si.getTitle());
//                }catch(Exception e){}


                if (layer.getFeatureSource().getSchema().getTypeName().equals("GridCoverage")) {
                    return "unknown raster : ";
                } else if (AbstractFileDataStore.class.isAssignableFrom(ds.getClass())) {

                    if (ds instanceof IndexedShapefileDataStore) {
                        return "Source : " + ((IndexedShapefileDataStore) ds).getInfo().getSource();
                    }

                    return "unknown file : " + ds.toString();
                } else if (JDBC1DataStore.class.isAssignableFrom(ds.getClass())) {
                    return "unknown database : ";
                } else {
                    return "unknown : ";
                }
            }

            @Override
            public void setValue(Object obj) {
            }
        };

        node.setUserObject(ds);

        String tooltip = null;
        if (layer.getFeatureSource().getSchema().getTypeName().equals("GridCoverage")) {
//                    return "unknown raster : " ;
        } else if (AbstractFileDataStore.class.isAssignableFrom(ds.getClass())) {

            if (ds instanceof IndexedShapefileDataStore) {
                tooltip = "Source : " + ((IndexedShapefileDataStore) ds).getInfo().getSource();
            }

//                    return "unknown file : " + ds.toString();
        } else if (JDBC1DataStore.class.isAssignableFrom(ds.getClass())) {
//                    return "unknown database : " ;
        } else {
//                    return "unknown : " ;
        }


        node.setToolTip(tooltip);
        model.insetNodeInto(node, parentnode, 0);
    }
    
    
    //private class-------------------------------------------------------------
    
    private class SourceNode extends ContextTreeNode{
        
        public SourceNode(LightContextTreeModel model){
            super(model);
        }
        
        @Override
        public Object getValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setValue(Object obj) {
        }

        @Override
        public Icon getIcon() {
            return IconBundle.EMPTY_ICON;
        }

        @Override
        public boolean isEditable() {
            return false;
        }
        
    }
    
    
}
