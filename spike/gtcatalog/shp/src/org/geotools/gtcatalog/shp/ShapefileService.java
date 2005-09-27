package org.geotools.gtcatalog.shp;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.gtcatalog.Catalog;
import org.geotools.gtcatalog.Service;
import org.geotools.gtcatalog.ServiceInfo;
import org.geotools.util.ProgressListener;

/**
 * Implementation of a service handle for shapefiles.
 * 
 * @see org.geotools.gtcatalog.Service 
 * 
 * @author David Zwiers, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public class ShapefileService extends Service {

	private ServiceInfo info;
	private ShapefileDataStore dataStore;
	private Throwable msg;
	private URI uri;
	private Map params;
	private List members;
	
	public ShapefileService(Catalog parent, URI uri, Map params) {
		super(parent);
		this.params = params;
		this.uri = uri;
	}

	public boolean canResolve(Class adaptee) {
		if (adaptee == null) 
			return false;
		
		return adaptee.isAssignableFrom(ServiceInfo.class) || 
			adaptee.isAssignableFrom(List.class) || 
			adaptee.isAssignableFrom(ShapefileDataStore.class);
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if (adaptee == null)
			return null;
		
		if (adaptee.isAssignableFrom(ServiceInfo.class))
			return getInfoInternal(monitor);
			
		
		if (adaptee.isAssignableFrom(List.class))
			return members(monitor);
		
		if (adaptee.isAssignableFrom(ShapefileDataStore.class))
			return getDataStore(monitor);
		
		return null;
	}
	
	protected ServiceInfo getInfoInternal(ProgressListener monitor)
		throws IOException {
		if (info == null) {
			synchronized (getDataStore(monitor)) {
				if (info == null) {
					String title = getIdentifier().getPath();
					String description = getIdentifier().toString();
					String[] keywords = new String[]{
						".shp", "Shapefile", 
						getDataStore(monitor).getTypeNames()[0]
					};
					
					info = new ServiceInfo(
						title,description,null,null,null,null,keywords,null
					);
					
				}
			}
		}
		
		return info;
	}
	
	protected ShapefileDataStore getDataStore(ProgressListener monitor) 
		throws IOException {
		if (dataStore == null) {
			synchronized (ShapefileDataStore.class) {
				if (dataStore == null) {
					ShapefileDataStoreFactory factory = 
						new ShapefileDataStoreFactory();
					if (factory.canProcess(params)) {
						try {
							msg = null;
							dataStore = 
								(ShapefileDataStore)factory.createDataStore(params);
						}
						catch(Throwable t) {
							//save error to report back later
							msg = t;
							new IOException().initCause(t);
						}
					}
				}
			}
		}
		
		return dataStore;
	}
	
	public List members(ProgressListener monitor) throws IOException {
		if (members == null) {
			synchronized (getDataStore(monitor)) {
				if (members == null) {
					members = new LinkedList();
					String[] typenames = getDataStore(monitor).getTypeNames();
					if (typenames != null) {
						for (int i = 0; i < typenames.length; i++) {
							members.add(new ShapefileGeoResource(this,typenames[i]));
						}
					}
				}
			}
		}
		return members;
	}

	public Map getConnectionParams() {
		return params;
	}

	public Status getStatus() {
		if (msg == null) {
			if (dataStore != null)
				return Status.CONNECTED;
			
			return Status.NOTCONNECTED;
		}
		
		return Status.BROKEN;
	}

	public Throwable getMessage() {
		return msg;
	}

	public URI getIdentifier() {
		return uri;
	}
}
