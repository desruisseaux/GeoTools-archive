/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wms.gce.test;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.gce.WMSFormat;
import org.geotools.data.wms.gce.WMSGridCoverageExchange;
import org.geotools.data.wms.gce.WMSReader;
import org.geotools.data.wms.test.ServerTestCase;
import org.geotools.parameter.Parameter;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;


/**
 * DOCUMENT ME!
 *
 * @author rgould
 * @source $URL$
 */
public class WMSReaderOnlineTest extends ServerTestCase {
    WMSCapabilities capabilities;
    WMSReader reader;
    URL server;
    WMSFormat format;

    public WMSReaderOnlineTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        server = new URL(
                "http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");

        WMSGridCoverageExchange exchange = new WMSGridCoverageExchange(server);

        reader = (WMSReader) exchange.getReader(server);
        format = (WMSFormat) reader.getFormat();
    }

    public void testGetFormat() {
        assertEquals(new WMSFormat(null).getClass(), reader.getFormat().getClass());
    }

    public void testHasMoreGridCoverages() {
    }

    public void testRead() throws Exception {
    	ParameterValueGroup valueGroup = reader.getFormat().getReadParameters();
    	
        DefaultParameterDescriptorGroup descriptorGroup =  (DefaultParameterDescriptorGroup) valueGroup.getDescriptor();

        List paramDescriptors = descriptorGroup.descriptors();
        GeneralParameterValue[] generalParameterValues = new GeneralParameterValue[paramDescriptors.size()];

        for (int i = 0; i < paramDescriptors.size(); i++) {
            GeneralParameterDescriptor paramDescriptor = (GeneralParameterDescriptor) paramDescriptors.get(i);
            GeneralParameterValue generalParameterValue = paramDescriptor.createValue();
            generalParameterValues[i] = generalParameterValue;

            String parameterName = paramDescriptor.getName().getCode();

            if (parameterName.equals("LAYERS")) {
                ParameterGroup groupValue = (ParameterGroup) generalParameterValue;
                DefaultParameterDescriptorGroup groupDesc = (DefaultParameterDescriptorGroup) generalParameterValue
                    .getDescriptor();

                DefaultParameterDescriptorGroup layerGroup = (DefaultParameterDescriptorGroup) paramDescriptor;
                
                List layerDescriptors = layerGroup.descriptors();
                GeneralParameterValue[] layerParameterValues = new GeneralParameterValue[layerDescriptors.size()];

                for (int j = 0; j < layerDescriptors.size(); j++) {
                    GeneralParameterDescriptor layerDescriptor = (GeneralParameterDescriptor) layerDescriptors.get(j);
                    Parameter layerValue = (Parameter) layerDescriptor.createValue();
                    layerParameterValues[j] = layerValue;

                    ParameterDescriptor layerDesc = (ParameterDescriptor) layerValue
                        .getDescriptor();
                    Set styles = layerDesc.getValidValues();
                    layerValue.setValue(styles.iterator().next());

                    groupValue.values().add(layerValue);
                }

                continue;
            }

            Parameter value = (Parameter) generalParameterValue;
            ParameterDescriptor desc = (ParameterDescriptor) generalParameterValue
                .getDescriptor();

            if (parameterName.equals("FORMAT")) {
                Iterator iter = desc.getValidValues().iterator();

                if (iter.hasNext()) {
                    String format = (String) iter.next();
                    value.setValue(format);
                }

                continue;
            }

            if (parameterName.equals("WIDTH") || parameterName.equals("HEIGHT")) {
                value.setValue(400);

                continue;
            }

            if (parameterName.equals("SRS")) {
                value.setValue("EPSG:4326");

                continue;
            }

            if (parameterName.equals("BBOX_MINX")) {
                value.setValue(-168.67);

                continue;
            }

            if (parameterName.equals("BBOX_MINY")) {
                value.setValue(17.84);

                continue;
            }

            if (parameterName.equals("BBOX_MAXX")) {
                value.setValue(-65.15);

                continue;
            }

            if (parameterName.equals("BBOX_MAXY")) {
                value.setValue(71.55);

                continue;
            }

            if (parameterName.equals("VERSION")) {
                value.setValue("1.1.1");

                continue;
            }
        }
//
//        Map properties = new HashMap();
//        properties.put(AbstractIdentifiedObject.NAME_KEY, "WMS");

        GridCoverage coverage = reader.read(generalParameterValues);
    }
}
