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
package org.geotools.gui.swing.propertyedit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.ImageIcon;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.geotools.gui.swing.crschooser.JCRSList;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapContext;
import org.geotools.referencing.wkt.UnformattableObjectException;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author  johann sorel
 */
public class ContextCRSPropertyPanel extends javax.swing.JPanel implements PropertyPanel {

    private MapContext context;
    private JCRSList liste = new JCRSList();

    /** 
     * Creates new form DefaultMapContextCRSEditPanel 
     */
    public ContextCRSPropertyPanel() {
        initComponents();

        pan_list.add(BorderLayout.CENTER, liste);

        liste.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                IdentifiedObject item;
                try {
                    item = liste.getSelectedItem();
                } catch (FactoryException ex) {
                    String message = ex.getLocalizedMessage();
                    if (message == null) {
                        message = Classes.getShortClassName(ex);
                    }
                    setErrorMessage(message);
                    return;
                }
                setIdentifiedObject(item);
            }
        });

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        gui_jtf_crs = new javax.swing.JTextField();
        pan_list = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        wktArea = new javax.swing.JTextArea();

        jLabel1.setText("Coordinate Reference Systems :");

        gui_jtf_crs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gui_jtf_crsActionPerformed(evt);
            }
        });
        gui_jtf_crs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                gui_jtf_crsKeyTyped(evt);
            }
        });

        pan_list.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pan_list, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .add(gui_jtf_crs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(21, 21, 21)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gui_jtf_crs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_list, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("List", jPanel1);

        wktArea.setColumns(20);
        wktArea.setEditable(false);
        wktArea.setRows(5);
        jScrollPane1.setViewportView(wktArea);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("WKT", jPanel2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void gui_jtf_crsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gui_jtf_crsActionPerformed
        liste.searchCRS(gui_jtf_crs.getText());
    }//GEN-LAST:event_gui_jtf_crsActionPerformed

    private void gui_jtf_crsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gui_jtf_crsKeyTyped
        liste.searchCRS(gui_jtf_crs.getText());
    }//GEN-LAST:event_gui_jtf_crsKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField gui_jtf_crs;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pan_list;
    private javax.swing.JTextArea wktArea;
    // End of variables declaration//GEN-END:variables
    public void setTarget(Object target) {
        context = (MapContext) target;
        init();
    }

    public void apply() {

        try {
            context.setCoordinateReferenceSystem(liste.getCRS());
        } catch (NoSuchAuthorityCodeException ex) {
            ex.printStackTrace();
        } catch (TransformException ex) {
            ex.printStackTrace();
        } catch (FactoryException ex) {
            ex.printStackTrace();
        }
    }

    public String getTitle() {
        return "CRS";
    }

    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("16_CRS");
    }

    public String getToolTip() {
        return "Projection";
    }

    public Component getPanel() {
        return this;
    }

    private void init() {
        String epsg = context.getCoordinateReferenceSystem().getName().toString();
        gui_jtf_crs.setText(epsg);
        liste.setCRS(context.getCoordinateReferenceSystem());
        setIdentifiedObject(context.getCoordinateReferenceSystem());
    }
    
    public void reset() {
        init();
    }
    
    private void setIdentifiedObject(final IdentifiedObject item) {
        String text;
        try {
            text = item.toWKT();
        } catch (UnsupportedOperationException e) {
            text = e.getLocalizedMessage();
            if (text == null) {
                text = Classes.getShortClassName(e);
            }
            final String lineSeparator = System.getProperty("line.separator", "\n");
            if (e instanceof UnformattableObjectException) {
                text = Vocabulary.format(VocabularyKeys.WARNING) + ": " + text +
                        lineSeparator + lineSeparator + item + lineSeparator;
            } else {
                text = Vocabulary.format(VocabularyKeys.ERROR) + ": " + text + lineSeparator;
            }
        }
        wktArea.setText(text);
    }

    /**
     * Sets an error message to display instead of the current identified object.
     *
     * @param message The error message.
     */
    private void setErrorMessage(final String message) {
        wktArea.setText(Vocabulary.format(VocabularyKeys.ERROR_$1, message));
    }
    
}
