/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.coverage.grid;

import org.geotools.factory.FactoryFinder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Is responsible for locating and instantiating GridCoverageExchanges
 * The getExchange(Object) method returns all the object that can communicate with the
 * Object
 *
 * @author jeichar
 *
 */
public class GridCoverageExchangeFinder {
    
    private GridCoverageExchangeFinder() {
    }

    /**
     * Analyses the datasource and attempts to determine what type of GridCoverage
     * Exchange can be communicate with the datasource
     *
     * @param datasource identifies a source of GridCoverages
     * @return GridCoverageExchange that can communication with the datasource
     *         null if a GridCoverage is not known
     */
    public static GridCoverageExchange[] getExchange(Object datasource) {
        Set set = new HashSet();

        Iterator iter = getAvailableExchanges();

        while (iter.hasNext()) {
            GridCoverageExchange exchange = (GridCoverageExchange) iter.next();

            if (exchange.setDataSource(datasource)) {
                set.add(exchange);
            }
        }

        if (set.isEmpty()) {
            return null;
        }

        GridCoverageExchange[] gce = new GridCoverageExchange[set.size()];

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
