package org.geotools.catalog;

import java.io.IOException;

import org.geotools.util.ProgressListener;

/**
 * Adapts a resolve handle into another type of object.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public interface ResolveAdapterFactory {

	/**
	 * Determines if a perticular adaptation is supported.
	 * 
	 * @param resolve The handle being adapted.
	 * @param adapter The adapting class.
	 * 
	 * @return True if supported, otherwise false.
	 */
	boolean canAdapt( Resolve resolve, Class adapter );
	
	/**
	 * Performs an adaptation to a particular adapter.
	 * 
	 * @param resolve The handle being adapted. 
	 * @param adapter The adapting class.
	 * @param monitor Progress monitor for blocking class.
	 * 
	 * @return The adapter, or null if adapation not possible.
	 *
	 * @throws IOException Any I/O errors that occur.
	 */
	Object adapt( Resolve resolve, Class adapter, ProgressListener monitor)
		throws IOException;
}
