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
package org.geotools.xml.impl.jxpath;

import junit.framework.TestCase;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextFactoryReferenceImpl;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.geotools.xml.impl.ElementHandlerImpl;
import org.geotools.xml.impl.ElementImpl;
import java.util.Iterator;


public class JXPathTest extends TestCase {
    ElementHandlerImpl root;
    ElementHandlerImpl child1;
    ElementHandlerImpl child2;
    JXPathContext context;

    protected void setUp() throws Exception {
        ElementImpl e = new ElementImpl(null);
        e.setName("root");
        root = new ElementHandlerImpl(null, null, null);
        root.setComponent(e);

        e = new ElementImpl(null);
        e.setName("child");

        child1 = new ElementHandlerImpl(null, root, null);
        child1.setComponent(e);

        e = new ElementImpl(null);
        e.setName("child");
        child2 = new ElementHandlerImpl(null, root, null);
        child2.setComponent(e);

        JXPathIntrospector.registerDynamicClass(ElementHandlerImpl.class,
            ElementHandlerPropertyHandler.class);
        //JXPathContextReferenceImpl.addNodePointerFactory(new ElementHandlerNodePointerFactory());
        context = JXPathContextFactory.newInstance().newContext(null, root);
    }

    public void testIterate() {
        root.getChildHandlers().add(child1);
        root.getChildHandlers().add(child2);

        Iterator itr = context.iterate("child");
        assertTrue(itr.hasNext());
        assertEquals(child1, itr.next());
        assertTrue(itr.hasNext());
        assertEquals(child2, itr.next());

        itr = context.iterate("child[position() = 2]");
        assertTrue(itr.hasNext());
        assertEquals(child2, itr.next());

        assertFalse(itr.hasNext());

        //		
        //		assertTrue(itr.hasNext());
        //		assertEquals(itr.next(), child2);
    }

    //	
    //	public void testSimple() {
    //		root.getChildHandlers().add(child1);
    //		root.getChildHandlers().add(child2);
    //		
    //		Object obj = context.getValue("child1");
    //		assertNotNull(obj);
    //		assertEquals(obj, child1);
    //		
    //		obj = context.getValue("child2");
    //		assertNotNull(obj);
    //		assertEquals(obj, child2);
    //	}
    //	
    //	public void testDynamic() {
    //		root.getChildHandlers().add(child1);
    //		
    //		Object obj = context.getValue("child1");
    //		assertNotNull(obj);
    //		assertEquals(obj, child1);
    //		
    //		root.getChildHandlers().add(child2);
    //		obj = context.getValue("child2");
    //		assertNotNull(obj);
    //		assertEquals(obj, child2);
    //	}
}
