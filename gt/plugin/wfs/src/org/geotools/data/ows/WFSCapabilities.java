/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.ows;

import java.util.Iterator;
import java.util.List;

import org.geotools.filter.FilterCapabilitiesMask;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class WFSCapabilities {
    private Service service; // no contact info provided
    private List featureTypes; // FeatureSetDescriptions
    private OperationType describeFeatureType;
    private OperationType getCapabilities;
    private OperationType getFeature;
    private OperationType getFeatureWithLock;
    private OperationType transaction;
    private OperationType lockFeature;
    private String vendorSpecificCapabilities;
    private FilterCapabilitiesMask filterCapabilities;

    public static FeatureSetDescription getFeatureSetDescription(WFSCapabilities capabilities, String typename){
        List l = capabilities.getFeatureTypes();
        Iterator i = l.iterator();
        String crsName = null;

        while (i.hasNext() && crsName==null) {
                FeatureSetDescription fsd = (FeatureSetDescription) i.next();
                if (typename.equals(fsd.getName()) || (fsd.getName()!=null && typename.equals(fsd.getName().substring(typename.indexOf(':')+1)))) {
                    return fsd;
                }
        }
        return null;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the featureTypes.
     */
    public List getFeatureTypes() {
        return featureTypes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureTypes The featureTypes to set.
     */
    public void setFeatureTypes(List featureTypes) {
        this.featureTypes = featureTypes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the filterCapabilities.
     */
    public FilterCapabilitiesMask getFilterCapabilities() {
        return filterCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filterCapabilities The filterCapabilities to set.
     */
    public void setFilterCapabilities(FilterCapabilitiesMask filterCapabilities) {
        this.filterCapabilities = filterCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the service.
     */
    public Service getService() {
        return service;
    }

    /**
     * DOCUMENT ME!
     *
     * @param service The service to set.
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the vendorSpecificCapabilities.
     */
    public String getVendorSpecificCapabilities() {
        return vendorSpecificCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param vendorSpecificCapabilities The vendorSpecificCapabilities to set.
     */
    public void setVendorSpecificCapabilities(String vendorSpecificCapabilities) {
        this.vendorSpecificCapabilities = vendorSpecificCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the describeFeatureType.
     */
    public OperationType getDescribeFeatureType() {
        return describeFeatureType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param describeFeatureType The describeFeatureType to set.
     */
    public void setDescribeFeatureType(OperationType describeFeatureType) {
        this.describeFeatureType = describeFeatureType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the getCapabilities.
     */
    public OperationType getGetCapabilities() {
        return getCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param getCapabilities The getCapabilities to set.
     */
    public void setGetCapabilities(OperationType getCapabilities) {
        this.getCapabilities = getCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the getFeature.
     */
    public OperationType getGetFeature() {
        return getFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param getFeature The getFeature to set.
     */
    public void setGetFeature(OperationType getFeature) {
        this.getFeature = getFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the getFeatureWithLock.
     */
    public OperationType getGetFeatureWithLock() {
        return getFeatureWithLock;
    }

    /**
     * DOCUMENT ME!
     *
     * @param getFeatureWithLock The getFeatureWithLock to set.
     */
    public void setGetFeatureWithLock(OperationType getFeatureWithLock) {
        this.getFeatureWithLock = getFeatureWithLock;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the transaction.
     */
    public OperationType getTransaction() {
        return transaction;
    }

    /**
     * DOCUMENT ME!
     *
     * @param transaction The transaction to set.
     */
    public void setTransaction(OperationType transaction) {
        this.transaction = transaction;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the lockFeature.
     */
    public OperationType getLockFeature() {
        return lockFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lockFeature The lockFeature to set.
     */
    public void setLockFeature(OperationType lockFeature) {
        this.lockFeature = lockFeature;
    }
}
