/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.cs;

// JAI dependencies
import javax.media.jai.ParameterList;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Latitude;
import org.geotools.pt.Longitude;
import org.geotools.pt.CoordinatePoint;


/**
 * Generate Automatic Projections (dynamic projections) based on code and location.
 * Automatic Projections are defined in Annex E of OGC-01-068r3.
 *
 * <ul>
 *   <li>AUTO projection codes are in the range 42000-42499</li>
 *   <li><var>lon0</var> and <var>lat0</var> are centeral point of the projection</li>
 * </ul>
 * <p>
 * The lon0/lat0 are provided by the SRS parameter of the map request
 * (see Section 6.5.5.2 of OGC 01-068r3)
 * </p>
 * <p>
 * This is a first-attempt CoordinateSystemAuthority to me and is not up
 * to the usual high standards of the rest of this package. Please aid in
 * improving this class with bug reports etc...
 * </p> 
 * @version $Id$
 * @author Jody Garnett
 */
public class CoordinateSystemAUTOFactory extends CoordinateSystemAuthorityFactory {
    /**
     * Construct a authority factory backed by the specified factory.
     *
     * @param factory The underlying factory used for objects creation.
     */
    public CoordinateSystemAUTOFactory(final CoordinateSystemFactory factory) {
        super(factory);
    }

    /**
     * Returns the authority name, which is known as "Automatic".
     * <p>
     * I assume this is the "display name" presented to end users?
     * </p>
     * @task REVISIT: "AUTO" is not really an authority name. Which organisation
     *                is the authour of annex E in OGC-01-068r3?
     */
    public String getAuthority() {
        return "AUTO";
    }

    /**
     * {@inheritDoc}
     */
    public Unit createUnit(final String code) throws FactoryException {
        final Code c = new Code(code, "Unit");
        switch (c.code) {
            case 42001: return Unit.DEGREE;
        }
        throw new NoSuchAuthorityCodeException(code);
    }

    /**
     * {@inheritDoc}
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        final Code c = new Code(code, "Ellipsoid");
        switch (c.code) {
            case 42001: return Ellipsoid.WGS84;
        }
        throw new NoSuchAuthorityCodeException(code);
    }

    /**
     * {@inheritDoc}
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        final Code c = new Code(code, "PrimeMeridian");
        switch (c.code) {
            case 42001: return PrimeMeridian.GREENWICH;
        }
        throw new NoSuchAuthorityCodeException(code);
    }

    /**
     * {@inheritDoc}
     */
    public Datum createDatum(String code) throws FactoryException {
        final Code c = new Code(code, "Datum");
        switch (c.code) {
            case 42001: return HorizontalDatum.WGS84;
        }
        throw new NoSuchAuthorityCodeException(code);
    }

    /**
     * {@inheritDoc}
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        final Code c = new Code(code, "Datum");
        switch (c.code) {
            case 42001: return create42001(c);
        }
        throw new NoSuchAuthorityCodeException(code);
    }

    /**
     * WGS 84 / Auto UTM creation for AUTO:42001
     * <p>
     * From the OGC 01-068r3:
     * <pre><code>
     * PROJCS["WGS 84 / Auto UTM",
     *   [GEOGCS["WGS 84",
     *       DATUM["WGS_1984",
     *           SPHEROID["WGS_1984", 6378137, 298.257223564]
     *       ],
     *       PRIMEM["Greenwich",0],
     *       UNIT["Decimal_Degree", 0.0174532925199433]
     *   ],
     *   PROJECTON["Transverse_Mercator"],
     *   PARAMETER["Central_Meridian", $centralMeridian ],
     *   PARAMETER["Latitude_of_Origion", 0 ],
     *   PARAMETER["False_Easting", 500000 ],
     *   PARAMETER["False_Northing", $falseNorthing ],
     *   PARAMETER["Scale_Factor", 0.9996 ],
     *   UNIT["Meter",1],
     * ]
     * </code></pre>
     * </p>
     * <p>
     * Where:
     * <ul>
     * <li>$centralMeridian = -183 + $zone * 6
     * <li>$zone = min( floor( $lon0 + 180.0)/6)+1, 60 )
     * <li>$falseNorthing = $lat0 >= 0 ? 0 : 10000000;
     * </ul>
     * </p>
     *
     * @param code The code.
     * @return The coordinate system.
     * @throws FactoryException if the coordinate system can't be created.
     */
    private CoordinateSystem create42001(final Code code) throws FactoryException {
        final double   falseNorthing   = code.latitude >= 0.0 ? 0.0 : 10000000.0;
        final double   zone            = Math.min(Math.floor((code.longitude + 180.0)/6.0)+1, 60);
        final double   centralMeridian = -183.0 + zone*6.0;
        final String   classification  = "Transverse_Mercator";
        final ParameterList parameters = factory.createProjectionParameterList(classification);
        parameters.setParameter("central_meridian", centralMeridian);
        parameters.setParameter("false_northing",   falseNorthing);
        final Projection projection = factory.createProjection("Auto UTM", classification, parameters);
        return factory.createProjectedCoordinateSystem("WGS 84 / Auto UTM",
                                                       GeographicCoordinateSystem.WGS84,
                                                       projection);
    }

    /**
     * A code parsed by the {@link CoordinateSystemAUTOFactory#parseCode} method.
     * The expected format is <code>code|lon0|lat0</code>.
     *
     * @version $Id$
     * @author Jody Garnett
     * @author Martin Desruisseaux
     */
    private static class Code {
        /**
         * The code number.
         */
        public int code;

        /**
         * The central longitude.
         */
        public double longitude = Double.NaN;

        /**
         * The central latitude.
         */
        public double latitude = Double.NaN;

        /**
         * Parse the code string to retrive the code number and central longitude / latitude.
         * Assumed format is <code>AUTO:code|lon0|lat0</code>.
         *
         * @param  text The code in the <code>AUTO:code|lon0|lat0</code> format.
         * @param  classname The short class name of the class to be constructed (e.g. "Ellipsoid").
         *         Used only in case of failure for constructing an error message.
         * @throws NoSuchAuthorityCodeException if the specified code can't be parsed.
         */
        public Code(final String text, final String classname) throws NoSuchAuthorityCodeException {
            int startField = -1;
    parse:  for (int i=0; ; i++) {
                int endField = text.indexOf('|', ++startField);
                if (endField < 0) {
                    endField = text.length();
                }
                if (endField <= startField) {
                    // A required field was not found.
                    throw new NoSuchAuthorityCodeException(text, classname);
                }
                final String field = text.substring(startField, endField).trim();
                try {
                    switch (i) {
                        case 0:  code      = Integer.parseInt  (field); break;
                        case 1:  longitude = Double.parseDouble(field); break;
                        case 2:  latitude  = Double.parseDouble(field); break;
                        // Add case statements here if the is more fields to parse.
                        default: break parse;
                    }
                } catch (NumberFormatException exception) {
                    // If a number can't be parsed, then this is an invalid authority code.
                    NoSuchAuthorityCodeException e = new NoSuchAuthorityCodeException(text, classname);
                    e.initCause(exception);
                    throw e;
                }
                startField = endField;
            }
            if (!(longitude>=Longitude.MIN_VALUE && longitude<=Longitude.MAX_VALUE &&
                  latitude >= Latitude.MIN_VALUE && latitude <= Latitude.MAX_VALUE))
            {
                // A longitude or latitude is out of range, or was not present
                // (i.e. the field still has a NaN value).
                throw new NoSuchAuthorityCodeException(text, classname);
            }
        }
    }
}
