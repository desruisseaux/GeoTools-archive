/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 *    
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies and extensions
import java.util.Set;

import javax.units.Unit;

import org.geotools.factory.BufferedFactory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.geotools.util.ObjectCache;
import org.geotools.util.ObjectCaches;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CylindricalCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.PolarCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.ImageDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.util.InternationalString;

/**
 * An authority factory that consults (a possibily shared) cache before generating
 * content itself.
 * </p>
 * The behaviour of the {@code createFoo(String)} methods first looks if a
 * previously created object exists for the given code. If such an object
 * exists, it is returned directly. The testing of the cache is synchronized and
 * may block if the referencing object is under construction.
 * <p>
 * If the object is not yet created, the definition is delegated to the
 * appropratie the {@code generateFoo} methd and the result is cached for
 * next time.
 * <p>
 * This object is responsible for using a provided {{ReferencingObjectCache}}.
 * </p>
 * 
 * @since 2.4
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/factory/AbstractBufferedAuthorityFactory.java $
 * @version $Id: BufferedAuthorityDecorator.java 26038 2007-06-27 01:58:12Z
 *          jgarnett $
 * @author Jody Garnett
 */
public abstract class AbstractCachedAuthorityFactory extends ReferencingFactory
		implements AuthorityFactory, CRSAuthorityFactory, CSAuthorityFactory,
		DatumAuthorityFactory, CoordinateOperationAuthorityFactory,
		BufferedFactory {

	/**
	 * Cache to be used for referencing objects.
	 * Please note that this cache may be shared!
	 */
	ObjectCache cache;

	/**
	 * Constructs an instance making use of the default cache.
	 *
	 * @param factory
	 *            The factory to cache. Can not be {@code null}.
	 */
	public AbstractCachedAuthorityFactory( int priority) {
		this( priority, ObjectCaches.create("weak", 50 ) );
	}

	/**
	 * Constructs an instance making use of the indicated cache.
	 * <p>
	 * This constructor is protected because subclasses must declare which of
	 * the {@link DatumAuthorityFactory}, {@link CSAuthorityFactory},
	 * {@link CRSAuthorityFactory} and
	 * {@link CoordinateOperationAuthorityFactory} interfaces they choose to
	 * implement.
	 * 
	 * @param factory
	 *            The factory to cache. Can not be {@code null}.
	 * @param maxStrongReferences
	 *            The maximum number of objects to keep by strong reference.
	 */
	protected AbstractCachedAuthorityFactory(int priority, ObjectCache cache) {
		this.cache = cache;
	}

	/** Utility method used to produce cache based on hint */
	protected static ObjectCache createCache(final Hints hints)
			throws FactoryRegistryException {
		return ObjectCaches.create(hints);
	}

	//
	// Utility Methods and Cache Care and Feeding
	//
	protected String toKey(String code) {
		return ObjectCaches.toKey( getAuthority(), code);
	}

	//
	// AuthorityFactory
	//    
	public abstract Citation getAuthority();
	public abstract Set getAuthorityCodes(Class type) throws FactoryException;
	public abstract InternationalString getDescriptionText(String code);
	public IdentifiedObject createObject(String code) throws FactoryException {
		final String key = toKey(code);
		IdentifiedObject obj = (IdentifiedObject) cache.get(key);
		if (obj == null) {
			try {
				cache.writeLock(key);
				obj = (IdentifiedObject) cache.peek(key);
				if (obj == null) {
					obj = generateObject(code);
					cache.put(key, obj);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return obj;
	}
	protected abstract IdentifiedObject generateObject( String code ) throws FactoryException;

	//
	// CRSAuthority
	//
	public synchronized CompoundCRS createCompoundCRS(final String code)
			throws FactoryException {
		final String key = toKey(code);
		CompoundCRS crs = (CompoundCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (CompoundCRS) cache.peek(key);
				if (crs == null) {
					crs = generateCompoundCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
    protected abstract CompoundCRS generateCompoundCRS(String code);

	public CoordinateReferenceSystem createCoordinateReferenceSystem(String code)
			throws FactoryException {
		final String key = toKey(code);
		CoordinateReferenceSystem crs = (CoordinateReferenceSystem) cache
				.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (CoordinateReferenceSystem) cache.peek(key);
				if (crs == null) {
					crs = generateCoordinateReferenceSystem(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
	protected abstract CoordinateReferenceSystem generateCoordinateReferenceSystem(String code);

	public DerivedCRS createDerivedCRS(String code) throws FactoryException {
		final String key = toKey(code);
		DerivedCRS crs = (DerivedCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (DerivedCRS) cache.peek(key);
				if (crs == null) {
					crs = generateDerivedCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}

	protected abstract DerivedCRS generateDerivedCRS(String code);

	public EngineeringCRS createEngineeringCRS(String code)
			throws FactoryException {
		final String key = toKey(code);
		EngineeringCRS crs = (EngineeringCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (EngineeringCRS) cache.peek(key);
				if (crs == null) {
					crs = generateEngineeringCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
	protected abstract EngineeringCRS generateEngineeringCRS(String code);

	public GeocentricCRS createGeocentricCRS(String code)
			throws FactoryException {
		final String key = toKey(code);
		GeocentricCRS crs = (GeocentricCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (GeocentricCRS) cache.peek(key);
				if (crs == null) {
					crs = generateGeocentricCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
	protected abstract GeocentricCRS generateGeocentricCRS(String code);

	public GeographicCRS createGeographicCRS(String code)
			throws FactoryException {
		final String key = toKey(code);
		GeographicCRS crs = (GeographicCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (GeographicCRS) cache.peek(key);
				if (crs == null) {
					crs = generateGeographicCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}

	protected abstract GeographicCRS generateGeographicCRS(String code);

	public ImageCRS createImageCRS(String code) throws FactoryException {
		final String key = toKey(code);
		ImageCRS crs = (ImageCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (ImageCRS) cache.peek(key);
				if (crs == null) {
					crs = generateImageCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
	protected abstract ImageCRS generateImageCRS(String code);

	public ProjectedCRS createProjectedCRS(String code) throws FactoryException {
		final String key = toKey(code);
		ProjectedCRS crs = (ProjectedCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (ProjectedCRS) cache.peek(key);
				if (crs == null) {
					crs = generateProjectedCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
	protected abstract ProjectedCRS generateProjectedCRS(String code);

	public TemporalCRS createTemporalCRS(String code) throws FactoryException {
		final String key = toKey(code);
		TemporalCRS crs = (TemporalCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (TemporalCRS) cache.peek(key);
				if (crs == null) {
					crs = generateTemporalCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
	protected abstract TemporalCRS generateTemporalCRS(String code);

	public VerticalCRS createVerticalCRS(String code) throws FactoryException {
		final String key = toKey(code);
		VerticalCRS crs = (VerticalCRS) cache.get(key);
		if (crs == null) {
			try {
				cache.writeLock(key);
				crs = (VerticalCRS) cache.peek(key);
				if (crs == null) {
					crs = generateVerticalCRS(code);
					cache.put(key, crs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return crs;
	}
	protected abstract VerticalCRS generateVerticalCRS(String code);

	//
	// CSAuthority
	//
	public CartesianCS createCartesianCS(String code) throws FactoryException {
		final String key = toKey(code);
		CartesianCS cs = (CartesianCS) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (CartesianCS) cache.peek(key);
				if (cs == null) {
					cs = generateCartesianCS(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract CartesianCS generateCartesianCS(String code);

	public CoordinateSystem createCoordinateSystem(String code)
			throws FactoryException {
		final String key = toKey(code);
		CoordinateSystem cs = (CoordinateSystem) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (CoordinateSystem) cache.peek(key);
				if (cs == null) {
					cs = generateCoordinateSystem(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract CoordinateSystem generateCoordinateSystem(String code);

	// sample implemenation with get/test
	public CoordinateSystemAxis createCoordinateSystemAxis(String code)
			throws FactoryException {
		final String key = toKey(code);
		CoordinateSystemAxis axis = (CoordinateSystemAxis) cache.get(key);
		if (axis == null) {
			try {
				cache.writeLock(key);
				axis = (CoordinateSystemAxis) cache.peek(key);
				if (axis == null) {
					axis = generateCoordinateSystemAxis(code);
					cache.put(key, axis);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return axis;
	}

	protected abstract CoordinateSystemAxis generateCoordinateSystemAxis(String code);

	public CylindricalCS createCylindricalCS(String code)
			throws FactoryException {
		final String key = toKey(code);
		CylindricalCS cs = (CylindricalCS) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (CylindricalCS) cache.peek(key);
				if (cs == null) {
					cs = generateCylindricalCS(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract CylindricalCS generateCylindricalCS(String code);

	public EllipsoidalCS createEllipsoidalCS(String code)
			throws FactoryException {
		final String key = toKey(code);
		EllipsoidalCS cs = (EllipsoidalCS) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (EllipsoidalCS) cache.peek(key);
				if (cs == null) {
					cs = generateEllipsoidalCS(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract EllipsoidalCS generateEllipsoidalCS(String code);

	public PolarCS createPolarCS(String code) throws FactoryException {
		final String key = toKey(code);
		PolarCS cs = (PolarCS) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (PolarCS) cache.peek(key);
				if (cs == null) {
					cs = generatePolarCS(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract PolarCS generatePolarCS(String code);

	public SphericalCS createSphericalCS(String code) throws FactoryException {
		final String key = toKey(code);
		SphericalCS cs = (SphericalCS) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (SphericalCS) cache.peek(key);
				if (cs == null) {
					cs = generateSphericalCS(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract SphericalCS generateSphericalCS(String code);

	public TimeCS createTimeCS(String code) throws FactoryException {
		final String key = toKey(code);
		TimeCS cs = (TimeCS) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (TimeCS) cache.peek(key);
				if (cs == null) {
					cs = generateTimeCS(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract TimeCS generateTimeCS(String code);

	public Unit createUnit(String code) throws FactoryException {
		final String key = toKey(code);
		Unit unit = (Unit) cache.get(key);
		if (unit == null) {
			try {
				cache.writeLock(key);
				unit = (Unit) cache.peek(key);
				if (unit == null) {
					unit = generateUnit(code);
					cache.put(key, unit);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return unit;
	}

	protected abstract Unit generateUnit(String code);

	public VerticalCS createVerticalCS(String code) throws FactoryException {
		final String key = toKey(code);
		VerticalCS cs = (VerticalCS) cache.get(key);
		if (cs == null) {
			try {
				cache.writeLock(key);
				cs = (VerticalCS) cache.peek(key);
				if (cs == null) {
					cs = generateVerticalCS(code);
					cache.put(key, cs);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return cs;
	}

	protected abstract VerticalCS generateVerticalCS(String code);

	//
	// DatumAuthorityFactory
	//
	public Datum createDatum(String code) throws FactoryException {
		final String key = toKey(code);
		Datum datum = (Datum) cache.get(key);
		if (datum == null) {
			try {
				cache.writeLock(key);
				datum = (Datum) cache.peek(key);
				if (datum == null) {
					datum = generateDatum(code);
					cache.put(key, datum);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return datum;
	}

	protected abstract Datum generateDatum(String code);

	public Ellipsoid createEllipsoid(String code) throws FactoryException {
		final String key = toKey(code);
		Ellipsoid ellipsoid = (Ellipsoid) cache.get(key);
		if (ellipsoid == null) {
			try {
				cache.writeLock(key);
				ellipsoid = (Ellipsoid) cache.peek(key);
				if (ellipsoid == null) {
					ellipsoid = generateEllipsoid(code);
					cache.put(key, ellipsoid);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return ellipsoid;
	}

	protected abstract Ellipsoid generateEllipsoid(String code);

	public EngineeringDatum createEngineeringDatum(String code)
			throws FactoryException {
		final String key = toKey(code);
		EngineeringDatum datum = (EngineeringDatum) cache.get(key);
		if (datum == null) {
			try {
				cache.writeLock(key);
				datum = (EngineeringDatum) cache.peek(key);
				if (datum == null) {
					datum = generateEngineeringDatum(code);
					cache.put(key, datum);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return datum;
	}

	protected abstract EngineeringDatum generateEngineeringDatum(String code);

	public GeodeticDatum createGeodeticDatum(String code)
			throws FactoryException {
		final String key = toKey(code);
		GeodeticDatum datum = (GeodeticDatum) cache.get(key);
		if (datum == null) {
			try {
				cache.writeLock(key);
				datum = (GeodeticDatum) cache.peek(key);
				if (datum == null) {
					datum = generateGeodeticDatum(code);
					cache.put(key, datum);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return datum;
	}

	protected abstract GeodeticDatum generateGeodeticDatum(String code);

	public ImageDatum createImageDatum(String code) throws FactoryException {
		final String key = toKey(code);
		ImageDatum datum = (ImageDatum) cache.get(key);
		if (datum == null) {
			try {
				cache.writeLock(key);
				datum = (ImageDatum) cache.peek(key);
				if (datum == null) {
					datum = generateImageDatum(code);
					cache.put(key, datum);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return datum;
	}

	protected abstract ImageDatum generateImageDatum(String code);

	public PrimeMeridian createPrimeMeridian(String code)
			throws FactoryException {
		final String key = toKey(code);
		PrimeMeridian datum = (PrimeMeridian) cache.get(key);
		if (datum == null) {
			try {
				cache.writeLock(key);
				datum = (PrimeMeridian) cache.peek(key);
				if (datum == null) {
					datum = generatePrimeMeridian(code);
					cache.put(key, datum);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return datum;
	}

	protected abstract PrimeMeridian generatePrimeMeridian(String code);

	public TemporalDatum createTemporalDatum(String code)
			throws FactoryException {
		final String key = toKey(code);
		TemporalDatum datum = (TemporalDatum) cache.get(key);
		if (datum == null) {
			try {
				cache.writeLock(key);
				datum = (TemporalDatum) cache.peek(key);
				if (datum == null) {
					datum = generateTemporalDatum(code);
					cache.put(key, datum);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return datum;
	}

	protected abstract TemporalDatum generateTemporalDatum(String code);

	public VerticalDatum createVerticalDatum(String code)
			throws FactoryException {
		final String key = toKey(code);
		VerticalDatum datum = (VerticalDatum) cache.get(key);
		if (datum == null) {
			try {
				cache.writeLock(key);
				datum = (VerticalDatum) cache.peek(key);
				if (datum == null) {
					datum = generateVerticalDatum(code);
					cache.put(key, datum);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return datum;
	}

	protected abstract VerticalDatum generateVerticalDatum(String code);

	public CoordinateOperation createCoordinateOperation(String code)
			throws FactoryException {
		final String key = toKey(code);
		CoordinateOperation operation = (CoordinateOperation) cache.get(key);
		if (operation == null) {
			try {
				cache.writeLock(key);
				operation = (CoordinateOperation) cache.peek(key);
				if (operation == null) {
					operation = generateCoordinateOperation(code);
					cache.put(key, operation);
				}
			} finally {
				cache.writeUnLock(key);
			}
		}
		return operation;
	}

	protected abstract CoordinateOperation generateCoordinateOperation(String code);

	public synchronized Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
			final String sourceCode, final String targetCode)
			throws FactoryException {
		
		final Object key = ObjectCaches.toKey( getAuthority(),  sourceCode, targetCode );
		Set operations = (Set) cache.get(key);			
		if (operations == null) {
			try {
				cache.writeLock(key);
				operations = (Set) cache.peek(key);
				if (operations == null) {
					operations = generateFromCoordinateReferenceSystemCodes( sourceCode, targetCode );				
					// can we not trust operationAuthority to return us an unmodifiableSet ?
					//operations = Collections.unmodifiableSet( operations );
					
					cache.put( key, operations );
				}
			}
			finally {
				cache.writeUnLock(key);
			}
		}
		return operations;
	}

	protected abstract Set generateFromCoordinateReferenceSystemCodes(String sourceCode, String targetCode);
}
