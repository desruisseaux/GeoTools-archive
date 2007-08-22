/*
 * Created on 2006.11.21.
 *
 * $Id$
 *
 */
package org.geotools.data.gpx.memory;

import java.util.ArrayList;
import java.util.List;


public class Gpx {
    private GpxMetadata metadata;
    private List waypoints;
    private List tracks;

    public GpxMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(GpxMetadata metadata) {
        this.metadata = metadata;
    }

    public List getTracks() {
        return tracks;
    }

    public void setTracks(List tracks) {
        this.tracks = tracks;
    }

    public void addTrack(GpxTrack track) {
        if (tracks == null) {
            tracks = new ArrayList();
        }

        tracks.add(track);
    }

    public List getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List waypoints) {
        this.waypoints = waypoints;
    }

    public void addWaypoint(GpxPoint waypoint) {
        if (waypoints == null) {
            waypoints = new ArrayList();
        }

        waypoints.add(waypoint);
    }
}
