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

package org.geotools.gui.swing.icon;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;

/**
 * @author johann sorel
 */
public class IconBundle {

    private static IconBundle BUNDLE;
    private ImageIcon EMPTY_ICON = new ImageIcon(IconBundle.class.getResource("/org/geotools/gui/swing/icon/blanc.png"));
    private ResourceBundle defaultbundle = ResourceBundle.getBundle("org/geotools/gui/swing/icon/defaultset");
    private ResourceBundle activebundle = defaultbundle;
    private WeakHashMap<String, ImageIcon> iconsmap = new WeakHashMap<String, ImageIcon>();

    private IconBundle() {
    }

    /**
     * get the ImageIcon matching the key String.
     * return a blanc imageIcon size 1x1 if the key isn't found.
     * @param key
     * @return ImageIcon
     */
    public ImageIcon getIcon(String key) {
        
        if(key == null) return EMPTY_ICON;
        
        ImageIcon icon = EMPTY_ICON;
        
        if (iconsmap.containsKey(key)) {
            icon = iconsmap.get(key);
        } else {
            String adress = getValue(key);
            if (adress != null) {
                icon = new ImageIcon(IconBundle.class.getResource(adress));
                iconsmap.put(key, icon);
            }
        }

        return icon;
    }

    private String getValue(String key) {

        if (activebundle != null) {
            if ( existe(activebundle,key) ) {
                String adress = activebundle.getString(key);
                if (adress.startsWith("$")) {
                    return getValue(adress.substring(1));
                } else {
                    return adress;
                }
            }
        } else {
            if ( existe(defaultbundle,key)) {
                String adress = defaultbundle.getString(key);
                if (adress.startsWith("$")) {
                    getValue(adress.substring(1));
                } else {
                    return adress;
                }
            }
        }

        return null;
    }

    private boolean existe(ResourceBundle bundle, String key){
        
        Enumeration<String> keys = bundle.getKeys();
        
        while (keys.hasMoreElements()){
            if( key.equals( keys.nextElement() ))
               return true;
        }
        
        return false;
        
    }
    
    /**
     * use your own icon bundle, property file should look like defaultset.properties.
     * if icon is missing the defautltset icon will be used
     * @param bundle ResourceBundle
     */
    public void setBundle(ResourceBundle bundle) {
        activebundle = bundle;
    }

    public static IconBundle getResource() {
        if (BUNDLE == null) {
            BUNDLE = new IconBundle();
        }
        return BUNDLE;
    }
}