package org.geotools.gui.swing;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


public class ZoomInAction extends AbstractAction {
    
    /**
     * 
     */
    private static final long serialVersionUID = 5757407203303739037L;
    private ImageIcon icon;
    JMapPane map;
    
    public ZoomInAction(JMapPane map){
        
        URL url = this.getClass().getResource("resources/ZoomIn16.gif");
        icon = new ImageIcon(url);
        this.putValue(Action.SMALL_ICON,icon);
        this.putValue(Action.NAME,"Zoom In");
        
        this.map = map;
    }
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        map.setState(JMapPane.ZoomIn);
    }
    public ImageIcon getIcon() {
        return icon;
    }
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
 
}
