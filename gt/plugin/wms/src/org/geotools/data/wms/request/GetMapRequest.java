/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wms.request;

import java.util.List;
import java.util.Set;


/**
 * Construct a WMS getMap request.
 * 
 * <p>
 * Constructs a getMapRequest based on the following property values:
 * 
 * <ul>
 * <li>
 * ELEVATION
 * </li>
 * <li>
 * TIME
 * </li>
 * <li>
 * EXCEPTIONS
 * </li>
 * <li>
 * BGCOLOR
 * </li>
 * <li>
 * TRANSPARENT
 * </li>
 * <li>
 * WIDTH
 * </li>
 * <li>
 * HEIGHT
 * </li>
 * <li>
 * SRS
 * </li>
 * <li>
 * REQUEST
 * </li>
 * <li>
 * LAYERS
 * </li>
 * <li>
 * STYLES
 * </li>
 * <li>
 * <i>vendor specific parameters</i>
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Q: List availableFormats and availableExceptions - why are these here? It
 * looks like they are designed to restrict the values used for SRS, format
 * and exceptions. If so the code never uses them. Q: How constant is the
 * GetMapRequest format across WMS versions? Do we need to generalize here?
 * </p>
 *
 * @author Richard Gould, Refractions Research
 */
public interface GetMapRequest extends Request{
    /** Represents the ELEVATION parameter */
    public static final String ELEVATION = "ELEVATION"; //$NON-NLS-1$
    /** Represents the TIME parameter */
    public static final String TIME = "TIME"; //$NON-NLS-1$
    /** Represents the EXCEPTIONS parameter */
    public static final String EXCEPTIONS = "EXCEPTIONS"; //$NON-NLS-1$
    /** Represents the BGCOLOR parameter */
    public static final String BGCOLOR = "BGCOLOR"; //$NON-NLS-1$
    /** Represents the TRANSPARENT parameter */
    public static final String TRANSPARENT = "TRANSPARENT"; //$NON-NLS-1$
    /** Represents the WIDTH parameter */
    public static final String WIDTH = "WIDTH"; //$NON-NLS-1$
    /** Represents the HEIGHT parameter */
    public static final String HEIGHT = "HEIGHT"; //$NON-NLS-1$
    /** Represents the FORMAT parameter */
    public static final String FORMAT = "FORMAT"; //$NON-NLS-1$
    /** Represents the BBOX parameter */
    public static final String BBOX = "BBOX"; //$NON-NLS-1$
    /** Represents the SRS parameter */
    public static final String SRS = "SRS"; //$NON-NLS-1$
    /** Represents the LAYERS parameter */
    public static final String LAYERS = "LAYERS"; //$NON-NLS-1$
    /** Represents the STYLES parameter */
    public static final String STYLES = "STYLES"; //$NON-NLS-1$
    
    
    /** Represents the EXCEPTION_INIMAGE value */
    public static final String EXCEPTION_INIMAGE = "application/vnd.ogc.se_inimage"; //$NON-NLS-1$
    /** Represents the EXCEPTION_BLANK value */
    public static final String EXCEPTION_BLANK = "application/vnd.ogc.se_blank"; //$NON-NLS-1$
    

    /** =============== BEGIN SLD SPECIFICATION PARAMETERS =============== **/
    /** Represents the SLD parameter */
    public static final String SLD = "SLD";
    /** Represents the SLD_BODY parameter */
    public static final String SLD_BODY = "SLD_BODY";
    /** Represents the WFS parameter mentioned briefly in 1.1.1 */
    public static final String WFS = "WFS";
    /** Represents REMOTE_OWS_TYPE parameter */
    public static final String REMOTE_OWS_TYPE = "REMOTE_OWS_TYPE";
    /** Represents REMOTE_OWS_URL parameter */
    public static final String REMOVE_OWS_URL = "REMOTE_OWS_URL";
    
    
    /** <code>REMOTE_OWS_WFS</code> indicates WFS as a REMOTE_OWS_TYPE */
    public static final String REMOTE_OWS_WFS = "WFS";
    /** <code>REMOTE_OWS_WCS</code> indicates WCS as a REMOTE_OWS_TYPE */
    public static final String REMOTE_OWS_WCS = "WCS";
    /** =============== END SLD SPECIFICATION PARAMETERS =============== **/
    
    /**
     * Sets the version number of the request.
     *
     * @param version A String indicting a WMS Version ("1.0.0", "1.1.0",
     *        "1.1.1", "1.3.0", etc.)
     */
    public void setVersion(String version);

    /**
     * A list of type <code>SimpleLayer</code> used to set the layers and
     * styles for this request. The first element in the List is the first
     * Layer in the request (and the layer drawn on the bottom).
     * 
     * Typically, each layer name and style name is passed through an
     * encoder before being added to the final URL.
     * 
     * @param layers a List of type SimpleLayer
     */
    public void setLayers(List layers);

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
    public void setSRS(String srs);

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
    public void setBBox(String bbox);

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
    public void setFormat(String format);

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
    public void setDimensions(String width, String height);

    // End required parameters, begin optional ones.
    //TODO Implement optional parameters.

    /**
     * From the Web Map Service Implementation Specification: "The optional
     * TRANSPARENT parameter specifies whether the map background is to be
     * made transparent or not. The default value is false if the parameter is
     * absent from the request."
     *
     * @param transparent true for transparency, false otherwise
     */
    public void setTransparent(boolean transparent);

    /**
     * Specifies the colour, in hexidecimal format, to be used as the
     * background of the map. It is a String representing RGB values in
     * hexidecimal format, prefixed by "0x". The format is: 0xRRGGBB. The
     * default value is 0xFFFFFF (white)
     *
     * @param bgColour the background colour of the map, in the format 0xRRGGBB
     */
    public void setBGColour(String bgColour);

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
    public void setExceptions(String exceptions);

    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annexes B and
     * C
     *
     * @param time See the Web Map Server Implementation Specification 1.1.1,
     *        Annexes B and C
     */
    public void setTime(String time);

    /**
     * See the Web Map Server Implementation Specification 1.1.1, Annex C, in
     * particular section C.4
     *
     * @param elevation See the Web Map Server Implementation Specification
     *        1.1.1, Annex C
     */
    public void setElevation(String elevation);

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
    public void setSampleDimensionValue(String name, String value);

    /**
     * Used to implement vendor specific parameters. Entirely optional.
     *
     * @param name a request parameter name
     * @param value a value to accompany the name
     */
    public void setVendorSpecificParameter(String name, String value);

    /**
     * @return a List of type String, representing valid Exceptions
     */
    public List getAvailableExceptions();

    /**
     * @return a List of type String, representing valid GetMap formats
     */
    public List getAvailableFormats();

    /**
     * @return a List of SimpleLayers, representing available Layers and their Styles
     */
    public List getAvailableLayers();

    /**
     * @return a Set of Strings, representing available SRSs
     * TODO - might be a good idea to move these to SimpleLayers.
     */
    public Set getAvailableSRSs();
}
