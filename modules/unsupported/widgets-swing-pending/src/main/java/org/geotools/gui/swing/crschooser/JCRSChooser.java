/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.crschooser;

import java.awt.BorderLayout;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotools.referencing.wkt.UnformattableObjectException;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * CRSChooser component
 * 
 * @author Johann Sorel
 */
public class JCRSChooser extends javax.swing.JDialog {

    
    public static enum ACTION {
        APPROVE,
        CANCEL,
        CLOSE
    }
    private JCRSList liste = new JCRSList();
    private ACTION exitmode = ACTION.CLOSE;

    /** Creates new form JCRSChooser
     * @param parent
     * @param modal 
     */
    public JCRSChooser(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
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

    public void setCRS(CoordinateReferenceSystem crs) {
        if (crs != null) {
            String epsg = crs.getName().toString();
            gui_jtf_crs.setText(epsg);
            liste.setCRS(crs);
            setIdentifiedObject(crs);
        }
    }

    public CoordinateReferenceSystem getCRS() {
        return liste.getCRS();
    }

    private void setIdentifiedObject(final IdentifiedObject item) {
        String text = "";
        try {
            if (item != null) {
                text = item.toWKT();
            }
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

    public ACTION showDialog() {
        exitmode = ACTION.CLOSE;
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        return exitmode;
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
        but_valider = new javax.swing.JButton();
        but_fermer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/crschooser/Bundle"); // NOI18N
        setTitle(bundle.getString("title")); // NOI18N

        jLabel1.setText(bundle.getString("crs")); // NOI18N

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
                    .add(pan_list, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                    .add(gui_jtf_crs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
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
                .add(pan_list, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("list"), jPanel1); // NOI18N

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
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("wkt"), jPanel2); // NOI18N

        but_valider.setText(bundle.getString("apply")); // NOI18N
        but_valider.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_valideractionAjouter(evt);
            }
        });

        but_fermer.setText(bundle.getString("cancel")); // NOI18N
        but_fermer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_fermeractionFermer(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(348, Short.MAX_VALUE)
                .add(but_valider)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_fermer)
                .addContainerGap())
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_fermer)
                    .add(but_valider))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void gui_jtf_crsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gui_jtf_crsActionPerformed
        liste.searchCRS(gui_jtf_crs.getText());
    }//GEN-LAST:event_gui_jtf_crsActionPerformed

    private void gui_jtf_crsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gui_jtf_crsKeyTyped
        liste.searchCRS(gui_jtf_crs.getText());
    }//GEN-LAST:event_gui_jtf_crsKeyTyped

    private void but_valideractionAjouter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_valideractionAjouter
        exitmode = ACTION.APPROVE;
        dispose();
    }//GEN-LAST:event_but_valideractionAjouter

    private void but_fermeractionFermer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_fermeractionFermer
        exitmode = ACTION.CANCEL;
        dispose();
    }//GEN-LAST:event_but_fermeractionFermer
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_fermer;
    private javax.swing.JButton but_valider;
    private javax.swing.JTextField gui_jtf_crs;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pan_list;
    private javax.swing.JTextArea wktArea;
    // End of variables declaration//GEN-END:variables
}
