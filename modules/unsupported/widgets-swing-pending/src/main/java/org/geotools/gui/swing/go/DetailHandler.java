/*
 * DetailHandler.java
 *
 * Created on 19 mai 2008, 14:57
 */
package org.geotools.gui.swing.go;

import java.awt.Component;
import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.display.canvas.CanvasHandler;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.operation.matrix.AffineTransform2D;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.opengis.geometry.DirectPosition;

/**
 *
 * @author  sorel
 */
public class DetailHandler extends javax.swing.JPanel implements CanvasHandler {

    private AWTCanvas2D canvas = null;

    /** Creates new form DetailHandler */
    public DetailHandler() {
        initComponents();

    }

    private void refresh() {

        if (canvas != null) {
            AffineTransform2D aff = canvas.getTransform();

            
            DirectPosition center = canvas.getCenter();
            
            double transX = center.getOrdinate(0);
            double transY = center.getOrdinate(1);
            double scale = XAffineTransform.getScale(aff);
            double rotate = XAffineTransform.getRotation(aff);
            rotate = Math.toDegrees(rotate);

            guiJtfX.setText(String.valueOf(transX));
            guiJtfY.setText(String.valueOf(transY));
            guiJtfScale.setText(String.valueOf(scale));
            guiJtfRotate.setText(String.valueOf(rotate));

        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        guiJtfX = new javax.swing.JTextField();
        guiJtfY = new javax.swing.JTextField();
        guiJtfRotate = new javax.swing.JTextField();
        guiJtfScale = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Translation X :");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Translation Y :");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Rotation :");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Scale :");

        guiJtfX.setText("0");
        guiJtfX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiJtfXActionPerformed(evt);
            }
        });

        guiJtfY.setText("0");
        guiJtfY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiJtfYActionPerformed(evt);
            }
        });

        guiJtfRotate.setText("0");
        guiJtfRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiJtfRotateActionPerformed(evt);
            }
        });

        guiJtfScale.setText("1");
        guiJtfScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiJtfScaleActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(jLabel4)
                    .add(jLabel3)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(guiJtfScale, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .add(guiJtfRotate, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .add(guiJtfY, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .add(guiJtfX, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3, jLabel4}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(guiJtfX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(guiJtfY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(guiJtfRotate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel4)
                            .add(guiJtfScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void guiJtfScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiJtfScaleActionPerformed

    if (canvas != null) {
        double scale = Double.valueOf(guiJtfScale.getText());
        canvas.setScale(scale);
        refresh();
    }
    
}//GEN-LAST:event_guiJtfScaleActionPerformed

private void guiJtfRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiJtfRotateActionPerformed

    
}//GEN-LAST:event_guiJtfRotateActionPerformed

private void guiJtfYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiJtfYActionPerformed
    
    if (canvas != null) {
        double transX = Double.valueOf(guiJtfX.getText());
        double transY = Double.valueOf(guiJtfY.getText());

        DirectPosition center = new GeneralDirectPosition(canvas.getObjectiveCRS());
        center.setOrdinate(0, transX);
        center.setOrdinate(1, transY);
        canvas.setCenter(center);
        refresh();        
    }
    
}//GEN-LAST:event_guiJtfYActionPerformed

private void guiJtfXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiJtfXActionPerformed
    
    if (canvas != null) {
        double transX = Double.valueOf(guiJtfX.getText());
        double transY = Double.valueOf(guiJtfY.getText());

        DirectPosition center = new GeneralDirectPosition(canvas.getObjectiveCRS());
        center.setOrdinate(0, transX);
        center.setOrdinate(1, transY);
        canvas.setCenter(center);
        refresh();        
    }
    
}//GEN-LAST:event_guiJtfXActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField guiJtfRotate;
    private javax.swing.JTextField guiJtfScale;
    private javax.swing.JTextField guiJtfX;
    private javax.swing.JTextField guiJtfY;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables

    public void setCanvas(AWTCanvas2D canvas) {
        this.canvas = canvas;
        refresh();
    }

    public AWTCanvas2D getCanvas() {
        return canvas;
    }

    public void install(Component component) {
    }

    public void uninstall(Component component) {
    }
}
