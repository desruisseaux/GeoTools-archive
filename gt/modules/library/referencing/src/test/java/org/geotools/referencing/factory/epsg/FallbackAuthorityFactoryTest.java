/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;

/**
 * Checks the exception thrown by the fallback system do report actual errors when the code is
 * avaialable but for some reason broken, and not "code not found" ones
 * @author Administrator
 *
 */
public class FallbackAuthorityFactoryTest extends TestCase {
    
    public void setUp() {
        FactoryFinder.addAuthorityFactory(new FactoryESPGExtra());
        FactoryFinder.scanForPlugins();
    }
    
    public void testFactoryOrdering() {
        Set factories =  FactoryFinder.getCRSAuthorityFactories(null);
        boolean foundWkt = false;
        boolean foundExtra = false;
        for (Iterator it = factories.iterator(); it.hasNext();) {
            CRSAuthorityFactory factory = (CRSAuthorityFactory) it.next();
            if(factory.getClass() == FactoryESPGExtra.class) {
                foundExtra = true;
            } else if(factory.getClass() == FactoryUsingWKT.class) {
                foundWkt = true;
                if(!foundExtra)
                    fail("We should have encountered WKT factory before the extra one");
            }
        }
        assertTrue(foundWkt);
        assertTrue(foundExtra);
    }
    
    /**
     * Tests the {@code 00001} fake code.
     */
    public void test00001() throws FactoryException {
        try {
            CRS.decode("EPSG:00001");
            fail("This code should not be there");
        } catch(NoSuchAuthorityCodeException e) {
            fail("The code 00001 is there, exception should report it's broken");
        } catch(FactoryException e) {
            // cool, that's what we expected
        }
    }
    
    /**
     * Extra class used to make sure we have FactoryUsingWKT among the fallbacks
     * (used to check the fallback mechanism)
     * @author Andrea Aime, TOPP
     *
     */
    public static class FactoryESPGExtra extends FactoryUsingWKT {
        
        public FactoryESPGExtra() {
            // make sure we are after FactoryUsingWKT in the fallback chain
            super(null, FactoryUsingWKT.DEFAULT_PRIORITY + 5);
        }

        protected URL getDefinitionsURL() {
            return FactoryUsingWKT.class.getResource("epsg2.properties");
        }
    }
}
