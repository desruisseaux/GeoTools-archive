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
package org.geotools.gui.swing.datachooser;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.geotools.map.MapLayer;

/**
 * DataChooser Dialog
 *
 * @author Johann Sorel
 */
public class JDataChooser extends javax.swing.JDialog {

    public static enum ACTION {
        APPROVE,
        CANCEL,
        CLOSE
    }
    private ACTION exitmode = ACTION.CLOSE;
    private ArrayList<DataPanel> types = new ArrayList<DataPanel>();
    private DataPanel activDataPanel = null;
    private JTabbedPane tabbedpane = new JTabbedPane();

    /** Creates new form JDataChooser
     * @param parent
     * @param type 
     */
    public JDataChooser(java.awt.Frame parent, List<DataPanel> type) {
        super(parent, true);
        initComponents();

        tabbedpane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                activDataPanel = (DataPanel) tabbedpane.getSelectedComponent();
            }
        });
                
        setDataTypeChooser(type);
    }

    public void setDataTypeChooser(List<DataPanel> type) {

        types.clear();
        types.addAll(type);

        if (types.size() == 1) {
            DataPanel comp = types.get(0);
            txt_title.setTitle(comp.getTitle());
            pan_source.removeAll();
            pan_source.add(comp.getChooserComponent());
            pan_source.revalidate();
            pan_source.repaint();
            activDataPanel = comp;
        } else if (types.size() > 1) {
            panel.removeAll();
            panel.setLayout(new GridLayout(1, 1));
            tabbedpane.removeAll();
            panel.add(tabbedpane);
            for (DataPanel pan : types) {
                tabbedpane.addTab(pan.getTitle(), pan.getIcon(), pan.getChooserComponent());
            }
            tabbedpane.setSelectedIndex(0);
            activDataPanel = types.get(0);

            panel.revalidate();
            panel.repaint();
        }

    }

    public MapLayer[] getLayers() {
        return activDataPanel.getLayers();
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

        panel = new javax.swing.JPanel();
        txt_title = new org.jdesktop.swingx.JXTitledSeparator();
        pan_source = new javax.swing.JPanel();
        but_valider = new javax.swing.JButton();
        but_fermer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/datachooser/Bundle"); // NOI18N
        setTitle(bundle.getString("add_data_dialog")); // NOI18N

        txt_title.setTitle("");

        pan_source.setLayout(new java.awt.GridLayout(1, 1));

        org.jdesktop.layout.GroupLayout panelLayout = new org.jdesktop.layout.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(txt_title, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
            .add(pan_source, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelLayout.createSequentialGroup()
                .addContainerGap()
                .add(txt_title, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_source, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
        );

        but_valider.setText(bundle.getString("open")); // NOI18N
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
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(but_valider)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(but_fermer)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_fermer)
                    .add(but_valider))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
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
    private javax.swing.JPanel pan_source;
    private javax.swing.JPanel panel;
    private org.jdesktop.swingx.JXTitledSeparator txt_title;
    // End of variables declaration//GEN-END:variables
}
