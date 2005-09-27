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

import com.vividsolutions.jts.geom.Envelope;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import org.geotools.util.ProgressListener;

/**
 * Implementation of Resolve which represents a local catalog or web registry 
 * service.
 * <p>
 * Conceptually provides a searchable Catalog of "Spatial Data Sources". 
 * Metadata search is abitrary.
 * </p>
 * 
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.7.0
 * 
 */
public abstract class Catalog implements Resolve {
	
    /**
     * Catalogs do not have a parent so null is returned.
     * <p>
     * We can consider adding a global 'root' parent - but we will wait until we find a need, or if
     * users request.
     * </p>
     * 
     * @return null as catalogs do not have a parent
     */
    public Resolve parent( ProgressListener monitor ) {
        return null;
    }
    /**
     * Adds the specified entry to this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for additions.
     * <p>
     * An IService may belong to more than one Catalog.
     * </p>
     * 
     * @param entry
     * @throws UnsupportedOperationException
     */
    public abstract void add( Service service ) throws UnsupportedOperationException;

    /**
     * Removes the specified entry to this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for deletions.
     * 
     * @param service
     * @throws UnsupportedOperationException
     */
    public abstract void remove( Service service ) throws UnsupportedOperationException;

    /**
     * Replaces the specified entry in this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for deletions.
     * 
     * @param id
     * @param service
     * @throws UnsupportedOperationException
     */
    public abstract void replace( URI id, Service service ) throws UnsupportedOperationException;

    /**
     * Will attempt to morph into the adaptee, and return that object. Required adaptions:
     * <ul>
     * <li>ICatalogInfo.class
     * <li>List.class <IService>
     * </ul>
     * May Block.
     * 
     * @param adaptee
     * @param monitor May Be Null
     * @return
     * @see CatalogInfo
     * @see IService
     */
    public abstract Object resolve( Class adaptee, ProgressListener monitor ) throws IOException;

    /**
     * Aquire info on this Catalog.
     * <p>
     * This is functionally equivalent to: <core>resolve(ICatalogInfo.class,monitor)</code>
     * </p>
     * 
     * @see Catalog#resolve(Class, ProgressListener)
     * @return ICatalogInfo resolve(ICatalogInfo.class,ProgressListener monitor);
     */
    public CatalogInfo getInfo( ProgressListener monitor ) throws IOException {
        return (CatalogInfo) resolve(CatalogInfo.class, monitor);
    }

    /**
     * Find resources matching this id directly from this Catalog.
     * 
     * @param id used to match resolves
     * @param monitor used to show the progress of the find.
     * 
     * @return List (possibly empty) of resolves (objects implementing the 
     * Resolve interface)
     */
    public abstract List find( URI id, ProgressListener monitor );

    /**
     * Find Service matching this id directly from this Catalog.  This method is guaranteed to be non-blocking.
     * 
     * @param id used to match resolves
     * @param monitor monitor used to watch progress
     * 
     * @return List (possibly empty) of matching services (objects of type 
     * Service).
     */
    public abstract List findService( URI query, ProgressListener monitor );
    
    /**
     * Performs a search on this catalog based on the specified inputs.
     * <p>
     * The pattern uses the following conventions:
     * <ul>
     * <li>
     * <li> use " " to surround a phase
     * <li> use + to represent 'AND'
     * <li> use - to represent 'OR'
     * <li> use ! to represent 'NOT'
     * <li> use ( ) to designate scope
     * </ul>
     * The bbox provided shall be in Lat - Long, or null if the search is not to be contained within
     * a specified area.
     * </p>
     * 
     * @param pattern Search pattern (see above)
     * @param bbox The bbox in Lat-Long (ESPG 4269), or null
     * @param monitor for progress, or null if monitoring is not desired
     * 
     * @return List containg objects of type Resolve.
     */
    public abstract List search( String pattern, Envelope bbox, ProgressListener monitor )
            throws IOException;

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
}
