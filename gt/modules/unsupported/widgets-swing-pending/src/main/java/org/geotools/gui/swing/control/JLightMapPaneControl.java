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

package org.geotools.gui.swing.control;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.icon.IconBundle;

/**
 * @author johann sorel
 */
public class JLightMapPaneControl extends JPanel{
    
    private JMapPane pane;
    
    /**
     * Creates a new instance of DefaultLightMapPaneToolBar
     */
    public JLightMapPaneControl() {
        super(new FlowLayout(FlowLayout.LEFT));
        init();
    }
    
    public JLightMapPaneControl(JMapPane pane) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.pane = pane;
        init();
    }
    
    private void init(){
        JButton zoomin = buildButton( IconBundle.getResource().getIcon("16_zoom_in"));
        JButton zoomout = buildButton( IconBundle.getResource().getIcon("16_zoom_out"));
        JButton zoompan = buildButton( IconBundle.getResource().getIcon("16_zoom_pan"));
        JButton zoomall = buildButton( IconBundle.getResource().getIcon("16_zoom_all"));
        JButton select = buildButton( IconBundle.getResource().getIcon("16_select"));
        
        zoomin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( pane != null){
                    pane.setState(JMapPane.ZoomIn);
                }
            }
        });
        
        zoomout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( pane != null){
                    pane.setState(JMapPane.ZoomOut);
                }
            }
        });
        
        zoompan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( pane != null){
                    pane.setState(JMapPane.Pan);
                }
            }
        });
        
        zoomall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( pane != null){
                    
                    try{
                        pane.setMapArea(pane.getContext().getLayerBounds());
                        pane.setReset(true);
                        pane.repaint();
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                    
                }
            }
        });
        
        select.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pane.setState(JMapPane.Select);
            }
        });
        
        add(zoomall);
        add(zoomin);
        add(zoomout);
        add(zoompan);
        add(select);
    }
    
    private JButton buildButton(ImageIcon img){
        JButton but = new JButton(img);
        but.setBorder(new EmptyBorder(2,2,2,2));
        but.setBorderPainted(false);
        but.setContentAreaFilled(false);
        but.setOpaque(false);
        return but;
    }
    
    public void setMapPane(JMapPane pane){
        this.pane = pane;
    }
    
    
}
