/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.units.ConversionException;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.OperationMethod;

// Geotools dependencies
import org.geotools.util.Singleton;


/**
 * A set of utilities methods working on factories. Many of those methods requires more than
 * one factory. Concequently, they can't be a method in a single factory. Furthermore, since
 * they are helper methods and somewhat implementation-dependent, they are not part of GeoAPI.
 *
 * @version $Id
 * @author Martin Desruisseaux
 */
public class FactoryGroup {
    /**
     * The {@linkplain Datum datum} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private DatumFactory datumFactory;

    /**
     * The {@linkplain CoordinateSystem coordinate system} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private CSFactory csFactory;

    /**
     * The {@linkplain CoordinateReferenceSystem coordinate reference system} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private CRSFactory crsFactory;

    /**
     * The {@linkplain MathTransform math transform} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private MathTransformFactory mtFactory;

    /**
     * Constructs an instance using the default factories.
     * Default factories are:
     *
     * <blockquote><pre>
     * FactoryFinder.{@linkplain FactoryFinder#getDatumFactory           getDatumFactory()};
     * FactoryFinder.{@linkplain FactoryFinder#getCSFactory              getCSFactory()};
     * FactoryFinder.{@linkplain FactoryFinder#getCRSFactory             getCRSFactory()};
     * FactoryFinder.{@linkplain FactoryFinder#getMathTransformFactory() MathTransformFactory()};
     * </pre></blockquote>
     */
    public FactoryGroup() {
        this(null, null, null, null);
    }

    /**
     * Constructs an instance using the specified factories. If any factory is null,
     * a default instance will be created by {@link FactoryFinder} when first needed.
     *
     * @param datumFactory The {@linkplain Datum datum} factory.
     * @param    csFactory The {@linkplain CoordinateSystem coordinate system} factory.
     * @param   crsFactory The {@linkplain CoordinateReferenceSystem coordinate reference system}
     *                     factory.
     * @param    mtFactory The {@linkplain MathTransform math transform} factory.
     */
    public FactoryGroup(final DatumFactory      datumFactory,
                        final CSFactory            csFactory,
                        final CRSFactory          crsFactory,
                        final MathTransformFactory mtFactory)
    {
        this.datumFactory = datumFactory;
        this.csFactory    =    csFactory;
        this.crsFactory   =   crsFactory;
        this.mtFactory    =    mtFactory;
    }

    /**
     * Returns the {@linkplain Datum datum} factory.
     */
    public DatumFactory getDatumFactory() {
        if (datumFactory == null) {
            datumFactory = FactoryFinder.getDatumFactory();
        }
        return datumFactory;
    }

    /**
     * Returns the {@linkplain CoordinateSystem coordinate system} factory.
     */
    public CSFactory getCSFactory() {
        if (csFactory == null) {
            csFactory = FactoryFinder.getCSFactory();
        }
        return csFactory;
    }

    /**
     * Returns the {@linkplain CoordinateReferenceSystem coordinate reference system} factory.
     */
    public CRSFactory getCRSFactory() {
        if (crsFactory == null) {
            crsFactory = FactoryFinder.getCRSFactory();
        }
        return crsFactory;
    }

    /**
     * Returns the {@linkplain MathTransform math transform} factory.
     */
    public MathTransformFactory getMathTransformFactory() {
        if (mtFactory == null) {
            mtFactory = FactoryFinder.getMathTransformFactory();
        }
        return mtFactory;
    }

