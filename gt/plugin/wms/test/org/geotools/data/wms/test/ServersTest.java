/*
 * Created on 14-Oct-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.net.URL;

import junit.framework.TestCase;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServersTest extends TestCase {
    public void testServers() throws Exception{
    	URL[] servers = new URL[50];
//		servers[0] = new URL("http://wms.jpl.nasa.gov/wms.cgi?VERSION=1.1.1&SERVICE=WMS&REQUEST=GetCapabilities");
//    	servers[1] = new URL("http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi?CONFIG=main&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
//    	servers[2] = new URL("http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
//  This returning Gzip content  	
//    	servers[3] = new URL("http://wms.cits.rncan.gc.ca/cgi-bin/cubeserv.cgi?VERSION=1.1.0&REQUEST=GetCapabilities");
//    	servers[4] = new URL("http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
//    	servers[5] = new URL("http://www2.demis.nl/mapserver/Request.asp?VERSION=1.3.0&SERVICE=WMS&REQUEST=GetCapabilities");
//THIS ONE OFFLINE    	servers[6] = new URL("http://datamil.udel.edu/servlet/com.esri.wms.Esrimap?servicename=DE_census2k_sf1&VERSION=1.0.0&request=capabilities");
//    	servers[7] = new URL("http://www.lifemapper.org/Services/WMS/?Service=WMS&VERSION=1.1.1&request=getcapabilities");
//    	this server returns OGC for 1.3.0
//    	servers[8] = new URL("http://globe.digitalearth.gov/viz-bin/wmt.cgi?VERSION=1.1.0&Request=GetCapabilities");
//    	servers[9] = new URL("http://www.geographynetwork.ca/wmsconnector/com.esri.wsit.WMSServlet/Geobase_NRN_NewfoundlandAndLabrador_I_Detail?request=GetCapabilities");
//    	servers[10] = new URL("http://gisdata.usgs.net/servlet/com.esri.wms.Esrimap?REQUEST=GetCapabilities&VERSION=1.3.0&SERVICE=WMS");
//    	servers[11] = new URL("http://www.refractions.net:8080/geoserver/wms/?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.3.0"); //$NON-NLS-1$
//1.0.0 freezes.    	
//    	servers[12] = new URL("http://mapserv2.esrin.esa.it/cubestor/cubeserv/cubeserv.cgi?VERSION=1.1.1&REQUEST=GetCapabilities&SERVICE=WMS");
//		servers[13] = new URL("http://mesonet.agron.iastate.edu/wms/comprad.php?request=getcapabilities");
//		servers[14] = new URL("http://redspider.us/ionicweb/wfs/METRODC?request=getcapabilities&service=WMS&version=1.1.1");
//		servers[15] = new URL("http://webservices.ionicsoft.com/ionicweb/wfs/BOSTON_ORA?service=WMS&request=GetCapabilities&vesrion=1.1.1");
//		servers[16] = new URL("http://oceanesip.jpl.nasa.gov/de.xml");
//		servers[17] = new URL("http://libcwms.gov.bc.ca/wmsconnector/com.esri.wsit.WMSServlet/ogc_layer_service?REQUEST=capabilities");
//		servers[18] = new URL("http://www.geographynetwork.com/servlet/com.esri.wms.Esrimap?ServiceName=GFW_Forest&VERSION=1.0.0&request=capabilities");
//		servers[19] = new URL("http://atlas.gc.ca/cgi-bin/atlaswms_en?VERSION=1.1.0&request=GetCapabilities");
//OFFLINE		servers[20] = new URL("http://office.refractions.net:4001/cgi-bin/mapserv?map=/opt/dra2/orthophotos/tiles.map&request=getcapabilities");
//		servers[21] = new URL("http://mesonet.agron.iastate.edu/wms/comprad.php?request=getcapabilities");
//    	servers[22] = new URL("http://wms.larioja.org/request.asp?request=getcapabilities");
//    	servers[23] = new URL("http://atlas.walis.wa.gov.au/servlet/com.esri.wms.Esrimap?VERSION=1.1.0&Request=getcapabilities");
//    	servers[24] = new URL("http://iceds.ge.ucl.ac.uk/cgi-bin/wms?map=wms.map&SERVICE=WMS&REQUEST=GetCapabilities");
//    	servers[25] = new URL("http://test.landmap.ac.uk/ecwp/ecw_wms.dll?request=GetCapabilities&service=wms");
//    	servers[26] = new URL("http://emandev.cciw.ca/cgi-bin/mapserver/mapserv.exe?REQUEST=GetCapabilities&MAP=C:/Inetpub/wwwroot/emanco/cgi-bin/mapserver/naturewatch.map&VERSION=1.1.1&SERVICE=WMS");
   	
    	for (int i = 0; i < servers.length; i++) {
    		if (servers[i] != null) {
    		    WebMapServer wms = new WebMapServer(servers[i]);
    			assertNotNull("Missing Capabilities",wms.getCapabilities());
    		    WMSCapabilities capabilities = wms.getCapabilities();
    		    assertNotNull(capabilities.getRequest());
    		}
    	}
    }
}
