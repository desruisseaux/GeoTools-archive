/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wms;

import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.Service;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.ows.WMSRequest;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * Capabilities Builder for use by WMSParser instances.
 * <p>
 * WMSParsers are expected to call the methods in order:
 * <ol>
 * <li>buildCapabilities( version )
 * <li>buildService( name, title, onlineResource, abstract, keywords )
 * <li>buildGetCapabilitiesOperation( formats, get, post )
 * <li>buildGetMapOperation( formats, get, post )
 * <li>buildGetFeatureInfo( formats, get, post )
 * <li>buildLayer( title, name, queryable, parentLayerTitle )
 * <li>buildSRS( srs )
 * <li>buildStyle( style )
 * <li>finish() - to retrive generated Capabilities object
 * </ul>
 * </p>
 * <p>
 * At this time a WMSBuilder is not reuseable - finish() could be modified to
 * reset the builder if such reuse is considered desireable.
 * </p>
 * @author Richard Gould, Refractions Research
 */
public class WMSBuilder {
    private WMSCapabilities capabilities;
    private Service service;
    private WMSRequest request;
    private WMSOperationType getCapabilities;
    private WMSOperationType getMap;
    private WMSOperationType getFeatureInfo;
    private List layers;
    private Layer layer;
    private HashMap bboxes;

    /**
     * Constructs a WMSBuilder
     */
    public WMSBuilder() {
        layers = new ArrayList();
        request = new WMSRequest();
    }

    /**
     * Builds a capabilities object. This should only be called once each
     * time this object is constructed. Each call will reset the previous
     * capabilities object. 
     * 
     * @param version the version number for the capabilities object
     */
    public void buildCapabilities(String version) {
        capabilities = new WMSCapabilities();
        capabilities.setVersion(version);
    }

    /**
     * Builds a Service object with the provided parameters. 
     * @param name the machine-readable name of the service
     * @param title the human-readable title of the service
     * @param onlineResource the URL of the server that provides the service
     * @param _abstract a description of the service
     * @param keywords searchable metadata keywords about the server
     * @param maxHeight
     * @param maxWidth
     * @param layerLimit
     */
    public void buildService(String name, String title, URL onlineResource,
        String _abstract, String[] keywords, int layerLimit, int maxWidth, int maxHeight) {
        service = new Service();
        service.setName(name);
        service.setTitle(title);
        service.setOnlineResource(onlineResource);
        service.set_abstract(_abstract);
        service.setKeywordList(keywords);
        service.setLayerLimit(layerLimit);
        service.setMaxWidth(maxWidth);
        service.setMaxHeight(maxHeight);
    }

    /**
     * Builds a GetCapabilities operation
     *
     * @param formats List of available formats
     * @param get the URL for performing a GET request
     * @param post the URL for performing a POST request
     */
    public void buildGetCapabilitiesOperation(List formats, URL get, URL post) {
        getCapabilities = new WMSOperationType();
        buildWMSOperationType(getCapabilities, formats, get, post);
    }

    /**
     * Builds a GetMap operation
     * 
     * @param formats a List containing Strings of possible format for the GetMap response
     * @param get the URL for performing a GET request
     * @param post the URL for performing a POST request
     */
    public void buildGetMapOperation(List formats, URL get, URL post) {
        getMap = new WMSOperationType();
        buildWMSOperationType(getMap, formats, get, post);
    }
    /**
     * Builds a GetFeatureInfo operation
     * 
     * @param formats a List containing Strings of possible format for the GetFeatureInfo response
     * @param get the URL for performing a GET request
     * @param post the URL for performing a POST request
     */
    public void buildGetFeatureInfoOperation(List formats, URL get, URL post) {
        getFeatureInfo = new WMSOperationType();
        buildWMSOperationType(getFeatureInfo, formats, get, post);
    }

    private void buildWMSOperationType(WMSOperationType operationType,
        List formats, URL get, URL post) {
        String[] formatStrings = (String[]) formats.toArray(new String[formats.size()]);
        buildWMSOperationType(operationType, formatStrings, get, post);
    }

    private void buildWMSOperationType(WMSOperationType operationType,
        String[] formats, URL get, URL post) {
        operationType.setFormats(formats);
        operationType.setGet(get);
        operationType.setPost(post);
    }

    /**
     * Begins the construction of a Layer object. The layer has its initial
     * information added to it, but is not completed until another call to
     * buildLayer() or finish() is made. This allows the building of bounding
     * boxes objects. 
     * 
     * @param title a human-readable title
     * @param name a machine-readable name, indicated that this layer is drawable
     * @param queryable true if this layer is queryable, false otherwise
     * @param parentLayerTitle the title of this layer's parent
     * @param srss a List of Strings containing this layer's possible CRS/SRS values
     * @param styles a List of Strings containing this layer's possible style values
     */
    public void buildLayer(String title, String name, boolean queryable,
        String parentLayerTitle, Set srss, List styles) {
        if (layer != null) {
            finishLayer();
        }

        layer = new Layer(title);
        bboxes = new HashMap();

        layer.setName(name);
        layer.setQueryable(queryable);

        if ((parentLayerTitle != null) && (parentLayerTitle.length() != 0)) {
            int index = -1;

            for (int i = 0; i < layers.size(); i++) {
                if (((Layer) layers.get(i)).getTitle().equals(parentLayerTitle)) {
                    index = i;

                    break;
                }
            }

            layer.setParent((Layer) layers.get(index));
        }

        layer.setSrs(srss);
        layer.setStyles(styles);
    }

    /**
     * Constructs a BoundingBox object and adds it to the current layer.
     * 
     * @param crs the Coordinate Reference System (or SRS) that this bounding box is in
     * @param minX the X value of the lower coordinate
     * @param minY the Y value of the lower coordinate
     * @param maxX the X value of the higher coordinate
     * @param maxY the Y value of the higher coordinate
     */
    public void buildBoundingBox(String crs, double minX, double minY,
        double maxX, double maxY) {
        BoundingBox bbox = new BoundingBox(crs, minX, minY, maxX, maxY);
        bboxes.put(crs, bbox);
    }

    private void finishLayer() {
        layer.setBoundingBoxes(bboxes);

        layers.add(layer);

        bboxes = null;
    }

    /**
     * Completes the capabilities structure and returns it. 
     * @return the completed WMSCapabilities data object.
     */
    public WMSCapabilities finish() {
        capabilities.setService(service);

        request.setGetCapabilities(getCapabilities);
        request.setGetMap(getMap);
        request.setGetFeatureInfo(getFeatureInfo);

        capabilities.setRequest(request);

        finishLayer();

        Layer[] layerArray = new Layer[layers.size()];

        for (int i = 0; i < layers.size(); i++) {
            layerArray[i] = (Layer) layers.get(i);
        }

        capabilities.setLayers(layerArray);

        return capabilities;
    }
}
