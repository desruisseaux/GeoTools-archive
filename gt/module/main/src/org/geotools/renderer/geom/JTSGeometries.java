/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le D�veloppement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.renderer.geom;

// JTS dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.math.Statistics;
import org.geotools.renderer.array.JTSArray;
import org.geotools.resources.Utilities;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * A geometry collection backed by one or many JTS
 * {@link com.vividsolutions.jts.geom.Geometry} objects.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class JTSGeometries extends org.geotools.renderer.geom.GeometryCollection {
    /**
     * Num�ro de version pour compatibilit� avec des
     * bathym�tries enregistr�es sous d'anciennes versions.
     */
    private static final long serialVersionUID = 1390543865440404086L;

    /**
     * Construct an initially empty collection using the
     * {@linkplain #DEFAULT_COORDINATE_SYSTEM default coordinate system}.
     * Geometries can be added using {@link #add} method.
     */
    public JTSGeometries() {
    }

    /**
     * Construct an initialy empty collection.
     * Geometries can be added using {@link #add} method.
     *
     * @param cs The coordinate system to use for all points in this geometry,
     *           or <code>null</code> if unknow.
     */
    public JTSGeometries(final CoordinateSystem cs) {
        super(cs);
    }

    /**
     * Construct a collection for the specified geometry. The {@link #getValue value} is
     * computed from the mean value of all {@link Coordinate#z} in the specified geometry.
     *
     * @param geometry The geometry to wrap, or <code>null</code> if none.
     *
     * @task TODO: The coordinate system currently default to PROMISCUOUS.
     *             We should find it from the SRID code.
     */
    public JTSGeometries(final Geometry geometry) {
        if (geometry!=null) try {
            add(geometry);
        } catch (TransformException exception) {
            // Should not happen, since this collection is suppose to be
            // set to the same coordinate system than the geometry.
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
        setValue((float)statistics(geometry).mean());
    }

    /**
     * Returns the coordinate system for the specified JTS geometry.
     *
     * @task TODO: We should construct the coordinate system from SRID using
     *             {@link org.geotools.cs.CoordinateSystemAuthorityFactory}.
     */
    private CoordinateSystem getCoordinateSystem(final Geometry geometry) {
//      final int id = geometry.getSRID();
        // TODO: construct CS here.
        return getCoordinateSystem();
    }

    /**
     * Compute statistics about the <var>z</var> values in the specified geometry.
     * Statistics include minimum, maximum, mean value and standard deviation.
     * Unknow classes are ignored.
     *
     * @param  geometry The geometry to analyse.
     * @return The statistics.
     */
    private static Statistics statistics(final Geometry geometry) {
        if (geometry instanceof Polygon) {
            final Polygon polygon = (Polygon) geometry;
            final Statistics stats = statistics(polygon.getExteriorRing());
            final int n = polygon.getNumInteriorRing();
            for (int i=0; i<n; i++) {
                stats.add(statistics(polygon.getInteriorRingN(i)));
            }
            return stats;
        }
        final Statistics stats = new Statistics();
        if (geometry instanceof GeometryCollection) {
            final GeometryCollection collection = (GeometryCollection) geometry;
            final int n = collection.getNumGeometries();
            for (int i=0; i<n; i++) {
                stats.add(statistics(collection.getGeometryN(i)));
            }
        }
        else if (geometry instanceof Point) {
            stats.add(((Point) geometry).getCoordinate().z);
        }
        else if (geometry instanceof LineString) {
            final LineString line = (LineString) geometry;
            final int n = line.getNumPoints();
            for (int i=0; i<n; i++) {
                stats.add(line.getCoordinateN(i).z);
            }
        }
        return stats;
    }

    /**
     * Returns the specified line string as a {@link Polyline} object.
     *
     * @param geometry The line string to add.
     */
    private Polyline toPolyline(final LineString geometry) {
        final Coordinate[] coords = geometry.getCoordinates();
        final Polyline polyline = new Polyline(new JTSArray(coords), getCoordinateSystem(geometry));
        if (coords[0].equals(coords[coords.length - 1])) {
            polyline.close();
        }
        return polyline;
    }

    /**
     * Add the specified point to this collection. This method should rarely be
     * used, since polylines are not designed for handling individual points.
     *
     * @param  geometry The point to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this collection's coordinate system.
     */
    private org.geotools.renderer.geom.Geometry addSF(final Point geometry)
            throws TransformException
    {
        Coordinate coord = geometry.getCoordinate();
        return add(new org.geotools.renderer.geom.Point(coord, getCoordinateSystem(geometry)));
    }

    /**
     * Add the specified line string to this collection.
     *
     * @param  geometry The line string to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this collection's coordinate system.
     */
    private org.geotools.renderer.geom.Geometry addSF(final LineString geometry)
            throws TransformException
    {
        return add(toPolyline(geometry));
    }

    /**
     * Add the specified polygon to this collection.
     *
     * @param  geometry The polygon to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this collection's coordinate system.
     */
    private org.geotools.renderer.geom.Geometry addSF(final Polygon geometry)
            throws TransformException
    {
        final org.geotools.renderer.geom.Polygon polygon =
                new org.geotools.renderer.geom.Polygon(toPolyline(geometry.getExteriorRing()));
        final int n = geometry.getNumInteriorRing();
        for (int i=0; i<n; i++) {
            polygon.addHole(toPolyline(geometry.getInteriorRingN(i)));
        }
        return add(polygon);
    }

    /**
     * Add the specified geometry collection to this collection.
     *
     * @param  geometry The geometry collection to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this collection's coordinate system.
     */
    private org.geotools.renderer.geom.Geometry addSF(final GeometryCollection geometry)
            throws TransformException
    {
        final JTSGeometries collection = new JTSGeometries(getCoordinateSystem());
        final int n = geometry.getNumGeometries();
        for (int i=0; i<n; i++) {
            collection.add(geometry.getGeometryN(i));
        }
        return add(collection);
    }

    /**
     * Add the specified geometry to this collection. The geometry must be one of
     * the following classes: {@link Point}, {@link LineString}, {@link Polygon}
     * or {@link GeometryCollection}.
     *
     * @param  geometry The geometry to add.
     * @return The geometry as a {@link org.geotools.renderer.geom.Geometry} wrapper. The style can
     *         be set using <code>add(geometry).{@link org.geotools.renderer.geom.Geometry#setStyle
     *         setStyle}(style)</code>.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this collection's coordinate system.
     * @throws IllegalArgumentException if the geometry is not a a valid class.
     */
    public org.geotools.renderer.geom.Geometry add(final Geometry geometry)
            throws TransformException, IllegalArgumentException
    {
        if (geometry instanceof Point) {
            return addSF((Point) geometry);
        }
        if (geometry instanceof LineString) {
            return addSF((LineString) geometry);
        }
        if (geometry instanceof Polygon) {
            return addSF((Polygon) geometry);
        }
        if (geometry instanceof GeometryCollection) {
            return addSF((GeometryCollection) geometry);
        }
        throw new IllegalArgumentException(Utilities.getShortClassName(geometry));
    }

    /**
     * Freeze this collection. 
     */
    final void freeze() {
        super.freeze();
    }
}
