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
     * The layers contained in this Capabilities document, organized according
     * to the order they are encountered. Each Layer maintains knowledge of its
     * parent. The hierarchy can be reconstructed using that.
     *
     * @return Returns an array of the layers.
     */
    public Layer[] getLayers() {
        return layers;
    }

    /**
     * @param layers The layers to set.
     */
    public void setLayers(Layer[] layers) {
        this.layers = layers;
    }

    /**
     * The request contains information about possible Requests that can be 
     * made against this server, including URLs and formats.
     *
     * @return Returns the request.
     */
    public WMSRequest getRequest() {
        return request;
    }

    /**
     * @param request The request to set.
     */
    public void setRequest(WMSRequest request) {
        this.request = request;
    }

    /**
     * The Service contains metadata about the WMS.
     * 
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
     * The version that this Capabilities is in.
     * 
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
