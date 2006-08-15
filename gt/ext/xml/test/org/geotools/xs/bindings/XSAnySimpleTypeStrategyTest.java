package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.geotools.xml.SimpleBinding;
import org.geotools.xs.TestSchema;
import org.geotools.xs.bindings.XS;

public class XSAnySimpleTypeStrategyTest extends TestSchema {

	private XSDSimpleTypeDefinition typeDef;
	private SimpleBinding stratagy;
	
	protected void setUp() throws Exception {
		super.setUp();
		typeDef = xsdSimple( XS.ANYSIMPLETYPE.getLocalPart() );
		stratagy = (SimpleBinding) stratagy( XS.ANYSIMPLETYPE );
	}
	public void testSetUp(){
		assertNotNull( "XSD typedef", typeDef );
		assertNotNull( "found anySimpleType", stratagy );
	}
	public void testAnyTypeParse() throws Exception {
		assertEquals( "  hello world",
				stratagy.parse( element( "  hello world", XS.ANYSIMPLETYPE ), "  hello world" ) );
	}
	public void testHandlingOfWhiteSpace() throws Exception {
		assertEquals( "123",
				stratagy.parse( element( "  123", XS.DECIMAL ), "123" ) );

	}
	protected QName getQName() {
		// TODO Auto-generated method stub
		return null;
	}
}
