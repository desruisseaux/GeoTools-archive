package org.geotools.catalog.shp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.JTS.ReferencedEnvelope;
import org.geotools.catalog.GeoResource;
import org.geotools.catalog.GeoResourceInfo;
import org.geotools.catalog.Service;

import org.geotools.util.ProgressListener;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class ShapefileGeoResource extends GeoResource {

	private ShapefileService parent;
	private String typeName;
	private Throwable msg;
	private GeoResourceInfo info;
	private FeatureSource featureSource;
	
	ShapefileGeoResource(ShapefileService parent, String typeName) {
		this.parent = parent;
		this.typeName = typeName;
	}

	public boolean canResolve(Class adaptee) {
		if (adaptee == null)
			return false;
		
		return adaptee.isAssignableFrom(Service.class) || 
			adaptee.isAssignableFrom(GeoResourceInfo.class) || 
			adaptee.isAssignableFrom(FeatureStore.class) || 
			adaptee.isAssignableFrom(FeatureSource.class); 
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor)
			throws IOException {
		
		if (adaptee == null)
			return null;
		
		if (adaptee.isAssignableFrom(Service.class))
			return parent;
		
		if (adaptee.isAssignableFrom(GeoResource.class))
			return getInfoInternal(monitor);
		
		if (adaptee.isAssignableFrom(FeatureStore.class)) {
			FeatureSource featureSource = getFeatureSource(monitor);
			if (featureSource instanceof FeatureStore)
				return featureSource;
		}
		
		if (adaptee.isAssignableFrom(FeatureSource.class)) 
			return getFeatureSource(monitor);
		
		return null;
	}


	protected GeoResourceInfo getInfoInternal(ProgressListener monitor) 
		throws IOException {
		
		if (info == null) {
			synchronized (parent.getDataStore(monitor)) {
				if (info == null) {
					//calculate some meta data based on the feature type
					FeatureType type = getFeatureSource(monitor).getSchema();
					CoordinateReferenceSystem crs = type.getDefaultGeometry()
						.getCoordinateSystem();
					URI schema = type.getNamespace();
					String name = type.getTypeName();
					String title = name;
					String description = name;
					String[] keywords = new String[]{
                        ".shp","Shapefile",name,schema.toString()
                    };
					
					//calculate bounds
					ReferencedEnvelope bounds = null;
					try {
						Envelope tmpBounds = getFeatureSource(monitor).getBounds();
						if( tmpBounds instanceof ReferencedEnvelope)
							bounds = (ReferencedEnvelope) tmpBounds;
						else
						    bounds = new ReferencedEnvelope(tmpBounds, crs);
						
						if( bounds==null ){
							bounds = new ReferencedEnvelope(new Envelope(),crs);
						    for( FeatureReader iter = getFeatureSource(monitor)
						    		.getFeatures().reader(); iter.hasNext(); ) {
						    	Feature element = iter.next();
						        if( bounds.isNull() )
						            bounds.init(element.getBounds());
						        else
						            bounds.expandToInclude(element.getBounds());
						    }
						}
					} 
					catch (Exception e) {
						//something bad happend, return an i dont know
						bounds = new ReferencedEnvelope(new Envelope(),crs);
					}
					
					info = new GeoResourceInfo(
						title,name,description,schema,bounds,crs,keywords,null
					);
				}
			}
		}
		
		return info;
	}
	
	protected FeatureSource getFeatureSource(ProgressListener monitor) 
		throws IOException {
		
		if (featureSource == null) {
			synchronized (parent.getDataStore(monitor)) {
				if (featureSource == null) {
					try {
						msg = null;
						DataStore dataStore = parent.getDataStore(monitor);
						if (dataStore != null) {
							featureSource = dataStore.getFeatureSource(typeName); 
						}
					} 
					catch (Throwable t) {
						msg = t;
						new IOException().initCause(t);
					}
				}
			}
		}
		
		
		return featureSource;
	}

	public Status getStatus() {
		if (msg == null) {
			if (featureSource != null)
				return Status.CONNECTED;
			
			return Status.NOTCONNECTED;
		}
		
		return Status.BROKEN;
	}

	public Throwable getMessage() {
		return msg;
	}

	public URI getIdentifier() {
		URI uri = parent.getIdentifier();
		if (uri != null) {
			try {
				return new URI(uri.toString() + "#" + typeName);
			} 
			catch (URISyntaxException e) {
				return null;
			}
		}
		
		return null;
	}

}
