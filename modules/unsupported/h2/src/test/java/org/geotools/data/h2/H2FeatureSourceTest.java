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
package org.geotools.data.h2;

import org.geotools.jdbc.JDBCFeatureSourceTest;
import org.geotools.jdbc.JDBCTestSetup;


/**
 * FeatureSource<SimpleFeatureType, SimpleFeature> test for H2.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2FeatureSourceTest extends JDBCFeatureSourceTest {
    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }
}
