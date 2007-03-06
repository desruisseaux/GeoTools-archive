/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.epsg;

import java.net.URL;

/**
 * Provides common {@linkplain CoordinateReferenceSystem Coordinate Reference Systems}
 * not found in the standard EPSG database. Those CRS will be registered in 
 * {@code "ESRI"} name space.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Andrea Aime
 */
public class FactoryEPSGExtension extends FactoryUsingWKT {

    protected URL getDefinitionsURL() {
        return FactoryUsingWKT.class.getResource("epsg-extension.properties");
    }
}
