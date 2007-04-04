package org.geotools.factory;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class GeoToolsTest extends TestCase {

    public void testNothing(){
        
    }
    /** DefaultInitialization should occure during class load */
    public void XtestInitializationRequired(){
        Hints hints = GeoTools.getDefaultHints();
        assertNotNull( hints );
    }
    public void XtestSystemHints(){
        GeoTools.init( null ); // use SystemHints
        
        Hints hints = GeoTools.getDefaultHints();
        assertNotNull( hints );
    }
    public void XtestMyHints(){
        Map defaults = new HashMap();        
        // require use of indicated FilterFactory
        defaults.put( Hints.FILTER_FACTORY, "org.geotools.filter.FilterFactoryImpl");
        
        Hints hints = new Hints( defaults );
        
        GeoTools.init( hints );
        assertEquals( "same same", hints, GeoTools.getDefaultHints() );
        assertNotSame( "but different", hints, GeoTools.getDefaultHints() );        
    }
    
}
