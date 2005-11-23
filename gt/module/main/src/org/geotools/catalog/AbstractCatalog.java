package org.geotools.catalog;

import java.io.IOException;

import org.geotools.util.ProgressListener;

/**
 * Abstract implementation of Catalog.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class AbstractCatalog implements Catalog {
	
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
