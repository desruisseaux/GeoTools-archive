/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.geotools.factory.Factory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * Constructs a live DataStore from a set of parameters.
 *
 * <p>
 * An instance of this interface should exist for all data stores which want to
 * take advantage of the dynamic plugin system. In addition to implementing
 * this interface datastores should have a services file:
 * </p>
 *
 * <p>
 * <code>META-INF/services/org.geotools.data.DataStoreFactorySpi</code>
 * </p>
 *
 * <p>
 * The file should contain a single line which gives the full name of the
 * implementing class.
 * </p>
 *
 * <p>
 * example:<br/><code>e.g.
 * org.geotools.data.mytype.MyTypeDataSourceFacotry</code>
 * </p>
 *
 * <p>
 * The factories are never called directly by client code, instead the
 * DataStoreFinder class is used.
 * </p>
 *
 * <p>
 * The following example shows how a user might connect to a PostGIS database,
 * and maintain the resulting datastore in a registry:
 * </p>
 *
 * <p>
 * <pre><code>
 * HashMap params = new HashMap();
 * params.put("namespace", "leeds");
 * params.put("dbtype", "postgis");
 * params.put("host","feathers.leeds.ac.uk");
 * params.put("port", "5432");
 * params.put("database","postgis_test");
 * params.put("user","postgis_ro");
 * params.put("passwd","postgis_ro");
 *
 * DefaultRegistry registry = new DefaultRegistry();
 * registry.addDataStore("leeds", params);
 *
 * DataStore postgis = registry.getDataStore( "leeds" );
 * FeatureSource = postgis.getFeatureSource( "table" );
 * </code></pre>
 * </p>
 * <h2>
 *
 * <ul>
 * <li>
 * Jody - can we please get something better then Param to describe what is
 * allowed? <br>
 * Jody - ISO19119 has something that looks okay, WSDL/SOAP could be used?
 * </li>
 * <li>
 * Jody - can we seperate out Identification of a Service from configration of
 * the service? <br>
 * Jody - this is mostly a problem when managing user supplied configurations
 * in GeoServer and uDig. <br>
 * Jody - the "Catalog API" has now been ported and contains a URI as
 * indentification, while still allowing configuration using a Map of
 * parameters
 * </li>
 * </ul>
 *
 *
 * @author Jody Garnett, Refractions Research
 * @source $URL$
 */
public interface DataStoreFactorySpi extends DataRepositoryFactory<SimpleFeatureType, SimpleFeature> {

    DataStore createDataStore(Map params) throws IOException;

    DataStore createNewDataStore(Map params) throws IOException;
}
