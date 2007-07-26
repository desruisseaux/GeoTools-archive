package org.geotools.demo.main;

import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionImpl;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.AbstractFeatureVisitor;
import org.geotools.filter.function.Classifier;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Function;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * This class collects several examples on the use of DefaultFeatureCollection.
 * <p>
 * For the wiki page associated with these examples please visit:
 * <ul>
 * <li><a href="http://docs.codehaus.org/display/GEOTDOC/05+Main">Main Module Wiki Page</a>
 * <li><a href="http://docs.codehaus.org/display/GEOTDOC/08+FeatureCollection">Feature Collection</a>
 * <li><a href="http://docs.codehaus.org/display/GEOTDOC/09+FeatureCollection+Iterator">Feature Collection Iterator</a>
 * <li><a href="http://docs.codehaus.org/display/GEOTDOC/10+FeatureCollection+Functions">Feature Collection Functions</a>
 * </ul>
 * If you update this file please correct the above pages.
 * <p>
 * Implementation notes: Where possible we are restricting ourself to the formal api, we try not
 * to use keyword "new" (and instead make use of a Factory).
 * </p>
 * @author Jody Garnett
 */
public class FeatureCollectionExampleTest extends TestCase {
    SimpleFeatureCollection features;
    private Feature feature1;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GeometryFactory geomFactory = new GeometryFactory();
        
        features = FeatureCollections.newCollection("internal");
        
        FeatureType type = DataUtilities.createType("location","geom:Point,name:String,age:Integer");        
        
        Point point1 = geomFactory.createPoint( new Coordinate(40,50));
        feature1 = type.create( new Object[]{ point1, "name1", 17} );
        features.add( feature1 );
        
        Point point2 = geomFactory.createPoint( new Coordinate(30,45));
        features.add( type.create( new Object[]{ point2, "name2",24} ) );
        
        Point point3 = geomFactory.createPoint( new Coordinate(35,46));
        features.add( type.create( new Object[]{ point3, "name2",24} ) );
    }
    /**
     * You can create using a normal constructor ... but it is not recommended.
     * We can produce a better implementation than DefaultFeatureCollection, and we will ..
     * ... by using FeatureCollections your application will be sure to pick up the best
     * implementation we can make available.
     */
    public void testNewDefaultFeatureCollection(){
        SimpleFeatureCollection collection = new DefaultFeatureCollection("internal", null );        
    }
    public void testFeatureCollectionsNewCollection(){
        SimpleFeatureCollection collection = FeatureCollections.newCollection("internal");
    }
    
    public void testAddingContentToYourFeatureCollection() throws Exception{
        GeometryFactory geomFactory = new GeometryFactory();
        Point point1 = geomFactory.createPoint( new Coordinate(40,50));
        Point point2 = geomFactory.createPoint( new Coordinate(30,45));
        
        SimpleFeatureCollection collection = FeatureCollections.newCollection("internal");
        
        FeatureType type = DataUtilities.createType("location","geom:Point,name:String");        
        collection.add( type.create( new Object[]{ point1, "name1"} ) );
        collection.add( type.create( new Object[]{ point2, "name2"} ) );        
    }
    public void testSum(){
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Function sum = ff.function("Collection_Sum", ff.property("age"));
        
        Object value = sum.evaluate( features );
        assertEquals( 65, value );
    }
    
    /**
     * See <a href="http://docs.codehaus.org/display/GEOTDOC/11+FeatureCollection+Classification+Functions">FeatureCollection Classigication Functions</a> in the wiki
     */
    public void testClassifier(){
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Function classify = ff.function("Quantile", ff.property("name"), ff.literal(2));
        
        Classifier groups = (Classifier) classify.evaluate( features );
        groups.setTitle(0, "Group A");
        groups.setTitle(1, "Group B");
        
        assertNotNull( groups.toString() );
        
        Function sort = ff.function("classify",ff.property("name"), ff.literal( groups ));
        int slot = (Integer) sort.evaluate( feature1 );
        assertEquals( 0, slot );
        assertEquals( "Group A", groups.getTitle( slot ) );
    }
    
    /** The easiest way to work with data */
    public void testFeatureVisitor(){
        CoordinateReferenceSystem crs = features.getMemberType().getCRS();
        final BoundingBox bounds = new ReferencedEnvelope( crs );
        
        Iterator<SimpleFeature> iterator = features.iterator();
        features.accepts( new AbstractFeatureVisitor(){
            public void visit( org.opengis.feature.Feature feature ) {
                bounds.include( feature.getBounds() );
            }            
        }, null );
        assertFalse( bounds.isEmpty() );
    }
    
    /** IMPORTANT - You must close your iterator */
    public void testIterator(){
        CoordinateReferenceSystem crs = features.getMemberType().getCRS();
        BoundingBox bounds = new ReferencedEnvelope( crs );
        
        Iterator<SimpleFeature> iterator = features.iterator();
        try {
            while( iterator.hasNext()){
                SimpleFeature feature = iterator.next();
                bounds.include( feature.getBounds() );
            }
        }
        finally{
            features.close( iterator );
        }
        assertFalse( bounds.isEmpty() );
    }
    /** Important when working with real data */
    public void testIteratorSafe(){
        int count = 0;
        Iterator<SimpleFeature> iterator = features.iterator();
        try {
            while( iterator.hasNext()){
                try {
                    SimpleFeature feature = iterator.next();
                    count++;
                }
                catch( RuntimeException dataProblem ){
                    if( iterator.hasNext() ) continue; // skip invalid data and try the next one
                    throw dataProblem;
                }
            }
        }
        finally{
            features.close( iterator );
        }
        assertTrue( count != 0 );
    }
    /** This example also works with Java 1.4 */
    public void testFeatureIterator(){
        FeatureCollection collection = (FeatureCollection) features;
        
        CoordinateReferenceSystem crs = features.getMemberType().getCRS();
        Envelope bounds = new Envelope();
        
        FeatureIterator features = collection.features();
        try {
            while( features.hasNext()){
                Feature feature = features.next();
                bounds.expandToInclude( feature.getBounds() );
            }
        }
        finally{
            features.close();
        }
        assertFalse( bounds.isNull() );
    }
}
