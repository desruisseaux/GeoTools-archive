/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2008, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.DataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * {@link DataStore} extension interface to provide WFS specific extra
 * information.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public interface WFSDataStore extends DataStore {
    
    public String getTitle();
    
    /**
     * Provide access to ServiceInfo generated from the wfs capabilities document.
     * 
     * @return ServiceInfo
     */
    public String getTitle(String typeName) throws NoSuchElementException;

    public String getAbstract();
    
    /**
     * 
     * @param typeName
     *            the type name to return the Abstract from.
     * @return
     * @throws NoSuchElementException
     *             if typeName does not correspond to a FeatureType declared in
     *             the WFS capabilities document.
     */
    public String getAbstract(String typeName) throws NoSuchElementException;

    public List<String> getKeywords();
    
    /**
     * The bounds of {@code typeName} in {@code EPSG:4326} as stated in the WFS
     * capabilities document.
     * 
     * @param typeName
     *            the type name to return the WGS84 bounds from.
     * @return
     * @throws NoSuchElementException
     *             if typeName does not correspond to a FeatureType declared in
     *             the WFS capabilities document.
     */
    public ReferencedEnvelope getLatLonBoundingBox(String typeName) throws NoSuchElementException;
    
    public URL getOperation(WFSOperationType operationType, HttpMethod method);
    
    public String getDefaultCrs(String typeName);
    
    //Gonna replace the above metadta fetching methods 
    //ServiceInfo getInfo();
}
