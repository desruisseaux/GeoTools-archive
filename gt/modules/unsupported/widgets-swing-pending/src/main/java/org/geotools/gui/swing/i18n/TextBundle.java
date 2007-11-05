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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author johann sorel
 */
public class TextBundle {
    
    private static TextBundle instance;    
    private List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
    
    private TextBundle() {
        bundles.add( ResourceBundle.getBundle("org/geotools/gui/swing/i18n/translate") );
        bundles.add( ResourceBundle.getBundle("org/geotools/gui/swing/i18n/trs_fileformat") );
    }
    
        
    
    public String getString(String key){
        
        for(int i = bundles.size()-1; i>=0; i--){
            ResourceBundle bundle = bundles.get(i);
            
            if (existe(bundle, key)) {
                String text = bundle.getString(key);
                if (text.startsWith("$")) {
                    return getString(text.substring(1));
                } else {
                    return text;
                }
            }
        }

        return "Key:"+ key +" -missing text-";        
    }
    
    
    private boolean existe(ResourceBundle bundle, String key) {

        Enumeration<String> keys = bundle.getKeys();

        while (keys.hasMoreElements()) {
            if (key.equals(keys.nextElement())) {
                return true;
            }
        }

        return false;
    }
    
    
    
    public void addBundle(ResourceBundle bundle){
        bundles.add(bundle);
    }
    
    
    
    public static TextBundle getResource() {
        if (instance == null) {
            instance = new TextBundle();
        }
        return instance;
    }
    
}
