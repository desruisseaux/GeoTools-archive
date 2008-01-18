package org.geotools.repository.styling;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.geotools.repository.AbstractFileGeoResource;
import org.geotools.repository.AbstractFileService;
import org.geotools.repository.GeoResourceInfo;
import org.geotools.repository.defaults.DefaultGeoResourceInfo;
import org.geotools.styling.SLDParser;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.util.ProgressListener;

public class SLDGeoResource extends AbstractFileGeoResource {

	/**
	 * Parsed style object.
	 */
	StyledLayerDescriptor sld;
	
	public SLDGeoResource(AbstractFileService service, File file) {
		super(service, file);
	}
	
	public boolean canResolve(Class adaptee) {
		if ( adaptee == null )
			return false;
	
		if ( adaptee.isAssignableFrom( StyledLayerDescriptor.class ) ) 
			return true;
		
		return super.canResolve( adaptee );
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if ( adaptee == null ) 
			return null;
		
		if ( adaptee.isAssignableFrom( StyledLayerDescriptor.class ) ) {
			return style( monitor );
		}
		
		return super.resolve( adaptee, monitor );
		
	}
	
	public GeoResourceInfo getInfo(ProgressListener monitor) throws IOException {
		StyledLayerDescriptor sld = style( monitor );
		
		URI schema = null;
		try {
			schema = new URI( SLDServiceFactory.SLD_NAMESPACE );
		} 
		catch (URISyntaxException e) { }
		
		String[] keywords = new String[] {
			"sld", "Styled Layer Descriptor"
		};
		
		return new DefaultGeoResourceInfo( 
			sld.getTitle(), sld.getName(), sld.getAbstract(), schema, null, null, keywords, null
		);
	}
	
	
	protected StyledLayerDescriptor style( ProgressListener monitor ) throws IOException {
		if ( sld == null ) {
			synchronized ( this ) {
				if ( sld == null ) {
					StyleFactory styleFactory = StyleFactoryFinder.createStyleFactory();
					
					try {
						SLDParser parser = new SLDParser( styleFactory, getFile() );
						sld = parser.parseSLD();
						setMessage( null );
					}
					catch( Throwable t ) {
						String msg = "Error parsing sld: " + getFile();
						logger.log( Level.WARNING, msg, t );
						setMessage( t );
					}
					
				}
			}
		}
		
		return sld;
	}
}
