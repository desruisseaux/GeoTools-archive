/*
 * Created on Jun 24, 2004
 *
 */
package org.geotools.data.wms;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


import org.geotools.data.wms.getCapabilities.Layer;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author rgould
 * 
 * WebMapServer is a class representing a WMS.
 */
public class WebMapServer {

	/**
	 * Retrieves the getCapabilities request from the WMS and populates
	 * a Capabilities object with the data.
	 * 
	 * When performing the getCapabilities request, all query parameters
	 * are saved and over-ride the defaults:
	 * service=WMS
	 * version=1.1.1
	 * request=GetCapabilities 
	 * 
	 * @param serverURL a URL pointing to the WebMapServer (equivalent to it's OnlineResource)
	 * @return a WMT_MS_Capabilities object containing the results of the getCapabilities request
	 * @throws IOException if there is a network error
	 * @throws JDOMException if the XML returned by the getcapabilities request is malformed
	 * @throws ParseCapabilitiesException if the XML does not conform to WMS specification
	 */
	public static WMT_MS_Capabilities getCapabilities(URL serverURL) throws ParseCapabilitiesException, IOException, JDOMException {

		//Get the actual getCapabilities XML string from the server.
		URL getCapabilitiesURL = null;
		String query = "";
		int index = serverURL.toExternalForm().lastIndexOf("?");
		String urlWithoutQuery = null;
		if (index <= 0) {
			urlWithoutQuery = serverURL.toExternalForm();
		} else {
			urlWithoutQuery = serverURL.toExternalForm().substring(0, index);
		}
		
		if (serverURL.getQuery() == null || serverURL.getQuery().length() == 0) {
			query = "?request=GetCapabilities&service=WMS&version=1.1.1";
		} else {
			
			//Doing this preserves all of the query parameters while
			//enforcing the mandatory ones
			
			Properties properties = new Properties();
			properties.setProperty("request", "GetCapabilities");
			properties.setProperty("service", "WMS");
			properties.setProperty("version", "1.1.1");
			
			StringTokenizer tokenizer = new StringTokenizer(serverURL.getQuery(), "&");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				String[] param = token.split("=");
				properties.setProperty(param[0], param[1]);
			}
			Iterator iter = properties.keySet().iterator();
			query = query+"?";
			while (iter.hasNext()) {
				String key = (String) iter.next();
				query = query + key+"="+properties.getProperty(key);
				if (iter.hasNext()) {
					query = query+"&";
				}
			}
		}
		getCapabilitiesURL = new URL(urlWithoutQuery + query);
		
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(getCapabilitiesURL);

		Element root = document.getRootElement(); //Root = "WMT_MS_Capabilities"
		
		return CapabilitiesParser.parseCapabilities(root);
	}
	
	/**
	 * Executes a GetMap request and returns the results. 
	 * 
	 * @param request a GetMapRequest containing all the valid parameters
	 * @return the response from the server as a result of the request
	 * @throws IOException if there is a network error
	 */	
	public static GetMapResponse issueGetMapRequest(GetMapRequest request) throws IOException {
	    GetMapResponse response;
		
	    URL finalURL = request.getFinalURL();

	    URLConnection connection = finalURL.openConnection();
	    InputStream inputStream = connection.getInputStream();

	    String contentType = connection.getContentType();
	    
	    response = new GetMapResponse(contentType, inputStream);
	    
	    return response;
	}
	
    /**
     * Iterate through the layers and extract and return all layers containing a <name>.
     * @param layers The list of Layers to iterate through.
     * @return A list of Layers each containing a <name>
     */
    public static List getNamedLayers(List layers) {
        List namedLayers = new ArrayList();
        
        Iterator iter = layers.iterator();
        while (iter.hasNext()) {
            Layer layer = (Layer) iter.next();
            if (layer.getName() != null && !layer.getName().equals("")) {
                namedLayers.add(layer);
            }
            if (layer.getSubLayers() != null) {
                namedLayers.add(getNamedLayers(layer.getSubLayers()));
            }
        }
        
        return namedLayers;
    }

}
