package org.geotools.caching.firstdraft.util;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;


public class IndexUtilities {
    /** Transform a JTS Envelope to a Region
    *
    * @param JTS Envelope
    * @return Region
    */
    public static Region toRegion(final Envelope e) {
        Region r = new Region(new double[] { e.getMinX(), e.getMinY() },
                new double[] { e.getMaxX(), e.getMaxY() });

        return r;
    }
}
