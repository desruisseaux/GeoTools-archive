/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */

package org.geotools.gui.tools;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.TransformException;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;


/**
 * Pan the map so that the new extent has the click point in the middle of the
 * map and then zoom in/out by the zoomFactor.
 *
 * @author Cameron Shorter
 * @version $Id: ClickPanToolImpl.java,v 1.4 2003/05/02 10:54:57 desruisseaux Exp $
 */
public class ClickPanToolImpl extends PanToolImpl implements ClickPanTool {
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.gui.tools.ClickPanImpl");

    /**
     * Construct a ZoomTool.
     */
    public ClickPanToolImpl() {
        setName("Click Pan");
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Do nothing for mousePressed.
     *
     * @param e MouseEvent
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * Do nothing for mouseReleased.
     *
     * @param e MouseEvent
     */
    public void mouseReleased(MouseEvent e) {}

    /**
     * Register this tool to receive MouseEvents from <code>component</code>.
     *
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     */
    public void addMouseListener(
        Component component,
        Context context
    ) {
        super.addMouseListener(component, context, this);
    }
}
