/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.contexttree.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.JPanel;

/**
 * @author johann Sorel
 * specific header component styled for JXMapContextTree
 */
public class ColumnHeader extends JPanel{
    
    private String name = "";
    private Component comp;
    
    
    /** 
     * Creates a new instance of JXMapContextRowHeader
     * @param name 
     * @param c 
     */
    public ColumnHeader(String name,Component c) {        
        super( new GridLayout(1,1));       
        setPreferredSize( new Dimension(20,20));
        setToolTipText(name);
        setOpaque(true);
        this.name = name;
        comp = c;
        add(comp);
    }
    
    
    @Override
    public String toString(){
        return name;
    }
        
    @Override
    protected void paintComponent(Graphics g) {
        
        g.setColor(Color.WHITE);
        g.drawRect(0,0,getWidth(),getHeight());
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(new GradientPaint(0,0,Color.WHITE,getWidth()-1,getHeight()-1,Color.LIGHT_GRAY));
        g2.fillRect(0, 0, getWidth()-1,getHeight()-1);

        paintChildren(g);
        
        g.setColor(Color.GRAY);
        g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,5,5);
    }
        
}
