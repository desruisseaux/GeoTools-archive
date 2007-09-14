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

package org.geotools.gui.swing.propertyedit.styleproperty;

import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.xml.transform.TransformerException;
import org.geotools.map.MapLayer;
import org.geotools.styling.SLDTransformer;

/**
 *
 * @author  johann sorel
 */
public class XMLStylePanel extends javax.swing.JPanel implements StylePanel{
    
    private MapLayer layer;
    
    
    /** Creates new form XMLStylePanel */
    public XMLStylePanel() {
        initComponents();
        lbl_check.setText(TextBundle.getResource().getString("checkstyle"));
        but_check.setText(TextBundle.getResource().getString("check"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        but_check = new javax.swing.JButton();
        lbl_check = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        editpane = new javax.swing.JTextPane();

        but_check.setText("jButton1");
        but_check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheck(evt);
            }
        });

        lbl_check.setText("jLabel1");

        jScrollPane1.setViewportView(editpane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lbl_check)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_check)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_check)
                    .add(but_check))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionCheck(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionCheck

       
    }//GEN-LAST:event_actionCheck

    public JComponent getPanel() {
        return this;
    }

    public void apply() {
    }

    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("CP16_mimetypes_source_s");
    }

    public String getTitle() {
        return TextBundle.getResource().getString("xml");
    }

    public void setTarget(MapLayer layer) {        
        this.layer = layer;
        
        SLDTransformer ss = new SLDTransformer();
        String str;
        try {
            str = ss.transform(layer.getStyle());
            str = "grdgdgfgd";
            
            
            editpane.setText(str);
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_check;
    private javax.swing.JTextPane editpane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_check;
    // End of variables declaration//GEN-END:variables
    
}
