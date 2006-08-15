package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

public class GMLNullTypeBindingTest extends AbstractGMLBindingTest {

	ElementInstance nul;
	
	protected void setUp() throws Exception {
		super.setUp();
		nul = createElement(GML.NAMESPACE,"myNull",GML.NULLTYPE,null);
	}
	
	public void testAllowable() throws Exception {
		Node node = createNode(nul,null,null,null,null);
		
		GMLNullTypeBinding s = (GMLNullTypeBinding)getBinding(GML.NULLTYPE);
		
		 assertEquals("inapplicable", s.parse(nul,"inapplicable"));
		 assertEquals("unknown", s.parse(nul,"unknown"));
		 assertEquals("unavailable", s.parse(nul,"unavailable"));
		 assertEquals("missing", s.parse(nul,"missing"));
	}
}
