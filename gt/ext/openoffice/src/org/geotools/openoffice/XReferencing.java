/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.openoffice;

// OpenOffice dependencies
import com.sun.star.uno.XInterface;
import com.sun.star.beans.XPropertySet;


/**
 * The services to be exported to <A HREF="http://www.openoffice.org">OpenOffice</A>.
 * This interface is derived from the {@code XReferencing.idl} file using the {@code javamaker}
 * tool provided in OpenOffice SDK, and disassembling the output using the {@code javap} tool
 * provided in Java SDK. This source file exists mostly for javadoc purpose and in order to keep
 * IDE happy. The {@code .class} file compiled from this source file <strong>MUST</strong> be
 * overwritten by the {@code .class} file generated by {@code javamaker}.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface XReferencing extends XInterface {
    /**
     * Converts text in degrees-minutes-seconds to an angle in decimal degrees.
     *
     * @param xOptions Provided by OpenOffice.
     * @param text The text to be converted to an angle.
     */
    double parseAngle(XPropertySet xOptions, String text);

    /**
     * Converts an angle to text according to a given format.
     *
     * @param xOptions Provided by OpenOffice.
     * @param value The angle value (in decimal degrees) to be converted.
     * @param pattern The text that describes the format (example: "D�MM.m'").
     */
    String formatAngle(XPropertySet xOptions, double value, String pattern);

    /**
     * Converts a longitude to text according to a given format.
     *
     * @param xOptions Provided by OpenOffice.
     * @param value The longitude value (in decimal degrees) to be converted.
     * @param pattern The text that describes the format (example: "D�MM.m'").
     */
    String formatLongitude(XPropertySet xOptions, double value, String pattern);

    /**
     * Converts a latitude to text according to a given format.
     *
     * @param xOptions Provided by OpenOffice.
     * @param value The latitude value (in decimal degrees) to be converted.
     * @param pattern The text that describes the format (example: "D�MM.m'").
     */
    String formatLatitude(XPropertySet xOptions, double value, String pattern);

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
     * Returns the remarks for an identified object.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     */
    String getRemarks(XPropertySet xOptions, String authorityCode);

    /**
     * Returns the Well Know Text (WKT) for an identified object.
     *
     * @param xOptions Provided by OpenOffice.
     * @param authorityCode The code allocated by the authority.
     */
    String getWKT(XPropertySet xOptions, String authorityCode);

    /**
     * Returns the accuracy of a transformation between two coordinate reference systems.
     *
     * @param xOptions Provided by OpenOffice.
     * @param sourceCRS The authority code for the source coordinate reference system.
     * @param sourceCRS The authority code for the target coordinate reference system.
     */
    double getAccuracy(XPropertySet xOptions, String sourceCRS, String targetCRS);

    /**
     * Transforms coordinates from the specified source CRS to the specified target CRS.
     *
     * @param xOptions Provided by OpenOffice.
     * @param coordinates The coordinates to transform.
     * @param sourceCRS The authority code for the source coordinate reference system.
     * @param sourceCRS The authority code for the target coordinate reference system.
     */
    double[][] transform(XPropertySet xOptions, double[][] coordinates, String sourceCRS, String targetCRS);
}
