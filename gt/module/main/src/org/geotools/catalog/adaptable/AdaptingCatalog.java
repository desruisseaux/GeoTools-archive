package org.geotools.catalog.adaptable;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.CatalogInfo;

import org.geotools.catalog.Service;
import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;

public class AdaptingCatalog extends AdaptingResolve
	implements Catalog {
	
	
	protected AdaptingCatalog( Catalog catalog, ResolveAdapterFactoryFinder adapter ) {
		super(catalog, adapter);
		
	}
	
	protected Catalog catalog() {
		return (Catalog) resolve;
	}

	public void add(Service service) throws UnsupportedOperationException {
		if ( !( service instanceof AdaptingResolve ) ) {
			service = new AdaptingService( service, finder );
		}
		
		catalog().add( service );
	}

	public void remove(Service service) throws UnsupportedOperationException {
		catalog().remove( service );
	}

	public void replace(URI id, Service service) throws UnsupportedOperationException {
		catalog().replace( id, service );
	}

	public List find(URI id, ProgressListener monitor) {
		return catalog().find( id, monitor );
	}

	public List findService(URI query, ProgressListener monitor) {
		return catalog().findService( query, monitor );
	}

	public List search(String pattern, Envelope bbox, ProgressListener monitor) throws IOException {
		return catalog().search( pattern, bbox, monitor );
	}

	public CatalogInfo getInfo(ProgressListener monitor) throws IOException {
		return catalog().getInfo( monitor );
	}

}
