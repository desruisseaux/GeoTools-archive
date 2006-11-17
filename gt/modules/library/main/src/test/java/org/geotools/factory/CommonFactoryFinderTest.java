package org.geotools.factory;

import org.geotools.filter.FilterFactoryFinder;

import junit.framework.TestCase;

public class CommonFactoryFinderTest extends TestCase {

    public void testGetStyleFactory() {
        assertNotNull( CommonFactoryFinder.getStyleFactories( null ));
    }

    public void testGetFilterFactory() {
        assertNotNull( FilterFactoryFinder.createFilterFactory() );
        assertNotNull( CommonFactoryFinder.getFilterFactory( null ));
    }

}
