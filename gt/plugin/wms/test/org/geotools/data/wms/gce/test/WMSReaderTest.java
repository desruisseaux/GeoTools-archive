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

import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.gce.WMSFormat;
import org.geotools.data.wms.gce.WMSGridCoverageExchange;
import org.geotools.data.wms.gce.WMSOperationParameter;
import org.geotools.data.wms.gce.WMSParameterValue;
import org.geotools.data.wms.gce.WMSReader;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.geotools.gc.GridCoverage;
import org.opengis.parameter.GeneralOperationParameter;


/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSReaderTest extends TestCase {

	WMT_MS_Capabilities capabilities;
	WMSReader reader;
	URL server;
	WMSFormat format;
	
	protected void setUp() throws Exception {
		super.setUp();
		server = new URL("http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
		WMSGridCoverageExchange exchange = new WMSGridCoverageExchange(server);

		reader = (WMSReader) exchange.getReader(server);
		format = (WMSFormat) reader.getFormat();
	}

	public WMSReaderTest(String arg0) {
		super(arg0);
	}

	public void testGetFormat() {
		assertEquals(new WMSFormat().getClass(), reader.getFormat().getClass());
	}

	public void testHasMoreGridCoverages() {
	}

	public void testRead() {
		try {
			WMSParameterValue[] params = new WMSParameterValue[16];

			
			GeneralOperationParameter[] newParams = format.getReadParameters();
			for (int i = 0; i < newParams.length; i++) {
				WMSOperationParameter parameter = (WMSOperationParameter) newParams[i];
				
				if (parameter.getName(null).equals("LAYERS")) {
					ArrayList layerList = new ArrayList();
					List availableLayers = parameter.getAvailableLayers();
					for (int j = 0; j < availableLayers.size(); j++) {
						SimpleLayer simpleLayer = (SimpleLayer) availableLayers.get(j);
						simpleLayer.setStyle("");
						layerList.add(j, simpleLayer);
					}
					params[i] = new WMSParameterValue(layerList, parameter);
					continue;
				}
				
				if (parameter.getName(null).equals("FORMAT")) {
					Iterator iter = parameter.getValidValues().iterator();
					if (iter.hasNext()) {
						String format = (String) iter.next();
						params[i] = new WMSParameterValue(format, parameter);
					}
					continue;
				}				
				
				if (parameter.getName(null).equals("WIDTH") || parameter.getName(null).equals("HEIGHT")) {
					params[i] = new WMSParameterValue("400", parameter);
					continue;
				}
				
				if (parameter.getName(null).equals("SRS")) {
					params[i] = new WMSParameterValue("EPSG:26904", parameter);
					continue;
				}

				if (parameter.getName(null).equals("BBOX_MINX")) {
					params[i] = new WMSParameterValue("366800", parameter);
					continue;
				}
				
				if (parameter.getName(null).equals("BBOX_MINY")) {
					params[i] = new WMSParameterValue("2170400", parameter);
					continue;
				}
				
				if (parameter.getName(null).equals("BBOX_MAXX")) {
					params[i] = new WMSParameterValue("816000", parameter);
					continue;
				}
				
				if (parameter.getName(null).equals("BBOX_MAXY")) {
					params[i] = new WMSParameterValue("2460400", parameter);
					continue;
				}
				
				params[i] = new WMSParameterValue(parameter.getDefaultValue(), parameter);
			}

			GridCoverage coverage = reader.read(params);
			
			System.out.println(coverage.getEnvelope());
		} catch (IOException e) {
			
		}
	}

}
