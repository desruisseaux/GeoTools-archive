/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.map.map2d.overLayer;

import java.awt.Font;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;

/**
 *
 * @author Johann Sorel
 */
public class WaitingOverLayer extends JComponent{

    private final ImageIcon anim = IconBundle.getResource().getIcon("JS_GT");
    private final String msg = TextBundle.getResource().getString("drawing_wait");
    private boolean drawing = false;
    
    public WaitingOverLayer(){}
    
    public void setDrawing(boolean b){
        drawing = b;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if(drawing){
            g.drawImage(anim.getImage(), 0, 0, this);
            g.setFont(new Font("Arial",Font.BOLD|Font.ITALIC,13));
            g.drawString(msg, 35, 25);
            }
    }

    
    
}
