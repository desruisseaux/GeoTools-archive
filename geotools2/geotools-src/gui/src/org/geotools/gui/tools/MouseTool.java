package org.geotools.gui.tools;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JComponent;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.gui.tools.AbstractToolImpl;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;
/**
 * Abstract class for geotools Tools that use Mouse events.  Refer to Tool
 * javadocs for more information.
 */
public abstract class MouseTool extends AbstractToolImpl implements MouseListener {

    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.tools.MouseTool");

    /**
     * Construct a MouseTool.
     */
    public MouseTool() {
        super();
    }
    
    
    /**
     * Set the Widget which sends MouseEvents and contains widget size
     * information.  Registers for mouseEvents.
     * @param widget The widget to get size information from.
     * @param listener The widget to send mouseEvents to, usually the child of
     * this class.
     * @throws IllegalStateException if the widget has already been set to
     * another widget.
     */
    public void setWidget(
        JComponent widget,
        MouseListener listener) throws IllegalStateException
    {
        super.setWidget(widget);
        if (this.widget!=null){
            widget.addMouseListener(listener);
        }
    }

    /** Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     */
    public void mouseClicked(MouseEvent e) {
    }
    
    /** Invoked when the mouse enters a component.
     *
     */
    public void mouseEntered(MouseEvent e) {
    }
    
    /** Invoked when the mouse exits a component.
     *
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /** Invoked when a mouse button has been pressed on a component.
     *
     */
    public void mousePressed(MouseEvent e) {
    }
    
    /** Invoked when a mouse button has been released on a component.
     *
     */
    public void mouseReleased(MouseEvent e) {
    }
    
    /**
     * Clean up this class.
     */
    public void destroy(){
        if (this.widget!=null){
            widget.removeMouseListener(this);
        }
    }
}
