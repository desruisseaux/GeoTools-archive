/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.factory.FactoryFinder;

/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GridCoverageExchangeFinder {
    private GridCoverageExchangeFinder(){}
    
    /**
     * Analyses the datasource and attempts to determine what type of GridCoverage
     * Exchange can be communicate with the datasource
     * 
     * @param datasource identifies a source of GridCoverages
     * @return GridCoverageExchange that can communication with the datasource
     * 	null if a GridCoverage is not known
     */
    public static GridCoverageExchange[] getExchange( URL datasource ){
        Set set=new HashSet();
        
        Iterator iter=getAvailableExchanges();
        while(iter.hasNext()){
            GridCoverageExchange exchange=(GridCoverageExchange)iter.next();
            
            if( exchange.setDataSource(datasource) )
                set.add(exchange);
        }
        
        if(set.isEmpty())
            return null;
        
        GridCoverageExchange[] gce=new GridCoverageExchange[set.size()];
        return (GridCoverageExchange[]) set.toArray(gce);
    }
    
    /**
     * Finds all implemtaions of GridCoverageExchange which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     *
     * @return An iterator over all discovered datastores which have registered
     *         factories, and whose available method returns true.
     */
    public static Iterator getAvailableExchanges() {
        Set available = new HashSet();
        Iterator it = FactoryFinder.factories(GridCoverageExchange.class);

        while (it.hasNext()) {
            GridCoverageExchange exchange = (GridCoverageExchange) it.next();

            if (exchange.isAvailable()) {
                available.add(exchange);
            }
        }

        return available.iterator();
    }
    
    
}
