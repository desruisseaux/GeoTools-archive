/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geotools.resources.TestData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

/**
 * @author jeichar
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class XPathFactoryTest extends TestCase {

    Document root;

    XPath xpath;

    String pathString = "doc/child1/Child2";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        File f = TestData.file(this, "test.xml");

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fac.newDocumentBuilder();
        root = builder.parse(f);
    }

    public void testCreateXPath() {
        xpath = XPathFactory.createXPath("doc", root.getClass());
        assertNotNull(xpath);
        assertEquals("doc", ((Pattern)xpath.getTerms()[0]).pattern());
    }

    public void testFind() {
        List list = XPathFactory.find(pathString, root);

        assertEquals(2, list.size());
        assertEquals("Child2", ((Node) list.get(0)).getNodeName());
        assertEquals("Child2", ((Node) list.get(1)).getNodeName());
    }

    public void testValue() {
        List list = XPathFactory.value(pathString, root);

        assertEquals(2, list.size());
        assertEquals("value1", (String) list.get(0));
        assertEquals("value2", (String) list.get(1));
    }

    public void testNodePaths() {
        List list = XPathFactory.nodePaths(pathString, root);

        assertEquals(2, list.size());
        assertEquals(4, ((List) list.get(0)).size());
        assertEquals(4, ((List) list.get(1)).size());
    }
    
    
    public void testFindStringListClass() {
        List elem=new ArrayList();
        Element e=root.getDocumentElement();
        NodeList nl=e.getElementsByTagName("Child2");
        for (int i = 0; i < nl.getLength(); i++) {
            elem.add(nl.item(i));
        }
        List list = XPathFactory.find("Child2", elem, root.getClass());

        assertEquals(2, list.size());
        assertEquals("Child2", ((Node) list.get(0)).getNodeName());
        assertEquals("Child2", ((Node) list.get(1)).getNodeName());
        
        elem.add("This is not a legal node");
        list = XPathFactory.find("Child2", elem, root.getClass());
        assertNull(list);
    }

    public void testValueStringListClass() {
        List elem=new ArrayList();

        Element e=root.getDocumentElement();
        NodeList nl=e.getElementsByTagName("Child2");
        for (int i = 0; i < nl.getLength(); i++) {
            elem.add(nl.item(i));
        }
        List list = XPathFactory.value("Child2", elem, root.getClass());

        assertEquals(2, list.size());
        assertEquals("value1", (String) list.get(0));
        assertEquals("value2", (String) list.get(1));
        
        elem.add("This is not a legal node");
        list = XPathFactory.find("Child2", elem, root.getClass());
        assertNull(list);
    }

    public void testNodePathsStringListClass() {
        List elem=new ArrayList();
        Element e=root.getDocumentElement();
        NodeList nl=e.getElementsByTagName("Child2");
        for (int i = 0; i < nl.getLength(); i++) {
            elem.add(nl.item(i));
        }
        List list = XPathFactory.nodePaths("Child2", elem, root.getClass());

        assertEquals(2, list.size());
        assertEquals(1, ((List) list.get(0)).size());
        assertEquals(1, ((List) list.get(1)).size());
        
        elem.add("This is not a legal node");
        list = XPathFactory.find("Child2", elem, root.getClass());
        assertNull(list);
    }

}