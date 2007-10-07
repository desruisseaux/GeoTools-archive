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

package org.geotools.gui.swing.misc.filtre.raster;
import java.io.File;

import javax.swing.filechooser.FileFilter;
/**
 * Filtre par fichier GrassRaster
 * @author johann Sorel
 */
public class FiltreRasterGrass extends FileFilter{

    
    /** Creates a new instance of FilterRasterGRass */
    public FiltreRasterGrass() {
    }
    public boolean accept(File fichier) {
        
        String nom = fichier.getName();
        
        if ( nom.endsWith(".GRASS RASTER")){
            return true;
        }
        if (fichier.isDirectory()){
            return true;
        }
    
        return false;
        
    }

    public String getDescription() {
        return ".GRASS RASTER";
    }
    

}
