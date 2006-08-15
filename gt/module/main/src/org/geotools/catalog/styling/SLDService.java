package org.geotools.catalog.styling;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.catalog.AbstractFileService;
import org.geotools.catalog.Catalog;
import org.geotools.catalog.GeoResource;
import org.geotools.catalog.ServiceInfo;
import org.geotools.catalog.defaults.DefaultServiceInfo;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.util.ProgressListener;

public class SLDService extends AbstractFileService {

	List members;
	
	public SLDService( Catalog parent, File file ) {
		super(parent, file);
	}
	
	public boolean canResolve(Class adaptee) {
		if ( adaptee == null ) 
			return false;
		
		if ( adaptee.equals( StyledLayerDescriptor[].class ) ) {
			return true;
		}
		
		return super.canResolve( adaptee );
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if ( adaptee == null )
			return null;
		
		if ( adaptee.equals( StyledLayerDescriptor[].class ) ) {
			List members = members( monitor );
			List styles = new ArrayList();
			
			for ( Iterator m = members.iterator(); m.hasNext(); ) {
				GeoResource member = (GeoResource) m.next();
				StyledLayerDescriptor sld = 
					(StyledLayerDescriptor) member.resolve( StyledLayerDescriptor.class, monitor );
				if ( sld != null ) {
					styles.add( sld );
				}
			
			}
			
			return (StyledLayerDescriptor[]) styles.toArray( new StyledLayerDescriptor[ styles.size() ] );
		}
		
		return super.resolve( adaptee, monitor );
	}
	
	protected List createMembers(ProgressListener monitor) throws IOException {
		if ( members == null ) {
			synchronized ( this ) {
				if ( members == null ) {
					members = new ArrayList();
					
					if ( getFile().isDirectory() ) {
						File[] files = getFile().listFiles( 
							new FileFilter() {

								public boolean accept(File pathname) {
									return SLDServiceFactory.isSLDFile( pathname );
								}
							}
						);
						
						for ( int i = 0; i < files.length; i++ ) {
							members.add( new SLDGeoResource( this, files[i] ) );
						}
						
					}
					else {
						members.add( new SLDGeoResource( this, getFile() ) );
						
					}
					
				}
			}
		}
		
		return members;
	}

	public ServiceInfo getInfo(ProgressListener monitor) throws IOException {
		try {
			return new DefaultServiceInfo( 
				"Styled Layer Descriptor", null, null, getFile().toURI(), null, 
				new URI( SLDServiceFactory.SLD_NAMESPACE ), null, null
			);
		} 
		catch (URISyntaxException e) {
			throw (IOException) new IOException().initCause( e );
		}
	}

}
