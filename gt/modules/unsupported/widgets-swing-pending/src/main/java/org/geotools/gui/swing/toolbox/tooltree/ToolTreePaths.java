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

import java.lang.ref.WeakReference;

/**
 *
 * @author johann sorel
 */
public class ToolTreePaths {

    private static WeakReference<ToolTreePaths> ref = null;
    
    
    public final ToolTreePath ANALYSE = new ToolTreePath(null,"analyse");
    public final ToolTreePath ANALYSE_GEOMETRIE = new ToolTreePath(ANALYSE,"geometry");
    
    public final ToolTreePath DATABASE = new ToolTreePath(null,"database");
    public final ToolTreePath DATABASE_CONVERT = new ToolTreePath(DATABASE,"convert");
    public final ToolTreePath DATABASE_CREATE = new ToolTreePath(DATABASE,"create");
    
    public final ToolTreePath FILE = new ToolTreePath(null,"file");
    public final ToolTreePath FILE_CONVERT = new ToolTreePath(FILE,"convert");
    public final ToolTreePath FILE_CREATE = new ToolTreePath(FILE,"create");
    
    
    
        
    /**
     * ToolTreePaths instance
     * @return ToolTreePaths
     */
    public static ToolTreePaths getInstance(){
     
        ToolTreePaths cst = null;
        if(ref != null){
            cst = ref.get();
           }
        
        if(cst == null){
            cst = new ToolTreePaths();
            ref = new WeakReference(cst);
        }
        
        return cst;        
    }
    
           
}
