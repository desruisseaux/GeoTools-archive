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

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 *
 * @author Johann Sorel
 */
public class EditionOverLayer extends JComponent{

    private final Color borderColor = new Color(0,255,0);
    private final Color fillColor = new Color(0,255,0,30);
    
    private int startx =0;
    private int starty =0;
    private int width = 0;
    private int height = 0;
    private boolean draw = false;
    private boolean fill = false;
    
    public EditionOverLayer(){}
    
    public void setFill(boolean fill){
        this.fill = fill;
    }
    
    
    public void setCoord(int sx, int sy, int ex, int ey, boolean draw){
        startx = sx;
        starty = sy;
        width = ex;
        height = ey;
        this.draw = draw;
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if(draw){
                        
            if(fill){
                g.setColor(fillColor);
                g.fillRect(startx, starty, width, height);
            }
            
            g.setColor(borderColor);
            g.drawRect(startx, starty, width, height);
            }
    }
}
