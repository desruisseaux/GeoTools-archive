package org.geotools.gui.swing;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


public class ZoomOutAction extends AbstractAction {
    
    
    private static final long serialVersionUID = 8669650422678543113L;
    private ImageIcon icon;
    JMapPane map;
    
    public ZoomOutAction(JMapPane map){
        URL url = this.getClass().getResource("resources/ZoomOut16.gif");
        icon = new ImageIcon(url);
        this.putValue(Action.SMALL_ICON,icon);
        this.map = map;
    }
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        map.setState(JMapPane.ZoomOut);
    }
    
}

