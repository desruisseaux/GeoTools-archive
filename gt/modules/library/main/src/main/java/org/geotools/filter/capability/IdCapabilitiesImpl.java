package org.geotools.filter.capability;

import org.opengis.filter.capability.IdCapabilities;

/**
 * Implementation of the IdCapabilities interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class IdCapabilitiesImpl implements IdCapabilities {

    boolean eid;
    boolean fid;
    
    public IdCapabilitiesImpl( boolean eid, boolean fid ) {
        this.eid = eid;
        this.fid = fid;
    }
    
    public boolean hasEID() {
        return eid;
    }

    public boolean hasFID() {
        return fid;
    }
}
