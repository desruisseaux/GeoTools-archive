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

package org.geotools.gui.swing.toolbox;

import org.geotools.gui.swing.toolbox.tooltree.*;
import org.geotools.gui.swing.toolbox.WidgetTool;
import java.util.Map;

/**
 * @author johann sorel
 */
public interface WidgetToolDescriptor {

    /**
     * empty Parameter array
     */
    public final Parameter[] EMPTY_PARAMETER_ARRAY = {};
    
    /**
     * @return name of the tool
     */
    public String getTitle();
    
    /**
     * used to categorize a widget tool
     * @return String Path ex: {utilities,convert}
     */
    public String[] getPath();
    
    /**
     * @param parameters 
     * @return the panel of the tool
     */
    public WidgetTool createTool(Map parameters);
                
    /**
     * get an array of parameter describing parameters for the createComponent method
     * @return array of Parameter
     */
    public Parameter[] getParametersInfo();
    
    
}
