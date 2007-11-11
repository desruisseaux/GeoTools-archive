/*
 * JMap2DInfoBar.java
 *
 * Created on 5 octobre 2007, 11:49
 */

package org.geotools.gui.swing.map.map2d.control;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.referencing.AuthorityCodesComboBox;
import org.jdesktop.layout.GroupLayout;
import org.opengis.referencing.FactoryException;

/**
 *
 * @author johann Sorel
 */
public class JMap2DInfoBar extends javax.swing.JPanel implements PropertyChangeListener{

    private AuthorityCodesComboBox comboCRS;
    private JPanel flowpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private JMapPane map;

    /** Creates new form JMap2DInfoBar */
    public JMap2DInfoBar() {

        initComponents();
        setLayout(new BorderLayout());

        try {
            comboCRS = new AuthorityCodesComboBox();
        } catch (FactoryRegistryException ex) {
            Logger.getLogger(JMap2DInfoBar.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FactoryException ex) {
            Logger.getLogger(JMap2DInfoBar.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         add(BorderLayout.CENTER,flowpanel);
        
        if(comboCRS != null){
            add(BorderLayout.EAST,comboCRS);            
        }
    }

    public void setMapPane(JMapPane map){
        
        if(map != null){
            map.getContext().addPropertyChangeListener(this);
        }
        
        this.map = map;
        init();
    }
    
    private void init(){
        
        if(comboCRS != null && map.getContext() != null){
            comboCRS.filter( map.getContext().getCoordinateReferenceSystem().getName().getCode());
        }
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
            .add(0, 437, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 30, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public void propertyChange(PropertyChangeEvent evt) {
        init();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}