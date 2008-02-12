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

package org.geotools.gui.swing.toolbox.process;

import java.util.Map;
import org.geotools.gui.swing.toolbox.Parameter;

/**
 * @author johann sorel
 */
public interface ProcessDescriptor {

    /**
     * empty Parameter array
     */
    public final Parameter[] EMPTY_PARAMETER_ARRAY = {};
    
    /**
     * @return name of the tool
     */
    public String getTitle();
        
    /**
     * @param parameters 
     * @return ProcessTool
     * @throws java.lang.IllegalArgumentException 
     */
    public Process createProcess(Map parameters) throws IllegalArgumentException;
                
    /**
     * get an array of parameter to config the process tool
     * @return array of Parameter
     */
    public Parameter[] getParametersInfo();
    
    
}
