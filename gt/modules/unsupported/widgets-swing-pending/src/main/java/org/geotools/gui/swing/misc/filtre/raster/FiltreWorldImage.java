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
 * Filtre par fichier Raster
 * @author johann Sorel
 */
public class FiltreWorldImage extends FileFilter{
    
    private String[] extension = {
            ".jpg",
            ".jpeg",
            ".bmp",
            ".png",
        };
    
    
    /**
     * Creates a new instance of FiltreRaster
     */
    public FiltreWorldImage() {
    }
    public boolean accept(File fichier) {
                
        String nom = fichier.getName();        
        
        for( int i=0; i<extension.length; i++){
            if ( nom.toLowerCase().endsWith(extension[i]) || fichier.isDirectory()){
                return true;
            }
        }
        return false;
        
    }
    
    public String getDescription() {
        return "World Image (.bmp .jpg .jpeg .png)";
    }
}
