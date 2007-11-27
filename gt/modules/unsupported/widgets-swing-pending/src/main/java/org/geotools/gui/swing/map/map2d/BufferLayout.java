/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.map.map2d;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 *
 * @author Administrateur
 */
public class BufferLayout implements LayoutManager{

    Dimension dim = new Dimension(0, 0);
    
    public BufferLayout() {
    }

    /* Required by LayoutManager. */
    public void addLayoutComponent(String name, Component comp) {
    }

    /* Required by LayoutManager. */
    public void removeLayoutComponent(Component comp) {
    }
    
    /* Required by LayoutManager. */
    public Dimension preferredLayoutSize(Container parent) {
        return dim;
    }

    /* Required by LayoutManager. */
    public Dimension minimumLayoutSize(Container parent) {
        return dim;
    }

    /* Required by LayoutManager. */
    /*
     * This is called when the panel is first displayed,
     * and every time its size changes.
     * Note: You CAN'T assume preferredLayoutSize or
     * minimumLayoutSize will be called -- in the case
     * of applets, at least, they probably won't be.
     */
    public void layoutContainer(Container parent) {
        int maxWidth = parent.getWidth();
        int maxHeight = parent.getHeight();
        int nComps = parent.getComponentCount();    
        
        for (int i = 0 ; i < nComps ; i++) {
            Component c = parent.getComponent(i);                
            c.setBounds(0, 0, maxWidth,maxHeight);
        }
    }
    
    
}
