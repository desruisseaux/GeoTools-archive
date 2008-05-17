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
package org.geotools.gui.swing.style.sld;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotools.map.MapLayer;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.StyleBuilder;

/**
 *
 * @author johann sorel
 */
public class JExternalGraphicPane extends javax.swing.JPanel implements StyleElementEditor<ExternalGraphic> {

    private MapLayer layer = null;
    private ExternalGraphic external = null;

    /** Creates new form JDisplacementPanel */
    public JExternalGraphicPane() {
        initComponents();
        init();
    }

    private void init() {
    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setEdited(ExternalGraphic ext) {
        this.external = ext;

        if (external != null) {
            //TODO : not handled yet
            //external.getCustomProperties();
            guiMime.setText(external.getFormat());
            try {
                guiURL.setText(external.getLocation().toString());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public ExternalGraphic getEdited() {

        if (external == null) {
            try {
                StyleBuilder sb = new StyleBuilder();
                external = sb.createExternalGraphic(new URL(""), "png");
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }

        apply();
        return external;
    }

    public void apply() {
        if (external != null) {

            external.setFormat(guiMime.getText());
            try {
                external.setLocation(new URL(guiURL.getText()));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public Component getComponent() {
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        guiMime = new javax.swing.JTextField();
        guiURL = new javax.swing.JTextField();

        setOpaque(false);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jLabel2.setText(bundle.getString("mime")); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText(bundle.getString("url")); // NOI18N

        guiMime.setOpaque(false);

        guiURL.setOpaque(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiMime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {jLabel2, jLabel3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(guiMime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(guiURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField guiMime;
    private javax.swing.JTextField guiURL;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables

}
