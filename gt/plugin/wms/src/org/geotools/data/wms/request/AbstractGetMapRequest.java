/*
 * Created on 10-Nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.request;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.geotools.data.ows.LatLonBoundingBox;

import org.geotools.data.wms.SimpleLayer;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractGetMapRequest extends AbstractRequest implements GetMapRequest {
 

    /**
     * Constructs a GetMapRequest. The data passed in represents valid values 
     * that can be used.
     * 
     * @param onlineResource the location that the request should be applied to
     * @param properties pre-set properties to be used. Can be null.
     */
    public AbstractGetMapRequest(URL onlineResource, Properties properties) {
        super(onlineResource, properties);

        initRequest();
        initVersion();
    }

    protected abstract void initVersion();
    
    protected void initRequest() {
        setProperty(REQUEST, "GetMap"); //$NON-NLS-1$
    }

    /**
     * Sets the version number of the request.
     *
     * @param version A String indicting a WMS Version ("1.0.0", "1.1.0",
     *        "1.1.1", or "1.3.0")
     */
    public void setVersion(String version) {
        properties.setProperty(VERSION, version);
    }

    /**
     * @see org.geotools.data.wms.request.GetMapRequest#setLayers(java.util.List)
     */
    public void setLayers(List layers) {
        String layerString = ""; //$NON-NLS-1$
        String styleString = ""; //$NON-NLS-1$

        for (int i = 0; i < layers.size(); i++) {
            SimpleLayer simpleLayer = (SimpleLayer) layers.get(i);
            try {
				layerString = layerString + URLEncoder.encode(simpleLayer.getName(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				layerString = layerString + simpleLayer.getName();
			}
            try {
				styleString = styleString + URLEncoder.encode(StringUtils.defaultString(simpleLayer.getStyle()), "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				styleString = styleString + StringUtils.defaultString(simpleLayer.getStyle());
			}

            if (i != (layers.size() - 1)) {
                layerString = layerString + ","; //$NON-NLS-1$
                styleString = styleString + ","; //$NON-NLS-1$
            }
        }

        setProperty(LAYERS, layerString);
        setProperty(STYLES, styleString);
    }

    /**
     * From the Web Map Service Implementation Specification: "The required SRS
     * parameter states which Spatial Reference System applies to the values
     * in the BBOX parameter. The value of the SRS parameter shall be on of
     * the values defined in the character data section of an &lt;SRS> element
     * defined or inherited by the requested layer. The same SRS applies to
     * all layers in a single request. If the WMS has declared SRS=NONE for a
     * Layer, then the Layer does not have a well-defined spatial reference
     * system and should not be shown in conjunction with other layers. The
     * client shall specify SRS as "none" in the GetMap request and the Server
     * may issue a Service Exception otherwise."
     *
     * @param srs A String indicating the Spatial Reference System to render
     *        the layers in.
     */
    public void setSRS(String srs) {
        properties.setProperty(SRS, srs);
    }

    /**
     * From the Web Map Service Implementation Specification: "The required
     * BBOX parameter allows a Client to request a particular Bounding Box.
     * The value of the BBOX parameter in a GetMap request is a list of
     * comma-separated numbers of the form "minx,miny,maxx,maxy". If the WMS
     * server has declared that a Layer is not subsettable, then the Client
     * shall specify exactly the declared Bounding Box values in the GetMap
     * request and the Server may issue a Service Exception otherwise."
     *
     * @param bbox A string representing a bounding box in the format
     *        "minx,miny,maxx,maxy"
     */
    public void setBBox(String bbox) {
        //TODO enforce non-subsettable layers
        properties.setProperty(BBOX, bbox);
    }

    public void setBBox(LatLonBoundingBox box){
        StringBuffer sb = new StringBuffer();
        sb.append(box.getMinX());
        sb.append(",");
        sb.append(box.getMinY()+",");
        sb.append(box.getMaxX()+",");
        sb.append(box.getMaxY());
        setBBox(sb.toString());
    }
    /**
     * From the Web Map Service Implementation Specification: "The required
     * FORMAT parameter states the desired format of the response to an
     * operation. Supported values for a GetMap request on a WMS instance are
     * listed in one or more &lt;Format> elements in the
     * &;ltRequest>&lt;GetMap> element of its Capabilities XML. The entire
     * MIME type string in &lt;Format> is used as the value of the FORMAT
     * parameter."
     *
     * @param format The desired format for the GetMap response
     */
    public void setFormat(String format) {
        properties.setProperty(FORMAT, format);
    }

    /**
     * From the Web Map Service Implementation Specification: "The required
     * WIDTH and HEIGHT parameters specify the size in integer pixels of the
     * map image to be produced. WIDTH specifies the number of pixels to be
     * used between the minimum and maximum X values (inclusive) in the BBOX
     * parameter, while HEIGHT specifies the number of pixels between the
     * minimum and maximum Y values. If the WMS server has declared that a
     * Layer has fixed width and height, then the Client shall specify exactly
     * those WIDTH and HEIGHT values in the GetMap request and the Server may
     * issue a Service Exception otherwise."
     *
     * @param width
     * @param height
     */
    public void setDimensions(String width, String height) {
        properties.setProperty(HEIGHT, height);
        properties.setProperty(WIDTH, width);
    }

    // End required parameters, being optional ones.
    //TODO Implement optional parameters.

    /**
     * From the Web Map Service Implementation Specification: "The optional
     * TRANSPARENT parameter specifies whether the map background is to be
     * made transparent or not. The default value is false if the parameter is
     * absent from the request."
     *
     * @param transparent true for transparency, false otherwise
     */
    public void setTransparent(boolean transparent) {
        String value = "FALSE"; //$NON-NLS-1$

        if (transparent) {
            value = "TRUE"; //$NON-NLS-1$
        }

        properties.setProperty(TRANSPARENT, value);
    }

    /**
     * Specifies the colour, in hexidecimal format, to be used as the
     * background of the map. It is a String representing RGB values in
     * hexidecimal format, prefixed by "0x". The format is: 0xRRGGBB. The
     * default value is 0xFFFFFF (white)
     *
     * @param bgColour the background colour of the map, in the format 0xRRGGBB
     */
    public void setBGColour(String bgColour) {
        properties.setProperty(BGCOLOR, bgColour);
    }

    /**
     * The exceptions type specifies what format the server should return
     * exceptions in.
     * 
     * <p>
     * Valid values are:
     * 
     * <ul>
     * <li>
     * "application/vnd.ogc.se_xml" (the default)
     * </li>
     * <li>
     * "application/vnd.ogc.se_inimage"
     * </li>
     * <li>
     * "application/vnd.ogc.se_blank"
     * </li>
     * </ul>
     * </p>
     *
     * @param exceptions
     */
    public void setExceptions(String exceptions) {
        properties.setProperty(EXCEPTIONS, exceptions);
    }

    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annexes B and
     * C
     *
     * @param time See the Web Map Server Implementation Specification 1.1.1,
     *        Annexes B and C
     */
    public void setTime(String time) {
        properties.setProperty(TIME, time);
    }

    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annex C, in
     * particular section C.4
     *
     * @param elevation See the Web Map Server Implementation Specification
     *        1.1.1, Annex C
     */
    public void setElevation(String elevation) {
        properties.setProperty(ELEVATION, elevation);
    }

    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annex C, in
     * particular section C.4.2
     * 
     * <p>
     * Example use: <code>request.setSampleDimensionValue("DIM_WAVELENGTH",
     * "4000");</code>
     * </p>
     *
     * @param name the request parameter name to set (usually with 'dim_' as
     *        prefix)
     * @param value the value of the request parameter (value, interval or
     *        comma-separated list)
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

    public void setDimensions(int width, int height) {
        setDimensions(""+width,""+height);
    }

    public void setProperties(Properties p) {
        properties = p;
    }
    
}
