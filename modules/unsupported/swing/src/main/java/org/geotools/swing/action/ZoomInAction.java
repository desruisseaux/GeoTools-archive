/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.action;

import java.awt.event.ActionEvent;
import org.geotools.swing.JMapPane;
import org.geotools.swing.tool.ZoomInTool;

/**
 * An action for connect a control (probably a JButton) to
 * the ZoomInTool for zooming the map with mouse clicks
 * or drags.
 * 
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class ZoomInAction extends MapAction {

    /**
     * Constructor - when used with a JButton the button will
     * display a small icon only
     * 
     * @param pane the map pane being serviced by this action
     */
    public ZoomInAction(JMapPane pane) {
        this(pane, false);
    }

    /**
     * Constructor
     * 
     * @param pane the map pane being serviced by this action
     * @param showToolName set to true for the control to display the tool name
     */
    public ZoomInAction(JMapPane pane, boolean showToolName) {
        String toolName = showToolName ? ZoomInTool.TOOL_NAME : null;
        super.init(pane, toolName, ZoomInTool.TOOL_TIP, ZoomInTool.ICON_IMAGE);
    }
    
    /**
     * Called when the associated control is activated. Leads to the
     * map pane's cursor tool being set to a new ZoomInTool object
     */
    public void actionPerformed(ActionEvent e) {
        pane.setCursorTool(new ZoomInTool(pane));
    }

}
