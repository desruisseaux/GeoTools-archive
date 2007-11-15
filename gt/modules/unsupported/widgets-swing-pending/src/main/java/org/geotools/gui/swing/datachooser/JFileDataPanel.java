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

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.event.EventListenerList;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.GridCoverageFinder;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.gui.swing.misc.filtre.raster.FiltreTIF;
import org.geotools.gui.swing.misc.filtre.raster.FiltreWorldImage;
import org.geotools.gui.swing.misc.filtre.vecteur.FiltreShape;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author  johann sorel
 */
public class JFileDataPanel extends javax.swing.JPanel implements DataPanel {

    private static File LASTPATH = null;
    private EventListenerList listeners = new EventListenerList();

    /** Creates new form DefaultShapeTypeChooser */
    public JFileDataPanel() {
        initComponents();
              
        gui_choose.addChoosableFileFilter(new FiltreWorldImage());
        gui_choose.addChoosableFileFilter(new FiltreTIF());
        gui_choose.addChoosableFileFilter(new FiltreShape());
        gui_choose.setMultiSelectionEnabled(true);
        
        if(LASTPATH != null){
            gui_choose.setCurrentDirectory(LASTPATH);
            }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        but_nouveau = new javax.swing.JButton();
        jtf_error = new javax.swing.JTextField();
        gui_choose = new javax.swing.JFileChooser();

        but_nouveau.setIcon(IconBundle.getResource().getIcon("16_data_add"));
        but_nouveau.setText(TextBundle.getResource().getString("add"));
        but_nouveau.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        but_nouveau.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionNouveau(evt);
            }
        });

        jtf_error.setEditable(false);

        gui_choose.setControlButtonsAreShown(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jtf_error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_nouveau)
                .addContainerGap())
            .add(gui_choose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 367, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(gui_choose, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_nouveau)
                    .add(jtf_error, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionNouveau(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionNouveau
        ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
        RandomStyleFactory rsf = new RandomStyleFactory();
        
        File[] files = gui_choose.getSelectedFiles();
            for (File f : files) {
                LASTPATH = f;
                Object source = getDataStore(f);
                if (source != null) {
                    try {
                        FeatureSource fs = ((DataStore) source).getFeatureSource(((DataStore) source).getTypeNames()[0]);
                        Style style = rsf.createRandomVectorStyle(fs);
                        MapLayer layer = new DefaultMapLayer(fs, style);
                        layer.setTitle(f.getName());
                        layers.add(layer);
                    } catch (IOException ex) {
                        jtf_error.setText(TextBundle.getResource().getString("DefaultFileTypeChooser_error"));
                    }
                } else {

                    source = getGridCoverage(f);
                    if (source != null) {
                        try {
                            Style style = rsf.createRasterStyle();
                            MapLayer layer = new DefaultMapLayer((GridCoverage) source, style);
                            layer.setTitle(f.getName());
                            layers.add(layer);
                        } catch (TransformException ex) {
                            jtf_error.setText(TextBundle.getResource().getString("DefaultFileTypeChooser_error"));
                        } catch (FactoryConfigurationError ex) {
                            jtf_error.setText(TextBundle.getResource().getString("DefaultFileTypeChooser_error"));
                        } catch (SchemaException ex) {
                            jtf_error.setText(TextBundle.getResource().getString("DefaultFileTypeChooser_error"));
                        } catch (IllegalAttributeException ex) {
                            jtf_error.setText(TextBundle.getResource().getString("DefaultFileTypeChooser_error"));
                        }
                    } else {
                        jtf_error.setText(TextBundle.getResource().getString("DefaultFileTypeChooser_error"));
                    }
                }
                LASTPATH = f;
            }
            
            if(layers.size()>0){
                MapLayer[] lys = new MapLayer[layers.size()];
                for(int i=0;i<layers.size();i++){
                    lys[i] =  layers.get(i);
                }
                fireEvent( lys );
            }
        
    }//GEN-LAST:event_actionNouveau

    public ImageIcon getIcon16() {
        return IconBundle.getResource().getIcon("16_geofile");
    }

    public ImageIcon getIcon48() {
        return IconBundle.getResource().getIcon("48_geofile");
    }

    public String getTitle() {
        return TextBundle.getResource().getString("files");
    }

    public Component getChooserComponent() {
        return this;
    }

    private DataStore getDataStore(File f) {
        Map<String, Object> map = new HashMap<String, Object>();
        DataStore dataStore = null;
        try {
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
        GridCoverage cover = null;
        try {
            map.put("url", f.toURI().toURL());
            cover = GridCoverageFinder.getGridCoverage(map);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return cover;
    }

    private void fireEvent(MapLayer[] layers){
        for( DataListener lst : listeners.getListeners(DataListener.class)){
            lst.addLayers(layers);
        }
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_nouveau;
    private javax.swing.JFileChooser gui_choose;
    private javax.swing.JTextField jtf_error;
    // End of variables declaration//GEN-END:variables

    public void addListener(DataListener listener) {
        listeners.add(DataListener.class, listener);
    }

    public void removeListener(DataListener listener) {
        listeners.remove(DataListener.class, listener);
    }
}