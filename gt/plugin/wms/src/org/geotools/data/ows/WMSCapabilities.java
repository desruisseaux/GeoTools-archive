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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a base object for a WMS getCapabilities response.
 *
 * @author Richard Gould, Refractions Research
 */
public class WMSCapabilities {
    private Service service;
    private WMSRequest request;
    private Layer layer;
    private String version;
    private String[] exceptions;
    
    private List layers; //cache

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }
    
    public List getLayerList() {
        if (layers == null) {
            layers = new ArrayList();
            layers.add(layer);
            addChildrenRecursive(layers, layer);
        }        
        return Collections.unmodifiableList(layers);
    }
    
    private void addChildrenRecursive(List layers, Layer layer) {
        if (layer.getChildren() != null) {
            for (int i = 0; i < layer.getChildren().length; i++) {
                layers.add(layer.getChildren()[i]);
                addChildrenRecursive(layers, layer.getChildren()[i]);
            }
        }
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
    public String[] getExceptions() {
        return exceptions;
    }
    public void setExceptions( String[] exceptions ) {
        this.exceptions = exceptions;
    }
}
