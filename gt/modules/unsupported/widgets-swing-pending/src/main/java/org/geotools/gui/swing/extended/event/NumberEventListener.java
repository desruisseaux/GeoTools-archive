/*
 * NumberEvent.java
 * 
 * Created on 11 sept. 2007, 17:24:41
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.extended.event;

import java.util.EventListener;

/**
 *
 * @author Administrateur
 */
public interface NumberEventListener extends EventListener {

    
	void NumberChanged(double newval);
    
}
