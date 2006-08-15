/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Refractions Research Inc.
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
 */
package org.geotools.catalog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.geotools.util.ProgressListener;

public abstract class AbstractFileGeoResource extends AbstractGeoResource {

	/**
	 * The file
	 */
	File file;
	
	/**
	 * Service containing the resource.
	 */
	AbstractFileService service;
	
	public AbstractFileGeoResource( AbstractFileService service ) {
		this.service = service;
		this.file = service.getFile();
	}
	
	public AbstractFileGeoResource( AbstractFileService service, File file ) {
		this.service = service;
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public Resolve parent(ProgressListener monitor) throws IOException {
		return service;
	}
	
	/**
	 * Supports default GeoResource resolves with an additional resolve to a 
	 * {@link java.io.File}. 
	 * <p>
	 * Subclasses may wish to extend, or override this method.
	 * </p>
	 */
	public boolean canResolve(Class adaptee) {
		
		if ( adaptee == null ) 
			return false;
		
		return adaptee.isAssignableFrom( Service.class ) || 
			adaptee.isAssignableFrom( GeoResourceInfo.class  ) ||
			adaptee.isAssignableFrom( File.class );
	}
	
	/**
	 * Supports default GeoResource resolves with an additional resolve to a 
	 * {@link java.io.File}. 
	 * <p>
	 * Subclasses may wish to extend, or override this method.
	 * </p>
	 */
	public Object resolve(Class adaptee, ProgressListener monitor)
			throws IOException {
		
		if (adaptee == null)
			return null;
		
		if ( adaptee.isAssignableFrom( Service.class) ) {
			return parent( monitor );
		}
		
		if ( adaptee.isAssignableFrom( GeoResourceInfo.class ) ) {
			return getInfo( monitor );
		}
		
		if ( adaptee.isAssignableFrom( File.class ) ) {
			return service.getFile();
		}
		
		return null;
	}

	/**
	 * Returns {@link #service}#getStatus()
	 */
	public Status getStatus() {
		return service.getStatus();
	}
	
	public URI getIdentifier() {
		return file.toURI();
	}

}
