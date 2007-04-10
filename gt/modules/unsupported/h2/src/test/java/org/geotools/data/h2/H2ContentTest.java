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

import java.util.List;
import org.geotools.feature.type.TypeName;


public class H2ContentTest extends H2TestSupport {
    H2Content content;

    protected void setUp() throws Exception {
        super.setUp();

        content = (H2Content) dataStore.getContent();
    }

    public void testGetTypeNames() throws Exception {
        List typeNames = content.getTypeNames();
        assertEquals(1, typeNames.size());

        TypeName typeName = (TypeName) typeNames.get(0);
        assertEquals("featureType1", typeName.getLocalPart());
    }
}
