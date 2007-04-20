/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gml.producer;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Coordinate;


//import org.geotools.feature.*;

/**
 * Handles the writing of coordinates for gml.
 *
 * @author Chris Holmes
 * @author Ian Schneider
 * @source $URL$
 */
public class CoordinateWriter {
    
    /**
     * Internal representation of coordinate delimeter (',' for GML is default)
     */
    private final String coordinateDelimiter;
    
    /** Internal representation of tuple delimeter (' ' for GML is  default) */
    private final String tupleDelimiter;
    
    /** To be used for formatting numbers, uses US locale. */
    private final NumberFormat coordFormatter = NumberFormat.getInstance(Locale.US);
    
    private final AttributesImpl atts = new org.xml.sax.helpers.AttributesImpl();
    
    private final StringBuffer coordBuff = new StringBuffer();
    
    private final FieldPosition zero = new FieldPosition(0);
    
    private char[] buff = new char[200];
    
    private final boolean useDummyZ;
    
    private final double dummyZ;
    
    /**
     * Flag controlling wether namespaces should be ignored.
     */
    private boolean namespaceAware = true;
    /**
     * Namepsace prefix + uri, default to gml
     */
    private String prefix = "gml";
    private String namespaceUri = GMLUtils.GML_URL;
    
    public CoordinateWriter() {
        this(4);
    }
    
    public CoordinateWriter(int numDecimals, boolean isDummyZEnabled) {
        this(numDecimals," ",",", isDummyZEnabled);
    }
    
    public CoordinateWriter(int numDecimals) {
        this(numDecimals,false);
    }
    
    //TODO: check gml spec - can it be strings?  Or just chars?
    public CoordinateWriter(int numDecimals, String tupleDelim, String coordDelim){
        this(numDecimals, tupleDelim, coordDelim, false);
    }
    
    public CoordinateWriter(int numDecimals, String tupleDelim, String coordDelim, boolean isDummyZEnabled){
        this(numDecimals, tupleDelim, coordDelim, isDummyZEnabled, 0);
    }
    
    public int getNumDecimals(){
        return coordFormatter.getMaximumFractionDigits();
    }
    
    public boolean isDummyZEnabled(){
        return useDummyZ;
    }
    
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }
    
    public CoordinateWriter(int numDecimals, String tupleDelim, String coordDelim, boolean useDummyZ, double zValue) {
        
        if (tupleDelim == null || tupleDelim.length() == 0)
            throw new IllegalArgumentException("Tuple delimeter cannot be null or zero length");
        
        if ((coordDelim != null) && coordDelim.length() == 0) {
            throw new IllegalArgumentException("Coordinate delimeter cannot be null or zero length");
        }
        
        tupleDelimiter = tupleDelim;
        coordinateDelimiter = coordDelim;
        
        coordFormatter.setMaximumFractionDigits(numDecimals);
        coordFormatter.setGroupingUsed(false);
        
        String uri = namespaceUri;
        if ( !namespaceAware ) {
            uri = null;
        }
        
        atts.addAttribute(uri, "decimal", "decimal", "decimal", ".");
        atts.addAttribute(uri, "cs", "cs", "cs",
                coordinateDelimiter);
        atts.addAttribute(uri, "ts", "ts", "ts", tupleDelimiter);
        
        this.useDummyZ = useDummyZ;
        this.dummyZ = zValue;
    }
    
    public void writeCoordinates(Coordinate[] c, ContentHandler output)
    throws SAXException {
        
        String prefix = this.prefix + ":";
        String namespaceUri = this.namespaceUri;
        
        if ( !namespaceAware ) {
            prefix = "";
            namespaceUri = null;
        }
        
        output.startElement(namespaceUri, "coordinates", prefix + "coordinates",
                    atts);    
                
        for (int i = 0, n = c.length; i < n; i++) {
            // clear the buffer
            coordBuff.delete(0, coordBuff.length());
            // format x into buffer and append delimiter
            coordFormatter.format(c[i].x,coordBuff,zero).append(coordinateDelimiter);
            // format y into buffer
            if(useDummyZ){
                coordFormatter.format(c[i].y,coordBuff,zero).append(coordinateDelimiter);
            } else{
                coordFormatter.format(c[i].y,coordBuff,zero);
            }
            // format dummy z into buffer if required
            if(useDummyZ){
                coordFormatter.format(dummyZ, coordBuff, zero);
            }
            // if theres another coordinate, tack on a tuple delimeter
            if (i + 1 < c.length)
                coordBuff.append(tupleDelimiter);
            // make sure our character buffer is big enough
            if (coordBuff.length() > buff.length) {
                buff = new char[coordBuff.length()];
            }
            // copy the characters
            coordBuff.getChars(0, coordBuff.length(), buff, 0);
            // finally, output
            output.characters(buff, 0, coordBuff.length());
        }
        
        output.endElement(namespaceUri,"coordinates", prefix + "coordinates");
    }
}
