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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;

/**
 *
 * @author Johann Sorel
 */
public class WaitingOverLayer extends JComponent implements OverLayer{

    private final BufferedImage buffer;
    private boolean drawing = false;
    
    public WaitingOverLayer(){
        ImageIcon anim = IconBundle.getResource().getIcon("JS_GT");
        String msg = TextBundle.getResource().getString("drawing_wait");
        
        
        Font currentFont = new Font("Arial",Font.BOLD|Font.ITALIC,13);
        FontMetrics currentMetrics = getFontMetrics(currentFont);
        int high = (currentMetrics.getHeight() > anim.getIconHeight()) ? currentMetrics.getHeight() : anim.getIconHeight();
        int width = currentMetrics.stringWidth(msg)+anim.getIconWidth()+2;
        
        buffer = new BufferedImage(width+9, high+7, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(2, 2, (width+6), (high+4), 9,9);
        g2d.drawImage(anim.getImage(), 5, 4, this);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(2, 2, (width+6), (high+4), 9,9);                
        g2d.setFont(new Font("Arial",Font.BOLD|Font.ITALIC,13));
        g2d.drawString(msg, (anim.getIconWidth()+2+3+2) , (buffer.getHeight()/2 + currentMetrics.getHeight()/2)  );
        
    }
    
    public void setDrawing(boolean b){        
        drawing = b;
        revalidate();
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if(drawing){
            g.drawImage(buffer, 0, 0, this);           
            }
    }

    public void refresh() {
        repaint();
    }

    public JComponent geComponent() {
        return this;
    }

    
    
}
