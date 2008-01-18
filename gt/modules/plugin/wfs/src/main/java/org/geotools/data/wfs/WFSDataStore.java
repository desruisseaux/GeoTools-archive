package org.geotools.data.wfs;

import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.DataStore;
import org.geotools.data.ServiceInfo;
import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * {@link DataStore} extension interface to provide WFS specific extra
 * information.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public interface WFSDataStore extends DataStore {
    
    /**
     * Provide access to ServiceInfo generated from the wfs capabilities document.
     * 
     * @return ServiceInfo
     */
    ServiceInfo getInfo();
}
