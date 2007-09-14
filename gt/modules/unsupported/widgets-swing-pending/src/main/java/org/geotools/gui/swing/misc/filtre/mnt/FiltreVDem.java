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

package org.geotools.gui.swing.misc.filtre.mnt;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * 
 * @author johann Sorel
 */
public class FiltreVDem extends FileFilter{
    
    @Override
    public String getDescription(){
        return "MNT VisualDEM (.dem)";
    }
    
    public boolean accept( File f ){
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".dem");
    }
    
}
