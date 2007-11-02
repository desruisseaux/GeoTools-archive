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

import org.geotools.map.MapLayer;
import org.geotools.styling.StyleBuilder;
import org.opengis.filter.expression.Expression;

/**
 *
 * @author johann sorel
 */
public class JDegreePanel extends javax.swing.JPanel {

    /** 
     * Creates new form JDegreePanel 
     */
    public JDegreePanel() {
        initComponents();
        clock.setPan(this);
    }

    /**
     * 
     * @param exp the default expression
     */
    public void setExpression(Expression exp) {
            
        exptxt.setExpression(exp);
    
        try {
            clock.setDegree(Double.valueOf(exp.toString()));
        } catch (Exception e) {
        }
    }

    /**
     * 
     * @return Expression : new Expression
     */
    public Expression getExpression() {
        return exptxt.getExpression();
    }

    void update() {
        StyleBuilder sb = new StyleBuilder();
        int val = (int) Math.round(clock.getDegree());

        exptxt.setExpression(sb.literalExpression(val));
    }

    /**
     * 
     * @param layer the layer to edit
     */
    public void setLayer(MapLayer layer){
        exptxt.setLayer(layer);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        clock = new org.geotools.gui.swing.style.sld.DegreeClock();
        exptxt = new org.geotools.gui.swing.style.sld.JExpressionPanel();

        setMaximumSize(new java.awt.Dimension(100, 40));
        setMinimumSize(new java.awt.Dimension(100, 40));
        setPreferredSize(new java.awt.Dimension(100, 40));

        org.jdesktop.layout.GroupLayout clockLayout = new org.jdesktop.layout.GroupLayout(clock);
        clock.setLayout(clockLayout);
        clockLayout.setHorizontalGroup(
            clockLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );
        clockLayout.setVerticalGroup(
            clockLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 41, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(clock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(exptxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(clock, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(exptxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.DegreeClock clock;
    private org.geotools.gui.swing.style.sld.JExpressionPanel exptxt;
    // End of variables declaration//GEN-END:variables
}
