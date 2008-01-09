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

package org.geotools.gui.swing.toolbox.tools.svg2mif;

import java.util.Map;
import java.util.ResourceBundle;
import org.geotools.gui.swing.toolbox.tooltree.Parameter;
import org.geotools.gui.swing.toolbox.WidgetTool;
import org.geotools.gui.swing.toolbox.tooltree.ToolTreePaths;
import org.geotools.gui.swing.toolbox.tooltree.TreeToolDescriptor;

/**
 *
 * @author Laurent Jegou
 */
public class SVG2MIFTTDescriptor implements TreeToolDescriptor{

    private final String[] path = ToolTreePaths.getInstance().FILE_CONVERT.getPath();    
    String title = ResourceBundle.getBundle("org/geotools/gui/swing/toolbox/tools/svg2mif/Bundle").getString("title");
    
    public String getTitle(){
        return "SVG > MIF";
    }

    public String[] getPath() {
        return path;
    }

    public WidgetTool createTool(Map parameters) {
        return new SVG2MIFTool();
    }

    public Parameter[] getParametersInfo() {
        return EMPTY_PARAMETER_ARRAY;
    }

}
