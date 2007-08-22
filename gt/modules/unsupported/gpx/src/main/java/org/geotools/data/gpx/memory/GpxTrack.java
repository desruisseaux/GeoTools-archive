/*
 * Created on 2006.11.21.
 *
 * $Id$
 *
 */
package org.geotools.data.gpx.memory;

import java.util.ArrayList;
import java.util.List;


public class GpxTrack {
    private String name;
    private List segments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getSegments() {
        return segments;
    }

    public void setSegments(List segments) {
        this.segments = segments;
    }

    public void addSegment(GpxTrackSegment segment) {
        if (segments == null) {
            segments = new ArrayList();
        }

        segments.add(segment);
    }
}
