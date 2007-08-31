/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.caching.grid;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.Coordinate;
import org.opengis.filter.Filter;
import org.geotools.caching.util.Generator;
import org.geotools.feature.DefaultFeatureCollection;


public class DataUtilities {
    public static DefaultFeatureCollection createUnitsquareDataSet(int numdata) {
        Generator gen = new Generator(1, 1);
        DefaultFeatureCollection dataset = new DefaultFeatureCollection("Test", Generator.type);

        for (int i = 0; i < numdata; i++) {
            dataset.add(gen.createFeature(i));
        }

        return dataset;
    }

    public static ArrayList<Filter> createUnitsquareFilterSet(int numfilters, double[] windows) {
        ArrayList<Filter> filterset = new ArrayList<Filter>(numfilters);

        //Coordinate p = Generator.pickRandomPoint(new Coordinate(0.5, 0.5), .950, .950);
        Coordinate p = new Coordinate(0.5, 0.5);

        for (int i = 0; i < numfilters; i += windows.length) {
            for (int j = 0; j < windows.length; j++) {
                filterset.add(Generator.createBboxFilter(p, windows[j], windows[j]));
                p = Generator.pickRandomPoint(p, windows[j], windows[j]);
            }
        }

        return filterset;
    }
}
