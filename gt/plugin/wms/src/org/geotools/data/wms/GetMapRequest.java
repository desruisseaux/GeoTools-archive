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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.geotools.data.wms.getCapabilities.Layer;


/**
 * @author rgould
 *
 * Construct a WMS getMap request. 
 */
public class GetMapRequest {
	
	/** Represents OGC Exception MIME types */
	public static final String EXCEPTION_XML     = "application/vnd.ogc.se_xml";
	public static final String EXCEPTION_INIMAGE = "application/vnd.ogc.se_inimage";
	public static final String EXCEPTION_BLANK   = "application/vnd.ogc.se_blank";
	
    private URL onlineResource;
    private Properties properties;
    
    /**
     * Initialize properties and set the request propertie to "GetMap"
     */
    public GetMapRequest(URL onlineResource) {
        this.onlineResource = onlineResource;
        
        properties = new Properties();
        properties.setProperty("REQUEST", "GetMap");
        //TODO remove this when more version support comes along
        properties.setProperty("VERSION", "1.1.1");
    }
    
    public URL getFinalURL() {
        String url = onlineResource.toExternalForm();
        if (!url.endsWith("?")) {
            url = url.concat("?");
        }
        
        Iterator iter = properties.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String param = entry.getKey() +"="+entry.getValue();
            if (iter.hasNext()) {
                param = param.concat("&");
            }
            url = url.concat(param);
        }
        
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            //If something is wrong here, this is something wrong with the code above.
        }
        
        return null;
    }

    /**
     * Sets the version number of the request.
     * @param version A String indicting a WMS Version ("1.0.0", "1.1.0", or "1.1.1")
     */
    public void setVersion(String version) {
        //TODO Version stuff here
        properties.setProperty("VERSION", version);
    }
    
    /**
     * Adds the specified Layers into the request. Does not add sub-layers. 
     * Each layer MUST have a it's <code>name</code> field set. 
     * 
     * The layers are drawn bottom-up, with the layer at element 0 drawn 
     * on the bottom and the layer at layers.size() on top.
     * 
     * @param layers A list of Layers, each containing a name.
     */
    public void setLayers(List layers) {
        String layerList = "";
        
        for (int i = 0; i < layers.size(); i++) {
            Layer layer = (Layer) layers.get(i);
            layerList.concat(layer.getName());
            
            if (i != layers.size()-1) {
                layerList.concat(",");
            }
        }
        setLayers(layerList);
    }
    
    /**
     * From the Web Map Service Implementation Specification:
     * "The required LAYERS parameter lists the map layer(s) to be returned by this GetMap
     * request. The value of the LAYERS parameter is a comma-separated list of one or more
     * valid layer names. Allowed layer names are the character data content of any
     * &lt;Layer>&lt;Name> element in the Capabilities XML.
     * 
     * Layers are rendered by drawing the leftmost in the list bottommost, the next one 
     * over that, and so on."
     * 
     * @param layerList A comma-delimited String containing the names of Layers to be rendered.
     */
    public void setLayers(String layerList) {
       properties.setProperty("LAYERS", layerList); 
    }
    
    /**
     * From the Web Map Service Implementation Specification:
     * "The required STYLES parameter lists the style in which each layer is to be 
     * rendered. The value of the styleList is a comma-separated list of one or 
     * more valid style names. There is a one-to-one correspondance between the values
     * in the LAYERS parameter and the values in the STYLES parameter. Each map in the
     * list of LAYERS is drawn using the corresponding style in the same position in the
     * list of STYLES. The Client may not request a Layer in a Style that was only
     * defined for a different Layer.
     * 
     * A client may request the default Style by using an empty String (""). If several
     * layers are requested with a mixture of named and default styles, the STYLES parameter
     * includes null values between commas (eg. "STYLES=style1,,style2,,")."
     * 
     * @param styleList A comma-separated list of style names.
     */
    public void setStyles(String styleList) {
        properties.setProperty("STYLES", styleList);
    }
    
    /**
     * From the Web Map Service Implementation Specification:
     * "The required SRS parameter states which Spatial Reference System applies to the values
     * in the BBOX parameter. The value of the SRS parameter shall be on of the values 
     * defined in the character data section of an &lt;SRS> element defined or inherited by
     * the requested layer. The same SRS applies to all layers in a single request.
     * 
     * If the WMS has declared SRS=NONE for a Layer, then the Layer does not have a well-defined
     * spatial reference system and should not be shown in conjunction with other layers. The client
     * shall specify SRS as "none" in the GetMap request and the Server may issue a Service 
     * Exception otherwise."
     * 
     * @param srs A String indicating the Spatial Reference System to render the layers in.
     */
    public void setSRS(String srs) {
        properties.setProperty("SRS", srs);
    }
    
    /**
     * From the Web Map Service Implementation Specification:
     * "The required BBOX parameter allows a Client to request a particular Bounding Box.
     * The value of the BBOX parameter in a GetMap request is a list of comma-separated numbers
     * of the form "minx,miny,maxx,maxy".
     * 
     * If the WMS server has declared that a Layer is not subsettable, then the Client shall
     * specify exactly the declared Bounding Box values in the GetMap request and the Server may
     * issue a Service Exception otherwise."
     * 
     * @param bbox A string representing a bounding box in the format "minx,miny,maxx,maxy"
     */
    public void setBBox(String bbox) {
        properties.setProperty("BBOX", bbox);
    }
    
    /**
     * From the Web Map Service Implementation Specification:
     * "The required FORMAT parameter states the desired format of the response to an
     * operation. Supported values for a GetMap request on a WMS instance are listed
     * in one or more &lt;Format> elements in the &;ltRequest>&lt;GetMap> element of
     * its Capabilities XML. The entire MIME type string in &lt;Format> is used as 
     * the value of the FORMAT parameter."
     *  
     * @param format The desired format for the GetMap response
     */
    public void setFormat(String format) {
        properties.setProperty("FORMAT", format);
    }
    
    /**
     * From the Web Map Service Implementation Specification:
     * "The required WIDTH and HEIGHT parameters specify the size in integer pixels of the
     * map image to be produced. WIDTH specifies the number of pixels to be used between
     * the minimum and maximum X values (inclusive) in the BBOX parameter, while
     * HEIGHT specifies the number of pixels between the minimum and maximum Y values.
     * 
     * If the WMS server has declared that a Layer has fixed width and height, then the 
     * Client shall specify exactly those WIDTH and HEIGHT values in the GetMap request
     * and the Server may issue a Service Exception otherwise."
     * 
     * @param width
     * @param height
     */
    public void setDimensions(String width, String height) {
        properties.setProperty("HEIGHT", height);
        properties.setProperty("WIDTH", width);
    }
    
    // End required parameters, being optional ones.
    
    //TODO Implement optional parameters.
    
    /**
     * From the Web Map Service Implementation Specification:
     * "The optional TRANSPARENT parameter specifies whether the map background is to be
     * made transparent or not. The default value is false if the parameter is absent
     * from the request."
     * 
     * @param transparent true for transparency, false otherwise
     */
    public void setTransparent(boolean transparent) {
    	String value = "FALSE";
    	if (transparent) {
    		value = "TRUE";
    	}
    	properties.setProperty("TRANSPARENT", value);
    }
    
    /**
     * Specifies the colour, in hexidecimal format, to be used as the background of the map.
     * It is a String representing RGB values in hexidecimal format, prefixed by "0x".
     * The format is: 0xRRGGBB.
     * The default value is 0xFFFFFF (white)
     * 
     * @param bgColour the background colour of the map, in the format 0xRRGGBB
     */
    public void setBGColour(String bgColour) {
    	properties.setProperty("BGCOLOR", bgColour);
    }
    
    /**
     * The exceptions type specifies what format the server should return exceptions in.
     * The default is "application/vnd.ogc.se_xml". 
     * Other valid values are:
     * "application/vnd.ogc.se_inimage"
     * "application/vnd.ogc.se_blank"
     * @param exceptions
     */
    public void setExceptions(String exceptions) {
    	properties.setProperty("EXCEPTIONS", exceptions);
    }
    
    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annexes B and C
     * @param time See the Web Map Server Implementation Specification 1.1.1, Annexes B and C
     */
    public void setTime (String time) {
    	properties.setProperty("TIME", time);
    }
    
    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annex C, in
     * particular section C.4
     * @param elevation See the Web Map Server Implementation Specification 1.1.1, Annex C
     */
    public void setElevation (String elevation) {
    	properties.setProperty("ELEVATION", elevation);
    }
    
    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annex C, in
     * particular section C.4.2
     * 
     * Example use:
     * request.setSampleDimensionValue("DIM_WAVELENGTH", "4000");
     * 
     * @param name the request parameter name to set (usually with 'dim_' as prefix)
     * @param value the value of the request parameter (value, interval or comma-separated list)
     */
    public void setSampleDimensionValue(String name, String value) {
    	properties.setProperty(name, value);
    }
    
    /**
     * Used to implement vendor specific parameters. Entirely optional.
     * 
     * @param name a request parameter name
     * @param value a value to accompany the name
     */
    public void setVendorSpecificParameter(String name, String value) {
    	properties.setProperty(name, value);
    }
    
    public void setProperty(String name, String value) {
    	properties.setProperty(name, value);
    }
}
