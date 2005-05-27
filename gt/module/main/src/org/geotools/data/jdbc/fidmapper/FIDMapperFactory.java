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
package org.geotools.data.jdbc.fidmapper;

import org.geotools.feature.FeatureType;
import java.io.IOException;
import java.sql.Connection;


/**
 * Provides a way to plug in user defined policies for primary key to feature
 * ID mapping. In particular, implementors of this interface will provide a
 * FIDMapper given the FeatureType name and the database metadata
 *
 * @author aaime
 */
public interface FIDMapperFactory {
    /**
     * Returns a FIDMapper for the specified table
     *
     * @param catalog
     * @param schema
     * @param typeName DOCUMENT ME!
     * @param connection DOCUMENT ME!
     *
     * @return
     *
     * @throws IOException
     */
    public FIDMapper getMapper(String catalog, String schema, String typeName,
        Connection connection) throws IOException;

    /**
     * Returns a FIDMapper for the specified feature type. This one is called
     * when creating new tables if the user did not provide a FIDMapper
     *
     * @param featureType
     *
     * @return
     */
    FIDMapper getMapper(FeatureType featureType);
}
