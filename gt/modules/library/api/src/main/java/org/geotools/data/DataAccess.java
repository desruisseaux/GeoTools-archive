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

import java.util.List;
import org.opengis.feature.type.TypeName;
import org.geotools.catalog.ServiceInfo;


/**
 * <p>Interface providing Open Web Service style access to geo resource content.</p>
 *
 * <p>The basic idea is to have simple, very general interface to access and query
 * data that is in some way or another spatially enabled. Extending interfaces can
 * add methods that make it easier to access data for their specific model.</p>
 * <p><em>This should become a super interface of {@link DataStore} and eventually
 * replace it(?).</em></p>
 *
 * @author Jody Garnett
 * @author Thomas Marti
 * @author Stefan Schmid
 *
 * @source $URL$
 * @version $Id$
 */
public interface DataAccess /*<Content,Description>*/ {
    /**
     * Information about this data acess point.
     * <p>
     * Contains a human readable description of the service,
     * with enough information for searching.
     * </p>
     *
     * @return GridServiceInfo ?
     */
    ServiceInfo getInfo();

    /**
     * Names for the content we are providing access to.
     *
     * @return List<TypeName>, may be emtpy, but never null
     */
    List /*<TypeName>*/ getTypeNames();

    /**
     * Description of content in an appropriate format.
     * <ul>
     *   <li>FeatureType: when serving up features</li>
     *   <li>Class: when providing access to a java domain model</li>
     *   <li>URL: of XSD document when working with XML document</li>
     *   <li>etc...</li>
     * </ul>
     * Please note this is a <strong>direct</strong> description of the
     * content, and contains no fluffy human readible concerns (like
     * title) for that kind of information please use {@link #getInfo()}.
     *
     * @see Source#getInfo()
     * @param typeName
     * @return FeatureType, ResultSetMetaData, Class, whatever?
     */
    Object /*Description*/ describe(TypeName typeName);

    /**
     * Provides access to the data source for the given type name.
     *
     * @return Data source, null if typeName is not available
     */
    Source access(TypeName typeName);

    /**
     * Clean up any and all data connections.
     * <p>
     * Please note the <code>DataAccess</code> instance will <b>not</b>
     * be useable after this method is called. All methods should throw
     * an {@link IllegalStateException} (but probably will just throw a
     * {@link NullPointerException} when implementators are lazy).
     *
     */
    void dispose();
}
