/*
 * Created on Jul 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce.test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.gce.WMSFormat;
import org.geotools.data.wms.gce.WMSGridCoverageExchange;
import org.geotools.data.wms.gce.WMSOperationParameter;
import org.geotools.data.wms.gce.WMSParameterValue;
import org.geotools.data.wms.gce.WMSReader;
import org.geotools.gc.GridCoverage;
import org.geotools.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameterGroup;


/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
        OperationParameterGroup info = reader.getFormat().getReadParameters();            
            
        ParameterValueGroup group = (ParameterValueGroup) info.createValue();
        GeneralParameterValue[] values = group.getValues(); // new WMSParameterValue[16];
        
        for (int i = 0; i < values.length; i++) {
            WMSOperationParameter parameter = (WMSOperationParameter) values[i].getDescriptor();
            if (parameter.getName(null).equals("LAYERS")) {
                ArrayList layerList = new ArrayList();
                List availableLayers = parameter.getAvailableLayers();

                for (int j = 0; j < availableLayers.size(); j++) {
                    SimpleLayer simpleLayer = (SimpleLayer) availableLayers.get(j);
                    simpleLayer.setStyle("");
                    layerList.add(j, simpleLayer);
                }
                // We should of used the value that was created for u
                // ...and then modifed it?
                values[i] = new WMSParameterValue(layerList, parameter);

                continue;
            }

            if (parameter.getName(null).equals("FORMAT")) {
                Iterator iter = parameter.getValidValues().iterator();

                if (iter.hasNext()) {
                    String format = (String) iter.next();
                    values[i] = new WMSParameterValue(format, parameter);
                }
                
                continue;
            }

            if (parameter.getName(null).equals("WIDTH") ||
                    parameter.getName(null).equals("HEIGHT")) {
                values[i] = new WMSParameterValue("400", parameter);

                continue;
            }

            if (parameter.getName(null).equals("SRS")) {
                values[i] = new WMSParameterValue("EPSG:26904", parameter);

                continue;
            }

            if (parameter.getName(null).equals("BBOX_MINX")) {
                values[i] = new WMSParameterValue("366800", parameter);

                continue;
            }

            if (parameter.getName(null).equals("BBOX_MINY")) {
                values[i] = new WMSParameterValue("2170400", parameter);

                continue;
            }

            if (parameter.getName(null).equals("BBOX_MAXX")) {
                values[i] = new WMSParameterValue("816000", parameter);

                continue;
            }

            if (parameter.getName(null).equals("BBOX_MAXY")) {
                values[i] = new WMSParameterValue("2460400", parameter);

                continue;
            }
            
            if (parameter.getName(null).equals("VERSION")) {
                values[i] = new WMSParameterValue("1.1.1", parameter);
                continue;
            }

            // All of these should be no-ops..because we used the Group to create
            //
            // values[i] = parameter.createValue();
            // values[i] = new WMSParameterValue(parameter.getDefaultValue(), parameter);
        }            
        ParameterValueGroup parameters = new ParameterValueGroup( info, values );            
        GridCoverage coverage = reader.read( parameters );
    }
}