/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


package org.geotools.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.geotools.gui.widget.FrameWidget;
import org.geotools.gui.widget.PanelWidget;

/**
 * A frame which can contain other widgets.
 *
 * @author Cameron Shorter
 * @version $Id: FrameWidgetImpl.java,v 1.3 2003/02/25 11:13:13 camerons Exp $
 * @deprecated Use JFrame instead.
 *
 */

public class FrameWidgetImpl extends JFrame implements FrameWidget {

    /**
     * The class used for identifying for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.swing.MapWidgetImpl");

    /** Creates new form MapWidgetImpl.  This should only be done from
     * WidgetFactory */
    protected FrameWidgetImpl() {
    }

    /**
     * Sets the title for this frame to the specified string.
     * @param    title    the title to be displayed in the frame's border
     * @param title the title to be displayed in the frame's border.
     *              A <code>null</code> value
     *              is treated as an empty string, "".
     * @see      #getTitle
     */
    public void setTitle(String title) {
        super.setTitle(title);
    }
    
    /**
     * Set up a BorderLayout.
     * @task TODO Allow setting of other Layout types.
     */
    public void setBorderLayout(){
        this.getContentPane().setLayout(new BorderLayout());
    }
    
    /**
     * Add a widget to be displayed inside this frame.
     */
    public void addPanelWidget(
            PanelWidget widget)
    {
            this.getContentPane().add(
                (PanelWidgetImpl)widget);
    }
    
    /**
     * Pack the widgets within this frame.
     */
    public void pack(){
        super.pack();
    }

    /**
     * Adds the specified mouse listener to receive mouse events from
     * this component.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     *
     * @param    l   the mouse listener
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #removeMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     */
    public synchronized void addMouseListener(Object l) {
        super.addMouseListener((MouseListener)l);
    }

    /**
     * Removes the specified mouse listener so that it no longer
     * receives mouse events from this component. This method performs 
     * no function, nor does it throw an exception, if the listener 
     * specified by the argument was not previously added to this component.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     *
     * @param    l   the mouse listener
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #addMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     */
    public synchronized void removeMouseListener(Object l) {
        super.removeMouseListener((MouseListener)l);
    }

    /**
     * Returns the current width of this component.
     * @return the current width of this component
     */
    public int getWidth() {
        return super.getWidth();
    }
    
    /**
     * Adds the specified window listener to receive window events from
     * this window.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param 	l the window listener
     * @see #removeWindowListener
     * @see #getWindowListeners
     */ 
    public void addWindowListener(WindowListener l) {
        super.addWindowListener(l);
    }
    /**
     * Removes the specified window listener so that it no longer
     * receives window events from this window.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param 	l the window listener
     * @see #addWindowListener
     * @see #getWindowListeners
     */ 
    public void removeWindowListener(WindowListener l) {
        super.removeWindowListener(l);
    }

   /**
     * Makes the Window visible. If the Window and/or its owner
     * are not yet displayable, both are made displayable.  The 
     * Window will be validated prior to being made visible.  
     * If the Window is already visible, this will bring the Window 
     * to the front.
     * @see       Component#isDisplayable
     * @see       #toFront
     * @see       Component#setVisible
     */
    public void show() {
        super.show();
    }
}
