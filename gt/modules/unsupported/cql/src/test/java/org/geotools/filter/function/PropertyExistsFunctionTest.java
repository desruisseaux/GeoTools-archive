package org.geotools.filter.function;

import java.util.Collections;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Function;
import org.opengis.metadata.citation.Citation;

public class PropertyExistsFunctionTest extends TestCase {

    private static final FilterFactory ff = CommonFactoryFinder
            .getFilterFactory(null);

    PropertyExistsFunction f;

    public void setUp() {
        f = new PropertyExistsFunction();
    }

    public void tearDown() {
        f = null;
    }

    public void testName() {
        assertEquals("propertyexists", f.getName().toLowerCase());
    }

    public void testFind() {
        Function function = ff.function("propertyexists", ff
                .property("testPropName"));
        assertNotNull(function);
    }

    public void testEvaluateFeature() throws SchemaException,
            IllegalAttributeException {
        FeatureType type = DataUtilities.createType("ns",
                "name:string,geom:Geometry");
        Feature feature = type.create(new Object[] { "testName", null });

        f.setParameters(Collections.singletonList(ff.property("nonExistant")));
        assertEquals(Boolean.FALSE, f.evaluate(feature));

        f.setParameters(Collections.singletonList(ff.property("name")));
        assertEquals(Boolean.TRUE, f.evaluate(feature));

        f.setParameters(Collections.singletonList(ff.property("geom")));
        assertEquals(Boolean.TRUE, f.evaluate(feature));
    }

    public void testEvaluatePojo(){
        Citation pojo = new CitationImpl();
        
        f.setParameters(Collections.singletonList(ff.property("edition")));
        assertEquals(Boolean.TRUE, f.evaluate(pojo));

        f.setParameters(Collections.singletonList(ff.property("alternateTitles")));
        assertEquals(Boolean.TRUE, f.evaluate(pojo));
        
        //worng case (note the first letter)
        f.setParameters(Collections.singletonList(ff.property("AlternateTitles")));
        assertEquals(Boolean.FALSE, f.evaluate(pojo));

        f.setParameters(Collections.singletonList(ff.property("nonExistentProperty")));
        assertEquals(Boolean.FALSE, f.evaluate(pojo));
    }
    
    
    // @todo: REVISIT. don't we implement equals on functions/filters/etc?
    // public void testEquals(){
    // FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    // Function actual = new PropertyExistsFunction();
    // f.setParameters(Collections.singletonList(ff.property("testPropName")));
    // actual.setParameters(Collections.singletonList(ff.property("testPropName")));
    // assertEquals(f, actual);
    // }

}
