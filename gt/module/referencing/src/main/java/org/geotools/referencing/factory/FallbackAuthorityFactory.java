/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.factory.FactoryNotFoundException;


/**
 * A factory which delegates all object creation to a <cite>main</cite> factory, and fallback
 * on an other one if the main factory failed.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FallbackAuthorityFactory extends AuthorityFactoryAdapter {
    /**
     * The factory to use as a fallback if the main factory failed.
     */
    private final AbstractAuthorityFactory fallback;

    /**
     * Wraps two authority factories.
     * <p>
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory}
     * and {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     *
     * @param main The main factory.
     * @param fallback The factory to use as a fallback if the main factory failed.
     *
     * @see #create
     */
    protected FallbackAuthorityFactory(final AuthorityFactory main,
                                       final AuthorityFactory fallback)
    {
        super(main);
        this.fallback = wrap(fallback);
    }

    /**
     * Wraps two or more authority factories. The specified collection must contains at least
     * two elements. Every additional elements are chained through recursive creation of
     * {@code FallbackAuthorityFactory} instances.
     * <p>
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory}
     * and {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     *
     * @param  factories The factories to wrap, in iteration order.
     * @throws NoSuchElementException if the collection doesn't contains at least two elements.
     *
     * @see #create
     */
    protected FallbackAuthorityFactory(final Collection/*<? extends AuthorityFactory>*/ factories)
            throws NoSuchElementException
    {
        this(factories.iterator());
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private FallbackAuthorityFactory(final Iterator/*<? extends AuthorityFactory>*/ fallbacks)
            throws NoSuchElementException
    {
        this((AuthorityFactory) fallbacks.next(), fallbacks);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private FallbackAuthorityFactory(final AuthorityFactory main,
                                     final Iterator/*<? extends AuthorityFactory>*/ fallbacks)
            throws NoSuchElementException
    {
        super(main);
        AuthorityFactory next = (AuthorityFactory) fallbacks.next();
        if (fallbacks.hasNext()) {
            next = new FallbackAuthorityFactory(next, fallbacks);
        }
        fallback = wrap(next);
    }

    /**
     * Wraps the specified factory into an AbstractAuthorityFactory object. There is no public
     * method for this operation because the wrapper do not implements any authority factory
     * interface. But it is suffisient for this implementation, as long as the {@link #fallback}
     * factory stay private.
     */
    private static AbstractAuthorityFactory wrap(final AuthorityFactory fallback) {
        ensureNonNull("fallback", fallback);
        return (fallback instanceof AbstractAuthorityFactory) ?
                (AbstractAuthorityFactory) fallback : new AuthorityFactoryAdapter(fallback);
    }

    /**
     * Wraps the specified authority factories. If the specified collection contains more than
     * one element, then a chain of {@code FallbackAuthorityFactory} instances is created.
     *
     * @param  type The interface to implement. Should be one of {@link DatumAuthorityFactory},
     *         {@link CSAuthorityFactory}, {@link CRSAuthorityFactory} and
     *         {@link CoordinateOperationAuthorityFactory}.
     * @param  factories The factories to wrap, in iteration order.
     * @throws NoSuchElementException if the collection doesn't contains at least one element.
     * @throws ClassCastException if {@code type} is illegal.
     *
     * @todo Use generic types when we will be allowed to compile for J2SE 1.5 (the return type
     *       should be T).
     */
    public static AuthorityFactory create(final Class/*<T extends AuthorityFactory>*/ type,
                                          final Collection/*<T>*/ factories)
            throws FactoryNotFoundException, ClassCastException
    {
        ensureNonNull("type", type);
        ensureNonNull("factories", factories);
        final AuthorityFactory factory;
        switch (factories.size()) {
            case 0: {
                throw new FactoryNotFoundException(Errors.format(ErrorKeys.FACTORY_NOT_FOUND_$1,
                        Utilities.getShortName(type)));
            }
            case 1: {
                factory = (AuthorityFactory) factories.iterator().next();
                break;
            }
            default: {
                if (CRSAuthorityFactory.class.equals(type)) {
                    factory = new CRS(factories);
                } else if (CSAuthorityFactory.class.equals(type)) {
                    factory = new CS(factories);
                } else if (DatumAuthorityFactory.class.equals(type)) {
                    factory = new Datum(factories);
                } else if (CoordinateOperationAuthorityFactory.class.equals(type)) {
                    factory = new Operation(factories);
                } else {
                    factory = new FallbackAuthorityFactory(factories);
                }
                break;
            }
        }
        // return type.cast(factory);  // TODO: uncomment with J2SE 1.5.
        return factory;
    }

    /**
     * Returns the set of authority code for the specified type. The default implementation
     * returns the union of the authority codes from the <cite>main</cite> and the
     * <cite>fallback</cite> factories.
     */
    public Set/*<String>*/ getAuthorityCodes(final Class type) throws FactoryException {
        final Set codes = new LinkedHashSet(super.getAuthorityCodes(type));
        codes.addAll(fallback.getAuthorityCodes(type));
        return codes;
    }

    /**
     * Returns a description for the object identified by the specified code.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        try {
            return super.getDescriptionText(code);
        } catch (FactoryException exception) {
            try {
                return fallback.getDescriptionText(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns an arbitrary object from a code.
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        try {
            return super.createObject(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createObject(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns an arbitrary {@linkplain org.opengis.referencing.datum.Datum datum} from a code.
     */
    public org.opengis.referencing.datum.Datum createDatum(final String code) throws FactoryException {
        try {
            return super.createDatum(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createDatum(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a {@linkplain EngineeringDatum engineering datum} from a code.
     */
    public EngineeringDatum createEngineeringDatum(final String code) throws FactoryException {
        try {
            return super.createEngineeringDatum(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createEngineeringDatum(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a {@linkplain ImageDatum image datum} from a code.
     */
    public ImageDatum createImageDatum(final String code) throws FactoryException {
        try {
            return super.createImageDatum(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createImageDatum(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a {@linkplain VerticalDatum vertical datum} from a code.
     */
    public VerticalDatum createVerticalDatum(final String code) throws FactoryException {
        try {
            return super.createVerticalDatum(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createVerticalDatum(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a {@linkplain TemporalDatum temporal datum} from a code.
     */
    public TemporalDatum createTemporalDatum(final String code) throws FactoryException {
        try {
            return super.createTemporalDatum(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createTemporalDatum(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     */
    public GeodeticDatum createGeodeticDatum(final String code) throws FactoryException {
        try {
            return super.createGeodeticDatum(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createGeodeticDatum(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code.
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        try {
            return super.createEllipsoid(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createEllipsoid(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a code.
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        try {
            return super.createPrimeMeridian(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createPrimeMeridian(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns a {@linkplain Extent extent} (usually an area of validity) from a code.
     */
    public Extent createExtent(final String code) throws FactoryException {
        try {
            return super.createExtent(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createExtent(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        try {
            return super.createCoordinateSystem(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createCoordinateSystem(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a cartesian coordinate system from a code.
     */
    public CartesianCS createCartesianCS(final String code) throws FactoryException {
        try {
            return super.createCartesianCS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createCartesianCS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a polar coordinate system from a code.
     */
    public PolarCS createPolarCS(final String code) throws FactoryException {
        try {
            return super.createPolarCS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createPolarCS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a cylindrical coordinate system from a code.
     */
    public CylindricalCS createCylindricalCS(final String code) throws FactoryException {
        try {
            return super.createCylindricalCS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createCylindricalCS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a spherical coordinate system from a code.
     */
    public SphericalCS createSphericalCS(final String code) throws FactoryException {
        try {
            return super.createSphericalCS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createSphericalCS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     */
    public EllipsoidalCS createEllipsoidalCS(final String code) throws FactoryException {
        try {
            return super.createEllipsoidalCS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createEllipsoidalCS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a vertical coordinate system from a code.
     */
    public VerticalCS createVerticalCS(final String code) throws FactoryException {
        try {
            return super.createVerticalCS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createVerticalCS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a temporal coordinate system from a code.
     */
    public TimeCS createTimeCS(final String code) throws FactoryException {
        try {
            return super.createTimeCS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createTimeCS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        try {
            return super.createCoordinateSystemAxis(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createCoordinateSystemAxis(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns an {@linkplain Unit unit} from a code.
     */
    public Unit createUnit(final String code) throws FactoryException {
        try {
            return super.createUnit(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createUnit(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code.
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        try {
            return super.createCoordinateReferenceSystem(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createCoordinateReferenceSystem(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a 3D coordinate reference system from a code.
     */
    public CompoundCRS createCompoundCRS(final String code) throws FactoryException {
        try {
            return super.createCompoundCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createCompoundCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a derived coordinate reference system from a code.
     */
    public DerivedCRS createDerivedCRS(final String code) throws FactoryException {
        try {
            return super.createDerivedCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createDerivedCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }
    
    /**
     * Creates a {@linkplain EngineeringCRS engineering coordinate reference system} from a code.
     */
    public EngineeringCRS createEngineeringCRS(final String code) throws FactoryException {
        try {
            return super.createEngineeringCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createEngineeringCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     */
    public GeographicCRS createGeographicCRS(final String code) throws FactoryException {
        try {
            return super.createGeographicCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createGeographicCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns a {@linkplain GeocentricCRS geocentric coordinate reference system} from a code.
     */
    public GeocentricCRS createGeocentricCRS(final String code) throws FactoryException {
        try {
            return super.createGeocentricCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createGeocentricCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a {@linkplain ImageCRS image coordinate reference system} from a code.
     */
    public ImageCRS createImageCRS(final String code) throws FactoryException {
        try {
            return super.createImageCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createImageCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Returns a {@linkplain ProjectedCRS projected coordinate reference system} from a code.
     */
    public ProjectedCRS createProjectedCRS(final String code) throws FactoryException {
        try {
            return super.createProjectedCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createProjectedCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a {@linkplain TemporalCRS temporal coordinate reference system} from a code.
     */
    public TemporalCRS createTemporalCRS(final String code) throws FactoryException {
        try {
            return super.createTemporalCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createTemporalCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a {@linkplain VerticalCRS vertical coordinate reference system} from a code.
     */
    public VerticalCRS createVerticalCRS(final String code) throws FactoryException {
        try {
            return super.createVerticalCRS(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createVerticalCRS(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates a parameter descriptor from a code. 
     */
    public ParameterDescriptor createParameterDescriptor(final String code) throws FactoryException {
        try {
            return super.createParameterDescriptor(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createParameterDescriptor(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates an operation method from a code. 
     */
    public OperationMethod createOperationMethod(final String code) throws FactoryException {
        try {
            return super.createOperationMethod(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createOperationMethod(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates an operation from a single operation code. 
     */
    public CoordinateOperation createCoordinateOperation(final String code) throws FactoryException {
        try {
            return super.createCoordinateOperation(code);
        } catch (FactoryException exception) {
            try {
                return fallback.createCoordinateOperation(code);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /**
     * Creates an operation from coordinate reference system codes.
     */
    public Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
                                        final String sourceCode, final String targetCode)
            throws FactoryException
    {
        try {
            return super.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
        } catch (FactoryException exception) {
            try {
                return fallback.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
            } catch (FactoryException ignore) {
                throw exception;
            }
        }
    }

    /** For internal use by {@link FallbackAuthorityFactory#create} only. */
    private static final class CRS extends FallbackAuthorityFactory
            implements CRSAuthorityFactory
    {
        CRS(Collection/*<? extends AuthorityFactory>*/ factories) {
            super(factories);
        }
    }

    /** For internal use by {@link FallbackAuthorityFactory#create} only. */
    private static final class CS extends FallbackAuthorityFactory
            implements CSAuthorityFactory
    {
        CS(Collection/*<? extends AuthorityFactory>*/ factories) {
            super(factories);
        }
    }

    /** For internal use by {@link FallbackAuthorityFactory#create} only. */
    private static final class Datum extends FallbackAuthorityFactory
            implements DatumAuthorityFactory
    {
        Datum(Collection/*<? extends AuthorityFactory>*/ factories) {
            super(factories);
        }
    }

    /** For internal use by {@link FallbackAuthorityFactory#create} only. */
    private static final class Operation extends FallbackAuthorityFactory
            implements CoordinateOperationAuthorityFactory
    {
        Operation(Collection/*<? extends AuthorityFactory>*/ factories) {
            super(factories);
        }
    }
}
