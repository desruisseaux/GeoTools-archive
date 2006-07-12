package org.geotools.catalog.adaptable;

import org.geotools.catalog.ResolveAdapterFactory;

/**
 * Implemented by {@link ResolveAdapterFactory} instances who need 
 * access to the adapting delegate of a resolve being adapted.
 *
 * <p>
 * This interfaces is used only inside of the {@link AdaptingResolve} 
 * framework, and nowhere else.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public interface AdaptingResolveAware {

	/**
	 * Sets the adapting delegate.
	 * <p>
	 * This method is always called before either of:
	 * {@link ResolveAdapterFactory#canAdapt(Resolve, Class)}
	 * {@link ResolveAdapterFactory#adapt(Resolve, Class, ProgressListener)
	 * </p>
	 * @param adaptingResolve The adapting resolve which is a delegate for 
	 * the resolve being adapted.
	 */
	void setAdaptingResolve( AdaptingResolve adaptingResolve );
}
