package org.geotools.feature.type;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;

import junit.framework.TestCase;

/**
 * Sanity tests that ensures the complex type business
 * driver examples can be created using the new typing system.
 *  
 * @author Gabriel Roldan, Axios Engineering
 */
public class BusinessDriverExamplesTest extends TestCase {

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
	 * sample GML schema at 
FeatureType[
	name = wq_plus
	identified = true
	super = Feature
	abstract = false
	binding = Feature.class
	restrictions = EMPTY_SET
	nillable = false
	defaultGeometry = #location
	descriptor = OrderedDescriptor(1, 1)[
		sequence = List[
			AttributeDescriptor(1, 1)[
				type = AttributeType[
					name = sitename
					identified = false
					super = null (?????????)
					abstract = false
					binding = String.class
					restrictions = EMPTY_SET
					nillable = false
				]
			],
			AttributeDescriptor(0, 1)[
				type = AttributeType[
					name = anzlic_no
					identified = false
					super = null (?????????)
					abstract = false
					binding = String.class
					restrictions = EMPTY_SET
					nillable = true
				]
			],
			AttributeDescriptor(0, 1)[
				type = GeometryAttribute[
					name = location
					identified = false
					super = HERE WE NEED TO REFER TO  gml:LocationPropertyType
					abstract = false
					binding = Point.class
					restrictions = EMPTY_SET
					nillable = true
				]
			],
			AttributeDescriptor (0, Integer.MAX_VALUE)[
				type = ComplexType[
					name = measurement
					identified = true
					super = null (?????????????????)
					abstract = false
					binding = null
					restrictions = EMPTY_SET
					nillable = true
					descriptor = OrderedDescriptor(0, Integer.MAX_VALUE)[
						AttributeDescriptor(1, 1)[
							type = AttributeType[
								name = determinand_description
								identified = false
								super = null (?????????)
								abstract = false
								binding = String.class
								restrictions = EMPTY_SET
								nillable = false
							]
						],
						AttributeDescriptor(1, 1)[
							type = AttributeType[
								name = result
								identified = false
								super = null (?????????)
								abstract = false
								binding = String.class
								restrictions = EMPTY_SET
								nillable = false
							]
						]
					]
				]
			], //measurement
			AttributeDescriptor(0, 1)[
				type = AttributeType[
					name = project_no
					identified = false
					super = null (?????????)
					abstract = false
					binding = String.class
					restrictions = EMPTY_SET
					nillable = false
				]
			]
    	]
	]
]	 
	 */
	public void test01SingleRepeatedProperty(){
		FeatureType wqPlusType;
		final QName name = new QName("http://online.socialchange.net.au", "wq_plus");
		final Descriptor schema;
		final AttributeType defaultGeom = null;
		
		List<Descriptor> content = new ArrayList<Descriptor>();
		
		AttributeType sitename = typeFactory.createType("sitename", String.class);
		content.add(descFactory.node(sitename, 0, Integer.MAX_VALUE));

		schema = descFactory.ordered(content, 1, 1);
		wqPlusType = typeFactory.createFeatureType(name, schema, defaultGeom);
		
		assertEquals(Feature.class, wqPlusType.getBinding());
		assertEquals(defaultGeom, wqPlusType.getDefaultGeometry());
		assertEquals(name, wqPlusType.getName());
		assertNotNull(wqPlusType.getRestrictions());
		assertEquals(0, wqPlusType.getRestrictions().size());
		assertNotNull(wqPlusType.getSuper());
	}
}
