/*
 * Created on Jul 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce.test;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.gce.WMSFormat;
import org.geotools.data.wms.gce.WMSGridCoverageExchange;
import org.geotools.data.wms.gce.WMSOperationParameter;
import org.geotools.data.wms.gce.WMSParameterMaker;
import org.geotools.data.wms.gce.WMSParameterValue;
import org.geotools.data.wms.gce.WMSReader;
import org.geotools.data.wms.getCapabilities.Layer;
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
		capabilities = exchange.getCapabilities();
		reader = (WMSReader) exchange.getReader(server);
		format = (WMSFormat) reader.getFormat();
	}

	public WMSReaderTest(String arg0) {
		super(arg0);
	}

	public void testGetFormat() {
		assertEquals(new WMSFormat().getClass(), reader.getFormat().getClass());
	}

	public void testGetSource() {
		assertEquals(server, reader.getSource());
	}

	public void testHasMoreGridCoverages() {
	}

	public void testRead() {
		try {
			WMSParameterValue[] params = new WMSParameterValue[10];
			
			WMSParameterMaker maker = new WMSParameterMaker(capabilities);
			
			params[0] = new WMSParameterValue("1.1.1", maker.createVersionReadParam());
			params[1] = new WMSParameterValue("GetMap", maker.createRequestReadParam());
			String layerList = "";
			Iterator iter = capabilities.getCapability().getLayer().getSubLayers().iterator();
			while (iter.hasNext()) {
				Layer layer = (Layer) iter.next();
				layerList = layerList + layer.getName();	
				if (iter.hasNext()) {
					layerList = layerList + ",";
				}
			}
			params[2] = new WMSParameterValue(layerList, WMSParameterMaker.createLayersReadParam());
			params[3] = new WMSParameterValue("", WMSParameterMaker.createStylesReadParam());
			params[4] = new WMSParameterValue("EPSG:26904", maker.createSRSReadParam());
			params[5] = new WMSParameterValue("366800,2170400,816000,2460400", WMSParameterMaker.createBBoxReadParam());
			params[6] = new WMSParameterValue("400", maker.createWidthReadParam());
			params[7] = new WMSParameterValue("400", maker.createHeightReadParam());

			//params[8] = new WMSParameterValue("image/jpeg", WMSOperationParameter.createFormatReadParam());
			GeneralOperationParameter[] newParams = format.getReadParameters();
			WMSOperationParameter param = (WMSOperationParameter) newParams[0];
			String formatValue = "image/jpeg";
			Iterator iter2 = param.getValidValues().iterator();
			while (iter2.hasNext()) {
				String format = (String) iter2.next();
				System.out.println("FORMAT: "+format);				
			}
			params[8] = new WMSParameterValue(formatValue, param);
			GridCoverage coverage = reader.read(params);
			
			System.out.println(coverage.getEnvelope());
		} catch (IOException e) {
			
		}
	}

}
