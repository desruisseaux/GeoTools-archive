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
package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xs.TestSchema;


public class XSLongStrategyTest extends TestSchema {
    /**
     * long has a lexical representation consisting of an
     * optional sign followed by a finite-length sequence
     * of decimal digits (#x30-#x39). If the sign is omitted,
     * "+" is assumed.
     *
     * For example: -1, 0, 12678967543233, +100000.
     * @throws Exception
     *
     */

    /*
     * Test method for 'org.geotools.xml.strategies.xs.XSLongStrategy.parse(Element, Node[], Object)'
     */
    public void testParse() throws Exception {
        validateValues("-1", new Long(-1));
        validateValues("0", new Long(0));
        validateValues("12678967543233", new Long(12678967543233L));
        validateValues("+100000", new Long(100000));
    }

    protected QName getQName() {
        return XS.LONG;
    }
}
