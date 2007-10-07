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

package org.geotools.gui.swing.propertyedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.gui.swing.i18n.TextBundle;

/**
 * @author johann sorel
 */
public class JPropertyDialog extends JDialog{
    
    private JButton apply = new JButton(TextBundle.getResource().getString("apply"));
    private JButton revert = new JButton(TextBundle.getResource().getString("revert"));
    private JButton close = new JButton(TextBundle.getResource().getString("close"));
    
    private JTabbedPane tabs = new JTabbedPane();    
    private PropertyPanel activePanel = null;    
    private ArrayList<PropertyPanel> panels = new ArrayList<PropertyPanel>();
    
    /** Creates a new instance of ASDialog */
    private JPropertyDialog() {
        super();
        setModal(true);
        setTitle(TextBundle.getResource().getString("properties"));
        //setIconImage(IconBundle.getResource().getIcon("16_jpropertydialog").getImage());
        
        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bas.add(apply);
        bas.add(revert);
        bas.add(close);
        
        tabs.setTabPlacement(JTabbedPane.TOP);
        
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                activePanel = (PropertyPanel)tabs.getSelectedComponent();
            }
        });
        
        apply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for(PropertyPanel edit : panels){
                    edit.apply();
                }
            }
        });
        
        revert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(activePanel != null)
                    activePanel.revert();
            }
        });
        
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for(PropertyPanel edit : panels){
                    edit.apply();
                }
                dispose();
            }
        });
        
        setLayout( new BorderLayout());
        add(BorderLayout.SOUTH,bas);
        
    }
    
    public void addEditPanel(PropertyPanel pan){
        panels.add(pan);        
        tabs.addTab(pan.getTitle(),pan.getIcon(),pan.getPanel(),pan.getToolTip());        
    }
    

    @Override
    public void setVisible(boolean b) {
        if(b){
            if(panels.size()>1){
                add(BorderLayout.CENTER,tabs);
            }else if(panels.size() == 1){
                add(BorderLayout.CENTER,(JComponent)panels.get(0));
            }
        }      
        super.setVisible(b);
    }
    
    public static void showDialog(List<PropertyPanel> lst, Object target){
        JPropertyDialog dia = new JPropertyDialog();
        
        for(PropertyPanel pro : lst){
            pro.setTarget(target);
            dia.addEditPanel(pro);
        }
        
        dia.setSize(700,500);
        dia.setLocationRelativeTo(null);
        dia.setVisible(true);
    }
   
    
}
