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

package org.geotools.gui.swing.extended;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXTitledPanel;

/**
 *
 * @author johann sorel
 */
public class JButtonPanel extends JXTitledPanel{
    
    private BoxLayout box ;
    private JPanel pan ;
    private List<JToggleButton> buttons = new ArrayList<JToggleButton>();
    
    /** Creates a new instance of ButtonPanel */
    public JButtonPanel() {
        super();
        
        pan = new JPanel();
        box = new BoxLayout(pan,BoxLayout.PAGE_AXIS);
        pan.setLayout(box);
        
        pan.setBackground(Color.WHITE);
        setBackground(Color.WHITE);
        
        JScrollPane jsp = new JScrollPane(pan);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(jsp);
    }
    
    
    public void addToggleButton(JToggleButton button){
        buttons.add(button);
        
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBackground(Color.WHITE);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setMaximumSize(new Dimension(6000,80));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        /*button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(JToggleButton t : buttons){
                    t.setBackground(Color.WHITE);
                }
                ((JToggleButton)e.getSource()).setBackground(Color.BLUE);
            }
        });*/
                
        pan.add(button);
    }
    
}
