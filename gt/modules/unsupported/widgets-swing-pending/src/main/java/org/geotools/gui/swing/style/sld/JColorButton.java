/*
 * JColorButton.java
 *
 * Created on 19 oct. 2007, 18:45:52
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.style.sld;

import javax.swing.JButton;
import org.geotools.gui.swing.icon.IconBundle;

/**
 *
 * @author johann Sorel
 */
public class JColorButton extends JButton {

    public JColorButton() {
        super();
        setIcon(IconBundle.getResource().getIcon("JS16_color"));
    }
}