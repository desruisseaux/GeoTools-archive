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

/**
 * Represents a base object for a WMS getCapabilities response.
 *
 * @author Richard Gould, Refractions Research
 */
public class WMSCapabilities {
    private Service service;
    private WMSRequest request;
    private Layer[] layers;
    private String version;

    /**
     * DOCUMENT ME!
     *
     * @return Returns the layers.
     */
    public Layer[] getLayers() {
        return layers;
    }

    /**
     * DOCUMENT ME!
     *
     * @param layers The layers to set.
     */
    public void setLayers(Layer[] layers) {
        this.layers = layers;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the request.
     */
    public WMSRequest getRequest() {
        return request;
    }

    /**
     * DOCUMENT ME!
     *
     * @param request The request to set.
     */
    public void setRequest(WMSRequest request) {
        this.request = request;
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
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * DOCUMENT ME!
     *
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
