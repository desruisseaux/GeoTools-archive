package org.geotools.catalog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.util.ProgressListener;

/**
 * Abstract service implementation for file based services.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractFileService extends AbstractService {

	/**
	 * the file
	 */
	File file;
	
	public AbstractFileService( Catalog parent, File file ) {
		super( parent );
		this.file = file;
	}

	public AbstractFileService( Catalog parent, Map params, File file ) {
		super( parent, params );
		this.file = file;
	}
	
	/**
	 * @return the underlying file handle.
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Returns a single element list which contains a single instance of 
	 * {@link AbstractFileGeoResource}.
	 */
	public List members(ProgressListener monitor) throws IOException {
		if ( getMembers() != null ) 
			return getMembers();
		
		synchronized (this) {
			if ( getMembers() != null ) {
				return getMembers();
			}
			
			List members = null; 
			try {
				members = createMembers( monitor );
				return members;
			}
			catch( IOException e ) {
				//TODO: log this
				setMessage( e );
				throw e;
			}
		
		}
	}

	abstract protected List/*<AbstractFileGeoResource>*/
		createMembers( ProgressListener monitor ) throws IOException;
	
	/**
	 * Supports default Service resolves with an additional resolve to a 
	 * {@link java.io.File}. 
	 * <p>
	 * Subclasses may wish to extend, or override this method.
	 * </p>
	 */
	public boolean canResolve(Class adaptee) {
		
		return List.class.isAssignableFrom( adaptee )  || 
			ServiceInfo.class.isAssignableFrom( adaptee ) || 
			File.class.isAssignableFrom( adaptee );
	}

	/**
	 * Supports default Service resolves with an additional resolve to a 
	 * {@link java.io.File}. 
	 * <p>
	 * Subclasses may wish to extend, or override this method.
	 * </p>
	 */
	public Object resolve(Class adaptee, ProgressListener monitor)
			throws IOException {
		
		if ( adaptee.isAssignableFrom( List.class ) ) {
			return members( monitor );
		}
		
		if ( adaptee.isAssignableFrom( ServiceInfo.class ) ) {
			return getInfo( monitor );
		}
		
		if ( adaptee.isAssignableFrom(  File.class ) ) {
			return file;
		}
		
		return null;
	}

	/**
	 * Returns the file uri.
	 * 
	 * @see #getFile()
	 */
	public URI getIdentifier() {
		return file.toURI();
	}

}
