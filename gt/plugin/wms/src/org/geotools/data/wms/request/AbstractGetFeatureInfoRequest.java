/*
 * Created on 10-Nov-2004 TODO To change the template for this generated file go to Window - Preferences - Java - Code
 * Style - Code Templates
 */
package org.geotools.data.wms.request;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.ows.Layer;

/**
 * A base class for GetFeatureInfoRequests that provides some
 * functionality.
 */
public abstract class AbstractGetFeatureInfoRequest extends AbstractRequest implements GetFeatureInfoRequest {
    /** A list of type String, each representing a format for the request */
    private List infoFormats;

    /**
     * A list of type Layer, each of which has queryable set to true. This is instantiated usually from a
     * GetCapabilities document
     */
    private Set queryableLayers;

    /** A set of type Layer, each of which is to be queried in the request */
    private Set queryLayers;

    /**
     * Constructs a GetFeatureInfoRequest. It will set the REQUEST and VERSION
     * parameters, over-writing and values set there previously.
     * 
     * @param onlineResource the URL pointing to the place to execute a GetFeatureInfo request
     * @param request a previously configured GetMapRequest that the query will be executed on
     * @param queryableLayers a Set of all the Layers that can be queried
     * @param infoFormats all the known formats that can be returned by the request
     */
    public AbstractGetFeatureInfoRequest( URL onlineResource, GetMapRequest request,
            Set queryableLayers, String[] infoFormats ) {
        super(onlineResource, request.getProperties());

        this.queryableLayers = queryableLayers;
        this.infoFormats = Arrays.asList(infoFormats);
        queryLayers = new TreeSet();
        
        initRequest();
        initVersion();
    }

    /**
     * @see org.geotools.data.wms.request.Request#getFinalURL()
     */
    public URL getFinalURL() {
        Iterator iter = queryLayers.iterator();
        String queryLayerString = properties.getProperty(QUERY_LAYERS) == null ? "" : properties.getProperty(QUERY_LAYERS); //$NON-NLS-1$

        while( iter.hasNext() ) {
            Layer layer = (Layer) iter.next();
            queryLayerString = queryLayerString + layer.getName();

            if (iter.hasNext()) {
                queryLayerString = queryLayerString + ","; //$NON-NLS-1$
            }
        }

        setProperty(QUERY_LAYERS, queryLayerString);

        return super.getFinalURL();
    }

    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#addQueryLayer(org.geotools.data.ows.Layer)
     */
    public void addQueryLayer( Layer layer ) {
        queryLayers.add(layer);
    }

    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#setQueryLayers(java.util.Set)
     */
    public void setQueryLayers( Set layers ) {
        queryLayers = layers;
    }

    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#setInfoFormat(java.lang.String)
     */
    public void setInfoFormat( String infoFormat ) {
        setProperty(INFO_FORMAT, infoFormat);
    }

    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#setFeatureCount(java.lang.String)
     */
    public void setFeatureCount( String featureCount ) {
        setProperty(FEATURE_COUNT, featureCount);
    }

    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#setFeatureCount(int)
     */
    public void setFeatureCount( int featureCount ) {
        setFeatureCount(Integer.toString(featureCount));
    }

    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#setQueryPoint(int, int)
     */
    public void setQueryPoint( int x, int y ) {
        setProperty(getQueryX(), Integer.toString(x));
        setProperty(getQueryY(), Integer.toString(y));
    }

    /**
     * Created because the 1.3.0 spec changes this parameter name.
     * The 1.3.0 spec should over-ride this method.
     * @return a String representing the x-axis query point
     */
    protected String getQueryX() {
        return QUERY_X;
    }

    /**
     * Created because the 1.3.0 spec changes this parameter name.
     * The 1.3.0 spec should over-ride this method.
     * @return a String representing the y-axis query point
     */
    protected String getQueryY() {
        return QUERY_Y;
    }
    
    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#getInfoFormats()
     */
    public List getInfoFormats() {
        return infoFormats;
    }

    /**
     * @see org.geotools.data.wms.request.GetFeatureInfoRequest#getQueryableLayers()
     */
    public Set getQueryableLayers() {
        return queryableLayers;
    }

    protected void initRequest() {
        setProperty(REQUEST, "feature_info"); //$NON-NLS-1$
    }
    
    protected abstract void initVersion();
}
