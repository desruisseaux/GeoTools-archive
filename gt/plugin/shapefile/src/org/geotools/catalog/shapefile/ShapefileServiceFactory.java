package org.geotools.catalog.shapefile;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.Service;
import org.geotools.catalog.ServiceFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

public class ShapefileServiceFactory implements ServiceFactory {

	 private static ShapefileDataStoreFactory shpDSFactory
	 	= new ShapefileDataStoreFactory();
	    
	 public Service createService(Catalog parent, URI id, Map params) {
		if(params.containsKey(ShapefileDataStoreFactory.URLP.key) ){
            // shapefile ...

            URL url = null;
            if(params.get(ShapefileDataStoreFactory.URLP.key) instanceof URL){
                url = (URL)params.get(ShapefileDataStoreFactory.URLP.key);
            }
            else{
                try {
                	String surl = 
                		params.get(ShapefileDataStoreFactory.URLP.key).toString();
                    url = (URL)ShapefileDataStoreFactory.URLP.parse(surl);
                    params.put(ShapefileDataStoreFactory.URLP.key,url);
                } 
                catch (Throwable e) {
                	//TODO: log exception
                    return null;
                }
            }
           if (!shpDSFactory.canProcess(url))
        	   return null;
            
            if(id == null){
               try {
            	   id = new URI(url.toExternalForm());
               } 
               catch (URISyntaxException e) {}
               
               return new ShapefileService(parent,id,params);
            }
            
            return new ShapefileService(parent,id,params);
        }
		
        return null;
	}

	public boolean canProcess(URI uri) {
		try {
			return shpDSFactory.canProcess(uri.toURL());
		} 
		catch (MalformedURLException e) {
			return false;
		}
	}

	public Map createParams(URI uri) {
		URL url = null;
		try {
			url = uri.toURL();
		} 
		catch (MalformedURLException e) {
			return null;
		}
		
		 if(shpDSFactory.canProcess(url)){
	            // shapefile
            HashMap params = new HashMap();
            params.put(ShapefileDataStoreFactory.URLP.key,url); 
            return params;
        }
        return null;
	}
}
