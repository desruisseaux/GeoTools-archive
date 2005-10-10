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
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public abstract class ComplexTestData extends TestCase {

	public static final String NSURI = "http://online.socialchange.net.au";

	public static final String GML_NSURI = "http://www.opengis.net/gml";

	/**
	 * <pre>
	 * <code>
	 *    	 FeatureType[
	 *    		name = wq_plus
	 *    	 	identified = true
	 *    	 	super = Feature
	 *    	 	abstract = false
	 *    	 	binding = Feature.class
	 *    	 	restrictions = EMPTY_SET
	 *    	 	nillable = false
	 *    	 	defaultGeometry = #location
	 *    	 	descriptor = OrderedDescriptor(1, 1)[
	 *    	 		sequence = List[
	 *    	 			AttributeDescriptor(1, 1)[
	 *    	 					type = AttributeType[
	 *    	 					name = sitename
	 *    	 					identified = false
	 *    	 					super = null 
	 *    	 					abstract = false
	 *    	 					binding = String.class
	 *    	 					restrictions = EMPTY_SET
	 *    	 					nillable = false
	 *    	 				]
	 *    	 			],
	 *    	 			AttributeDescriptor(0, 1)[
	 *    	 				type = AttributeType[
	 *    						name = anzlic_no
	 *    						identified = false
	 *    						super = null 
	 *    						abstract = false
	 *    						binding = String.class
	 *    						restrictions = EMPTY_SET
	 *    						nillable = true
	 *    	 				]
	 *    	 			],
	 *    	 			AttributeDescriptor(0, 1)[
	 *    	 				type = GeometryAttribute[
	 *    	 					name = location
	 *    	 					identified = false
	 *    	 					super = HERE WE NEED TO REFER TO  gml:LocationPropertyType
	 *    	 					abstract = false
	 *    	 					binding = Point.class
	 *    	 					restrictions = EMPTY_SET
	 *    	 					nillable = true
	 *    	 				]
	 *    	 			],
	 *    	 			AttributeDescriptor (0, Integer.MAX_VALUE)[
	 *    	 				type = ComplexType[
	 *    	 					name = measurement
	 *    	 					identified = true
	 *    	 					super = null 
	 *    	 					abstract = false
	 *    	 					binding = null
	 *    	 					restrictions = EMPTY_SET
	 *    	 					nillable = true
	 *    	 					descriptor = OrderedDescriptor(0, Integer.MAX_VALUE)[
	 *    	 						AttributeDescriptor(1, 1)[
	 *    	 							type = AttributeType[
	 *    	 								name = determinand_description
	 *    	 								identified = false
	 *    	 								super = null 
	 *    	 								abstract = false
	 *    	 								binding = String.class
	 *    	 								restrictions = EMPTY_SET
	 *    	 								nillable = false
	 *    	 							]
	 *    	 						],
	 *    	 						AttributeDescriptor(1, 1)[
	 *    	 							type = AttributeType[
	 *    	 								name = result
	 *    	 								identified = false
	 *    	 								super = null 
	 *    	 								abstract = false
	 *    	 								binding = String.class
	 *    	 								restrictions = EMPTY_SET
	 *    	 								nillable = false
	 *    	 							]
	 *    	 						]
	 *    	 					]//OrderedDescriptor
	 *    	 				] //ComplexType
	 *    	 			], //measurement
	 *    	 			AttributeDescriptor(0, 1)[
	 *    	 				type = AttributeType[
	 *    	 					name = project_no
	 *    	 					identified = false
	 *    	 					super = null 
	 *    	 					abstract = false
	 *    	 					binding = String.class
	 *    	 					restrictions = EMPTY_SET
	 *    	 					nillable = false
	 *    	 				]
	 *    	 			]
	 *    	 		]
	 *    	 	]
	 *    	 ]	 
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
		AttributeType measurement = createMeasurementType(typeFactory,
				descFactory);

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
	 * A feature type that has various multi-valued properties.
	 * <p>
	 * Multi valued properties: meassurement(0:unbounded),
	 * sitename(1:unbounded).
	 * 
	 * <pre>
	 * <code>
	 * </code>
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param typeFactory
	 * @param descFactory
	 * @return
	 */
	public static FeatureType createExample02Type(TypeFactory typeFactory,
			DescriptorFactory descFactory) {
		FeatureType wqPlusType;

		/* direct contents of wq_plus type */
		List<Descriptor> wq_plusContent = new ArrayList<Descriptor>();

		AttributeType measurement = createMeasurementType(typeFactory,
				descFactory);
		wq_plusContent.add(descFactory.node(measurement, 0, Integer.MAX_VALUE));

		GeometryType the_geom = (GeometryType) typeFactory.createType(
				new QName(NSURI, "the_geom"), Geometry.class);
		wq_plusContent.add(descFactory.node(the_geom, 1, 1));

		AttributeType sitename = typeFactory.createType(new QName(NSURI,
				"sitename"), String.class, false, true, null);
		wq_plusContent.add(descFactory.node(sitename, 1, Integer.MAX_VALUE));

		final QName name = new QName(NSURI, "wq_plus");
		final Descriptor schema;
		/* content descriptor of wq_plus */
		schema = descFactory.ordered(wq_plusContent, 1, 1);

		wqPlusType = typeFactory.createFeatureType(name, schema, the_geom);
		return wqPlusType;
	}

	/**
	 * A feature may have multiple geometries
	 * 
	 * @param typeFactory
	 * @param descFactory
	 * @return
	 */
	public static FeatureType createExample03Type(TypeFactory typeFactory,
			DescriptorFactory descFactory) {
		FeatureType wqPlusType;

		/* direct contents of wq_plus type */
		List<Descriptor> wq_plusContent = new ArrayList<Descriptor>();

		AttributeType measurement = createMeasurementType(typeFactory,
				descFactory);
		wq_plusContent.add(descFactory.node(measurement, 1, Integer.MAX_VALUE));

		AttributeType gmlLocationAssociation = createGmlLocation(typeFactory,
				descFactory);
		// inherits from gml:LocationPropertyType without adding extra content
		AttributeType scoLocation = typeFactory.createType(new QName(NSURI,
				"location"), gmlLocationAssociation.getBinding(), false, true,
				null, gmlLocationAssociation);
		wq_plusContent.add(descFactory.node(scoLocation, 1, 1));

		AttributeType gmlPointAssociation = createGmlPoint(typeFactory,
				descFactory);
		GeometryType nearestSlimePit = (GeometryType) typeFactory.createType(
				new QName(NSURI, "nearestSlimePit"), gmlPointAssociation
						.getBinding(), false, true, null, gmlPointAssociation);

		wq_plusContent.add(descFactory.node(nearestSlimePit, 1, 1));

		AttributeType sitename = typeFactory.createType(new QName(NSURI,
				"sitename"), String.class, false, true, null);
		wq_plusContent.add(descFactory.node(sitename, 1, Integer.MAX_VALUE));

		final QName name = new QName(NSURI, "wq_plus");
		final Descriptor schema;
		/* content descriptor of wq_plus */
		schema = descFactory.ordered(wq_plusContent, 1, 1);

		//use the second geometry attribute as the default one just for testing
		wqPlusType = typeFactory.createFeatureType(name, schema, nearestSlimePit);
		return wqPlusType;
	}

	public static ComplexType createMeasurementType(TypeFactory typeFactory,
			DescriptorFactory descFactory) {
		// build complex attribute
		List<Descriptor> masurementContents = new ArrayList<Descriptor>();

		AttributeType determinand_description = typeFactory.createType(
				new QName(NSURI, "determinand_description"), String.class);
		masurementContents.add(descFactory.node(determinand_description, 1, 1));

		AttributeType result = typeFactory.createType(
				new QName(NSURI, "result"), String.class);
		masurementContents.add(descFactory.node(result, 1, 1));

		OrderedDescriptor measurementContentsDescriptor = descFactory.ordered(
				masurementContents, 1, 1);

		boolean IDENTIFIED = true;
		Class BINDING = null;
		boolean NILLABLE = true;
		Set<Filter> RESTRICTIONS = null;

		ComplexType measurement = typeFactory.createType(new QName(NSURI,
				"measurement"), measurementContentsDescriptor, IDENTIFIED,
				BINDING, NILLABLE, RESTRICTIONS);

		return measurement;

	}

	/**
	 * Creates a representation of a gml:LocationPropertyType association. This
	 * would be better done by obtaining the type from a registry, so we can
	 * have GML2TypeRegistry, GML3TypeRegistry, DefaultTypeRegistry, etc.
	 * 
	 * @return
	 */
	public static AttributeType createGmlLocation(TypeFactory typeFactory,
			DescriptorFactory descFactory) {
		QName name = new QName(GML_NSURI, "LocationPropertyType");
		AttributeType type = typeFactory.createType(name, Point.class);
		return type;
	}

	/**
	 * Creates a representation of a gml:PointPropertyType association as an
	 * AttributeType. This would be better done by obtaining the type from a
	 * registry, so we can have GML2TypeRegistry, GML3TypeRegistry,
	 * DefaultTypeRegistry, etc.
	 * 
	 * @return
	 */
	public static AttributeType createGmlPoint(TypeFactory typeFactory,
			DescriptorFactory descFactory) {
		QName name = new QName(GML_NSURI, "PointPropertyType");
		AttributeType type = typeFactory.createType(name, Point.class);
		return type;
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
