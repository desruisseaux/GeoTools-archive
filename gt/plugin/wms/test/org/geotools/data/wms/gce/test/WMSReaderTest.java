/*
 * Created on Jul 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce.test;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.gce.WMSFormat;
import org.geotools.data.wms.gce.WMSOperationParameter;
import org.geotools.data.wms.gce.WMSParameterValue;
import org.geotools.data.wms.gce.WMSReader;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.geotools.gc.GridCoverage;
import org.geotools.parameter.GeneralParameterValue;

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
	
	protected void setUp() throws Exception {
		super.setUp();
		server = new URL("http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
		capabilities = WebMapServer.getCapabilities(server);
		reader = new WMSReader(server);
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
			params[0] = new WMSParameterValue("1.1.1", WMSOperationParameter.createVersionReadParam());
			params[1] = new WMSParameterValue("GetMap", WMSOperationParameter.createRequestReadParam());
			params[2] = new WMSParameterValue("DRG", WMSOperationParameter.createLayersReadParam());
			params[3] = new WMSParameterValue("", WMSOperationParameter.createStylesReadParam());
			params[4] = new WMSParameterValue("EPSG:26904", WMSOperationParameter.createSRSReadParam());
			params[5] = new WMSParameterValue("366800,2170400,816000,2460400", WMSOperationParameter.createBBoxReadParam());
			params[6] = new WMSParameterValue("400", WMSOperationParameter.createWidthReadParam());
			params[7] = new WMSParameterValue("400", WMSOperationParameter.createHeightReadParam());
			params[8] = new WMSParameterValue("image/jpeg", WMSOperationParameter.createFormatReadParam());			
		    
			GridCoverage coverage = reader.read(params);
			
			System.out.println(coverage.getEnvelope());
		} catch (IOException e) {
			
		}
	}

}
