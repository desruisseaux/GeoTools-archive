package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.xml.sax.helpers.NamespaceSupport;

import junit.framework.TestCase;


public class XSQNameBindingTest extends TestCase  {

	XSQNameBinding binding;
	
	protected void setUp() throws Exception {
		NamespaceSupport ns = new NamespaceSupport();
		ns.declarePrefix( "foo", "http://foo" );
		
		binding = new XSQNameBinding( ns );
	}
	
	public void testWithPrefix() throws Exception {
		QName qName = (QName) binding.parse( null, "foo:bar" );
		assertNotNull( qName );
		
		assertEquals( "foo", qName.getPrefix() );
		assertEquals( "http://foo", qName.getNamespaceURI() );
		assertEquals( "bar", qName.getLocalPart() );
	}
	
	public void testWithNoPrefix() throws Exception {
		QName qName = (QName) binding.parse( null, "bar:foo" );
		assertNotNull( qName );
		
		assertEquals( "bar", qName.getPrefix() );
		assertEquals( "", qName.getNamespaceURI() );
		assertEquals( "foo", qName.getLocalPart() );
	}
}
