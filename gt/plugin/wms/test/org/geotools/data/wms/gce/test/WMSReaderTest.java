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
/*
 * Created on Jul 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce.test;

import junit.framework.TestCase;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.gce.WMSFormat;
import org.geotools.data.wms.gce.WMSGridCoverageExchange;
import org.geotools.data.wms.gce.WMSReader;
import org.geotools.parameter.Parameter;
import org.geotools.parameter.ParameterGroup;
import org.geotools.parameter.ParameterGroupDescriptor;
import org.geotools.referencing.IdentifiedObject;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * DOCUMENT ME!
 *
 * @author rgould TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSReaderTest extends TestCase {
    WMSCapabilities capabilities;
    WMSReader reader;
    URL server;
    WMSFormat format;

    public WMSReaderTest(String arg0) {
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
        assertEquals(new WMSFormat().getClass(), reader.getFormat().getClass());
    }

    public void testHasMoreGridCoverages() {
    }

    public void testRead() throws Exception {
        ParameterDescriptorGroup descriptorGroup = reader.getFormat()
                                                         .getReadParameters();

        GeneralParameterDescriptor[] paramDescriptors = descriptorGroup
            .getParameters();
        GeneralParameterValue[] generalParameterValues = new GeneralParameterValue[paramDescriptors.length];

        for (int i = 0; i < paramDescriptors.length; i++) {
            GeneralParameterValue generalParameterValue = paramDescriptors[i]
                .createValue();
            generalParameterValues[i] = generalParameterValue;

            String parameterName = paramDescriptors[i].getName().toString();

            if (parameterName.equals("LAYERS")) {
                ParameterGroup groupValue = (ParameterGroup) generalParameterValue;
                ParameterGroupDescriptor groupDesc = (ParameterGroupDescriptor) generalParameterValue
                    .getDescriptor();

                ParameterDescriptorGroup layerGroup = (ParameterDescriptorGroup) paramDescriptors[i];

                GeneralParameterDescriptor[] layerDescriptors = layerGroup
                    .getParameters();
                GeneralParameterValue[] layerParameterValues = new GeneralParameterValue[layerDescriptors.length];

                for (int j = 0; j < layerDescriptors.length; j++) {
                    Parameter layerValue = (Parameter) layerDescriptors[j]
                        .createValue();
                    layerParameterValues[j] = layerValue;

                    ParameterDescriptor layerDesc = (ParameterDescriptor) layerValue
                        .getDescriptor();
                    Set styles = layerDesc.getValidValues();
                    layerValue.setValue(styles.iterator().next());

                    groupValue.add(layerValue);
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
                value.setValue("EPSG:26904");

                continue;
            }

            if (parameterName.equals("BBOX_MINX")) {
                value.setValue(366800.0);

                continue;
            }

            if (parameterName.equals("BBOX_MINY")) {
                value.setValue(2170400.0);

                continue;
            }

            if (parameterName.equals("BBOX_MAXX")) {
                value.setValue(816000.0);

                continue;
            }

            if (parameterName.equals("BBOX_MAXY")) {
                value.setValue(2460400.0);

                continue;
            }

            if (parameterName.equals("VERSION")) {
                value.setValue("1.1.1");

                continue;
            }
        }

        Map properties = new HashMap();
        properties.put(IdentifiedObject.NAME_PROPERTY, "WMS");

        ParameterValueGroup parameters = new ParameterGroup(properties,
                generalParameterValues);
        org.geotools.gc.GridCoverage coverage = reader.read(parameters);
    }
}
