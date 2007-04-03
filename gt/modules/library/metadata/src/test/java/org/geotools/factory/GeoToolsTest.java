package org.geotools.factory;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class GeoToolsTest extends TestCase {

    /** DefaultInitialization should occure during class load */
    public void testInitializationRequired(){
        Hints hints = GeoTools.getDefaultHints();
        assertNotNull( hints );
    }
    public void testSystemHints(){
        GeoTools.init( null ); // use SystemHints
        
        Hints hints = GeoTools.getDefaultHints();
        assertNotNull( hints );
    }
    public void testMyHints(){
        Map defaults = new HashMap();        
        // require use of indicated FilterFactory
        defaults.put( Hints.FILTER_FACTORY, "org.geotools.filter.FilterFactoryImpl");
        
        Hints hints = new Hints( defaults );
        
        GeoTools.init( hints );        
        assertSame( hints, GeoTools.getDefaultHints() );
    }
    
}
