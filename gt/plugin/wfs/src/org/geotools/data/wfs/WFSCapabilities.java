
package org.geotools.data.wfs;

import org.geotools.data.wms.capabilities.Service;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSCapabilities {
    private Service service; // no contact info provided
    private FeatureSetDescription[] featureTypes;
    private Capability[] capabilities;
    private String vendorSpecificCapabilities;
//    private FilterCapabilities filterCapabilities;
    private Object filterCapabilities;
    /**
     * @return Returns the capabilities.
     */
    public Capability[] getCapabilities() {
        return capabilities;
    }
    /**
     * @param capabilities The capabilities to set.
     */
    public void setCapabilities(Capability[] capabilities) {
        this.capabilities = capabilities;
    }
    /**
     * @return Returns the featureTypes.
     */
    public FeatureSetDescription[] getFeatureTypes() {
        return featureTypes;
    }
    /**
     * @param featureTypes The featureTypes to set.
     */
    public void setFeatureTypes(FeatureSetDescription[] featureTypes) {
        this.featureTypes = featureTypes;
    }
/**
 * @return Returns the filterCapabilities.
 */
public Object getFilterCapabilities() {
    return filterCapabilities;
}
/**
 * @param filterCapabilities The filterCapabilities to set.
 */
public void setFilterCapabilities(Object filterCapabilities) {
    this.filterCapabilities = filterCapabilities;
}
    /**
     * @return Returns the service.
     */
    public Service getService() {
        return service;
    }
    /**
     * @param service The service to set.
     */
    public void setService(Service service) {
        this.service = service;
    }
    /**
     * @return Returns the vendorSpecificCapabilities.
     */
    public String getVendorSpecificCapabilities() {
        return vendorSpecificCapabilities;
    }
    /**
     * @param vendorSpecificCapabilities The vendorSpecificCapabilities to set.
     */
    public void setVendorSpecificCapabilities(String vendorSpecificCapabilities) {
        this.vendorSpecificCapabilities = vendorSpecificCapabilities;
    }
}
