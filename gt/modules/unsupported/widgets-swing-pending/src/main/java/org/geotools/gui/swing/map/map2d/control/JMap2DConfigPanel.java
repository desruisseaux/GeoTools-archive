/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.map.map2d.control;

import java.awt.RenderingHints;
import org.geotools.gui.swing.map.map2d.JDefaultMap2D;
import org.geotools.gui.swing.map.map2d.strategy.MergeBufferedImageStrategy;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleBufferedImageStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleVolatileImageStrategy;
import org.geotools.renderer.GTRenderer;

/**
 *
 * @author  johann Sorel
 */
public class JMap2DConfigPanel extends javax.swing.JPanel {

    private JDefaultMap2D map = null;

    /** Creates new form JOptimizeMap2DPanel */
    public JMap2DConfigPanel() {
        initComponents();
    }

    private void setRenderingHints(RenderingHints.Key key, Object obj) {

        if (map != null && map.getRenderingStrategy().getRenderer() != null) {
            
            RenderingHints hints = map.getRenderingStrategy().getRenderer().getJava2DHints();

            if (hints != null) {
                hints.put(key,obj);
            }else{                
                map.getRenderingStrategy().getRenderer().setJava2DHints(new RenderingHints(key,obj));
            }
           
        }

    }

    private void setRendering(RenderingStrategy type) {
        if (map != null) {
            map.setRenderingStrategy(type);
        }
    }

    public JDefaultMap2D getMap() {
        return map;
    }

