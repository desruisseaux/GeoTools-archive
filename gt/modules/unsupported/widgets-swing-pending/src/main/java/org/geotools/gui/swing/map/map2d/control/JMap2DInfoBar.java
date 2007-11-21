/*
 * JMap2DInfoBar.java
 *
 * Created on 5 octobre 2007, 11:49
 */
package org.geotools.gui.swing.map.map2d.control;

import java.awt.RenderingHints;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.map2d.DefaultMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;

/**
 *
 * @author johann Sorel
 */
public class JMap2DInfoBar extends javax.swing.JPanel {

    private Map2D map;
    
    
    /** Creates new form JMap2DInfoBar */
    public JMap2DInfoBar() {
        initComponents();
        
                        
    }

    public void setMap(Map map) {

        if (map instanceof Map2D) {
            this.map = (Map2D) map;
        }

        init();
    }

    private void init() {

        if( map instanceof DefaultMap2D ){
            jrb_multi.setEnabled(true);
            jrb_single.setEnabled(true);
            jrb_merge.setEnabled(true);
            DefaultMap2D dm = (DefaultMap2D) map;
            
            DefaultMap2D.BUFFER_TYPE type = dm.getBufferType();
            
            switch(type){
                case SINGLE_BUFFER :
                    jrb_single.setSelected(true);
                    break;
                case MULTI_BUFFER :
                    jrb_multi.setSelected(true);
                    break;
                case MERGE_BUFFER :
                    jrb_merge.setSelected(true);
                    break;
            }
            
        }else{
            jrb_multi.setEnabled(false);
            jrb_single.setEnabled(false);
            jrb_merge.setEnabled(false);
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jrb_multi = new javax.swing.JRadioButton();
        jrb_single = new javax.swing.JRadioButton();
        jrb_merge = new javax.swing.JRadioButton();
        gui_config = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroup1.add(jrb_multi);
        jrb_multi.setText("Multi buffer");
        jrb_multi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_multiActionPerformed(evt);
            }
        });

        buttonGroup1.add(jrb_single);
        jrb_single.setText("Single buffer");
        jrb_single.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_singleActionPerformed(evt);
            }
        });

        buttonGroup1.add(jrb_merge);
        jrb_merge.setText("Merge Buffer");
        jrb_merge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_mergeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(jrb_single)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jrb_multi)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jrb_merge)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jrb_single)
                .add(jrb_multi)
                .add(jrb_merge))
        );

        gui_config.setIcon(IconBundle.getResource().getIcon("16_map2d_optimize"));
        gui_config.setEnabled(false);
        gui_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gui_configActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(gui_config)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gui_config)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void jrb_singleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_singleActionPerformed
        if( map instanceof DefaultMap2D ){
            DefaultMap2D dm = (DefaultMap2D) map;
            dm.setMapBufferType(DefaultMap2D.BUFFER_TYPE.SINGLE_BUFFER); 
        }
}//GEN-LAST:event_jrb_singleActionPerformed

    private void jrb_multiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_multiActionPerformed
        if( map instanceof DefaultMap2D ){
            DefaultMap2D dm = (DefaultMap2D) map; 
            dm.setMapBufferType(DefaultMap2D.BUFFER_TYPE.MULTI_BUFFER);
        }
    }//GEN-LAST:event_jrb_multiActionPerformed

    private void gui_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gui_configActionPerformed
        if(map != null){
            JPopupMenu pop = new JPopupMenu();
            
            

            
            
            pop.getHeight();
        }
        
        
}//GEN-LAST:event_gui_configActionPerformed

    private void jrb_mergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_mergeActionPerformed
        if( map instanceof DefaultMap2D ){
            DefaultMap2D dm = (DefaultMap2D) map; 
            dm.setMapBufferType(DefaultMap2D.BUFFER_TYPE.MERGE_BUFFER);
        }
    }//GEN-LAST:event_jrb_mergeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton gui_config;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jrb_merge;
    private javax.swing.JRadioButton jrb_multi;
    private javax.swing.JRadioButton jrb_single;
    // End of variables declaration//GEN-END:variables
}
