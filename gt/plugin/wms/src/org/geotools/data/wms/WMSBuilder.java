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
    private List srss;
    private List styles;
    private HashMap bboxes;

    public WMSBuilder() {
        layers = new ArrayList();
        request = new WMSRequest();
    }

    public void buildCapabilities(String version) {
        capabilities = new WMSCapabilities();
        capabilities.setVersion(version);
    }

    public void buildService(String name, String title, URL onlineResource,
        String _abstract, String[] keywords) {
        service = new Service();
        service.setName(name);
        service.setTitle(title);
        service.setOnlineResource(onlineResource);
        service.set_abstract(_abstract);
        service.setKeywordList(keywords);
    }

    /**
     * Build description of GetCapabilities operation.
     *
     * @param formats List<String> of available formats
     * @param get
     * @param post
     */
    public void buildGetCapabilitiesOperation(List formats, URL get, URL post) {
        getCapabilities = new WMSOperationType();
        buildWMSOperationType(getCapabilities, formats, get, post);
    }

    public void buildGetMapOperation(List formats, URL get, URL post) {
        getMap = new WMSOperationType();
        buildWMSOperationType(getMap, formats, get, post);
    }

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

    public void buildLayer(String title, String name, boolean queryable,
        String parentLayerTitle, List srss, List styles) {
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
