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
package org.geotools.filter.v1_1;

import org.w3c.dom.Document;
import org.opengis.filter.And;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Or;
import org.geotools.xml.Binding;


public class BinaryLogicOpTypeBindingTest extends FilterTestSupport {
    public void testBinaryLogicOpType() {
        assertEquals(BinaryLogicOperator.class, binding(OGC.BINARYLOGICOPTYPE).getType());
    }

    public void testAndType() {
        assertEquals(And.class, binding(OGC.AND).getType());
    }

    public void testAndExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.AND).getExecutionMode());
    }

    public void testAndParse() throws Exception {
        FilterMockData.and(document, document);

        And and = (And) parse();

        assertEquals(2, and.getChildren().size());
    }

    public void testAndEncode() throws Exception {
        Document dom = encode(FilterMockData.and(), OGC.AND);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYISEQUALTO.getLocalPart())
               .getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYISNOTEQUALTO.getLocalPart())
               .getLength());
    }

    public void testOrType() {
        assertEquals(Or.class, binding(OGC.OR).getType());
    }

    public void testOrExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.OR).getExecutionMode());
    }

    public void testOrParse() throws Exception {
        FilterMockData.or(document, document);

        Or or = (Or) parse();

        assertEquals(2, or.getChildren().size());
    }

    public void testOrEncode() throws Exception {
        Document dom = encode(FilterMockData.or(), OGC.OR);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYISEQUALTO.getLocalPart())
               .getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYISNOTEQUALTO.getLocalPart())
               .getLength());
    }
}
