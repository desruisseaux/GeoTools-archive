package org.geotools.data.complex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.type.TypeFactoryImpl;

import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class TestData {

	public final static String WATERSAMPLE_TYPENAME = "wq_ir_results";

	private TestData() {
	}

	/**
	 * Complex type:
	 * <ul>wq_plus
	 * <li>sitename
	 * <li>anzlic_no
	 * <li>project_no
	 * <li>measurement (0..*)
	 * 	<ul>
	 * 		<li>determinand_description</li>
	 * 		<li>result</li>
	 * 	</ul>
	 * </li>
	 * <li>location
	 * </ul>
	 * 
	 * @return
	 */
	public static FeatureType createComplexWaterQualityType() {
                TypeBuilder builder = new TypeBuilder(new TypeFactoryImpl());
	
		FeatureType wq_plusType;
		final AttributeName name = new AttributeName("wq_plus");
		final Descriptor schema;
	
		List/*<Descriptor>*/ measurementContents = new ArrayList/*<Descriptor>*/();
	
		AttributeType parameter = typeFactory.createType("determinand_description",
				String.class);
		AttributeType value = typeFactory.createType("result", Float.class);
	
		measurementContents.add(descFactory.node(parameter, 1, 1));
		measurementContents.add(descFactory.node(value, 1, 1));
	
		Descriptor measurementSchema = descFactory.ordered(measurementContents,
				1, 1);
	
		ComplexType measurementType = typeFactory.createType("measurement",
				measurementSchema);
	
		 /* <li>sitename
		 * <li>anzlic_no
		 * <li>project_no
		 * <li>location
		 * <li>measurement (0..*)
		 * 	<ul>
		 * 		<li>determinand_description</li>
		 * 		<li>result</li>
		 * 	</ul>*/
		List/*<Descriptor>*/ sequence = new ArrayList/*<Descriptor>*/();
		AttributeType type = typeFactory.createType("sitename", String.class);
		sequence.add(descFactory.node(type, 1, 1));
		
		type = typeFactory.createType("anzlic_no", String.class);
		sequence.add(descFactory.node(type, 1, 1));
		
		type = typeFactory.createType("project_no", String.class);
		sequence.add(descFactory.node(type, 1, 1));
		
		sequence.add(descFactory.node(measurementType, 0, Integer.MAX_VALUE));
		
		type = typeFactory.createType("location", Point.class);
		sequence.add(descFactory.node(type, 1, 1));
		
		schema = descFactory.ordered(sequence, 1, 1);
		wq_plusType = typeFactory.createFeatureType(name, schema, null);
	
		return wq_plusType;
	}

	public static FeatureType createComplexWaterSampleType() {
		TypeFactory typeFactory = new TypeFactoryImpl();
		DescriptorFactory descFactory = new DescriptorFactoryImpl();
	
		FeatureType sampleType;
		final AttributeName name = new AttributeName("sample");
		final Descriptor schema;
	
		List/*<Descriptor>*/ measurementContents = new ArrayList/*<Descriptor>*/();
	
		AttributeType parameter = typeFactory.createType("parameter",
				String.class);
		AttributeType value = typeFactory.createType("value", Double.class);
	
		measurementContents.add(descFactory.node(parameter, 1, 1));
		measurementContents.add(descFactory.node(value, 1, 1));
	
		Descriptor measurementSchema = descFactory.ordered(measurementContents,
				1, 1);
	
		ComplexType measurementType = typeFactory.createType("measurement",
				measurementSchema);
	
		List/*<Descriptor>*/ sequence = new ArrayList/*<Descriptor>*/();
		sequence.add(descFactory.node(measurementType, 0, Integer.MAX_VALUE));
		schema = descFactory.ordered(sequence, 1, 1);
	
		sampleType = typeFactory.createFeatureType(name, schema, null);
	
		return sampleType;
	}

	/**
	 * 
	 * <pre>
	 * </pre>
	 * @param simpleStore
	 * @return
	 * @throws Exception
	 */
	public static List/*<AttributeMapping>*/ createMappingsColumnsAndValues(
			MemoryDataAccess simpleStore) throws Exception {
		final FeatureSource wsSource = simpleStore
				.getFeatureSource(WATERSAMPLE_TYPENAME);
		final FeatureType sourceType = wsSource.getSchema();
	
		List mappings = new LinkedList();
		AttributeMapping attMapping;
		Expression source;
		String target;
	
		FilterFactory ff = FilterFactory.createFilterFactory();
	
		source = ff.createLiteralExpression("ph");
		target = "sample/measurement[1]/parameter";
		attMapping = new AttributeMapping(source, target);
		mappings.add(attMapping);
	
		source = ff.createAttributeExpression(sourceType, "ph");
		target = "sample/measurement[1]/value";
		attMapping = new AttributeMapping(source, target);
		mappings.add(attMapping);
	
		source = ff.createLiteralExpression("temp");
		target = "sample/measurement[2]/parameter";
		attMapping = new AttributeMapping(source, target);
		mappings.add(attMapping);
	
		source = ff.createAttributeExpression(sourceType, "temp");
		target = "sample/measurement[2]/value";
		attMapping = new AttributeMapping(source, target);
		mappings.add(attMapping);
	
		source = ff.createLiteralExpression("turbidity");
		target = "sample/measurement[3]/parameter";
		attMapping = new AttributeMapping(source, target);
		mappings.add(attMapping);
	
		source = ff.createAttributeExpression(sourceType, "turbidity");
		target = "sample/measurement[3]/value";
		attMapping = new AttributeMapping(source, target);
		mappings.add(attMapping);
	
		return mappings;
	}

	/**
	 * <p>
	 * Flat FeatureType:
	 * <ul>wq_ir_results
	 * <li> station_no</li>
	 * <li> sitename</li>
	 * <li> anzlic_no</li>
	 * <li> project_no</li>
	 * <li> id</li>
	 * <li> sample_collection_date</li>
	 * <li> determinand_description</li>
	 * <li> results_value</li>
	 * <li> location</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Complex type:
	 * <ul>wq_plus
	 * <li>sitename
	 * <li>anzlic_no
	 * <li>project_no
	 * <li>location
	 * <li>measurement (0..*)
	 * 	<ul>
	 * 		<li>determinand_description</li>
	 * 		<li>result</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 * </p>
	 * <p>
	 * Mappings definition:
	 * <pre>
	 * <strong>wq_ir_results</strong>			<strong>wq_plus</strong>
	 *  station_no		-->	wq_plus@id
	 *  sitename		-->	sitename	
	 *  anzlic_no		-->	anzlic_no
	 *  project_no		-->	project_no
	 *  id		-->	measurement/@id
	 *  sample_collection_date--> [not used]
	 *  determinand_description-->measurement/determinand_description	
	 *  results_value		-->measurement/result
	 *  location		-->location
	 * </pre>
	 * </p>
	 * @param simpleStore
	 * @return
	 * @throws Exception
	 */
	public static FeatureTypeMapping createMappingsGroupByStation(
			MemoryDataAccess simpleStore) throws Exception {
		final FeatureSource wsSource = simpleStore
				.getFeatureSource(WATERSAMPLE_TYPENAME);
		final FeatureType sourceType = wsSource.getSchema();
	
		List mappings = new LinkedList();
		Expression source;
		String target;
	
		FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
	
		source = ff.createAttributeExpression(sourceType, "sitename");
		target = "wq_plus/sitename";
		mappings.add(new AttributeMapping(source, target));
	
		source = ff.createAttributeExpression(sourceType, "anzlic_no");
		target = "wq_plus/anzlic_no";
		mappings.add(new AttributeMapping(source, target));
	
		source = ff.createAttributeExpression(sourceType, "project_no");
		target = "wq_plus/project_no";
		mappings.add(new AttributeMapping(source, target));
	
		source = null;
		target = "wq_plus/measurement";
		mappings.add(new AttributeMapping(source, target, null, true, null));
	
		source = ff.createAttributeExpression(sourceType, "determinand_description");
		target = "wq_plus/measurement/determinand_description";
		mappings.add(new AttributeMapping(source, target));
	
		source = ff.createAttributeExpression(sourceType, "results_value");
		target = "wq_plus/measurement/result";
		mappings.add(new AttributeMapping(source, target));
	
		source = ff.createAttributeExpression(sourceType, "location");
		target = "wq_plus/location";
		mappings.add(new AttributeMapping(source, target));
	
		FeatureType targetType = createComplexWaterQualityType();
		AttributeDescriptor targetNode = new NodeImpl(targetType);
		
		Expression wqPlusId = ff.createAttributeExpression(sourceType, "station_no");
		
		Expression measurementId = ff.createAttributeExpression(sourceType, "id");
		
		Map/*<String, Expression>*/idMappings = new HashMap/*<String, Expression>*/();
		idMappings.put(targetType.getName().getLocalPart(), wqPlusId);
		idMappings.put(targetType.getName().getLocalPart() + "/measurement", measurementId);
		
		FeatureTypeMapping mapper = new FeatureTypeMapping(wsSource,
				targetNode, mappings, idMappings);
		
		
		List/*<String>*/groupingAttributes = new ArrayList/*<String>*/();
		groupingAttributes.add("station_no");
		groupingAttributes.add("sitename");
		groupingAttributes.add("anzlic_no");
		groupingAttributes.add("project_no");
		groupingAttributes.add("location");
		
		mapper.setGroupByAttNames(groupingAttributes);
		
		return mapper;
	}


	/**
	 * Creates a flat FeatureType <code>wq_ir_results</code> with a structure
	 * like the following, from which a complex one should be constructed
	 * grouping by station_no attribute.
	 * <p>
	 * Following this sample schema, a total of 10 unique station_no identifiers
	 * will be created, and for each one, a total of N desagregate rows with the
	 * same station_no, where N goes from 1 to 10. So for the first station_no
	 * there will be just one occurrence and the last one will have 10. 
	 * </p>
	 * <p>
	 * <table>
	 * <tr>
	 * <td> station_no (string) </td>
	 * <td> sitename (string)</td>
	 * <td> anzlic_no (string)</td>
	 * <td> project_no (string)</td>
	 * <td> id (string)</td>
	 * <td> sample_collection_date (string)</td>
	 * <td> determinand_description (string)</td>
	 * <td> results_value (float)</td>
	 * <td> location (Point)</td>
	 * </tr>
	 * <tr>
	 * <td> station_1 </td>
	 * <td> sitename_1 </td>
	 * <td> anzlic_no_1 </td>
	 * <td> project_no_1 </td>
	 * <td> id_1_1 </td>
	 * <td> sample_collection_date_1_1 </td>
	 * <td> determinand_description_1_1 </td>
	 * <td> 1.1 </td>
	 * <td> POINT(1, 1) </td>
	 * </tr>
	 * <tr>
	 * <td> station_2 </td>
	 * <td> sitename_2 </td>
	 * <td> anzlic_no_2 </td>
	 * <td> project_no_2 </td>
	 * <td> id_2_1 </td>
	 * <td> sample_collection_date_2_1 </td>
	 * <td> determinand_description_2_1 </td>
	 * <td> 2.1 </td>
	 * <td> POINT(2, 2) </td>
	 * </tr>
	 * <tr>
	 * <td> station_2 </td>
	 * <td> sitename_2 </td>
	 * <td> anzlic_no_2 </td>
	 * <td> project_no_2 </td>
	 * <td> id_2_2 </td>
	 * <td> sample_collection_date_2_2 </td>
	 * <td> determinand_description_2_2 </td>
	 * <td> 2.2 </td>
	 * <td> POINT(2, 2) </td>
	 * </tr>
	 * <tr>
	 * <td colspan="9">...</td>
	 * </tr>
	 * <tr>
	 * <td> station_10 </td>
	 * <td> sitename_10 </td>
	 * <td> anzlic_no_10 </td>
	 * <td> project_no_10 </td>
	 * <td> id_10_10 </td>
	 * <td> sample_collection_date_10_9 </td>
	 * <td> determinand_description_10_9 </td>
	 * <td> 10.10 </td>
	 * <td> POINT(10, 10) </td>
	 * </tr>
	 * <tr>
	 * <td> station_10 </td>
	 * <td> sitename_10 </td>
	 * <td> anzlic_no_10 </td>
	 * <td> project_no_10 </td>
	 * <td> id_10_10 </td>
	 * <td> sample_collection_date_10_10 </td>
	 * <td> determinand_description_10_10 </td>
	 * <td> 10.10 </td>
	 * <td> POINT(10, 10) </td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public static MemoryDataAccess createDenormalizedWaterQualityResults()
			throws Exception {
		MemoryDataAccess dataStore = new MemoryDataAccess();
		String typeString = "station_no:string,sitename:string,anzlic_no:string,project_no:string,id:string,sample_collection_date:string,determinand_description:string,results_value:float,location:Point";
		SimpleFeatureType type = (SimpleFeatureType) DataUtilities.createType(
				TestData.WATERSAMPLE_TYPENAME, typeString);
		AttributeDescriptor node = new NodeImpl(type);
		dataStore.createSchema(type);

		final int NUM_STATIONS = 10;
		AttributeFactory af = new AttributeFactoryImpl();
		GeometryFactory gf = new GeometryFactory();

		for (int groupValue = 1; groupValue <= NUM_STATIONS; groupValue++) {
			
			for (int measurement = 1; measurement <= groupValue; measurement++) {
				String fid = type.getName().getLocalPart() + "." + groupValue + "." + measurement;
				SimpleFeature f = af.createSimpleFeature(node, fid);
				f.set("station_no", "station_no." + groupValue);
				f.set("sitename", "sitename" + groupValue);
				f.set("anzlic_no", "anzlic_no" + groupValue);
				f.set("project_no", "project_no" + groupValue);
				
				String sufix = "_" + groupValue + "_" + measurement;
				f.set("id", "id" + sufix);
				f.set("sample_collection_date", "sample_collection_date" + sufix);
				f.set("determinand_description", "determinand_description" + sufix);
				f.set("results_value", new Float(groupValue + "." + measurement));
				
				f.set("location", gf.createPoint(new Coordinate(groupValue, groupValue)));
				
				dataStore.addFeature(f);
				
			}
		}
		return dataStore;
	}

}
