package org.geotools.filter;

import static org.junit.Assert.*;

import org.geotools.factory.CommonFactoryFinder;
import org.junit.Test;
import org.opengis.filter.And;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Equals;

import com.sun.org.apache.bcel.internal.util.ClassQueue;

/**
 * FilterBuilder is a main entry from a fluent programming point of view. We will mostly test using
 * this as a starting point; and break out other test cases on an as needed basis.
 *
 * @source $URL: http://svn.somewhere.foo/org/geotools/trunk/modules/extension/brewer/src/test/java/org/geotools/filter/FilterBuilderTest.java $
 */
public class FilterBuilderTest {
    protected FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    @Test
    public void add() {
        Add expected = ff.add(ff.literal(1), ff.literal(1));
    }

    @Test
    public void and() {
        PropertyIsNull nn = ff.isNull(ff.property("bar"));
        Equals eq = ff.equal(ff.property("foo"), ff.literal("john"));
        And and = ff.and(nn, eq);
    }

//    @Test
//    public void bbox() {
//        ff.bbox(geometry, bounds);
//    }
//
//    public void between() {
//        ff.between(ff.property("value"), ff.literal(1), ff.literal(9));
//    }
//    public void beyond() {        
//        Geometry poly = ff.beyond(ff.literal(poly), ff.literal(point), 3.0, "km");
//
//    }
}
