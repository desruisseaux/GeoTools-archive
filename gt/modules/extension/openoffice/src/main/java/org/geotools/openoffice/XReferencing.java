/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.openoffice;

// OpenOffice dependencies
import com.sun.star.uno.XInterface;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;


/**
 * Services from the {@link org.geotools.referencing} package to be exported to
 * <A HREF="http://www.openoffice.org">OpenOffice</A>.
 *
 * This interface is derived from the {@code XReferencing.idl} file using the {@code javamaker}
 * tool provided in OpenOffice SDK, and disassembling the output using the {@code javap} tool
 * provided in Java SDK. This source file exists mostly for javadoc purpose and in order to keep
 * IDE happy. The {@code .class} file compiled from this source file <strong>MUST</strong> be
 * overwritten by the {@code .class} file generated by {@code javamaker}.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface XReferencing extends XInterface {
    /**
     * Converts text in degrees-minutes-seconds to an angle in decimal degrees.
     *
     * @param xOptions Provided by OpenOffice.
     * @param text The text to be converted to an angle.
     * @param pattern The text that describes the format (example: "D°MM.m'").
     * @throws IllegalArgumentException if {@code pattern} is illegal.
     */
    double getValueAngle(XPropertySet xOptions, String text, Object pattern)
            throws IllegalArgumentException;

    /**
     * Converts an angle to text according to a given format.
     *
     * @param xOptions Provided by OpenOffice.
     * @param value The angle value (in decimal degrees) to be converted.
     * @param pattern The text that describes the format (example: "D°MM.m'").
     * @throws IllegalArgumentException if {@code pattern} is illegal.
     */
    String getTextAngle(XPropertySet xOptions, double value, Object pattern)
            throws IllegalArgumentException;

    /**
     * Converts a longitude to text according to a given format.
     *
     * @param xOptions Provided by OpenOffice.
     * @param value The longitude value (in decimal degrees) to be converted.
     * @param pattern The text that describes the format (example: "D°MM.m'").
     * @throws IllegalArgumentException if {@code pattern} is illegal.
     */
    String getTextLongitude(XPropertySet xOptions, double value, Object pattern)
            throws IllegalArgumentException;

    /**
     * Converts a latitude to text according to a given format.
     *
     * @param xOptions Provided by OpenOffice.
     * @param value The latitude value (in decimal degrees) to be converted.
     * @param pattern The text that describes the format (example: "D°MM.m'").
     * @throws IllegalArgumentException if {@code pattern} is illegal.
     */
    String getTextLatitude(XPropertySet xOptions, double value, Object pattern)
            throws IllegalArgumentException;

    /**
     * Returns the identified object description from an authority code.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     */
    String getDescription(XPropertySet xOptions, String authorityCode);

    /**
     * Returns the scope for an identified object.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     */
    String getScope(XPropertySet xOptions, String authorityCode);

    /**
     * Returns the valid area as a textual description for an identified object.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     */
    String getValidArea(XPropertySet xOptions, String authorityCode);

    /**
     * Returns the valid area as a geographic bounding box for an identified object. This method
     * returns a 2&times;2 matrix. The first row contains the latitude and longitude of upper left
     * corder, and the second row contains the latitude and longitude or bottom right corner. Units
     * are degrees.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     */
    double[][] getBoundingBox(XPropertySet xOptions, String authorityCode);

    /**
     * Returns the remarks for an identified object.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     */
    String getRemarks(XPropertySet xOptions, String authorityCode);

    /**
     * Returns the axis name for the specified dimension in an identified object.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     * @param dimension The dimension (1, 2, ...).
     */
    String getAxis(XPropertySet xOptions, String authorityCode, int dimension);

    /**
     * Returns the value for a coordinate reference system parameter.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     * @param parameter The parameter name (e.g. "False easting").
     */
    Object getParameter(XPropertySet xOptions, String authorityCode, String parameter);

    /**
     * Returns the Well Know Text (WKT) for an identified object.
     *
     * @param xOptions      Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     * @param authority     The authority name for choice of parameter names. Usually "OGC".
     */
    String getWKT(XPropertySet xOptions, String authorityCode, Object authority);

    /**
     * Returns the Well Know Text (WKT) of a transformation between two coordinate reference
     * systems.
     *
     * @param xOptions Provided by OpenOffice.
     * @param sourceCRS The authority code for the source coordinate reference system.
     * @param targetCRS The authority code for the target coordinate reference system.
     * @param authority The authority name for choice of parameter names. Usually "OGC".
     */
    String getTransformWKT(XPropertySet xOptions, String sourceCRS, String targetCRS, Object authority);

    /**
     * Returns the accuracy of a transformation between two coordinate reference systems.
     *
     * @param xOptions Provided by OpenOffice.
     * @param sourceCRS The authority code for the source coordinate reference system.
     * @param targetCRS The authority code for the target coordinate reference system.
     */
    double getAccuracy(XPropertySet xOptions, String sourceCRS, String targetCRS);

    /**
     * Transforms coordinates from the specified source CRS to the specified target CRS.
     *
     * @param xOptions Provided by OpenOffice.
     * @param coordinates The coordinates to transform.
     * @param sourceCRS The authority code for the source coordinate reference system.
     * @param targetCRS The authority code for the target coordinate reference system.
     */
    double[][] getTransformedCoordinates(XPropertySet xOptions, double[][] coordinates,
                                         String sourceCRS, String targetCRS);

    /**
     * Computes the orthodromic distance and azimuth between two coordinates.
     *
     * @param xOptions Provided by OpenOffice.
     * @param source The source positions.
     * @param target The target positions.
     * @param CRS Authority code of the coordinate reference system.
     */
    double[][] getOrthodromicDistance(XPropertySet xOptions, double[][] source, double[][] target, Object CRS);

    /**
     * Computes the coordinates after a displacement of the specified distance.
     *
     * @param xOptions Provided by OpenOffice.
     * @param source The source positions.
     * @param displacement The distance and azimuth.
     * @param CRS Authority code of the coordinate reference system.
     */
    double[][] getOrthodromicForward(XPropertySet xOptions, double[][] source, double[][] displacement, Object CRS);
}
