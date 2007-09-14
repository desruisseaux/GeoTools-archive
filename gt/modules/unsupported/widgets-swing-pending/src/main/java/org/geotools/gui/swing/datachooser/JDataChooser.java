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

import org.geotools.gui.swing.extended.JButtonPanel;
import org.geotools.gui.swing.i18n.TextBundle;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.ReplicateScaleFilter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import org.geotools.map.MapLayer;

/**
 *
 * @author  johann sorel
 */
public class JDataChooser extends javax.swing.JPanel {
    
    public static enum STATE{
        TABBED,
        BUTTONED
    };
    
    private STATE state = STATE.TABBED;
    private ArrayList<DataPanel> types = new ArrayList<DataPanel>();
    private DataPanel activetype;
    
    private List<MapLayer> layers;
    private JDialog dia;
    
    private ButtonGroup group;
    private JButtonPanel pan_button;
    private JTabbedPane tabbedpane;
    private JPanel pan_source;
    private JSplitPane split;
    
    /** Creates new form JDataChooser 
     * @param dia 
     * @param state 
     */
    private JDataChooser(JDialog dia, STATE state) {
        initComponents();
        this.dia = dia;
        this.state = state;
        
        but_ajouter.setText( TextBundle.getResource().getString("add")  ) ;
        but_fermer.setText( TextBundle.getResource().getString("cancel")  ) ;
        
        panel.setLayout(new GridLayout(1,1));
        
        
        if(state == STATE.BUTTONED){
            split = new JSplitPane();
            
            pan_source = new JPanel();
            pan_source.setLayout(new GridLayout(1,1));
            
            group = new ButtonGroup();
            pan_button = new JButtonPanel();
            
            split.setLeftComponent(pan_button);
            split.setRightComponent(pan_source);
            split.setDividerLocation(140);
            split.setDividerSize(1);
            
            panel.setLayout(new GridLayout(1,1));
        } else if(state == STATE.TABBED){
            tabbedpane = new JTabbedPane();
        }
    }
    
    
    private void addDataTypeChooser( List<DataPanel> type){
        panel.removeAll();
        types.addAll(type);
        
        if(state == STATE.BUTTONED){
            panel.add(split);
            for(DataPanel pan : type){
                final DataPanel typ = pan;
                JToggleButton b = new JToggleButton(typ.getTitle(),typ.getIcon48());
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        activetype = typ;
                        setPanel(typ.getChooserComponent());
                        
                    }
                });
                group.add(b);
                pan_button.addToggleButton(b);
            }
        } else{
            if(types.size()==1){
                panel.add(type.get(0).getChooserComponent());
            } else if(types.size()>1){
                panel.add(tabbedpane);
                for(DataPanel pan : type){
                    tabbedpane.addTab(pan.getTitle(),pan.getIcon16(),pan.getChooserComponent());
                }
            }
        }
        
        
    }
    
    private BufferedImage scaleImage(Image sourceImage, int width, int height)
    {
        ImageFilter filter = new ReplicateScaleFilter(width,height);
        ImageProducer producer = new FilteredImageSource
        (sourceImage.getSource(),filter);
        Image resizedImage = Toolkit.getDefaultToolkit().createImage(producer);

        return this.toBufferedImage(resizedImage);
    }

    private BufferedImage toBufferedImage(Image image)
    {
        image = new ImageIcon(image).getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null)
        ,image.getHeight(null),BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0,image.getWidth(null),image.getHeight(null));
        g.drawImage(image,0,0,null);
        g.dispose();

        return bufferedImage;
    }
    
    
    private void setPanel(Component comp){
        if(pan_source != null){
            pan_source.removeAll();
            pan_source.add(comp);
            pan_source.revalidate();
            pan_source.repaint();
        }
    }
    
    
    private List<MapLayer> getLayers() {
        return layers;
    }
    
    private void buildLayers(){
        layers = new ArrayList<MapLayer>();
        for( DataPanel type : types){
            layers.addAll(type.read());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        but_fermer = new javax.swing.JButton();
        but_ajouter = new javax.swing.JButton();
        panel = new javax.swing.JPanel();

        but_fermer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionFermer(evt);
            }
        });

        but_ajouter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionAjouter(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panelLayout = new org.jdesktop.layout.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 460, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 295, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(378, Short.MAX_VALUE)
                .add(but_ajouter)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_fermer)
                .addContainerGap())
            .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {but_ajouter, but_fermer}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(but_ajouter)
                    .add(but_fermer))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void actionFermer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionFermer
        dia.dispose();
    }//GEN-LAST:event_actionFermer
    
    private void actionAjouter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAjouter
        buildLayers();
        dia.dispose();
    }//GEN-LAST:event_actionAjouter
    
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_ajouter;
    private javax.swing.JButton but_fermer;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
    
    
    
    public static List<MapLayer> showDialog(){
        List<DataPanel> lst = new ArrayList<DataPanel>();
        lst.add(new FileDataPanel());
        lst.add(new DatabaseDataPanel());
        lst.add(new ServerDataPanel());
        return showDialog(lst,STATE.TABBED);
    }
    
    public static List<MapLayer> showDialog(STATE state){
        List<DataPanel> lst = new ArrayList<DataPanel>();
        lst.add(new FileDataPanel());
        lst.add(new DatabaseDataPanel());
        lst.add(new ServerDataPanel());
        return showDialog(lst,state);
    }
    
    public static List<MapLayer> showDialog(List<DataPanel> lst){
        return showDialog(lst,STATE.TABBED);
    }
    
    public static List<MapLayer> showDialog(List<DataPanel> lst, STATE state) {
        JDialog dia = new JDialog();
        dia.setModal(true);
        JDataChooser choose = new JDataChooser(dia,state);
        
        if( lst.size()>0 ){
            DataPanel dat = lst.get(0);
            
            choose.addDataTypeChooser(lst);
            
            choose.setPanel( dat.getChooserComponent() );
            dia.add( choose );
            dia.setSize(640,480);
            dia.setTitle(TextBundle.getResource().getString("add_data_dialog"));
            //dia.setIconImage(IconBundle.getResource().getIcon("16_jdatachoose").getImage());
            dia.setLocationRelativeTo(null);
            dia.setVisible(true);
            return choose.getLayers();
        } else{
            return new ArrayList<MapLayer>();
        }
        
    }
}
