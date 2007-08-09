package org.geotools.filter.function;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

public class StringFunctionTest extends TestCase {

    public void testStrReplace() {
        
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Literal s1 = ff.literal("foo");
        Literal s2 = ff.literal("o");
        Literal s3 = ff.literal("bar");
        Literal b = ff.literal(true);
        
        Function f = ff.function("strReplace", new Expression[]{s1,s2,s3,b});
        String s = (String) f.evaluate(null,String.class);
        assertEquals( "fbarbar", s );
        
        b = ff.literal(false);
        f = ff.function("strReplace", new Expression[]{s1,s2,s3,b});
        s = (String) f.evaluate(null,String.class);
        assertEquals( "fbaro", s );
    }
}
