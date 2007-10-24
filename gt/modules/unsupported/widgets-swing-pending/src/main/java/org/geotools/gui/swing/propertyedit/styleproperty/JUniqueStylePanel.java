/*
 * JUniqueStylePanel.java
 *
 * Created on 19 octobre 2007, 14:01
 */

package org.geotools.gui.swing.propertyedit.styleproperty;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapLayer;

/**
 *
 * @author  Administrateur
 */
public class JUniqueStylePanel extends javax.swing.JPanel implements PropertyPanel{
    
    /** Creates new form JUniqueStylePanel */
    public JUniqueStylePanel() {
        initComponents();
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
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public JComponent getPanel() {
        return this;
    }

    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("CP16_apps_wp_protocol");
    }

    public String getTitle() {
        return TextBundle.getResource().getString("uniquestyle");
    }

    public void setTarget(MapLayer layer) {
        
    }

    public void apply() {
        
    }

    public void setTarget(Object target) {
    }

    public void reset() {
    }

    public String getToolTip() {
        return "";
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
