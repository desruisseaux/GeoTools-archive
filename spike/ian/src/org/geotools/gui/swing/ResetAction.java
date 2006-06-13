package org.geotools.gui.swing;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


public class ResetAction extends AbstractAction {
    
    
    private ImageIcon icon;
    JMapPane map;
    
    public ResetAction(JMapPane map){
        
        URL url = this.getClass().getResource("resources/Reset16.gif");
        icon = new ImageIcon(url);
        this.putValue(Action.SMALL_ICON,icon);
        this.putValue(Action.NAME,"Reset");
        
        this.map = map;
    }
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        map.mapArea = map.context.getAreaOfInterest();
        map.repaint();
    }
    public ImageIcon getIcon() {
        return icon;
    }
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
 
}
