/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.wms.capabilities;

import java.net.URL;

/**
 * @author rgould
 * 
 * A Map Server may use zero or more MetadataURL elements to offer detailed, 
 * standardized metadata about the data underneath a particular layer. The type
 * attribute indicates the standard to which the metadata complies. Two types
 * are defined at present: 'TC211' = ISO TC211 19115; 'FGDC' = FGDC CSDGM.  The
 * format element indicates how the metadata is structured.
 */
public class MetadataURL {
    
    public final static int TC211 = 1;
    public final static int FGDC  = 2;

    private String format;
    private URL onlineResource;
    
    private int type;
    
    public MetadataURL(){
        format = "";type = 0;onlineResource = null;
    }
    
    public static int parseType(String s){
        if("TC211".equals(s))
            return TC211;
        if("FGDC".equals(s))
            return FGDC;
        return 0;
    }
    
    public static String writeType(int type){
        switch(type){
        case TC211:
            return "TC211";
        case FGDC:
            return "FGDC";
        default:
            return null;
        }
    }
    
    /**
     * @param format
     * @param onlineResource
     * @param type
     */
    public MetadataURL(String format, URL onlineResource, int type) {
        super();
        this.format = format;
        this.onlineResource = onlineResource;
        this.type = type;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public URL getOnlineResource() {
        return onlineResource;
    }
    public void setOnlineResource(URL onlineResource) {
        this.onlineResource = onlineResource;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
}
