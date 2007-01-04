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
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.geotools.xml.Binding;


public class BinaryComparisonOpTypeBindingTest extends FilterTestSupport {
    public void testBinaryComparisonOpType() {
        assertEquals(BinaryComparisonOperator.class, binding(OGC.BINARYCOMPARISONOPTYPE).getType());
    }

    public void testPropertyIsEqualToType() {
        assertEquals(PropertyIsEqualTo.class, binding(OGC.PROPERTYISEQUALTO).getType());
    }

    public void testPropertyIsEqualToExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.PROPERTYISEQUALTO).getExecutionMode());
    }

    public void testPropertyIsEqualToParse() throws Exception {
        FilterMockData.propertyIsEqualTo(document, document);

        PropertyIsEqualTo equalTo = (PropertyIsEqualTo) parse();
        assertNotNull(equalTo);

        assertNotNull(equalTo.getExpression1());
        assertNotNull(equalTo.getExpression2());
    }

    public void testPropertyIsEqualToEncode() throws Exception {
        PropertyIsEqualTo equalTo = FilterMockData.propertyIsEqualTo();

        Document dom = encode(equalTo, OGC.PROPERTYISEQUALTO);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYNAME.getLocalPart()).getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.LITERAL.getLocalPart()).getLength());
    }

    public void testPropertyIsNotEqualToType() {
        assertEquals(PropertyIsNotEqualTo.class, binding(OGC.PROPERTYISNOTEQUALTO).getType());
    }

    public void testPropertyIsNotEqualToExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.PROPERTYISNOTEQUALTO).getExecutionMode());
    }

    public void testPropertyIsNotEqualToParse() throws Exception {
        FilterMockData.propertyIsNotEqualTo(document, document);

        PropertyIsNotEqualTo equalTo = (PropertyIsNotEqualTo) parse();
        assertNotNull(equalTo);

        assertNotNull(equalTo.getExpression1());
        assertNotNull(equalTo.getExpression2());
    }

    public void testPropertyIsNotEqualToEncode() throws Exception {
        PropertyIsNotEqualTo equalTo = FilterMockData.propertyIsNotEqualTo();

        Document dom = encode(equalTo, OGC.PROPERTYISNOTEQUALTO);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYNAME.getLocalPart()).getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.LITERAL.getLocalPart()).getLength());
    }

    public void testPropertyIsLessThanType() {
        assertEquals(PropertyIsLessThan.class, binding(OGC.PROPERTYISLESSTHAN).getType());
    }

    public void testPropertyIsLessThanExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.PROPERTYISLESSTHAN).getExecutionMode());
    }

    public void testPropertyIsLessThanParse() throws Exception {
        FilterMockData.propertyIsLessThan(document, document);

        PropertyIsLessThan equalTo = (PropertyIsLessThan) parse();
        assertNotNull(equalTo);

        assertNotNull(equalTo.getExpression1());
        assertNotNull(equalTo.getExpression2());
    }

    public void testPropertyIsLessThanEncode() throws Exception {
        PropertyIsLessThan equalTo = FilterMockData.propertyIsLessThan();

        Document dom = encode(equalTo, OGC.PROPERTYISLESSTHAN);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYNAME.getLocalPart()).getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.LITERAL.getLocalPart()).getLength());
    }

    public void testPropertyIsLessThanOrEqualToType() {
        assertEquals(PropertyIsLessThanOrEqualTo.class,
            binding(OGC.PROPERTYISLESSTHANOREQUALTO).getType());
    }

    public void testPropertyIsLessThanOrEqualToExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.PROPERTYISLESSTHANOREQUALTO).getExecutionMode());
    }

    public void testPropertyIsLessThanOrEqualToParse()
        throws Exception {
        FilterMockData.propertyIsLessThanOrEqualTo(document, document);

        PropertyIsLessThanOrEqualTo equalTo = (PropertyIsLessThanOrEqualTo) parse();
        assertNotNull(equalTo);

        assertNotNull(equalTo.getExpression1());
        assertNotNull(equalTo.getExpression2());
    }

    public void testPropertyIsLessThanOrEqualToEncode()
        throws Exception {
        PropertyIsLessThanOrEqualTo equalTo = FilterMockData.propertyIsLessThanOrEqualTo();

        Document dom = encode(equalTo, OGC.PROPERTYISLESSTHANOREQUALTO);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYNAME.getLocalPart()).getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.LITERAL.getLocalPart()).getLength());
    }

    public void testPropertyIsGreaterThanType() {
        assertEquals(PropertyIsGreaterThan.class, binding(OGC.PROPERTYISGREATERTHAN).getType());
    }

    public void testPropertyIsGreaterThanExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.PROPERTYISGREATERTHAN).getExecutionMode());
    }

    public void testPropertyIsGreaterThanParse() throws Exception {
        FilterMockData.propertyIsGreaterThan(document, document);

        PropertyIsGreaterThan equalTo = (PropertyIsGreaterThan) parse();
        assertNotNull(equalTo);

        assertNotNull(equalTo.getExpression1());
        assertNotNull(equalTo.getExpression2());
    }

    public void testPropertyIsGreaterThanEncode() throws Exception {
        PropertyIsGreaterThan equalTo = FilterMockData.propertyIsGreaterThan();

        Document dom = encode(equalTo, OGC.PROPERTYISGREATERTHAN);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYNAME.getLocalPart()).getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.LITERAL.getLocalPart()).getLength());
    }

    public void testPropertyIsGreaterThanOrEqualToType() {
        assertEquals(PropertyIsGreaterThanOrEqualTo.class,
            binding(OGC.PROPERTYISGREATERTHANOREQUALTO).getType());
    }

    public void testPropertyIsGreaterThanOrEqualToExecutionMode() {
        assertEquals(Binding.AFTER, binding(OGC.PROPERTYISGREATERTHANOREQUALTO).getExecutionMode());
    }

    public void testPropertyIsGreaterThanOrEqualToParse()
        throws Exception {
        FilterMockData.propertyIsGreaterThanOrEqualTo(document, document);

        PropertyIsGreaterThanOrEqualTo equalTo = (PropertyIsGreaterThanOrEqualTo) parse();
        assertNotNull(equalTo);

        assertNotNull(equalTo.getExpression1());
        assertNotNull(equalTo.getExpression2());
    }

    public void testPropertyIsGreaterThanOrEqualToEncode()
        throws Exception {
        PropertyIsGreaterThanOrEqualTo equalTo = FilterMockData.propertyIsGreaterThanOrEqualTo();

        Document dom = encode(equalTo, OGC.PROPERTYISGREATERTHANOREQUALTO);
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.PROPERTYNAME.getLocalPart()).getLength());
        assertEquals(1,
            dom.getElementsByTagNameNS(OGC.NAMESPACE, OGC.LITERAL.getLocalPart()).getLength());
    }
}
