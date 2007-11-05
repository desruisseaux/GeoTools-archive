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
package org.geotools.wfs.bindings;

import net.opengis.wfs.XlinkPropertyNameType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.math.BigInteger;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;


/**
 *
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public class _XlinkPropertyNameBindingTest extends WFSTestSupport {
    public _XlinkPropertyNameBindingTest() {
        super(WFS._XlinkPropertyName, XlinkPropertyNameType.class, Binding.OVERRIDE);
    }

    public void testEncode() throws Exception {
        XlinkPropertyNameType xlink = factory.createXlinkPropertyNameType();
        xlink.setTraverseXlinkDepth("1");
        xlink.setTraverseXlinkExpiry(BigInteger.valueOf(10));
        xlink.setValue("gt:propertyC/gt:propertyD");

        Document dom = encode(xlink, WFS.XlinkPropertyName);
        Element root = dom.getDocumentElement();

        assertName(WFS.XlinkPropertyName, root);
        assertEquals("1", root.getAttribute("traverseXlinkDepth"));
        assertEquals("10", root.getAttribute("traverseXlinkExpiry"));
        assertEquals("gt:propertyC/gt:propertyD", root.getFirstChild().getNodeValue());
    }

    public void testParse() throws Exception {
        final String xml = "<XlinkPropertyName traverseXlinkDepth=\"1\" "
            + "traverseXlinkExpiry=\"10\">gt:propertyC/gt:propertyD</XlinkPropertyName>";

        buildDocument(xml);

        Object parsed = parse();
        assertTrue(parsed instanceof XlinkPropertyNameType);

        XlinkPropertyNameType xlink = (XlinkPropertyNameType) parsed;
        assertEquals("1", xlink.getTraverseXlinkDepth());
        assertEquals(BigInteger.valueOf(10), xlink.getTraverseXlinkExpiry());
        assertEquals("gt:propertyC/gt:propertyD", xlink.getValue());
    }
}
