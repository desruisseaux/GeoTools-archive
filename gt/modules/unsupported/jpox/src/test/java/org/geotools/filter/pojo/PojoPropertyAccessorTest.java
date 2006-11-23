package org.geotools.filter.pojo;

import java.util.Date;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;

import junit.framework.TestCase;

public class PojoPropertyAccessorTest extends TestCase {
    PojoPropertyAccessor access;
    private FilterFactory ff;
    
    protected void setUp() throws Exception {
        access = new PojoPropertyAccessor();
        ff = CommonFactoryFinder.getFilterFactory(null);
        super.setUp();
    }
    
    public void testDateAccess(){
        Date date = new Date();
        
        assertEquals( date.getHours(), access.get(date, ff.property( "hours" )) );
        
    }
    
    /**
     * Type fudge for the Java 5 impared.
     * @param expected new Integer( expected )
     * @param value
     */
    protected void assertEquals( int expected, Object value ){
        assertEquals( new Integer( expected), value );
    }
}
