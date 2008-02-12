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

package org.geotools.gui.swing.toolbox.widgettool.clipping;

import java.util.Map;
import java.util.ResourceBundle;
import org.geotools.gui.swing.toolbox.Parameter;
import org.geotools.gui.swing.toolbox.widgettool.WidgetTool;
import org.geotools.gui.swing.toolbox.tooltree.ToolTreeConstants;
import org.geotools.gui.swing.toolbox.widgettool.AbstractWidgetToolDescriptor;
import org.geotools.gui.swing.toolbox.widgettool.WidgetToolDescriptor;

/**
 *
 * @author johann sorel
 */
public class ClippingTTDescriptor extends AbstractWidgetToolDescriptor{

   
    private final String[] path = ToolTreeConstants.getInstance().ANALYSE_GEOMETRIE.getPath();
    
    private String title = ResourceBundle.getBundle("org/geotools/gui/swing/toolbox/tools/clipping/Bundle").getString("clip");
       
    
    public String getTitle() {
        return title;
    }

    public String[] getPath() {
        return path;
    }

    public WidgetTool createTool(Map parameters) {
        return new ClippingTool(parameters);
    }

    public Parameter[] getParametersInfo() {
        return EMPTY_PARAMETER_ARRAY;
    }

}
