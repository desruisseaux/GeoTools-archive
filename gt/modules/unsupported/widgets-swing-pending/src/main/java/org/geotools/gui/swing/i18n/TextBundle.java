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

package org.geotools.gui.swing.i18n;

import java.util.ResourceBundle;

/**
 * @author johann sorel
 */
public class TextBundle {
    
    private static TextBundle BUNDLE;    
    private ResourceBundle bundle = ResourceBundle.getBundle("org/geotools/gui/swing/i18n/translate");
    
    private TextBundle() {}
    
    public String getString(String key){
        String retour = "";
        
        try{
            retour = bundle.getString(key);
        } catch(Exception e){
            retour = "Key:"+ key +" -missing text-" ;
        }
        return retour;
    }
    
    public static TextBundle getResource() {
        if (BUNDLE == null) {
            BUNDLE = new TextBundle();
        }
        return BUNDLE;
    }
    
}
