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

import javax.swing.ImageIcon;
import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.jdbc.JDBC1DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.gui.swing.contexttree.ContextTreeModel;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
public class SourceGroup implements SubNodeGroup {

    public boolean isValid(Object target) {
        return (target instanceof MapLayer);
    }

    public ContextTreeNode[] createNodes(final ContextTreeModel model, Object target) {
        final MapLayer layer = (MapLayer) target;
        final DataStore ds = layer.getFeatureSource().getDataStore();

        ContextTreeNode node = new ContextTreeNode(model) {

            
            @Override
            public ImageIcon getIcon() {
                return null;
            }

            @Override
            public boolean isEditable() {
                return false;
            }

            @Override
            public Object getValue() {
                if (layer.getFeatureSource().getSchema().getTypeName().equals("GridCoverage")) {
                    return "unknown raster : " ;
                } else if (AbstractFileDataStore.class.isAssignableFrom(ds.getClass())) {
                    
                    if(ds instanceof ShapefileDataStore){
                        ShapefileDataStore store = (ShapefileDataStore) ds;
                        return "Source : " ;
                    }
                    
                    return "unknown file : " + ds.toString();
                } else if (JDBC1DataStore.class.isAssignableFrom(ds.getClass())) {
                    return "unknown database : " ;
                } else {
                    return "unknown : " ;
                }
            }

            @Override
            public void setValue(Object obj) {
            }
        };
        
        node.setUserObject(ds);
        
        return new ContextTreeNode[]{node};
    }
}
