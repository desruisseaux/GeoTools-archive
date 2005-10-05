package org.geotools.feature.type;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.Filter;

/**
 * Sanity tests that ensures the complex type business driver examples can be
 * created using the new typing system.
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class BusinessDriverExamplesTest extends ComplexTestData {

	private TypeFactory typeFactory;

	private DescriptorFactory descFactory;

	protected void setUp() throws Exception {
		super.setUp();
		typeFactory = new TypeFactoryImpl();
		descFactory = new DescriptorFactoryImpl();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		typeFactory = null;
		descFactory = null;
	}

	/**
	 * Test the appliance of a GeoTools complex FeatureType to mirror the
	 * following sample GML schema:
	 * 
	 * <pre>
	 * <code>
	 *   &lt;xs:complexType xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot; name=&quot;wq_plus_Type&quot;&gt;
	 * 	  &lt;xs:complexContent&gt;
	 * 	   &lt;xs:extension base=&quot;gml:AbstractFeatureType&quot;&gt;
	 * 	    &lt;xs:sequence&gt;
	 * 	     &lt;xs:element name=&quot;sitename&quot; minOccurs=&quot;1&quot; nillable=&quot;false&quot; type=&quot;xs:string&quot; /&gt;
	 * 	     &lt;xs:element name=&quot;anzlic_no&quot; minOccurs=&quot;0&quot; nillable=&quot;true&quot; type=&quot;xs:string&quot; /&gt;
	 * 	     &lt;xs:element name=&quot;location&quot; minOccurs=&quot;0&quot; nillable=&quot;true&quot; type=&quot;gml:LocationPropertyType&quot; /&gt;
	 * 	     &lt;xs:element name=&quot;measurement&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot; nillable=&quot;true&quot;&gt;
	 * 	      &lt;xs:complexType&gt;
	 * 	       &lt;xs:sequence&gt;
	 * 	        &lt;xs:element name=&quot;determinand_description&quot; type=&quot;xs:string&quot; minOccurs=&quot;1&quot;/&gt;
	 * 	        &lt;xs:element name=&quot;result&quot; type=&quot;xs:string&quot; minOccurs=&quot;1&quot;/&gt;
	 * 	       &lt;/xs:sequence&gt;
	 * 	       &lt;xs:attribute ref=&quot;gml:id&quot; use=&quot;optional&quot;/&gt;
	 * 	      &lt;/xs:complexType&gt; 
	 * 	     &lt;/xs:element&gt;
	 * 	     &lt;xs:element name=&quot;project_no&quot; minOccurs=&quot;0&quot; nillable=&quot;true&quot; type=&quot;xs:string&quot; /&gt;
	 * 	    &lt;/xs:sequence&gt;
	 * 	   &lt;/xs:extension&gt;
	 * 	  &lt;/xs:complexContent&gt;
	 * 	 &lt;/xs:complexType&gt;
	 * 	 
	 * 	 &lt;xs:element name='wq_plus' type='sco:wq_plus_Type' substitutionGroup=&quot;gml:_Feature&quot; /&gt;
	 * 
	 * </code>
	 * </pre>
	 */
	public void test01SingleRepeatedProperty() {
		final QName name = new QName(NSURI, "wq_plus");
		final Class binding = Feature.class;
		final Set<Filter> restrictions = Collections.emptySet();
		final boolean identified = true;
		final boolean isAbstract = false;
		final AttributeType superType = null;
		final boolean nillable = false;

		FeatureType wqPlusType = ComplexTestData.createExample01Type(
				typeFactory, descFactory);

		checkType(wqPlusType, name, binding, restrictions, identified,
				isAbstract, superType, nillable);

		assertNotNull(wqPlusType.getDescriptor());
		assertTrue(wqPlusType.getDescriptor() instanceof OrderedDescriptor);
		OrderedDescriptor schema = (OrderedDescriptor) wqPlusType
				.getDescriptor();
		assertEquals(1, schema.getMinOccurs());
		assertEquals(1, schema.getMaxOccurs());

		List<Descriptor> contents = schema.sequence();
		assertNotNull(contents);
		assertEquals(5, contents.size());
		final String[] names = { "sitename", "anzlic_no", "location",
				"measurement", "project_no" };
		final int[][] multiplicities = { { 1, 1 }, { 0, 1 }, { 0, 1 },
				{ 0, Integer.MAX_VALUE }, { 0, 1 } };
		int i = 0;
		for (Descriptor attDesc : contents) {
			assertTrue(attDesc instanceof AttributeDescriptor);
			AttributeDescriptor att = (AttributeDescriptor) attDesc;
			assertNotNull(att.getType());
			assertEquals(NSURI, att.getType().getName().getNamespaceURI());
			assertEquals(names[i], att.getType().name());
			assertEquals(multiplicities[i][0], att.getMinOccurs());
			assertEquals(multiplicities[i][1], att.getMaxOccurs());
			i++;
		}

		assertNotNull(wqPlusType.getDefaultGeometry());
		assertEquals("location", wqPlusType.getDefaultGeometry().name());
	}
}
