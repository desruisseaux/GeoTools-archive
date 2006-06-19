/*
 * CRSPicker.java
 *
 * Created on August 31, 2005, 3:36 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package edu.psu.geovista.geotools.crs.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.geowidgets.crs.widgets.swing.JProjectedCRSAssemblyPanel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 *
 * @author jamesm
 */
public class CRSPicker extends JPanel{
    
    /** Creates a new instance of CRSPicker */
    public CRSPicker() {
        final JProjectedCRSAssemblyPanel  proj = new JProjectedCRSAssemblyPanel(null, 2);
        setLayout(new BorderLayout());
        add(proj,BorderLayout.CENTER);
        JButton go = new JButton("go");
        add(go,BorderLayout.SOUTH);
        go.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
               try{
                setCRS(proj.getCRS());
               }
               catch(FactoryException fe){
                   System.err.println(fe);
               }
            }
        });
        setPreferredSize(new Dimension(410,410));
        
    }

    /**
     * Holds value of property crs.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Utility field used by bound properties.
     */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Getter for property crs.
     * @return Value of property crs.
     */
    public CoordinateReferenceSystem getCrs() {

        return this.crs;
    }
    
    protected void setCRS(CoordinateReferenceSystem newCRS){
         CoordinateReferenceSystem oldCRS = crs;
         crs = newCRS;
        propertyChangeSupport.firePropertyChange("crs", oldCRS, crs);
    }
    
}
