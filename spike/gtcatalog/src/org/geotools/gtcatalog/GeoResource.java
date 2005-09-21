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
package org.geotools.gtcatalog;

import java.io.IOException;
import java.util.List;

import org.geotools.util.ProgressListener;



/**
 * Represents a handle to a spatial resource.
 * <p>
 * The resource is not guaranteed to exist, nor do we guarantee that we can connect with the
 * resource. Some/All potions of this handle may be loaded as required. This resource handle may
 * also be the result a metadata service query.
 * 
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 */
public abstract class GeoResource implements Resolve {

    /**
     * Blocking operation to resolve into the adaptee, if available.
     * <p>
     * Required adaptions:
     * <ul>
     * <li>IGeoResourceInfo.class
     * <li>IService.class
     * </ul>
     * </p>
     * <p>
     * Example (no casting required):
     * 
     * <pre><code>
     * IGeoResourceInfo info = resovle(IGeoResourceInfo.class);
     * </code></pre>
     * 
     * </p>
     * <p>
     * Recommendated adaptions:
     * <ul>
     * <li>ImageDescriptor.class (for icon provided by external service)
     * </ul>
     * </p>
     * 
     * @param adaptee
     * @param monitor
     * @return instance of adaptee, or null if unavailable (IGeoResourceInfo and IService must be
     *         supported)
     * @see GeoResourceInfo
     * @see IService
     * @see IResolve#resolve(Class, ProgressListener)
     */
    public abstract Object resolve( Class adaptee, ProgressListener monitor ) throws IOException;

    /**
     * Blocking operation to describe this service.
     * <p>
     * As an example this method is used by LabelDecorators to aquire title, and icon.
     * </p>
     * 
     * @return IGeoResourceInfo resolve(IGeoResourceInfo.class,ProgressListener monitor);
     * @see GeoResource#resolve(Class, ProgressListener)
     */
    public GeoResourceInfo getInfo( ProgressListener monitor ) throws IOException {
        return (GeoResourceInfo) resolve(GeoResourceInfo.class, monitor);
    }

    /**
     * This method is shorthand for 
     * <pre>
     * 	<code>
     * 		return (Service) resolve(Service.class, monitor);
     * 	</code>
     * </pre>
     * 
     * @return The service containg the resource, an object of type Service.
     * @see GeoResource#resolve(Class, ProgressListener)
     */
    public Resolve parent( ProgressListener monitor ) throws IOException {
        return (Service) resolve(Service.class, monitor);
    }

    /**
     * return null ... almost always a leaf
     * 
     * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.ProgressListener)
     */
    public List members( ProgressListener monitor ) {
        return null;
    }

    /**
     * This should represent the identifier
     * 
     * @see Object#equals(java.lang.Object)
     * @param other
     * @return
     */
    public boolean equals( Object other ) {
        if (other != null && other instanceof GeoResource) {
            GeoResource resource = (GeoResource) other;
            if (getIdentifier() != null && resource.getIdentifier() != null)
                return getIdentifier().equals(resource.getIdentifier());
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
    public int hashCode() {
        if (getIdentifier() != null)
            return getIdentifier().hashCode();
        return super.hashCode();
    }

    /**
     * Non blocking label used by LabelProvider. public static final String
     * getGenericLabel(IGeoResource resource){ assert resource.getIdentifier() != null; return
     * resource==null ||
     * resource.getIdentifier()==null?"Resource":resource.getIdentifier().toString(); }
     */
    /**
     * Non blocking icon used by LabelProvider. public static final ImageDescriptor
     * getGenericIcon(IGeoResource resource){ if(resource !=null){ assert resource.getIdentifier() !=
     * null; if(resource.canResolve(FeatureSource.class)){ // default feature return
     * Images.getDescriptor(ISharedImages.FEATURE_OBJ); }
     * if(resource.canResolve(GridCoverage.class)){ // default raster return
     * Images.getDescriptor(ISharedImages.GRID_OBJ); } } return
     * Images.getDescriptor(ISharedImages.RESOURCE_OBJ); }
     */
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
