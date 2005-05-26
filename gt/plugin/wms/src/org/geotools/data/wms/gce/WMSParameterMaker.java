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
package org.geotools.data.wms.gce;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.WMSUtils;
import org.geotools.parameter.ParameterDescriptor;
import org.geotools.parameter.ParameterDescriptorGroup;
import org.geotools.referencing.DefaultIdentifiedObject;
import org.opengis.parameter.GeneralParameterDescriptor;


/**
 * DOCUMENT ME!
 *
 * @author Richard Gould TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class WMSParameterMaker {
    private WMSCapabilities capabilities;

    public WMSParameterMaker(WMSCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    private Map fillProperties(String name, String remarks) {
        Map properties = new HashMap();

        properties.put(DefaultIdentifiedObject.NAME_PROPERTY, name);
        properties.put(DefaultIdentifiedObject.REMARKS_PROPERTY, remarks);

        return properties;
    }

    public GeneralParameterDescriptor createVersionReadParam() {
        String[] validValues = { "1.0.0", "1.1.0", "1.1.1", "1.3.0" };

        Map properties = fillProperties("VERSION",
                "Value contains the version of the WMS server to be used");

        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class, validValues, null, null, null, null, true);

        return param;
    }

    public GeneralParameterDescriptor createFormatReadParam() {
        Map properties = fillProperties("FORMAT",
                "Value contains the desired format");

        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class,
                capabilities.getRequest().getGetMap().getFormatStrings(), null,
                null, null, null, true);

        return param;
    }

    public GeneralParameterDescriptor createRequestReadParam() {
        Map properties = fillProperties("REQUEST",
                "Value contains the the type of the request");
        String getMap = "GetMap";
        String[] validValues = { getMap };

        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class, validValues, getMap, null, null, null, true);

        return param;
    }

    public GeneralParameterDescriptor createSRSReadParam() {
        Map properties = fillProperties("SRS",
                "Value contains the desired SRS for the entire map");

        Set srs = new TreeSet();
        retrieveSRSs((Layer[]) capabilities.getLayerList().toArray(new Layer[capabilities.getLayerList().size()]), srs);

        Object[] validValues = (Object[]) srs.toArray();

        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class, validValues, null, null, null, null, true);

        return param;
    }

    private void retrieveSRSs(Layer[] layers, Set srsSet) {
        for (int i = 0; i < layers.length; i++) {
            if (layers[i].getSrs() != null) {
                srsSet.addAll(layers[i].getSrs());
            }
        }
    }

    public GeneralParameterDescriptor createWidthReadParam() {
        Map properties = fillProperties("WIDTH",
                "Value contains the width, in pixels, of the requested map");
        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                Integer.class, null, null, new Integer(1), null, null, true);

        return param;
    }

    public GeneralParameterDescriptor createHeightReadParam() {
        Map properties = fillProperties("HEIGHT",
                "Value contains the height, in pixels, of the requested map");
        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                Integer.class, null, null, new Integer(1), null, null, true);

        return param;
    }

    public GeneralParameterDescriptor createLayersReadParam() {
        Map properties = fillProperties("LAYERS",
                "Describes each layer in the WMS and the styles associated "
                + "with. The parameter name is the name of the layer. The value"
                + "is the style for that layer. The valid values are all the "
                + "styles that layer can be drawn with.");

        List layers = Arrays.asList(WMSUtils.findDrawableLayers(
                    capabilities.getLayerList()));

        GeneralParameterDescriptor[] layerParams = new ParameterDescriptor[layers
            .size()];

        for (int i = 0; i < layers.size(); i++) {
            SimpleLayer layer = (SimpleLayer) layers.get(i);

            Map layerProperties = fillProperties(layer.getName(), "");

            layerParams[i] = new ParameterDescriptor(layerProperties,
                    String.class, layer.getValidStyles().toArray(), null, null,
                    null, null, false);
        }

        GeneralParameterDescriptor param = new ParameterDescriptorGroup(properties,
                0, layers.size(), layerParams);

        return param;
    }

    private GeneralParameterDescriptor createBBoxParam(String coordDescriptor) {
        Map properties = fillProperties("BBOX_" + coordDescriptor.toUpperCase(),
                "Value contains the " + coordDescriptor
                + " value for the bounding box");
        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                Double.class, null, null, null, null, null, true);

        return param;
    }

    public GeneralParameterDescriptor createBBoxMinXReadParam() {
        return createBBoxParam("minX");
    }

    public GeneralParameterDescriptor createBBoxMinYReadParam() {
        return createBBoxParam("minY");
    }

    public GeneralParameterDescriptor createBBoxMaxXReadParam() {
        return createBBoxParam("maxX");
    }

    public GeneralParameterDescriptor createBBoxMaxYReadParam() {
        return createBBoxParam("maxY");
    }

    public GeneralParameterDescriptor createTransparentReadParam() {
        Map properties = fillProperties("TRANSPARENT",
                "Value indicates map transparency");
        Boolean[] validValues = { new Boolean(true), new Boolean(false) };
        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                Boolean.class, validValues, new Boolean(false), null,
                null, null, false);

        return param;
    }

    public GeneralParameterDescriptor createBGColorReadParam() {
        Map properties = fillProperties("BGCOLOR",
                "Value indicates map background colour in hex format (0xRRGGBB)");
        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class, null, "0xFFFFFF", null, null, null, false);

        return param;
    }

    public GeneralParameterDescriptor createExceptionsReadParam() {
        Map properties = fillProperties("EXCEPTIONS",
                "Value indicates the format in which exceptions are returned");
        String defaultValue = "application/vnd.ogc.se_xml";
        String[] validValues = { defaultValue };

        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class, validValues, defaultValue, null, null, null, false);

        //TODO Fix exceptions later
        //param.validValues = new TreeSet(capabilities..getException().getFormats());
        return param;
    }

    public GeneralParameterDescriptor createTimeReadParam() {
        Map properties = fillProperties("TIME",
                "Value indicates the time value desired");
        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class, null, null, null, null, null, false);

        return param;
    }

    public GeneralParameterDescriptor createElevationReadParam() {
        Map properties = fillProperties("ELEVATION",
                "Value indicates the elevation value desired");
        GeneralParameterDescriptor param = new ParameterDescriptor(properties,
                String.class, null, null, null, null, null, false);

        return param;
    }

    //TODO support Sample dimensions
    //TODO support VendorSpecific Parameters.
}
