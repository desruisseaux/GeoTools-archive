/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid.test;

import java.io.File;
import java.util.Iterator;

import org.geotools.data.arcgrid.test.TestCaseSupport;
import org.geotools.data.coverage.grid.GridCoverageExchange;
import org.geotools.data.coverage.grid.GridCoverageExchangeFinder;


/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GridCoverageExchangeFinderTest extends TestCaseSupport {

    /**
     * @param name
     */
    public GridCoverageExchangeFinderTest(String name) {
        super(name);
    }

    public void testGetAvailableExchanges() {
        Iterator iter=GridCoverageExchangeFinder.getAvailableExchanges();
        assertTrue(iter.hasNext());
		while (iter.hasNext())
		    iter.next();    }

    public void testGetExchange() throws Exception{
        File[] f=File.listRoots();
        GridCoverageExchange[] gce=GridCoverageExchangeFinder.getExchange(f[0].toURL());
        assertNotNull(gce);
    }

}