    /**
     * Creates a transform from a group of parameters and add the method used to a list.
     * This variant of <code>createParameterizedTransform(...)</code> provide a way for
     * the client to keep trace of any {@linkplain OperationMethod operation method}
     * used by this factory. 
     *
     * @param  parameters The parameter values.
     * @param  methods A collection where to add the operation method that apply to the transform,
     *                 or <code>null</code> if none.
     * @return The parameterized transform.
     * @throws NoSuchIdentifierException if there is no transform registered for the method.
     * @throws FactoryException if the object creation failed. This exception is thrown
     *         if some required parameter has not been supplied, or has illegal value.
     *
     * @see MathTransformFactory#createParameterizedTransform
     */
    public MathTransform createParameterizedTransform(ParameterValueGroup parameters,
                                                      Collection          methods)
            throws NoSuchIdentifierException, FactoryException
    {
        final MathTransform transform;
        final MathTransformFactory mtFactory = getMathTransformFactory();
        if (mtFactory instanceof org.geotools.referencing.operation.MathTransformFactory) {
            // Special processing for Geotools implementation.
            transform = ((org.geotools.referencing.operation.MathTransformFactory) mtFactory)
                        .createParameterizedTransform(parameters, methods);
        } else {
            // Not a geotools implementation. Try to guess the method.
            transform = mtFactory.createParameterizedTransform(parameters);
            final Set operations = mtFactory.getAvailableMethods(Operation.class);
            final String classification = parameters.getDescriptor().getName().getCode();
            for (final Iterator it=operations.iterator(); it.hasNext();) {
                final OperationMethod method = (OperationMethod) it.next();
                if (IdentifiedObject.nameMatches(method.getParameters(), classification)) {
                    methods.add(method);
                    break;
                }
            }
        }
        return transform;
    }

    /**
     * Creates a projected coordinate reference system from a set of parameters.
     * The client must supply at least the <code>"semi_major"</code> and
     * <code>"semi_minor"</code> parameters for cartographic projection.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  base Geographic coordinate reference system to base projection on.
     * @param  parameters The parameter values to give to the projection.
     * @param  derivedCS The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     */
    public ProjectedCRS createProjectedCRS(Map                 properties,
                                           GeographicCRS             base,
                                           ParameterValueGroup parameters,
                                           CartesianCS          derivedCS)
            throws FactoryException
    {
        /*
         * Computes matrix for swapping axis and performing units conversion.
         * There is one matrix to apply before projection on (longitude,latitude)
         * coordinates, and one matrix to apply after projection on (easting,northing)
         * coordinates.
         */
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final EllipsoidalCS geoCS = (EllipsoidalCS) base.getCoordinateSystem();
        final Matrix swap1, swap3;
        try {
            swap1 = org.geotools.referencing.cs.EllipsoidalCS.swapAndScaleAxis(geoCS,
                    org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_2D);
            swap3 = org.geotools.referencing.cs.CartesianCS.swapAndScaleAxis(
                    org.geotools.referencing.cs.CartesianCS.PROJECTED, derivedCS);
        } catch (IllegalArgumentException cause) {
            // User-specified axis don't match.
            throw new FactoryException(cause);
        } catch (ConversionException cause) {
            // A Unit conversion is non-linear.
            throw new FactoryException(cause);
        }
        /*
         * Create a concatenation of the matrix computed above and the projection.
         * If 'method' is null, an exception will be thrown in 'createProjectedCRS'.
         */
        final Singleton methods = new Singleton();
        final MathTransformFactory  mtFactory = getMathTransformFactory();
        final MathTransform step1 = mtFactory.createAffineTransform(swap1);
        final MathTransform step2 = createParameterizedTransform(parameters, methods);
        final MathTransform step3 = mtFactory.createAffineTransform(swap3);
        final MathTransform mt    = mtFactory.createConcatenatedTransform(
                                    mtFactory.createConcatenatedTransform(step1, step2), step3);
        return getCRSFactory().createProjectedCRS(properties, (OperationMethod) methods.get(),
                                                  base, mt, derivedCS);
    }

