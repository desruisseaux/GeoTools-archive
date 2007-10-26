/*
 * JDataChooser.java
 *
 * Created on 24 octobre 2007, 19:10
 */
package org.geotools.gui.swing.datachooser;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import org.geotools.gui.swing.datachooser.model.DataModel;
import org.geotools.gui.swing.extended.JButtonPanel;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
public class JDataChooser extends javax.swing.JDialog implements DataListener {

    public static enum STATE {
        TABBED, BUTTONED
    }
    public static int CLOSED_EXIT = 0;
    public static int ADD_EXIT = 1;
    private int exitmode = 0;
    private STATE state = STATE.TABBED;
    private ArrayList<DataPanel> types = new ArrayList<DataPanel>();
    private DataPanel activetype;
    private ButtonGroup group;
    private JButtonPanel pan_button;
    private JTabbedPane tabbedpane = new JTabbedPane();

    /** Creates new form JDataChooser */
    public JDataChooser(java.awt.Frame parent, boolean modal, STATE state) {
        super(parent, modal);
        initComponents();

        this.state = state;
        
        but_ajouter.setText(TextBundle.getResource().getString("add"));
        but_fermer.setText(TextBundle.getResource().getString("cancel"));
        but_remove.setText(TextBundle.getResource().getString("remove"));
        txt_datas.setTitle(TextBundle.getResource().getString("layers"));

         split.setResizeWeight(0);
        split.remove(split.getLeftComponent());

        if (state == STATE.BUTTONED) {

            pan_source.setLayout(new GridLayout(1, 1));

            group = new ButtonGroup();
            pan_button = new JButtonPanel();

            split.setLeftComponent(pan_button);
            split.setDividerLocation(140);
            split.setDividerSize(1);
        } else if (state == STATE.TABBED) {
            pan_source.setLayout(new GridLayout(1, 1));
            split.setDividerLocation(0);
            split.setDividerSize(0);
            tabbedpane = new JTabbedPane();
        }

        tab_data.setModel(new DataModel(tab_data));
        tab_data.setTableHeader(null);

        List<DataPanel> lst = new ArrayList<DataPanel>();
        lst.add(new JFileDataPanel());
        lst.add(new JDatabaseDataPanel());
        lst.add(new ServerDataPanel());

        setDataTypeChooser(lst);

    }

    public void setDataTypeChooser(List<DataPanel> type) {
        
        for (DataPanel pan : types) {
            pan.removeListener(this);
        }
        
        types.clear();
        types.addAll(type);

        if (state == STATE.BUTTONED) {
           group = new ButtonGroup(); 
           pan_button = new JButtonPanel();
           split.setLeftComponent(pan_button);
           
           if(type.size()>0){
               setPanel(type.get(0));
           }
           
           
            for (DataPanel pan : type) {
                pan.addListener(this);
                final DataPanel typ = pan;
                JToggleButton b = new JToggleButton(typ.getTitle(), typ.getIcon48());
                b.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        activetype = typ;
                        txt_title.setTitle(typ.getTitle());
                        setPanel(typ);
                    }
                });
                group.add(b);
                pan_button.addToggleButton(b);
            }
        } else {

            if (types.size() == 1) {
                setPanel(types.get(0));
            } else if (types.size() > 1) {
                panel.removeAll();
                panel.setLayout(new GridLayout(1, 1));
                tabbedpane.removeAll();
                panel.add(tabbedpane);
                for (DataPanel pan : types) {
                    pan.addListener(this);
                    tabbedpane.addTab(pan.getTitle(), pan.getIcon16(), pan.getChooserComponent());
                }
                panel.revalidate();
                panel.repaint();
            }
        }
    }



    private void removeselected() {
        ((DataModel) tab_data.getModel()).removeSelected();
    }

    private void setPanel(DataPanel comp) {
        if (pan_source != null) {
            txt_title.setTitle(comp.getTitle());
            pan_source.removeAll();
            pan_source.add(comp.getChooserComponent());
            pan_source.revalidate();
            pan_source.repaint();
        }
    }

    public List<MapLayer> getLayers() {
        return ((DataModel) tab_data.getModel()).getLayers();
    }

    public void addLayers(MapLayer[] layers) {
        ((DataModel) tab_data.getModel()).addLayer(layers);
    }

    public int showDialog() {
        exitmode = CLOSED_EXIT;
        pack();
        setTitle(TextBundle.getResource().getString("add_data_dialog"));
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

        split = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        panel = new javax.swing.JPanel();
        txt_title = new org.jdesktop.swingx.JXTitledSeparator();
        pan_source = new javax.swing.JPanel();
        but_remove = new javax.swing.JButton();
        but_ajouter = new javax.swing.JButton();
        but_fermer = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab_data = new org.jdesktop.swingx.JXTable();
        txt_datas = new org.jdesktop.swingx.JXTitledSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        split.setDividerLocation(0);
        split.setDividerSize(0);

        txt_title.setTitle("");

        org.jdesktop.layout.GroupLayout pan_sourceLayout = new org.jdesktop.layout.GroupLayout(pan_source);
        pan_source.setLayout(pan_sourceLayout);
        pan_sourceLayout.setHorizontalGroup(
            pan_sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 528, Short.MAX_VALUE)
        );
        pan_sourceLayout.setVerticalGroup(
            pan_sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 158, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout panelLayout = new org.jdesktop.layout.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(txt_title, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE))
            .add(pan_source, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelLayout.createSequentialGroup()
                .addContainerGap()
                .add(txt_title, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_source, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        but_remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_removeactionRemove(evt);
            }
        });

        but_ajouter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_ajouteractionAjouter(evt);
            }
        });

        but_fermer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_fermeractionFermer(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        tab_data.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tab_data);

        txt_datas.setTitle(" ");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(but_remove)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 403, Short.MAX_VALUE)
                .add(but_ajouter)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_fermer)
                .addContainerGap())
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(txt_datas, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txt_datas, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(but_ajouter)
                    .add(but_fermer)
                    .add(but_remove))
                .addContainerGap())
        );

        split.setRightComponent(jPanel1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void but_removeactionRemove(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_removeactionRemove
        removeselected();
    }//GEN-LAST:event_but_removeactionRemove

    private void but_ajouteractionAjouter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_ajouteractionAjouter
        exitmode = ADD_EXIT;
        dispose();
    }//GEN-LAST:event_but_ajouteractionAjouter

    private void but_fermeractionFermer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_fermeractionFermer
        exitmode = CLOSED_EXIT;
        dispose();
    }//GEN-LAST:event_but_fermeractionFermer
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_ajouter;
    private javax.swing.JButton but_fermer;
    private javax.swing.JButton but_remove;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pan_source;
    private javax.swing.JPanel panel;
    private javax.swing.JSplitPane split;
    private org.jdesktop.swingx.JXTable tab_data;
    private org.jdesktop.swingx.JXTitledSeparator txt_datas;
    private org.jdesktop.swingx.JXTitledSeparator txt_title;
    // End of variables declaration//GEN-END:variables
}
