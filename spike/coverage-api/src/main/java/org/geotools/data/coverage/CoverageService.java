/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.data.coverage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.geotools.data.ServiceInfo;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 * Interface to implement a service for a certain flavors of coverage.
 * 
 * <p>
 * Classes implementing this interface basically act as factory for creating
 * connections to coverage sources like files, WCS services, WMS services, etc.
 * 
 * <p>
 * Purpose of this class is to provide basic information about a certain
 * coverage service as well as the parameters needed in order to connect to a
 * source as well as the default parameters to access/manipulate the single
 * coverage a source can serve.
 * 
 * <p>
 * Notice that as part as the "factory" interface this class makes available an
 * {@link #isAvailable()} method which should check if all the needed
 * dependencies which can be jars as well as native libs or configuration files.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @since 2.5
 * 
 * @todo TODO {@link Name} vs {@link InternationalString}
 * @todo TODO {@link Param} array vs {@link Map}
 * 
 */
public interface CoverageService {
	
	/**
	 * Retrieves basic information about this {@link CoverageService}. 
	 * 
	 * <p>
	 * 
	 * @param monitor {@link ProgressListener} class to monitor the progress of this request which could be blocking.
	 * @return an instance of the {@link ServiceInfo} class describing this {@link CoverageService}.
	 * @throws IOException
	 */
	ServiceInfo getInfo(ProgressListener monitor) throws IOException;

    /**
     * Describe the nature of the {@link CoverageDataStore} constructed by this factory.
     *
     * <p>
     * A description of this data store type.
     * </p>
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    Name getDescription();

    /**
	 * Name suitable for display to end user.
	 * 
	 * <p>
	 * A display name for this data store type. It is highly recommended that it
	 * is unique since it is expected that users will use it to uniquely refer
	 * to {@link CoverageService} instance.
	 * </p>
	 * 
	 * @return A short name suitable for display in a user interface.
	 */
    Name getName();

    /**
	 * Test to see if this {@link CoverageService} is available, if it has all
	 * the appropriate libraries. One may ask how this is different than
	 * canProcess, and basically available is used by the DataStoreFinder
	 * getAvailableDataStore method, so that DataStores that can not even be
	 * used do not show up as options in gui applications.
	 * 
	 * @return <tt>true</tt> if and only if this factory has all the
	 *         appropriate dependencies on the classpath to create DataStores.
	 */
    boolean isAvailable();

    /**
	 * MetaData describing the required Parameters for opening to a certain
	 * {@link CoverageDataStore}.
	 * 
	 * <p>
	 * 
     *
     * @return Param array describing the {@link Map} for {@link #connect(Map)}.
	 */
    Param[] getDefaultConnectionParameters();
    
    /**
     * 
     * @param accessType
     * @return
     */
    Param[] getDefaultAccessParameters(CoverageDataStore.AccessType accessType);

    /**
     * Open up a connection to a {@link CoverageDataStore}.
     * 
     * @param params required {@link Param}s to connect to a certain {@link CoverageStore}
     * @return a {@link CoverageDataStore} which 
     * @throws IOException
     */
	public CoverageDataStore connect(Map<String, Serializable> params) throws IOException;


	/**
	 * Test to see if this factory is suitable for processing the data pointed
	 * to by the params map.
	 *
	 * <p>
	 * If this datasource requires a number of parameters then this mehtod
	 * should check that they are all present and that they are all valid. If
	 * the datasource is a file reading data source then the extentions or
	 * mime types of any files specified should be checked. For example, a
	 * Shapefile datasource should check that the url param ends with shp,
	 * such tests should be case insensative.
	 * </p>
	 *
	 * @param params The full set of information needed to construct a live
	 *        data source.
	 *
	 * @return boolean true if and only if this factory can process the resource
	 *         indicated by the param set and all the required params are
	 *         pressent.
	 */
	boolean canConnect(java.util.Map<String, Serializable> params);
}
