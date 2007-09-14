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

package org.geotools.gui.swing.extended;

/**
 *
 * @author johann sorel
 */
public class JDegreePanel extends javax.swing.JPanel {
    
    /** Creates new form JDegreePanel */
    public JDegreePanel() {
        initComponents();
        clock.setPan(this);
    }
    
    public void setDegree(double degree){
        
    }
    
    public double getDegree(){       
        return clock.getDegree();
    }
    
    
    void update(){
        int val = (int) Math.round(clock.getDegree());
        
        txt.setText( String.valueOf(val) );
    }
    
    private void sendDegree(){        
        try{
            double d = Double.parseDouble(txt.getText());
            
            while(d>=360) d -= 360;
            clock.setDegree(d);
        }catch(Exception e){
            update();
        }     
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txt = new javax.swing.JTextField();
        clock = new org.geotools.gui.swing.extended.DegreeClock();

        setMaximumSize(new java.awt.Dimension(100, 40));
        setMinimumSize(new java.awt.Dimension(100, 40));
        setPreferredSize(new java.awt.Dimension(100, 40));

        txt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt.setText("0");
        txt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionText(evt);
            }
        });
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                JDegreePanel.this.focusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout clockLayout = new org.jdesktop.layout.GroupLayout(clock);
        clock.setLayout(clockLayout);
        clockLayout.setHorizontalGroup(
            clockLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );
        clockLayout.setVerticalGroup(
            clockLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(clock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(clock, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionText(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionText
        sendDegree();        
    }//GEN-LAST:event_actionText

    private void focusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusLost
        sendDegree();
    }//GEN-LAST:event_focusLost
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.extended.DegreeClock clock;
    private javax.swing.JTextField txt;
    // End of variables declaration//GEN-END:variables
    
}
