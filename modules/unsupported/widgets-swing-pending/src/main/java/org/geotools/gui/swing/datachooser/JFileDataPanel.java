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
package org.geotools.gui.swing.datachooser;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.GridCoverageFinder;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.gui.swing.misc.filter.FileFilterFactory;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author  johann sorel
 */
public class JFileDataPanel extends javax.swing.JPanel implements DataPanel {

    private static ResourceBundle BUNDLE = ResourceBundle.getBundle("org/geotools/gui/swing/datachooser/Bundle");
    
    public JFileDataPanel() {
       this(null);
    }
      
    
    /** 
     * Creates new form 
     * @param openPath
     * @param communPaths 
     */
    public JFileDataPanel(File openPath) {
        initComponents();
                
        if(openPath != null){
            gui_choose.setCurrentDirectory(openPath);
        }
                
        gui_choose.addChoosableFileFilter(FileFilterFactory.createFileFilter(FileFilterFactory.FORMAT.WORLD_IMAGE));
        gui_choose.addChoosableFileFilter(FileFilterFactory.createFileFilter(FileFilterFactory.FORMAT.GEOTIFF));
        gui_choose.addChoosableFileFilter(FileFilterFactory.createFileFilter(FileFilterFactory.FORMAT.GEOGRAPHY_MARKUP_LANGUAGE));
        gui_choose.addChoosableFileFilter(FileFilterFactory.createFileFilter(FileFilterFactory.FORMAT.ESRI_SHAPEFILE));
        gui_choose.setMultiSelectionEnabled(true);

    }

    public void setDirectory(File directory){
        gui_choose.setCurrentDirectory(directory);
    }
    
    public File getDirectory(){
        return gui_choose.getCurrentDirectory();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gui_choose = new javax.swing.JFileChooser();

        gui_choose.setControlButtonsAreShown(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gui_choose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 470, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gui_choose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 236, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("16_geofile");
    }

    public String getTitle() {
        return BUNDLE.getString("files");
    }

    public Component getChooserComponent() {
        return this;
    }

    private DataStore getDataStore(File f) {

        DataStore dataStore = null;

        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("url", f.toURI().toURL());
            dataStore = DataStoreFinder.getDataStore(map);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return dataStore;
    }

    private GridCoverage getGridCoverage(File f) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put("url", f.toURI().toURL());
            return GridCoverageFinder.getGridCoverage(map);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    private GridCoverageReader getGridCoverageReader(File f) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put("url", f.toURI().toURL());
            return GridCoverageFinder.getGridCoverageReader(map);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser gui_choose;
    // End of variables declaration//GEN-END:variables

    
    public MapLayer[] getLayers() {
        ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
        RandomStyleFactory rsf = new RandomStyleFactory();

        String errorStr = BUNDLE.getString("DefaultFileTypeChooser_error");

        File[] files = gui_choose.getSelectedFiles();
        for (File f : files) {
            Object source = getDataStore(f);
            if (source != null) {
                try {
                    FeatureSource<SimpleFeatureType, SimpleFeature> fs = ((DataStore) source).getFeatureSource(((DataStore) source).getTypeNames()[0]);
                    Style style = rsf.createRandomVectorStyle(fs);
                    MapLayer layer = new DefaultMapLayer(fs, style);
                    layer.setTitle(f.getName());
                    layers.add(layer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {

                source = getGridCoverageReader(f);
                if (source != null) {
                    try {
                        Style style = rsf.createRasterStyle();
//                        MapLayer layer = new DefaultMapLayer((GridCoverage) source, style);
                        MapLayer layer = new DefaultMapLayer((AbstractGridCoverage2DReader) source, style);
                        layer.setTitle(f.getName());
                        layers.add(layer);
                    } catch (TransformException ex) {
                        ex.printStackTrace();
                    } catch (FactoryRegistryException ex) {
                        ex.printStackTrace();
                    } catch (SchemaException ex) {
                        ex.printStackTrace();
                    } catch (IllegalAttributeException ex) {
                        ex.printStackTrace();
                    }
                } else {
                }
            }
        }
        
        return layers.toArray(new MapLayer[layers.size()]);
        
    }
}
