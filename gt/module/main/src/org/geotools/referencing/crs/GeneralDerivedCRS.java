/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.crs;

// J2SE dependencies
import java.util.Map;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.referencing.Info;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A coordinate reference system that is defined by its coordinate
 * {@linkplain Conversion conversion} from another coordinate reference system
 * (not by a {@linkplain org.opengis.referencing.datum.Datum datum}).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeneralDerivedCRS extends org.geotools.referencing.crs.CoordinateReferenceSystem
                            implements org.opengis.referencing.crs.GeneralDerivedCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -175151161496419854L;

    /**
     * The base coordinate reference system.
     */
    protected final CoordinateReferenceSystem baseCRS;

    /**
     * The conversion from the {@linkplain #getBaseCRS base CRS} to this CRS.
     */
    protected final Conversion conversionFromBase;

    /**
     * Constructs a derived CRS from a name.
     *
     * @param name The name.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDeviced</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public GeneralDerivedCRS(final String                    name,
                             final CoordinateReferenceSystem base,
                             final MathTransform    baseToDerived,
                             final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        this(Collections.singletonMap("name", name), base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a geographic CRS from a set of properties.
     * The properties are given unchanged to the super-class constructor.
     *
     * @param  properties Name and other properties to give to the new object.
     *         Available properties are {@linkplain org.geotools.referencing.Factory listed there}.
     *         Properties for the {@link Conversion} object to be created can be specified
     *         with the <code>"conversion."</code> prefix added in front of property names
     *         (example: <code>"conversion.name"</code>).
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDeviced</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public GeneralDerivedCRS(final Map                 properties,
                             final CoordinateReferenceSystem base,
                             final MathTransform    baseToDerived,
                             final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, derivedCS, base.getDatum());
        ensureNonNull("base",          base); // TODO: useless test, because of 'base.getDatum()' above...
        ensureNonNull("baseToDerived", baseToDerived);
        this.baseCRS            = base;
        this.conversionFromBase = null; // TODO
        int dim1, dim2;
        if ((dim1=baseToDerived.getDimSource()) != (dim2=base.getCoordinateSystem().getDimension()) ||
            (dim1=baseToDerived.getDimTarget()) != (dim2=derivedCS.getDimension()))
        {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                        new Integer(dim1), new Integer(dim2)));
        }
    }

    /**
     * Returns the base coordinate reference system.
     *
     * @return The base coordinate reference system.
     */
    public CoordinateReferenceSystem getBaseCRS() {
        return baseCRS;
    }

    /**
     * Returns the conversion from the {@linkplain #getBaseCRS base CRS} to this CRS.
     *
     * @return The conversion to this CRS.
     */
    public Conversion getConversionFromBase() {
        return conversionFromBase;
    }

    /**
     * Compare this coordinate reference system with the specified object for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final GeneralDerivedCRS that = (GeneralDerivedCRS) object;
            return equals(this.baseCRS,            that.baseCRS,            compareMetadata) &&
                   equals(this.conversionFromBase, that.conversionFromBase, compareMetadata);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this derived CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ baseCRS.hashCode() ^ conversionFromBase.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "FITTED_CS"
     */
    protected String formatWKT(final Formatter formatter) {
        try {
            formatter.append(conversionFromBase.getMathTransform().inverse());
            formatter.append(baseCRS);
            return "FITTED_CS";
        } catch (NoninvertibleTransformException exception) {
            // TODO: provide a more accurate error message.
            IllegalStateException e = new IllegalStateException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }
}
