package org.geotools.gui.swing;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


public class SelectAction extends AbstractAction {
    
    
    
    private ImageIcon icon;
    JMapPane map;
    
    public SelectAction(JMapPane map){
        URL url = this.getClass().getResource("resources/Add16.gif");
        
        icon = new ImageIcon(url);
        this.putValue(Action.SMALL_ICON,icon);
        
        this.map = map;
    }
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        map.setState(JMapPane.Select);
    }
    public ImageIcon getIcon() {
        return icon;
    }
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
 
}
