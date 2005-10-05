package org.geotools.feature.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.filter.Filter;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Point;

public abstract class ComplexTestData extends TestCase {

	public static final String NSURI = "http://online.socialchange.net.au";

	/**
	 * <pre>
	 * <code>
	 *   	 FeatureType[
	 *   		name = wq_plus
	 *   	 	identified = true
	 *   	 	super = Feature
	 *   	 	abstract = false
	 *   	 	binding = Feature.class
	 *   	 	restrictions = EMPTY_SET
	 *   	 	nillable = false
	 *   	 	defaultGeometry = #location
	 *   	 	descriptor = OrderedDescriptor(1, 1)[
	 *   	 		sequence = List[
	 *   	 			AttributeDescriptor(1, 1)[
	 *   	 					type = AttributeType[
	 *   	 					name = sitename
	 *   	 					identified = false
	 *   	 					super = null 
	 *   	 					abstract = false
	 *   	 					binding = String.class
	 *   	 					restrictions = EMPTY_SET
	 *   	 					nillable = false
	 *   	 				]
	 *   	 			],
	 *   	 			AttributeDescriptor(0, 1)[
	 *   	 				type = AttributeType[
	 *   						name = anzlic_no
	 *   						identified = false
	 *   						super = null 
	 *   						abstract = false
	 *   						binding = String.class
	 *   						restrictions = EMPTY_SET
	 *   						nillable = true
	 *   	 				]
	 *   	 			],
	 *   	 			AttributeDescriptor(0, 1)[
	 *   	 				type = GeometryAttribute[
	 *   	 					name = location
	 *   	 					identified = false
	 *   	 					super = HERE WE NEED TO REFER TO  gml:LocationPropertyType
	 *   	 					abstract = false
	 *   	 					binding = Point.class
	 *   	 					restrictions = EMPTY_SET
	 *   	 					nillable = true
	 *   	 				]
	 *   	 			],
	 *   	 			AttributeDescriptor (0, Integer.MAX_VALUE)[
	 *   	 				type = ComplexType[
	 *   	 					name = measurement
	 *   	 					identified = true
	 *   	 					super = null 
	 *   	 					abstract = false
	 *   	 					binding = null
	 *   	 					restrictions = EMPTY_SET
	 *   	 					nillable = true
	 *   	 					descriptor = OrderedDescriptor(0, Integer.MAX_VALUE)[
	 *   	 						AttributeDescriptor(1, 1)[
	 *   	 							type = AttributeType[
	 *   	 								name = determinand_description
	 *   	 								identified = false
	 *   	 								super = null 
	 *   	 								abstract = false
	 *   	 								binding = String.class
	 *   	 								restrictions = EMPTY_SET
	 *   	 								nillable = false
	 *   	 							]
	 *   	 						],
	 *   	 						AttributeDescriptor(1, 1)[
	 *   	 							type = AttributeType[
	 *   	 								name = result
	 *   	 								identified = false
	 *   	 								super = null 
	 *   	 								abstract = false
	 *   	 								binding = String.class
	 *   	 								restrictions = EMPTY_SET
	 *   	 								nillable = false
	 *   	 							]
	 *   	 						]
	 *   	 					]//OrderedDescriptor
	 *   	 				] //ComplexType
	 *   	 			], //measurement
	 *   	 			AttributeDescriptor(0, 1)[
	 *   	 				type = AttributeType[
	 *   	 					name = project_no
	 *   	 					identified = false
	 *   	 					super = null 
	 *   	 					abstract = false
	 *   	 					binding = String.class
	 *   	 					restrictions = EMPTY_SET
	 *   	 					nillable = false
	 *   	 				]
	 *   	 			]
	 *   	 		]
	 *   	 	]
	 *   	 ]	 
	 * </code>
	 * </pre>
	 * 
	 * @param typeFactory
	 * @param descFactory
	 * @return
	 */
	public static FeatureType createExample01Type(TypeFactory typeFactory,
			DescriptorFactory descFactory) {
		FeatureType wqPlusType;

		/* direct contents of wq_plus type */
		List<Descriptor> wq_plusContent = new ArrayList<Descriptor>();

		AttributeType sitename = typeFactory.createType(new QName(NSURI,
				"sitename"), String.class);
		wq_plusContent.add(descFactory.node(sitename, 1, 1));

		AttributeType anzlic_no = typeFactory.createType(new QName(NSURI,
				"anzlic_no"), String.class);
		wq_plusContent.add(descFactory.node(anzlic_no, 0, 1));

		GeometryType location = (GeometryType) typeFactory.createType(
				new QName(NSURI, "location"), Point.class);
		wq_plusContent.add(descFactory.node(location, 0, 1));

		// build complex attribute
		List<Descriptor> masurementContents = new ArrayList<Descriptor>();

		AttributeType determinand_description = typeFactory.createType(
				new QName(NSURI, "determinand_description"), String.class);
		masurementContents.add(descFactory.node(determinand_description, 1, 1));

		AttributeType result = typeFactory.createType(
				new QName(NSURI, "result"), String.class);
		masurementContents.add(descFactory.node(result, 1, 1));

		OrderedDescriptor measurementContentsDescriptor = descFactory.ordered(
				masurementContents, 0, Integer.MAX_VALUE);

		boolean IDENTIFIED = true;
		Class BINDING = null;
		boolean NILLABLE = true;
		Set<Filter> RESTRICTIONS = null;

		AttributeType measurement = typeFactory.createType(new QName(NSURI,
				"measurement"), measurementContentsDescriptor, IDENTIFIED,
				BINDING, NILLABLE, RESTRICTIONS);

		wq_plusContent.add(descFactory.node(measurement, 0, Integer.MAX_VALUE));

		AttributeType project_no = typeFactory.createType(new QName(NSURI,
				"project_no"), String.class);
		wq_plusContent.add(descFactory.node(project_no, 0, 1));

		final QName name = new QName(NSURI, "wq_plus");
		final Descriptor schema;
		/* content descriptor of wq_plus */
		schema = descFactory.ordered(wq_plusContent, 1, 1);

		wqPlusType = typeFactory.createFeatureType(name, schema, location);
		return wqPlusType;
	}

	/**
	 * Asserts the corresponding properties of <code>type</code> for equality
	 * with the provided parameter values
	 * 
	 * @param type
	 * @param name
	 * @param binding
	 * @param restrictions
	 * @param identified
	 * @param _abstract
	 * @param superType
	 * @param nillable
	 */
	public static void checkType(AttributeType type, QName name, Class binding,
			Set<Filter> restrictions, boolean identified, boolean _abstract,
			AttributeType superType, boolean nillable) {
		assertNotNull(type);
		assertEquals(name.getLocalPart(), type.name());
		assertEquals(name, type.getName());
		assertEquals(binding, type.getBinding());
		assertNotNull(type.getRestrictions());
		assertEquals(restrictions, type.getRestrictions());
		assertEquals(identified, type.isIdentified());
		assertEquals(_abstract, type.isAbstract());
		assertEquals(superType, type.getSuper());
		assertEquals(Boolean.valueOf(nillable), type.isNillable());
	}

}
