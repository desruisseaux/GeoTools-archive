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

package org.geotools.gui.swing.toolbox.tooltree;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 *
 * @author johann sorel
 */
public class ToolTreePath {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/geotools/gui/swing/toolbox/tooltree/Bundle");
    private static final String[] EMPTY_STRING_ARRAY = {};
    
    private final List<String> paths = new ArrayList<String>();
    
    ToolTreePath(ToolTreePath father, String i18nKey){
        String myPath = BUNDLE.getString(i18nKey);
        
        if(father != null){
            paths.addAll(father.getInerPath());
        }
        paths.add(myPath);
    }
    
    List<String> getInerPath(){
        return paths;
    }
    
    
    /**
     * get the Sting array of this path
     * @return String[]
     */
    public String[] getPath(){
        return paths.toArray(EMPTY_STRING_ARRAY);        
    }
    
           
}
