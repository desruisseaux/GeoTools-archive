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

package org.geotools.gui.swing.toolbox.widgettool.vdem2csv;

import java.util.Map;
import java.util.ResourceBundle;
import org.geotools.gui.swing.toolbox.Parameter;
import org.geotools.gui.swing.toolbox.widgettool.WidgetTool;
import org.geotools.gui.swing.toolbox.tooltree.ToolTreeConstants;
import org.geotools.gui.swing.toolbox.widgettool.AbstractWidgetToolDescriptor;

/**
 *
 * @author johann Sorel
 */
public class VDem2CSVTTDescriptor extends AbstractWidgetToolDescriptor{

    private final String[] path = ToolTreeConstants.getInstance().FILE_CONVERT.getPath();
    String title = ResourceBundle.getBundle("org/geotools/gui/swing/toolbox/tools/vdem2csv/Bundle").getString("title");
    
    
    public String getTitle(){
        return "VDem > CSV";
    }


    @Override
    public String[] getPath() {
        return path;
    }

    public WidgetTool createTool(Map parameters) {        
        return new VDem2CSVTool();
    }

    @Override
    public Parameter[] getParametersInfo() {
        return EMPTY_PARAMETER_ARRAY;
    }


}
