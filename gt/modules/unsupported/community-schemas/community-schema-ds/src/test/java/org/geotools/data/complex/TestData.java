package org.geotools.data.complex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.iso.simple.SimpleTypeBuilder;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class TestData {

    public final static TypeName WATERSAMPLE_TYPENAME = new org.geotools.feature.type.TypeName(
            "wq_ir_results");

    private TestData() {
    }

    /**
     * Complex type:
     * <ul>
     * wq_plus
     * <li>sitename
     * <li>anzlic_no
     * <li>project_no
     * <li>measurement (0..*)
     * <ul>
     * <li>determinand_description</li>
     * <li>result</li>
     * </ul>
     * </li>
     * <li>location
     * </ul>
     * 
     * @return
     */
    public static FeatureType createComplexWaterQualityType() {
        TypeFactoryImpl tfac = new TypeFactoryImpl();
        TypeBuilder builder = new TypeBuilder(tfac);

        FeatureType wq_plusType;

        AttributeType detdesc = builder.name("determinand_description").bind(
                String.class).attribute();
        AttributeType result = builder.name("result").bind(Float.class)
                .attribute();

        builder.setName("measurement");
        builder.addAttribute("determinand_description", detdesc);
        builder.addAttribute("result", result);

        ComplexType MEASUREMENT = builder.complex();

        /*
         * <li>sitename <li>anzlic_no <li>project_no <li>location <li>measurement
         * (0..*) <ul> <li>determinand_description</li> <li>result</li>
         * </ul>
         */

        AttributeType sitename = builder.name("sitename").bind(String.class)
                .attribute();
        AttributeType anzlic_no = builder.name("anzlic_no").bind(String.class)
                .attribute();
        AttributeType project_no = builder.name("project_no")
                .bind(String.class).attribute();
        AttributeType location = builder.name("location").bind(Point.class)
                .geometry();

        builder.setName("wq_plus");
        builder.addAttribute("sitename", sitename);
        builder.addAttribute("anzlic_no", anzlic_no);
        builder.addAttribute("project_no", project_no);

        builder.cardinality(0, Integer.MAX_VALUE);
        builder.addAttribute("measurement", MEASUREMENT);

        builder.cardinality(1, 1);
        builder.addAttribute("location", location);

        wq_plusType = builder.feature();

        return wq_plusType;
    }

    public static FeatureType createComplexWaterSampleType() {
        TypeFactoryImpl tfac = new TypeFactoryImpl();
        TypeBuilder builder = new TypeBuilder(tfac);

        FeatureType sampleType;

        AttributeType parameter = builder.name("parameter").bind(String.class)
                .attribute();
        AttributeType value = builder.name("value").bind(Double.class)
                .attribute();

        builder.setName("measurement");
        builder.addAttribute("parameter", parameter);
        builder.addAttribute("value", value);
        ComplexType MEASUREMENT = builder.complex();

        builder.setName("sample");
        builder.cardinality(0, Integer.MAX_VALUE);
        builder.addAttribute("measurement", MEASUREMENT);

        sampleType = builder.feature();

        return sampleType;
    }

    /**
     * 
     * <pre>
     * </pre>
     * 
     * @param simpleStore
     * @return
     * @throws Exception
     */
    public static List/* <AttributeMapping> */createMappingsColumnsAndValues(
            MemoryDataAccess simpleStore) throws Exception {

        List mappings = new LinkedList();
        AttributeMapping attMapping;
        Expression source;
        String target;

        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        source = ff.literal("ph");
        target = "sample/measurement[1]/parameter";
        attMapping = new AttributeMapping(null, source, target);
        mappings.add(attMapping);

        source = ff.property("ph");
        target = "sample/measurement[1]/value";
        attMapping = new AttributeMapping(null, source, target);
        mappings.add(attMapping);

        source = ff.literal("temp");
        target = "sample/measurement[2]/parameter";
        attMapping = new AttributeMapping(null, source, target);
        mappings.add(attMapping);

        source = ff.property("temp");
        target = "sample/measurement[2]/value";
        attMapping = new AttributeMapping(null, source, target);
        mappings.add(attMapping);

        source = ff.literal("turbidity");
        target = "sample/measurement[3]/parameter";
        attMapping = new AttributeMapping(null, source, target);
        mappings.add(attMapping);

        source = ff.property("turbidity");
        target = "sample/measurement[3]/value";
        attMapping = new AttributeMapping(null, source, target);
        mappings.add(attMapping);

        return mappings;
    }

    /**
     * <p>
     * Flat FeatureType:
     * <ul>
     * wq_ir_results
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
     * <ul>
     * wq_plus
     * <li>sitename
     * <li>anzlic_no
     * <li>project_no
     * <li>location
     * <li>measurement (0..*)
     * <ul>
     * <li>determinand_description</li>
     * <li>result</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     * <p>
     * Mappings definition:
     * 
     * <pre>
     *       &lt;strong&gt;wq_ir_results&lt;/strong&gt;			&lt;strong&gt;wq_plus&lt;/strong&gt;
     *        station_no		--&gt;	wq_plus@id
     *        sitename		--&gt;	sitename	
     *        anzlic_no		--&gt;	anzlic_no
     *        project_no		--&gt;	project_no
     *        id		--&gt;	measurement/@id
     *        sample_collection_date--&gt; [not used]
     *        determinand_description--&gt;measurement/determinand_description	
     *        results_value		--&gt;measurement/result
     *        location		--&gt;location
     * </pre>
     * 
     * </p>
     * 
     * @param simpleStore
     * @return
     * @throws Exception
     */
    public static FeatureTypeMapping createMappingsGroupByStation(
            MemoryDataAccess simpleStore) throws Exception {
        TypeName typeName = WATERSAMPLE_TYPENAME;
        final FeatureSource2 wsSource = (FeatureSource2) simpleStore
                .access(typeName);

        AttributeDescriptor descriptor = (AttributeDescriptor) wsSource
                .describe();
        final FeatureType sourceType = (FeatureType) descriptor.getType();

        List mappings = new LinkedList();
        Expression id;
        Expression source;
        String target;

        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        id = ff.property("station_no");
        source = Expression.NIL;
        target = "wq_plus";
        mappings.add(new AttributeMapping(id, source, target));

        source = ff.property("sitename");
        target = "wq_plus/sitename";
        mappings.add(new AttributeMapping(null, source, target));

        source = ff.property("anzlic_no");
        target = "wq_plus/anzlic_no";
        mappings.add(new AttributeMapping(null, source, target));

        source = ff.property("project_no");
        target = "wq_plus/project_no";
        mappings.add(new AttributeMapping(null, source, target));

        id = ff.property("id");
        source = null;
        target = "wq_plus/measurement";
        mappings
                .add(new AttributeMapping(id, source, target, null, true, null));

        source = ff.property("determinand_description");
        target = "wq_plus/measurement/determinand_description";
        mappings.add(new AttributeMapping(null, source, target));

        source = ff.property("results_value");
        target = "wq_plus/measurement/result";
        mappings.add(new AttributeMapping(null, source, target));

        source = ff.property("location");
        target = "wq_plus/location";
        mappings.add(new AttributeMapping(null, source, target));

        FeatureType targetType = createComplexWaterQualityType();
        TypeFactory tf = new TypeFactoryImpl();
        AttributeDescriptor targetFeature = tf.createAttributeDescriptor(
                targetType, targetType.getName(), 0, Integer.MAX_VALUE, true);

        FeatureTypeMapping mapper = new FeatureTypeMapping(wsSource,
                targetFeature, mappings);

        List/* <String> */groupingAttributes = new ArrayList/* <String> */();
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
        SimpleTypeFactory tf = new SimpleTypeFactoryImpl();
        SimpleTypeBuilder builder = new SimpleTypeBuilder(tf);

        builder.setName(TestData.WATERSAMPLE_TYPENAME.getLocalPart());

        String typeString = "determinand_description:string,results_value:float,location:Point";
        builder.addAttribute("station_no", String.class);
        builder.addAttribute("sitename", String.class);
        builder.addAttribute("anzlic_no", String.class);
        builder.addAttribute("project_no", String.class);
        builder.addAttribute("id", String.class);
        builder.addAttribute("sample_collection_date", String.class);
        builder.addAttribute("determinand_description", String.class);
        builder.addAttribute("results_value", Float.class);
        builder.addGeometry("location", Point.class);

        SimpleFeatureType type = builder.feature();

        AttributeDescriptor node = null;// new NodeImpl(type);
        dataStore.createSchemaInternal(type);

        final int NUM_STATIONS = 10;
        SimpleFeatureFactory af = new SimpleFeatureFactoryImpl();
        GeometryFactory gf = new GeometryFactory();

        for (int groupValue = 1; groupValue <= NUM_STATIONS; groupValue++) {

            for (int measurement = 1; measurement <= groupValue; measurement++) {
                String fid = type.getName().getLocalPart() + "." + groupValue
                        + "." + measurement;
                SimpleFeature f = af.createSimpleFeature(type, fid, null);
                f.set("station_no", "station_no." + groupValue);
                f.set("sitename", "sitename" + groupValue);
                f.set("anzlic_no", "anzlic_no" + groupValue);
                f.set("project_no", "project_no" + groupValue);

                String sufix = "_" + groupValue + "_" + measurement;
                f.set("id", "id" + sufix);
                f.set("sample_collection_date", "sample_collection_date"
                        + sufix);
                f.set("determinand_description", "determinand_description"
                        + sufix);
                f.set("results_value",
                        new Float(groupValue + "." + measurement));

                f.set("location", gf.createPoint(new Coordinate(groupValue,
                        groupValue)));

                dataStore.addFeatureInternal(f);
            }
        }
        return dataStore;
    }

}