    public void setMap(JDefaultMap2D map) {
        this.map = map;

        if (map != null && map.getRenderingStrategy().getRenderer() != null) {
            GTRenderer renderer = map.getRenderingStrategy().getRenderer();
            RenderingHints rh = renderer.getJava2DHints();

            RenderingStrategy cl = map.getRenderingStrategy();
            
            if(cl instanceof SingleBufferedImageStrategy){
                jrb_rendering_single_buffer.setSelected(true);
            }else if(cl instanceof MergeBufferedImageStrategy){
                jrb_rendering_merge_buffer.setSelected(true);
            }else if(cl instanceof SingleBufferedImageStrategy){
                jrb_rendering_single_volatile.setSelected(true);
            }
                       
            
            if (rh != null) {
                Object value = null;
                                

                value = rh.get(RenderingHints.KEY_RENDERING);
                if (RenderingHints.VALUE_RENDER_QUALITY.equals(value)) {
                    jrb_render_quality.setSelected(true);
                } else if (RenderingHints.VALUE_RENDER_SPEED.equals(value)) {
                    jrb_render_speed.setSelected(true);
                } else {
                    jrb_render_default.setSelected(true);
                }


                value = rh.get(RenderingHints.KEY_COLOR_RENDERING);
                if (RenderingHints.VALUE_COLOR_RENDER_QUALITY.equals(value)) {
                    jrb_color_quality.setSelected(true);
                } else if (RenderingHints.VALUE_COLOR_RENDER_SPEED.equals(value)) {
                    jrb_color_speed.setSelected(true);
                } else {
                    jrb_color_default.setSelected(true);
                }

                value = rh.get(RenderingHints.KEY_ANTIALIASING);
                if (RenderingHints.VALUE_ANTIALIAS_ON.equals(value)) {
                    jcb_antialiasing.setSelectedIndex(1);
                } else if (RenderingHints.VALUE_ANTIALIAS_OFF.equals(value)) {
                    jcb_antialiasing.setSelectedIndex(2);
                } else {
                    jcb_antialiasing.setSelectedIndex(0);
                }

                value = rh.get(RenderingHints.KEY_TEXT_ANTIALIASING);
                if (RenderingHints.VALUE_TEXT_ANTIALIAS_ON.equals(value)) {
                    jcb_text_antialiasing.setSelectedIndex(1);
                } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.equals(value)) {
                    jcb_text_antialiasing.setSelectedIndex(2);
                } else {
                    jcb_text_antialiasing.setSelectedIndex(0);
                }

                value = rh.get(RenderingHints.KEY_FRACTIONALMETRICS);
                if (RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(value)) {
                    jcb_fractional.setSelectedIndex(1);
                } else if (RenderingHints.VALUE_FRACTIONALMETRICS_OFF.equals(value)) {
                    jcb_fractional.setSelectedIndex(2);
                } else {
                    jcb_fractional.setSelectedIndex(0);
                }

                value = rh.get(RenderingHints.KEY_DITHERING);
                if (RenderingHints.VALUE_DITHER_ENABLE.equals(value)) {
                    jcb_dithering.setSelectedIndex(1);
                } else if (RenderingHints.VALUE_DITHER_DISABLE.equals(value)) {
                    jcb_dithering.setSelectedIndex(2);
                } else {
                    jcb_dithering.setSelectedIndex(0);
                }

            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg_j2d_rendering = new javax.swing.ButtonGroup();
        bg_j2d_color = new javax.swing.ButtonGroup();
        bg_rendering = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jrb_rendering_single_buffer = new javax.swing.JRadioButton();
        jrb_rendering_merge_buffer = new javax.swing.JRadioButton();
        jrb_rendering_single_volatile = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jrb_render_default = new javax.swing.JRadioButton();
        jrb_render_quality = new javax.swing.JRadioButton();
        jrb_render_speed = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jrb_color_speed = new javax.swing.JRadioButton();
        jrb_color_quality = new javax.swing.JRadioButton();
        jrb_color_default = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jcb_antialiasing = new javax.swing.JComboBox();
        jcb_text_antialiasing = new javax.swing.JComboBox();
        jcb_fractional = new javax.swing.JComboBox();
        jcb_dithering = new javax.swing.JComboBox();

        jLabel7.setText("Software solutions :");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel8.setText("Hardware solutions :");

        bg_rendering.add(jrb_rendering_single_buffer);
        jrb_rendering_single_buffer.setText("Single buffered image");
        jrb_rendering_single_buffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_rendering_single_bufferActionPerformed(evt);
            }
        });

        bg_rendering.add(jrb_rendering_merge_buffer);
        jrb_rendering_merge_buffer.setText("Merge buffered images");
        jrb_rendering_merge_buffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_rendering_merge_bufferActionPerformed(evt);
            }
        });

        bg_rendering.add(jrb_rendering_single_volatile);
        jrb_rendering_single_volatile.setSelected(true);
        jrb_rendering_single_volatile.setText("Volatile image");
        jrb_rendering_single_volatile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_rendering_single_volatileActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 131, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jrb_rendering_merge_buffer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jrb_rendering_single_buffer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                                .add(28, 28, 28)))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jrb_rendering_single_volatile)))
                .add(158, 158, 158))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_rendering_single_volatile))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_rendering_single_buffer)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_rendering_merge_buffer))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Rendering", jPanel1);

        jLabel1.setText("Rendering :");

        bg_j2d_rendering.add(jrb_render_default);
        jrb_render_default.setSelected(true);
        jrb_render_default.setText("Default");
        jrb_render_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_render_defaultActionPerformed(evt);
            }
        });

        bg_j2d_rendering.add(jrb_render_quality);
        jrb_render_quality.setText("Quality");
        jrb_render_quality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_render_qualityActionPerformed(evt);
            }
        });

        bg_j2d_rendering.add(jrb_render_speed);
        jrb_render_speed.setText("Speed");
        jrb_render_speed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_render_speedActionPerformed(evt);
            }
        });

        jLabel2.setText("Color rendering :");

        bg_j2d_color.add(jrb_color_speed);
        jrb_color_speed.setText("Speed");
        jrb_color_speed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_color_speedActionPerformed(evt);
            }
        });

        bg_j2d_color.add(jrb_color_quality);
        jrb_color_quality.setText("Quality");
        jrb_color_quality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_color_qualityActionPerformed(evt);
            }
        });

        bg_j2d_color.add(jrb_color_default);
        jrb_color_default.setSelected(true);
        jrb_color_default.setText("Default");
        jrb_color_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrb_color_defaultActionPerformed(evt);
            }
        });

        jLabel3.setText("Antialiasing :");

        jLabel4.setText("Text antialiasing :");

        jLabel5.setText("Fractional metrics :");

        jLabel6.setText("Dithering :");

        jcb_antialiasing.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEFAULT", "ON", "OFF" }));
        jcb_antialiasing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb_antialiasingActionPerformed(evt);
            }
        });

        jcb_text_antialiasing.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEFAULT", "ON", "OFF" }));
        jcb_text_antialiasing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb_text_antialiasingActionPerformed(evt);
            }
        });

        jcb_fractional.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEFAULT", "ON", "OFF" }));
        jcb_fractional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb_fractionalActionPerformed(evt);
            }
        });

        jcb_dithering.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DEFAULT", "ENABLE", "DISABLE" }));
        jcb_dithering.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcb_ditheringActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jrb_render_default)
                    .add(jLabel2)
                    .add(jrb_color_default)
                    .add(jrb_render_speed)
                    .add(jrb_render_quality)
                    .add(jrb_color_speed)
                    .add(jrb_color_quality)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 206, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jLabel5)
                    .add(jLabel6)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jcb_antialiasing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jcb_dithering, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jcb_fractional, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jcb_text_antialiasing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(36, 36, 36))
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {jcb_antialiasing, jcb_dithering, jcb_fractional, jcb_text_antialiasing}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_render_default)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_render_quality)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_render_speed)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jrb_color_default)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_color_quality)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jrb_color_speed))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jcb_antialiasing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel4)
                            .add(jcb_text_antialiasing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(jcb_fractional, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jcb_dithering, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6))))
                .addContainerGap(113, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Java2D", jPanel2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private void jcb_antialiasingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcb_antialiasingActionPerformed
        switch (jcb_antialiasing.getSelectedIndex()) {
            case 0:
                setRenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
                break;
            case 1:
                setRenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                break;
            case 2:
                setRenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                break;
        }
}//GEN-LAST:event_jcb_antialiasingActionPerformed

    private void jrb_render_defaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_render_defaultActionPerformed
        setRenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
}//GEN-LAST:event_jrb_render_defaultActionPerformed

    private void jrb_render_qualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_render_qualityActionPerformed
        setRenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }//GEN-LAST:event_jrb_render_qualityActionPerformed

    private void jrb_render_speedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_render_speedActionPerformed
        setRenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }//GEN-LAST:event_jrb_render_speedActionPerformed

    private void jrb_color_defaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_color_defaultActionPerformed
        setRenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
    }//GEN-LAST:event_jrb_color_defaultActionPerformed

    private void jrb_color_qualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_color_qualityActionPerformed
        setRenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }//GEN-LAST:event_jrb_color_qualityActionPerformed

    private void jrb_color_speedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_color_speedActionPerformed
        setRenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }//GEN-LAST:event_jrb_color_speedActionPerformed

    private void jcb_text_antialiasingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcb_text_antialiasingActionPerformed
        switch (jcb_text_antialiasing.getSelectedIndex()) {
            case 0:
                setRenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
                break;
            case 1:
                setRenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                break;
            case 2:
                setRenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                break;
        }
    }//GEN-LAST:event_jcb_text_antialiasingActionPerformed

    private void jcb_fractionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcb_fractionalActionPerformed
        switch (jcb_fractional.getSelectedIndex()) {
            case 0:
                setRenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
                break;
            case 1:
                setRenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                break;
            case 2:
                setRenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                break;
        }
    }//GEN-LAST:event_jcb_fractionalActionPerformed

    private void jcb_ditheringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcb_ditheringActionPerformed
        switch (jcb_dithering.getSelectedIndex()) {
            case 0:
                setRenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
                break;
            case 1:
                setRenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                break;
            case 2:
                setRenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                break;
        }
    }//GEN-LAST:event_jcb_ditheringActionPerformed

    private void jrb_rendering_single_bufferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_rendering_single_bufferActionPerformed
        setRendering(new SingleBufferedImageStrategy());
}//GEN-LAST:event_jrb_rendering_single_bufferActionPerformed

    private void jrb_rendering_merge_bufferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_rendering_merge_bufferActionPerformed
        setRendering(new MergeBufferedImageStrategy());
    }//GEN-LAST:event_jrb_rendering_merge_bufferActionPerformed

    private void jrb_rendering_single_volatileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrb_rendering_single_volatileActionPerformed
        setRendering(new SingleVolatileImageStrategy());
    }//GEN-LAST:event_jrb_rendering_single_volatileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bg_j2d_color;
    private javax.swing.ButtonGroup bg_j2d_rendering;
    private javax.swing.ButtonGroup bg_rendering;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jcb_antialiasing;
    private javax.swing.JComboBox jcb_dithering;
    private javax.swing.JComboBox jcb_fractional;
    private javax.swing.JComboBox jcb_text_antialiasing;
    private javax.swing.JRadioButton jrb_color_default;
    private javax.swing.JRadioButton jrb_color_quality;
    private javax.swing.JRadioButton jrb_color_speed;
    private javax.swing.JRadioButton jrb_render_default;
    private javax.swing.JRadioButton jrb_render_quality;
    private javax.swing.JRadioButton jrb_render_speed;
    private javax.swing.JRadioButton jrb_rendering_merge_buffer;
    private javax.swing.JRadioButton jrb_rendering_single_buffer;
    private javax.swing.JRadioButton jrb_rendering_single_volatile;
    // End of variables declaration//GEN-END:variables
}
