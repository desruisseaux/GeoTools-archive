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

package org.geotools.gui.swing.misc.filtre.vecteur;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Vector filters
 * @author johann Sorel
 */
public class FiltreVecteur extends FileFilter{
    
    private String[] extension = {
            ".dgn",
            ".gml",
            ".kml",
            ".mif",
            ".shp",
            ".tab"
        };
    
    public boolean accept(File fichier) {
                
        String nom = fichier.getName();
        
        if( fichier.isDirectory())
            return true;
        
        for( byte i=0; i<extension.length; i++){
            if ( nom.toLowerCase().endsWith(extension[i]))
                return true;            
        }
        
        return false;        
    }
    
    public String getDescription() {
        return "Format Vecteur";
    }
}
