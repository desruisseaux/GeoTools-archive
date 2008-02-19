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

import java.awt.Component;

import javax.swing.ImageIcon;

import org.geotools.map.MapContext;

/**
 *
 * @author johann sorel
 */
public class ContextGeneralPanel extends javax.swing.JPanel implements PropertyPanel{
    
    private MapContext context = null;
    
    /** Creates new form ContextGeneralPanel */
    public ContextGeneralPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        gui_jtf_name = new javax.swing.JTextField();

        jLabel1.setText("Name : ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gui_jtf_name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(gui_jtf_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(270, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void parse(){
        if(context != null){
            gui_jtf_name.setText(context.getTitle());
        }else{
            gui_jtf_name.setText("");
        }
    }
    
    
    public void setTarget(Object target) {
        if(target instanceof MapContext){
            context = (MapContext) target;
        }else{
            context = null;
        }
        parse();
    }

    public void apply() {
        if(context != null){
            context.setTitle(gui_jtf_name.getText());
        }
    }

    public void reset() {
        parse();
    }

    public String getTitle() {
        return "General";
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getToolTip() {
        return "General";
    }

    public Component getPanel() {
        return this;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField gui_jtf_name;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
