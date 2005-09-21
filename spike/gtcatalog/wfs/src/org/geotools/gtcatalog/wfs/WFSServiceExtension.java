/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.gtcatalog.wfs;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.gtcatalog.Catalog;
import org.geotools.gtcatalog.Service;
import org.geotools.gtcatalog.ServiceExtension;
import org.geotools.gtcatalog.wfs.WFSService.MyWFSDataStore;

/**
 * Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p>
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class WFSServiceExtension implements ServiceExtension {

    private static WFSDataStoreFactory wfsDSFactory;
    /**
     * TODO summary sentence for getWFSDSFactory ...
     * 
     * @return x
     */
    public static WFSDataStoreFactory getWFSDSFactory(){
        if(wfsDSFactory == null)
            wfsDSFactory = new WFSDataStoreFactory();
        return wfsDSFactory;
    }
    
    /**
     * TODO summary sentence for createService ...
     * 
     * @param id
     * @param params
     * @return x
     */
    public Service createService( Catalog parent, URI id, Map params ) {
        if(params == null || !params.containsKey(WFSDataStoreFactory.URL.key))
            return null;
        
        try {
			if(id == null){
			    URL base = (URL)params.get(WFSDataStoreFactory.URL.key);
			    base = base == null?null:MyWFSDataStore.createGetCapabilitiesRequest(base);
			    return new WFSService(parent,new URI(base.toExternalForm()),params);
			}
			return new WFSService(parent,id,params);
		} 
        catch (URISyntaxException e) {
        	return null;
		}
    }

    /**
     * TODO summary sentence for createParams ...
     * 
     * @see net.refractions.udig.catalog.ServiceExtension#createParams(java.net.URL)
     * @param url
     * @return x
     */
    public Map createParams( URI uri ) {
    	URL url;
		try {
			url = uri.toURL();
		} 
		catch (MalformedURLException e) {
			return null;
		}
		
        if (!isWFS(url)) {
            return null;
        }
        
        // wfs check
        Map params = new HashMap();
        params.put(WFSDataStoreFactory.URL.key,url);
        
        // don't check ... it blocks
        // (XXX: but we are using that to figure out if the service will work?)
        return params;
    }
    
    public boolean canProcess(URI uri) {
		try {
			URL url = uri.toURL();
			return isWFS(url);
		} 
		catch (MalformedURLException e) {
			return false;
		}
	}
    
    /** A couple quick checks on the url */ 
    private static final boolean isWFS( URL url ){
    	String PATH = url.getPath();
        String QUERY = url.getQuery();
        String PROTOCOL = url.getProtocol();
        
        if( !"http".equals(PROTOCOL)){ //$NON-NLS-1$
            return false;
        }
        if( QUERY != null && QUERY.toUpperCase().indexOf( "SERVICE=" ) != -1){ //$NON-NLS-1$
            // we have a service! it better be wfs            
            return QUERY.toUpperCase().indexOf( "SERVICE=WFS") != -1; //$NON-NLS-1$
        }
        if( PATH != null && PATH.toUpperCase().indexOf( "GEOSERVER/WFS" ) != -1){ //$NON-NLS-1$
            return true;
        }  
        return true; // try it anyway
    }

}
