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


import javax.swing.ImageIcon;
import org.geotools.gui.swing.i18n.TextBundle;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.propertyedit.styleproperty.JSimpleStylePanel;
import org.geotools.gui.swing.propertyedit.styleproperty.JXMLStylePanel;

/**
 *
 * @author  johann sorel
 */
public class LayerStylePropertyPanel extends MultiPropertyPanel {


    /** Creates new form DefaultMapContextCRSEditPanel */
    public LayerStylePropertyPanel() {
        super();

        addPropertyPanel(new JSimpleStylePanel());
        //addPropertyPanel(new JScaleStylePanel());
        //addPropertyPanel(new JUniqueStylePanel());
        addPropertyPanel(new JXMLStylePanel());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 487, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 322, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public String getTitle() {
        return TextBundle.getResource().getString("style");
    }

    @Override
    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("16_style");
    }

    @Override
    public String getToolTip() {
        return TextBundle.getResource().getString("style");
    }

}