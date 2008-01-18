package org.geotools.catalog.property;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.Service;
import org.geotools.catalog.ServiceFactory;
import org.geotools.data.property.PropertyDataStoreFactory;

/**
 * Creates a new property service.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class PropertyServiceFactory implements ServiceFactory {

	public Service createService( Catalog catalog, URI uri, Map params ) {
		if ( params.containsKey( PropertyDataStoreFactory.DIRECTORY.key ) ) {
			
			File file;
			try {
				file = (File) PropertyDataStoreFactory.DIRECTORY.lookUp( params );
			} 
			catch (IOException e) {
				//TODO: log
				return null;
			}
			
			if ( file != null) {
				PropertyDataStoreFactory factory = new PropertyDataStoreFactory();
				return new PropertyService( catalog, params, file, factory );
			}
		}
		
		return null;
	}

	public boolean canProcess( URI uri ) {
		try {
			File file = new File( uri.toURL().getFile() );
			return file.isDirectory() && file.canRead() && containsPropertyFile( file );
		}
		catch( MalformedURLException e) {
			return false;
		}
	}

	public Map createParams( URI uri ) {
		if ( !canProcess( uri ) )
			return null;
		
		File file;
		try {
			file = new File( uri.toURL().getFile() );
		} 
		catch (MalformedURLException e) {
			return null;
		}
		HashMap map = new HashMap();
		map.put( PropertyDataStoreFactory.DIRECTORY.key, file );
	
		return map;
	}

	static boolean containsPropertyFile( File dir ) {
		if ( dir.isDirectory() ) {
			dir.listFiles( 
				new FilenameFilter() {

					public boolean accept(File dir, String name) {
						String s = ".properties";
						if ( name.length() > s.length() ) {
							return s.equals( name.substring( s.length() ) );
						}
						
						return false;
					}
					
				}
			);
		}
		
		return false;
	}
}
