/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
 *
 */
package org.geotools.catalog;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.geotools.util.ProgressListener;

/**
 * Represents a geo spatial service handle. Follows the same design as IResource.
 * <p>
 * Represents a spatial service, which may be lazily loaded. The existance of this object does not
 * ensure that the advertized data is guaranteed to exist, nor does this interface guarantee that
 * the service exists based on this object's existance. We should also note the resource management
 * is left to the user, and that resolve() is not guaranteed to return the same instance object from
 * two subsequent calls, but may. This is merely a handle to some information about a service, and a
 * method of aquiring an instance of the service ...
 * </p>
 * <p>
 * NOTE: This may be the result of communications with a metadata service, and as such this service
 * handle may not have been validated yet. Remember to check the service status.
 * </p>
 * 
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 * @see ServiceInfo
 * @see ServiceFinder
 */
public abstract class Service implements Resolve {
	
	/** parent catalog containing the service **/
	private Catalog parent;

	/**
	 * Creates a new service handle contained within a catalog.
	 * 
	 * @param parent The catalog containg the service.
	 */
	public Service(Catalog parent) {
		this.parent = parent;
	}
	
    /**
     * Will attempt to morph into the adaptee, and return that object. 
     * Required adaptions:
     * <ul>
     * <li>IServiceInfo.class
     * <li>List.class <IGeoResource>
     * </ul>
     * May Block.
     * 
     * @param adaptee
     * @param monitor
     * @return instance of adaptee, or null if unavailable (IServiceInfo and List<IGeoResource>
     *         must be supported)
     * @see ServiceInfo
     * @see GeoResource
     * @see IResolve#resolve(Class, ProgressListener)
     */
    public abstract Object resolve( Class adaptee, ProgressListener monitor ) throws IOException;

    /**
     * Returns the parent Catalog.
     */
    public Resolve parent( ProgressListener monitor ) {
        return parent;
    }
    
    /**
     * Return list of IGeoResources managed by this service. This method must 
     * return the same result as the following:
     * 
     * <pre>
     *   <code>
     *   (List)resolve(List.class,monitor);
     *   </code>
     * </pre>
     * <p>
     * Many file based serivces will just contain a single IGeoResource.
     * </p>
     * 
     * @return A list of type GeoResource.
     */
    public abstract List members( ProgressListener monitor ) throws IOException;

    /**
     * @return IServiceInfo resolve(IServiceInfo.class,ProgressListener monitor);
     * @see IService#resolve(Class, ProgressListener)
     */
    public abstract ServiceInfo getInfo( ProgressListener monitor ) throws IOException;
//    {
//        return (ServiceInfo) resolve(ServiceInfo.class, monitor);
//    }
    
    /**
     * Accessor to the set of params used to create this entry. There is no guarantee that these
     * params created a usable service (@see getStatus() ). These params may have been modified
     * within the factory during creation. This method is intended to be used for cloning (@see
     * IServiceFactory) or for persistence between sessions.
     * 
     * @see ServiceFinder
     * 
     * @return A map with key of type String, and value of type Serializable.
     */
    public abstract Map getConnectionParams();

    /**
     * This should represent the identifier
     * 
     * @see Object#equals(java.lang.Object)
     * @param other
     * @return
     */
    public final boolean equals( Object other ) {
        if (other != null && other instanceof Service) {
            Service service = (Service) other;
            if (getIdentifier() != null && service.getIdentifier() != null)
                return getIdentifier().equals(service.getIdentifier());
        }
        return false;
    }
    
    /**
     * This method does nothing. Sublcasses should override if events are 
     * supported.
     */
    public void addListener(ResolveChangeListener listener) {
    	// do nothing
    }
    
    /**
     * This method does nothing. Sublcasses should override if events are 
     * supported.
     */
    public void removeListener(ResolveChangeListener listener) {
    	// do nothing
    }
    
    /**
     * This method does nothing. Sublcasses should override if events are 
     * supported.
     */
    public void fire(ResolveChangeEvent event) {
    	// do nothing
    }
    
    /**
     * This should represent the identified
     * 
     * @see Object#hashCode()
     * @return
     */
    public final int hashCode() {
        if (getIdentifier() != null)
            return getIdentifier().hashCode();
        return super.hashCode();
    }

    /**
     * Indicate class and id.
     * 
     * @return string representing this IResolve
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        String classname = getClass().getName();
        String name = classname.substring(classname.lastIndexOf('.') + 1);
        buf.append(name);
        buf.append("("); //$NON-NLS-1$
        buf.append(getIdentifier());
        buf.append(")"); //$NON-NLS-1$
        return buf.toString();
    }
}
