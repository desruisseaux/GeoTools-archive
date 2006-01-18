/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
 * (C) 1998, P�ches et Oc�ans Canada
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
package org.geotools.geometry.array;

// J2SE directdependencies
import java.util.List;
import java.util.ArrayList;

// OpenGIS direct dependencies
import org.opengis.util.CodeList;

// JTS dependencies
import com.vividsolutions.jts.geom.Coordinate;  // For javadoc only


/**
 * The compression level for coordinate points in a {@link PointArray2D} object. Compressions
 * consists in a change of the storage type for (<var>x</var>,<var>y</var>) coordinates. Note
 * that the compression may be destructive, i.e. it may sacrifice data and/or precision.  For
 * example,  <A HREF="http://www.vividsolutions.com/JTS/jts_frame.htm">JTS</A> 1.3 stores points
 * as {@link Coordinate} objects with (<var>x</var>,<var>y</var>,<var>z</var>) {@code double}
 * values, which consume a lot of memory. The compression level {@link #DIRECT_AS_FLOATS} copies
 * the (<var>x</var>,<var>y</var>) ordinates in a {@code float[]} array, loosing the <var>z</var>
 * value and some precision due to the conversion of {@code double} to {@code float} values.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CompressionLevel extends CodeList {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2242053632880294753L;

    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List/*<CompressionLevel>*/ VALUES = new ArrayList/*<CompressionLevel>*/(2);

    /**
     * Transforms coordinate points into direct positions stored as {@code float} values.
     * This compression level has no effect if data are already stored as {@code float},
     * or if a more agressive compression is already in use (e.g. {@link #RELATIVE_AS_BYTES}).
     */
    public static final CompressionLevel DIRECT_AS_FLOATS = new CompressionLevel("DIRECT_AS_FLOATS");

    /**
     * Transforms coordinate points into relative positions stored as {@code byte} values.
     * This compression mode replaces absolute positions (left handed image below) by relative
     * positions (right handed image below), i.e. distances relative to the previous point.
     * Assuming that all distances are of similar magnitude, distances can be coded in
     * {@code byte} primitive type instead of <code>float</code>.
     * <p>
     * <table cellspacing='12'><tr>
     *   <td><p align="center"><img src="doc-files/uncompressed.png"></p></td>
     *   <td><p align="center"><img src="doc-files/compressed.png"></p></td>
     * </tr></table>
     * <p>
     * Before the compression, the coordinates may be resampled (if needed) in order to obtain
     * line segments of equal length. This compression level has no effect if the data are already
     * compressed as relative positions.
     */
    public static final CompressionLevel RELATIVE_AS_BYTES = new CompressionLevel("RELATIVE_AS_BYTES");

    /**
     * Constructs an enum with the given name. The new enum is
     * automatically added to the list returned by {@link #values}.
     *
     * @param name The enum name. This name must not be in use by an other enum of this type.
     */
    public CompressionLevel(final String name) {
        super(name, VALUES);
    }

    /**
     * Returns the list of {@code CompressionLevel}s.
     */
    public static CompressionLevel[] values() {
        synchronized (VALUES) {
            return (CompressionLevel[]) VALUES.toArray(new CompressionLevel[VALUES.size()]);
        }
    }

    /**
     * Returns the list of enumerations of the same kind than this enum.
     */
    public /*{CompressionLevel}*/ CodeList[] family() {
        return values();
    }
}