    /**
     * Converts a 2D&nbsp;+&nbsp;1D compound CRS into a 3D geographic CRS, if possible. More
     * specifically, if the specified {@linkplain CompoundCRS compound CRS} is made of a
     * {@linkplain GeographicCRS geographic} and a {@linkplain VerticalCRS vertical} CRS,
     * and if the vertical CRS datum type is {@linkplain VerticalDatumType#ELLIPSOIDAL height
     * above the ellipsoid}, then this method converts the compound CRS in a single 3D CRS.
     * Otherwise, the <code>crs</code> argument is returned unchanged.
     *
     * @param  crs The compound CRS to converts in a 3D geographic CRS.
     * @return The 3D geographic CRS, or <code>crs</code> if the conversion can't be applied.
     * @throws FactoryException if the object creation failed.
     *
     * @todo Consider extensions of this method to projected CRS if it is usefull for GEOT-401.
     */
    public CoordinateReferenceSystem toGeodetic3D(final CompoundCRS crs)
            throws FactoryException
    {
        final SingleCRS[] components = org.geotools.referencing.crs.CompoundCRS.getSingleCRS(crs);
        GeographicCRS horizontal = null;
        VerticalCRS   vertical   = null;
        int hi=0, vi=0;
        for (int i=0; i<components.length; i++) {
            final SingleCRS candidate = components[i];
            if (candidate instanceof VerticalCRS) {
                if (vertical == null) {
                    vertical = (VerticalCRS) candidate;
                    if (VerticalDatumType.ELLIPSOIDAL.equals( // TODO: remove cast with J2SE 1.5.
                        ((VerticalDatum) vertical.getDatum()).getVerticalDatumType()))
                    {
                        vi = i;
                        continue;
                    }
                }
                return crs;
            }
            if (candidate instanceof GeographicCRS) {
                if (horizontal == null) {
                    horizontal = (GeographicCRS) candidate;
                    if (horizontal.getCoordinateSystem().getDimension() == 2) {
                        hi = i;
                        continue;
                    }
                }
                return crs;
            }
        }
        if (horizontal!=null && vertical!=null && Math.abs(vi-hi)==1) {
            /*
             * Exactly one horizontal and one vertical CRS has been found,
             * and those two CRS are consecutive. Constructs the new 3D CS.
             * (TODO: remove 2 casts when we will be allowed to compile for J2SE 1.5).
             */
            final boolean classic = (hi < vi);
            final CoordinateSystemAxis[] axis = new CoordinateSystemAxis[3];
            final EllipsoidalCS cs = (EllipsoidalCS) horizontal.getCoordinateSystem();
            axis[classic ? 0 : 1] = cs.getAxis(0);
            axis[classic ? 1 : 2] = cs.getAxis(1);
            axis[classic ? 2 : 0] = vertical.getCoordinateSystem().getAxis(0);
            final Map csName, crsName;
            if (components.length == 2) {
                csName  = IdentifiedObject.getProperties(crs.getCoordinateSystem());
                crsName = IdentifiedObject.getProperties(crs);
            } else {
                csName  = getTemporaryName(cs);
                crsName = getTemporaryName(horizontal);
            }
            final  CSFactory  csFactory = getCSFactory();
            final CRSFactory crsFactory = getCRSFactory();
            final GeographicCRS single;
            single = crsFactory.createGeographicCRS(crsName, (GeodeticDatum) horizontal.getDatum(),
                      csFactory.createEllipsoidalCS(csName, axis[0], axis[1], axis[2]));
            /*
             * The single CRS has now been created. If it is the only CRS left, returns it.
             * Otherwise (for example a TemporalCRS way still around), build a new compound
             * CRS
             */
            if (components.length == 2) {
                return single;
            }
            final CoordinateReferenceSystem[] c=new CoordinateReferenceSystem[components.length-1];
            final int i = classic ? hi : vi;
            System.arraycopy(components, 0, c, 0, i);
            c[i] = single;
            System.arraycopy(components, i+2, c, i+1, components.length-(i+2));
            return crsFactory.createCompoundCRS(IdentifiedObject.getProperties(crs), c);
        }
        return crs;
    }
    
    /**
     * Returns a temporary name for object derived from the specified one.
     */
    private static Map getTemporaryName(final org.opengis.referencing.IdentifiedObject source) {
        return Collections.singletonMap(IdentifiedObject.NAME_PROPERTY,
                                        source.getName().getCode() + " (3D)");
    }
}
