package org.geotools.catalog;

import java.io.IOException;

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
public interface GeoResource extends Resolve {
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
    Object resolve( Class adaptee, ProgressListener monitor ) throws IOException;

    /**
     * Blocking operation to describe this service.
     * <p>
     * As an example this method is used by LabelDecorators to aquire title, and icon.
     * </p>
     * 
     * @return IGeoResourceInfo resolve(IGeoResourceInfo.class,ProgressListener monitor);
     * @see AbstractGeoResource#resolve(Class, ProgressListener)
     */
    GeoResourceInfo getInfo( ProgressListener monitor ) throws IOException;
}
