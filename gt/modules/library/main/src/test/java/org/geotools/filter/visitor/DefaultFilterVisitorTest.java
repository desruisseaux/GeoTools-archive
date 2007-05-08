package org.geotools.filter.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.expression.PropertyName;

import junit.framework.TestCase;

public class DefaultFilterVisitorTest extends TestCase {
    private FilterFactory ff;
    
    protected void setUp() throws Exception {    
        super.setUp();    
        ff = CommonFactoryFinder.getFilterFactory(null);
    }
    
    public void testFeatureIdExample() {
        Filter myFilter = ff.id( Collections.singleton( ff.featureId("fred")));
        FilterVisitor allFids = new DefaultFilterVisitor(){
            public Object visit( Id filter, Object data ) {
                Set set = (Set) data;
                set.addAll(filter.getIDs());
                return set;
            }
        };
        Set set = (Set) myFilter.accept( allFids, new HashSet());
        assertEquals( 1, set.size() );
    }
    
    public void testPropertyNameExample(){
Filter myFilter = ff.greater( ff.add(ff.property("foo"), ff.property("bar")), ff.literal(1) );

class FindNames extends DefaultFilterVisitor {
    public Object visit( PropertyName expression, Object data ) {
        Set set = (Set) data;
        set.add( expression.getPropertyName() );
        
        return set;
    }
}
Set set = (Set) myFilter.accept( new FindNames(), new HashSet() );
assertTrue( set.contains("foo") );
    }
}
