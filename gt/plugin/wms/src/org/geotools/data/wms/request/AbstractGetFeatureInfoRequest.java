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
 * @author Richard Gould TODO To change the template for this generated type comment go to Window - Preferences - Java -
 *         Code Style - Code Templates
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
     * Constructs a GetFeatureInfoRequest for use with a 1.0.0 server
     * 
     * @param onlineResource the URL pointing to the place to execute a GetFeatureInfo request
     * @param getMapRequest a previously executed GetMapRequest that the query will be executed on
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

    public URL getFinalURL() {
        Iterator iter = queryLayers.iterator();
        String queryLayerString = "";

        while( iter.hasNext() ) {
            Layer layer = (Layer) iter.next();
            queryLayerString = queryLayerString + layer.getName();

            if (iter.hasNext()) {
                queryLayerString = queryLayerString + ",";
            }
        }

        setProperty("QUERY_LAYERS", queryLayerString);

        return super.getFinalURL();
    }

    public void addQueryLayer( Layer layer ) {
        queryLayers.add(layer);
    }

    public void setQueryLayers( Set layers ) {
        queryLayers = layers;
    }

    public void setInfoFormat( String infoFormat ) {
        setProperty("INFO_FORMAT", infoFormat);
    }

    public void setFeatureCount( String featureCount ) {
        setProperty("FEATURE_COUNT", featureCount);
    }

    public void setFeatureCount( int featureCount ) {
        setFeatureCount(Integer.toString(featureCount));
    }

    public void setQueryPoint( int x, int y ) {
        setProperty(getQueryX(), Integer.toString(x));
        setProperty(getQueryY(), Integer.toString(y));
    }

    protected String getQueryX() {
        return "X";
    }

    protected String getQueryY() {
        return "Y";
    }
    
    public List getInfoFormats() {
        return infoFormats;
    }

    public Set getQueryableLayers() {
        return queryableLayers;
    }

    protected void initRequest() {
        setProperty("REQUEST", "feature_info");
    }
    
    protected abstract void initVersion();
}
