/*
 * Created on Jul 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.xml;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jones
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DOMXPathTest extends TestCase {

	Document root;
	XPath xpath;
	
	String pathString="doc/child1/Child2";
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		File f=TestData.file(this, "test.xml");

		DocumentBuilderFactory fac=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder=fac.newDocumentBuilder();
		root=builder.parse(f);
		xpath=XPathFactory.createXPath("doc", root);
	}

	
	public static List find(String path, Object root) {
		XPath xpath = XPathFactory.createXPath(path, root);
		return xpath.find(root);
	}

	public static List value(String xpath, Object root) {
		XPath path = XPathFactory.createXPath(xpath, root);
		return path.value(root);
	}

	public static List nodePaths(String xpath, Object root) {
		XPath path = XPathFactory.createXPath(xpath, root);
		return path.nodePaths(root);
	}
	
	public void testDOMXPath() {
		XPath x=XPathFactory.createXPath("doc", root.getDocumentElement());
		assertNotNull(x);
		assertEquals("doc",((Pattern)x.getTerms()[0]).pattern());
		assertEquals("doc",x.toString());
		
		x=XPathFactory.createXPath("", root);
		assertNotNull(x);
		assertEquals("",((Pattern)x.getTerms()[0]).pattern());
		assertEquals("",x.toString());
		
		x=XPathFactory.createXPath("doc", null);
		assertNull(x);
	}

	public void testNodeMatch() {

		boolean match=xpath.nodeMatch(Pattern.compile("doc"),root.getDocumentElement());
		assertTrue(match);
		
		assertTrue(root.hasChildNodes());
		
		NodeList children=root.getElementsByTagName("child1");
		Node child=children.item(0);
		String name=child.getNodeName();
		
		match=xpath.nodeMatch(Pattern.compile("child1"),child);
		assertTrue(match);
		
		match=xpath.nodeMatch(Pattern.compile("child2"),child);
		assertFalse(match);
		
		boolean e=false;
		try{
		    xpath.nodeMatch(Pattern.compile("doc"),null);
		}catch(Exception e2){ e=true; }
		
		assertTrue(e);
		
	}


    public void testGetChildren() {
    	Iterator i=xpath.getChildren(root);
    	
    	assertTrue(i.hasNext());
    	
    	Object doc=i.next();
    	
    	assertTrue( xpath.nodeMatch(Pattern.compile("doc"), doc));
    	
    	assertFalse( i.hasNext() );

		boolean e=false;
		try{
		    xpath.getChildren(null);
		}catch(Exception e2){ e=true; }
		
		assertTrue(e);    

		e=false;
		try{
		    xpath.getChildren("Hello");
		}catch(Exception e2){ e=true; }
		
		assertTrue(e);    
		
		i=xpath.getChildren(doc);
		
		int j=0;
		for( ; i.hasNext();j++ )
		    doc=i.next();
		
		assertEquals(1,j);
    
		i=xpath.getChildren(doc);
		
		j=0;
		for( ; i.hasNext();j++ )
		    doc=i.next();
		
		assertEquals(2,j);
    
    }


	/*
	 * Class under test for List find()
	 */
	public void testFind() {
		List list=xpath.find(root);
		assertEquals(1, list.size());
		
		Node n=(Node)list.get(0);
		
		assertTrue(n.getNodeName().equals("doc"));
		
		xpath=XPathFactory.createXPath("doc/child1/Child2", root);
		
		list = xpath.find(root);
		
		assertEquals(2,list.size());
		
		xpath=XPathFactory.createXPath("", root);
		
		list = xpath.find(root);
		
		assertEquals(0, list.size());
	}


	/*
	 * Class under test for List getNodePaths()
	 */
	public void testNodePaths() {
	    List list = xpath.nodePaths(root);
		
		assertEquals(1, list.size());
		Object o=list.get(0);
		assertTrue( o instanceof List );
		list=(List)o;
		
		assertEquals(2, list.size() );
		o=list.get(1);
		assertTrue( o instanceof Element );
		Element e=(Element) o;
		assertTrue( e.getNodeName().equals("doc"));
		
		xpath = XPathFactory.createXPath(pathString, root);
		list = xpath.nodePaths(root);
		
		assertEquals(2, list.size());

		o=list.get(0);
		assertTrue( o instanceof List );
		List sublist=(List)o;
		assertEquals(4, sublist.size() );
		
		o=sublist.get(1);
		assertTrue( o instanceof Element );
		e=(Element) o;
		assertTrue( e.getNodeName().equals("doc"));
		
		e=(Element)sublist.get(2);
		assertTrue( e.getNodeName().equals("child1"));
		e=(Element)sublist.get(3);
		assertTrue( e.getNodeName().equals("Child2"));
		
		o=list.get(1);
		assertTrue( o instanceof List );
		sublist=(List)o;
		assertEquals(4, sublist.size() );
		
		o=sublist.get(1);
		assertTrue( o instanceof Element );
		e=(Element) o;
		assertTrue( e.getNodeName().equals("doc"));
		
		e=(Element)sublist.get(2);
		assertTrue( e.getNodeName().equals("child1"));
		e=(Element)sublist.get(3);
		assertTrue( e.getNodeName().equals("Child2"));
		
	}

    public void testSolve() {
    	List list=xpath.nodePaths(root);
    	String value=(String) xpath.solve((List)list.get(0));
    	assertEquals("", value);
    	
        xpath=XPathFactory.createXPath(pathString, root);
        list=xpath.nodePaths(root);
        Object o=list.get(0);
        List resultpath=(List)o;
		value = (String) xpath.solve(resultpath);
		assertEquals("value1", value);
		resultpath=(List)list.get(1);
		value = (String) xpath.solve(resultpath);
		assertEquals("value2", value);
		
	}

	/*
	 * Class under test for List getValue()
	 */
	public void testValue() {
		List value=xpath.value(root);
		assertEquals(1, value.size());
		String s=(String)value.get(0);
		assertEquals("",s);
		
		xpath=new DOMXPath(pathString);
		value=xpath.value(root);
		assertEquals(2, value.size());
		assertTrue(value.get(0) instanceof String );
		s=(String)value.get(0);
		assertEquals("value1",s);
		s=(String)value.get(1);
		assertEquals("value2",s);
	}
}
