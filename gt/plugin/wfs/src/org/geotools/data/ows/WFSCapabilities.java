
package org.geotools.data.ows;

import java.util.List;

import org.geotools.data.ows.OperationType;
import org.geotools.data.ows.Service;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSCapabilities {
    private Service service; // no contact info provided
    private List featureTypes; // FeatureSetDescriptions
    
    private OperationType describeFeatureType;
    private OperationType getCapabilities;
    private OperationType getFeature;
    private OperationType getFeatureWithLock;
    private OperationType transaction;
    
    private String vendorSpecificCapabilities;
//    private FilterCapabilities filterCapabilities;
    private Object filterCapabilities;

    /**
     * @return Returns the featureTypes.
     */
    public List getFeatureTypes() {
        return featureTypes;
    }
    /**
     * @param featureTypes The featureTypes to set.
     */
    public void setFeatureTypes(List featureTypes) {
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
    /**
     * @return Returns the describeFeatureType.
     */
    OperationType getDescribeFeatureType() {
        return describeFeatureType;
    }
    /**
     * @param describeFeatureType The describeFeatureType to set.
     */
    void setDescribeFeatureType(OperationType describeFeatureType) {
        this.describeFeatureType = describeFeatureType;
    }
    /**
     * @return Returns the getCapabilities.
     */
    OperationType getGetCapabilities() {
        return getCapabilities;
    }
    /**
     * @param getCapabilities The getCapabilities to set.
     */
    void setGetCapabilities(OperationType getCapabilities) {
        this.getCapabilities = getCapabilities;
    }
    /**
     * @return Returns the getFeature.
     */
    OperationType getGetFeature() {
        return getFeature;
    }
    /**
     * @param getFeature The getFeature to set.
     */
    void setGetFeature(OperationType getFeature) {
        this.getFeature = getFeature;
    }
    /**
     * @return Returns the getFeatureWithLock.
     */
    OperationType getGetFeatureWithLock() {
        return getFeatureWithLock;
    }
    /**
     * @param getFeatureWithLock The getFeatureWithLock to set.
     */
    void setGetFeatureWithLock(OperationType getFeatureWithLock) {
        this.getFeatureWithLock = getFeatureWithLock;
    }
    /**
     * @return Returns the transaction.
     */
    OperationType getTransaction() {
        return transaction;
    }
    /**
     * @param transaction The transaction to set.
     */
    void setTransaction(OperationType transaction) {
        this.transaction = transaction;
    }
}
