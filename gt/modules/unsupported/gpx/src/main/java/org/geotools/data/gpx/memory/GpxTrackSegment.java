/*
 * Created on 2006.11.21.
 *
 * $Id$
 *
 */
package org.geotools.data.gpx.memory;

import java.util.ArrayList;
import java.util.List;


public class GpxTrackSegment {
    private List points;

    public List getPoints() {
        return points;
    }

    public void setPoints(List points) {
        this.points = points;
    }

    public void addPoint(GpxPoint point) {
        if (points == null) {
            points = new ArrayList();
        }

        points.add(point);
    }
}
