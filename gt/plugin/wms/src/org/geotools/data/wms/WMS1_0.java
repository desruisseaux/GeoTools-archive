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
package org.geotools.data.wms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.data.wms.capabilities.Capabilities;
import org.geotools.data.wms.capabilities.Layer;
import org.geotools.data.wms.capabilities.Request;
import org.geotools.data.wms.capabilities.Service;
import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Provides support for the Web Map Server 1.0 Specificaiton.
 * <p>
 * This class opperates as a Factory creating the following related objects.
 * <ul>
 * <li>WMS1_0.Parser - a WMSParser capable of parsing a Get Capabilities Document
 * <li>WMS1_0.Format - a WMSFormat describing required parameters
 * <li>WMS1_0.MapRequest - a MapRequest specific to WMS 1.0
 * </ul>
 * </p>
 * <p>
 * The idea is that this class opperates a Toolkit for all things assocated with
 * Web Map Server 1.0 Specification. The various objects produced by this toolkit
 * are used as stratagy objects for the top level Web Map Server objects:
 * <ul>
 * <li>Web Map Server - uses WMS1_0.Parser to derive a Get Capabilities Document
 * <li>Web Map Server - uses WMS1_0 as a WMSFormat factory to generate the correct
 *     WMS_1_0.Format.
 * </ul>
 * </p> 
 * <p>
 * WMS1_0 provides both name and version information that may be checked against
 * a GetCapabilities document during version negotiation.
 * </p> 
 * @author Jody Garnett, Refractions Research
 */
public class WMS1_0 extends Specification {
    
    /** Expected name attribute for root element */
    public String getName(){
        return "WMT_MS_Capabilities";
    }
    /**
     * Expected version attribute for root element.
     */
    public String getVersion(){
        return "1.0.0";
    }
    
    static final Map mime = new HashMap();
    static {
        mime.put("GIF", "image/gif");
        mime.put("PNG", "image/png");
        mime.put("JPEG", "image/jpeg");
        mime.put("WebCGM", null );
        mime.put("SVG", null );
        mime.put("GML.1", null );
        mime.put("GML.2", null );
        mime.put("GML.3", null );
        mime.put("WBMP", null );
        mime.put("WMS_XML", null );
        mime.put("MIME", null );
        mime.put("INIMAGE", null );
        mime.put("TIFF", null );
        mime.put("GeoTIFF", null );
        mime.put("PPM", null );
        mime.put("BLANK", null );
             
    }    
    /**
     * Provides mapping from well known format to MIME type.
     * <p>
     * WebMapServer api uses mime type internally for format information
     * (indeed WMS 1.0.0 is the only WMS specifcation not to use MIME type
     * directly).
     * </p>
     * <p>
     * @param format
     * @return MIME type for format
     */
    public static final String toMIME(String format ){
        if( mime.containsKey( format )){
            return (String) mime.get( format );
        }
        return null;
    }
    /**
     * Provides mapping from MIME type to WMS 1.0.0 Format.
     * <p>
     * WebMapServer api uses mime type internally for format information
     * (indeed WMS 1.0.0 is the only WMS specifcation not to use MIME type
     * directly).
     * </p>
     * <p>
     * @param mimeType MIME type such as "image/gif"
     * @return Format well known WMS 1.0.0 format such as "GIF"
     */
    public static final String toFormat(String mimeType ){
        for( Iterator i=mime.entrySet().iterator(); i.hasNext(); ){
            Map.Entry entry = (Map.Entry) i.next();
            if( mimeType.equals( entry.getValue())){
                return (String) entry.getKey();
            }            
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.geotools.data.wms.Specification#createParser(org.jdom.Document)
     */
    public WMSParser createParser(Document document) {
        return new Parser();
    }
    /* (non-Javadoc)
     * @see org.geotools.data.wms.Specification#createRequest(java.net.URL)
     */
    public GetCapabilitiesRequest createRequest(URL server) {
        return new GetCapsRequest( server );
    }
    /**
     * We need a custom request object.
     * <p>
     * WMS 1.0.0 does requests a bit differently:
     * <ul>
     * <li>WMSVER=1.0.0
     * <li>
     * </p>
     */ 
    static public class GetCapsRequest extends GetCapabilitiesRequest {
        /**
         * Construct a Request compatable with a 1.0.0 Web Feature Server.
         * 
         * @param urlGetCapabilities URL of GetCapabilities document.
         */
        public GetCapsRequest(URL urlGetCapabilities ) {
            super(urlGetCapabilities);
        }
        protected void initVersion(){
    	    setProperty("WEBVER", "1.0.0");
    	}
    }
    static public class Parser extends AbstractWMSParser {
        public String getVersion() {
            return "1.0.0";
        } 
        /**
         * WMS 1.0 makes use of a different definition of format that what we would like.
         * <p>
         * KnownFormats for 1.0.0:
         * <pre><code>
         * GIF | JPEG | PNG | WebCGM |
         * SVG | GML.1 | GML.2 | GML.3 |
         * WBMP | WMS_XML | MIME | INIMAGE |
         * TIFF | GeoTIFF | PPM | BLANK
         * </code></pre>
         * </p>
         * <p>
         * Our problem being that queryFormats is expected to return a list of mime types:
         * <ul>
         * <li>GIF mapped to "image/gif"
         * <li>PNG mapped to "image/png"
         * <li>JPEG mapped to "image/jpeg"
         * </ul>
         * </p>
         * <p>
         * We will need the reverse mapping when need to encode a WMS 1.0.0 request.
         * We should probably punt this mapping off to a property file.
         * </p>
         * @see org.geotools.data.wms.AbstractWMSParser#queryFormats(org.jdom.Element)
         */
        protected List queryFormats(Element op) {
            // Example: <Format><PNG /><JPEG /><GML.1 /></Format>
            Iterator iter;
            
            List formats = new ArrayList();
            List formatElements = op.getChildren("Format");
            iter = formatElements.iterator();
            while (iter.hasNext()) {
                Element format = (Element) iter.next();
                String mime = toMIME( format.getName() );
                if( mime != null ){
                    formats.add( mime );
                }
            }        
            return formats;
        }
    }
}
