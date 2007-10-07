/*
 * DataListener.java
 * 
 * Created on 19 sept. 2007, 23:12:52
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.datachooser;

import java.util.EventListener;

import org.geotools.map.MapLayer;

/**
 *
 * @author Administrateur
 */
public interface DataListener extends EventListener{

    public void addLayers( MapLayer[] layers );
}
