/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
import java.util.Collections;
import java.util.Map;

import javax.units.Unit;

import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Projection;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A 2D coordinate reference system used to approximate the shape of the earth on a planar surface.
 * It is done in such a way that the distortion that is inherent to the approximation is carefully
 * controlled and known. Distortion correction is commonly applied to calculated bearings and
 * distances to produce values that are a close match to actual field values.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link CartesianCS Cartesian}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ProjectedCRS extends org.geotools.referencing.crs.GeneralDerivedCRS
                       implements org.opengis.referencing.crs.ProjectedCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4502680112031773028L;

    /**
     * Constructs a projected CRS from a name.
     *
     * @param  name The name.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDeviced</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public ProjectedCRS(final String                 name,
                        final GeographicCRS          base,
                        final MathTransform baseToDerived,
                        final CartesianCS       derivedCS)
            throws MismatchedDimensionException
    {
        this(Collections.singletonMap(NAME_PROPERTY, name), base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a projected CRS from a set of properties. The properties are given unchanged to the
     * {@linkplain GeneralDerivedCRS#GeneralDerivedCRS(Map,CoordinateReferenceSystem,MathTransform,CoordinateSystem)
     * super-class constructor}.
     *
     * @param  properties Name and other properties to give to the new derived CRS object and to
     *         the underlying {@linkplain org.geotools.referencing.operation.Projection projection}.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDeviced</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public ProjectedCRS(final Map              properties,
                        final GeographicCRS          base,
                        final MathTransform baseToDerived,
                        final CartesianCS       derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, base, baseToDerived, derivedCS);
    }

    /**
     * Wraps the specified arguments in a {@link Projection} object. This method is invoked
     * by {@link GeneralDerivedCRS} constructor in order to construct a {@link Conversion}
     * object of the right kind.
     *
     * @todo Check the Projection subclasses (PlanarProjection, ConicProjection, etc.)
     */
    Conversion createConversion(final Map                       properties,
                                final CoordinateReferenceSystem sourceCRS,
                                final CoordinateReferenceSystem targetCRS,
                                final MathTransform             transform,
                                final OperationMethod           method)
    {
        return new org.geotools.referencing.operation.Projection(properties,
                    sourceCRS, targetCRS, transform, method);
    }

    /**
     * Returns a conversion from a source to target projected CRS, if this conversion
     * is representable as an affine transform. More specifically, if all projection
     * parameters are identical except the following ones:
     * <BR>
     * <UL>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#SCALE_FACTOR   scale_factor}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#SEMI_MAJOR     semi_major}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#SEMI_MINOR     semi_minor}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#FALSE_EASTING  false_easting}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#FALSE_NORTHING false_northing}</LI>
     * </UL>
     *
     * <P>Then the conversion between two projected CRS can sometime be represented as a linear
     * conversion. For example if only false easting/northing differ, than the coordinate conversion
     * is simply a translation. If no linear conversion has been found between the two CRS, then
     * this method returns <code>null</code>.</P>
     *
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @param  errorTolerance Relative error tolerance for considering two parameter values as
     *         equal. This is usually a small number like 1E-12.
     * @return The conversion from <code>sourceCRS</code> to <code>targetCRS</code> as an
     *         affine transform, or <code>null</code> if no linear transform has been found.
     */
    public static Matrix createLinearConversion(
                         final org.opengis.referencing.crs.ProjectedCRS sourceCRS,
                         final org.opengis.referencing.crs.ProjectedCRS targetCRS,
                         final double errorTolerance)
    {
        if (!equals(sourceCRS.getBaseCRS(), targetCRS.getBaseCRS(), false)) {
            return null;
        }
        // TODO: remove the cast once we will be allowed to compile for J2SE 1.5.
        final Conversion sourceOp = (Conversion) sourceCRS.getConversionFromBase();
        final Conversion targetOp = (Conversion) targetCRS.getConversionFromBase();
        if (!equals(sourceOp.getMethod(), targetOp.getMethod(), false)) {
            return null;
        }
        final GeneralParameterValue[] sourceParams = sourceOp.getParameterValues();
        final GeneralParameterValue[] targetParams = targetOp.getParameterValues();
        if (sourceParams==null || targetParams==null) {
            return null;
        }
        double scaleX = 1;
        double scaleY = 1;
        double  oldTX = 0;
        double  oldTY = 0;
        double  newTX = 0;
        double  newTY = 0;
        final boolean[] done = new boolean[sourceParams.length];
search: for (int i=0; i<targetParams.length; i++) {
            final GeneralParameterValue      targetParam = targetParams[i];
            final GeneralParameterDescriptor descriptor  = targetParam.getDescriptor();
            final String                     name        = descriptor.getName().getCode();
            for (int j=0; j<sourceParams.length; j++) {
                if (done[j]) {
                    continue;
                }
                final GeneralParameterValue sourceParam = sourceParams[j];
                if (nameMatches(sourceParam.getDescriptor(), name)) {
                    if (sourceParam instanceof ParameterValue &&
                        targetParam instanceof ParameterValue)
                    {
                        /*
                         * A pair of parameter values has been found (i.e. parameter with the
                         * same name in the source and destination arrays).   Now, search for
                         * map projection parameters  that can been factored out in an affine
                         * transform.  All other parameters (including non-numeric ones) must
                         * be identical.
                         */
                        final ParameterValue parameter = (ParameterValue) targetParam;
                        final ParameterValue candidate = (ParameterValue) sourceParam;
                        if (Number.class.isAssignableFrom(
                            ((ParameterDescriptor) descriptor).getValueClass()))
                        {
                            final double targetValue;
                            final double sourceValue;
                            final Unit unit = parameter.getUnit();
                            if (unit != null) {
                                targetValue = parameter.doubleValue(unit);
                                sourceValue = candidate.doubleValue(unit);
                            } else {
                                targetValue = parameter.doubleValue();
                                sourceValue = candidate.doubleValue();
                            }
                            if (nameMatches(descriptor, "scale_factor")) {
                                final double scale = targetValue / sourceValue;
                                scaleX *= scale;
                                scaleY *= scale;
                            } else if (nameMatches(descriptor, "semi_major")) {
                                scaleX *= (targetValue / sourceValue);
                            } else if (nameMatches(descriptor, "semi_minor")) {
                                scaleY *= (targetValue / sourceValue);
                            } else if (nameMatches(descriptor, "false_easting")) {
                                oldTX += sourceValue;
                                newTX += targetValue;
                            } else if (nameMatches(descriptor, "false_northing")) {
                                oldTY += sourceValue;
                                newTY += targetValue;
                            } else {
                                double error = (targetValue - sourceValue);
                                if (targetValue!=0) error /= targetValue;
                                if (!(Math.abs(error) <= errorTolerance)) { // '!' for trapping NaN
                                    return null;
                                }
                            }
                        } else if (!Utilities.equals(parameter.getValue(), candidate.getValue())) {
                            return null;
                        }
                    } else if (!Utilities.equals(targetParam, sourceParam)) {
                        return null;
                    }
                    /*
                     * End of processing of the pair of matching parameters.
                     * Search for a new pair.
                     */
                    done[j] = true;
                    continue search;
                }
            }
            /*
             * End of search in the array of source parameter.
             * A parameter in the target has no matching parameter in source.
             */
            return null;
        }
        /*
         * End of parameter comparaison. Check if there is any parameter in
         * the source array without a matching parameter in the destination
         * array.
         */
        for (int i=0; i<done.length; i++) {
            if (!done[i]) {
                return null;
            }
        }
        /*
         * At this stage, we have found exact matching pairs for all parameters,
         * and the only parameters to differ are the special one representables
         * in an affine transform. 'scaleX' and 'scaleY' must be identical since
         * they are actually about semi-major and semi-minor axis length, which
         * are involved in non-linear calculations.
         */
        if (!(Math.abs(scaleX - scaleY) <= errorTolerance)) { // '!' for trapping NaN
            return null;
        }
        /*
         * Creates the matrix (including axis order changes and unit conversions),
         * and apply the scale and translation inferred from the  "false_easting"
         * parameter and its friends. We perform the conversion in three conceptual
         * steps (in the end, everything is bundle in a single matrix):
         *
         *   1) remove the old false northing/easting
         *   2) apply the scale
         *   3) add the new false northing/easting
         *
         * Note that those operation are performed in units of the target CRS.
         */
        final double scale = 0.5 * (scaleX + scaleY);
        final CoordinateSystem sourceCS = sourceCRS.getCoordinateSystem();
        final CoordinateSystem targetCS = targetCRS.getCoordinateSystem();
        final Matrix matrix = org.geotools.referencing.cs.CoordinateSystem.
                              swapAndScaleAxis(sourceCS, targetCS);
        final int sourceDim = sourceCS.getDimension();
        final int targetDim = targetCS.getDimension();
        for (int j=0; j<targetDim; j++) {
            final AxisDirection axis = targetCS.getAxis(j).getDirection();
            final double oldT, newT;
            if (AxisDirection.EAST.equals(axis)) {
                oldT = +oldTX;
                newT = +newTX;
            } else if (AxisDirection.WEST.equals(axis)) {
                oldT = -oldTX;
                newT = -newTX;
            } else if (AxisDirection.NORTH.equals(axis)) {
                oldT = +oldTY;
                newT = +newTY;
            } else if (AxisDirection.SOUTH.equals(axis)) {
                oldT = -oldTY;
                newT = -newTY;
            } else {
                continue;
            }
            // Apply the scale. Usually all elements on the same row are equal to zero,
            // except one element in a column which depends on the source axis position.
            // Note that we must multiply the last column (unit offset) as well.
            for (int i=0; i<=sourceDim; i++) {
                matrix.setElement(j,i, matrix.getElement(j,i) * scale);
            }
            // Apply the translation. The old value in the matrix is usually 0,
            // but could be non-zero for some unit conversion, which we keep.
            matrix.setElement(j, sourceDim, matrix.getElement(j, sourceDim) + (newT - oldT*scale));
        }
        return matrix;
    }
    
    /**
     * Returns a hash value for this projected CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "PROJCS"
     */
    protected String formatWKT(final Formatter formatter) {
        final Unit unit = getUnit();
        final Unit linearUnit  = formatter.getLinearUnit();
        final Unit angularUnit = formatter.getAngularUnit();
        formatter.setLinearUnit(unit);
        formatter.setAngularUnit(org.geotools.referencing.crs.GeographicCRS.
                                 getAngularUnit(baseCRS.getCoordinateSystem()));
        formatter.append(baseCRS);
        formatter.append(conversionFromBase.getMethod());
        final GeneralParameterValue[] parameters = conversionFromBase.getParameterValues();
        for (int i=0; i<parameters.length; i++) {
            formatter.append(parameters[i]);
        }
        formatter.append(unit);
        final int dimension = coordinateSystem.getDimension();
        for (int i=0; i<dimension; i++) {
            formatter.append(coordinateSystem.getAxis(i));
        }
        if (unit == null) {
            formatter.setInvalidWKT();
        }
        formatter.setAngularUnit(angularUnit);
        formatter.setLinearUnit(linearUnit);
        return "PROJCS";
    }
}
