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
package org.geotools.data.wms.request;

import java.util.List;
import java.util.Set;

import org.geotools.data.ows.Layer;



/**
 * Information required for a GetFeatureInfo request.
 * 
 * <p>
 * Q: queryableLayers is a Set - is this true? Or is order important Q:
 * infoFormats - what does this do? Do these match up with querableLayers? Or
 * is it a list of formats our client is willing to understand?
 * </p>
 *
 * @author Richard Gould, Refractions Research
 */
public interface GetFeatureInfoRequest extends Request{
    
    /**
     * An unordered set of type Layer. Each layer will be queried in the
     * request
     *
     * @param layers A Set of type Layer, each to be queried
     */
    public void setQueryLayers(Set layers);
    
    public void addQueryLayer(Layer layer);
    
    public void setInfoFormat(String infoFormat);
    
    public void setFeatureCount(String featureCount);
    
    public void setFeatureCount(int featureCount);
    
    public void setQueryPoint(int x, int y);
    
    public List getInfoFormats();
    
    public Set getQueryableLayers();
}
