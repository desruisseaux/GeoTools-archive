/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.map.map2d;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.geotools.map.MapLayer;

/**
 *
 * @author Johann Sorel
 */
public 
class LayerComponent extends JComponent{

    public BufferedImage img;
    public MapLayer layer;
        
    public LayerComponent(){
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, this);
    }
    
    
    
    
    
}