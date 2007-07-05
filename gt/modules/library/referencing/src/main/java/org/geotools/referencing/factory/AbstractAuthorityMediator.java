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

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.ObjectPoolFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.geotools.factory.BufferedFactory;
import org.geotools.factory.Hints;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.ObjectCache;
import org.geotools.util.ObjectCaches;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
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
 * An authority mediator that consults (a possibily shared) cache before
 * delegating the generation of the content to an authority factory.
 * </p>
 * The behaviour of the {@code createFoo(String)} methods first looks if a
 * previously created object exists for the given code. If such an object
 * exists, it is returned directly. The testing of the cache is synchronized and
 * may block if the referencing object is under construction.
 * <p>
 * If the object is not yet created, the definition is delegated to the
 * appropriate {@code createFoo} method of the factory, which will 
 * cache the result for next time.
 * <p>
 * This object is responsible for using a provided {{ObjectCache}}.
 * </p>
 * 
 * @since 2.4
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/factory/AbstractBufferedAuthorityFactory.java $
 * @version $Id: BufferedAuthorityDecorator.java 26038 2007-06-27 01:58:12Z
 *          jgarnett $
 * @author Jody Garnett (Refractions Research)
 * @author Cory Horner (Refractions Research)
 */
public abstract class AbstractAuthorityMediator extends
        ReferencingFactory implements AuthorityFactory, CRSAuthorityFactory,
        CSAuthorityFactory, DatumAuthorityFactory,
        CoordinateOperationAuthorityFactory, BufferedFactory {

    /**
     * Cache to be used for referencing objects defined by this authority.
     * Please note that this cache may be shared!
     */
    ObjectCache cache;

    /**
     * Pool to hold workers which will be used to construct referencing objects
     * which are not present in the cache.
     */
    private ObjectPool pool;
    
    /**
     * Configuration object for the object pool. The constructor reads its hints
     * and sets the pool configuration in this object;
     */
    Config poolConfig = new Config();
    
    /**
     * A container of the "real factories" actually used to construct objects.
     */
    protected final ReferencingFactoryContainer factories;

    /**
     * Constructs an instance making use of the default cache.
     * 
     * @param factory
     *            The factory to cache. Can not be {@code null}.
     */
    protected AbstractAuthorityMediator(int priority) {
        this(priority, ObjectCaches.create("weak", 50),
                ReferencingFactoryContainer.instance(null));
    }

    /**
     * Constructs an instance making use of the default cache.
     * 
     * @param factory
     *            The factory to cache. Can not be {@code null}.
     */
    protected AbstractAuthorityMediator(int priority, Hints hints) {
        this(priority, ObjectCaches.create(hints), ReferencingFactoryContainer
                .instance(hints));
        if (hints.containsKey(Hints.AUTHORITY_POOL_MIN_IDLE)) {
            poolConfig.minIdle = ((Integer) hints.get(Hints.AUTHORITY_POOL_MIN_IDLE)).intValue();
        }
        if (hints.containsKey(Hints.AUTHORITY_POOL_MAX_IDLE)) {
            poolConfig.maxIdle = ((Integer) hints.get(Hints.AUTHORITY_POOL_MAX_IDLE)).intValue();
        }
        if (hints.containsKey(Hints.AUTHORITY_POOL_MAX_ACTIVE)) {
            poolConfig.maxActive = ((Integer) hints.get(Hints.AUTHORITY_POOL_MAX_ACTIVE)).intValue();
        }
        if (hints.containsKey(Hints.AUTHORITY_POOL_MAX_WAIT)) {
            poolConfig.maxWait = ((Integer) hints.get(Hints.AUTHORITY_POOL_MAX_WAIT)).intValue();
        }
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
    protected AbstractAuthorityMediator(int priority, ObjectCache cache,
            ReferencingFactoryContainer container) {
        super(priority);
        this.factories = container;
        this.cache = cache;
    }
    
    ObjectPool getPool() {
        if (pool == null) {
            //create pool
            PoolableObjectFactory objectFactory = new AuthorityPoolableObjectFactory();
            ObjectPoolFactory poolFactory = new GenericObjectPoolFactory(objectFactory, poolConfig);
            this.setPool(poolFactory.createPool());
        }
        return pool;
    }

    void setPool(ObjectPool pool) {
        this.pool = pool;
    }

    //
    // Utility Methods and Cache Care and Feeding
    //
    protected String toKey(String code) {
        return ObjectCaches.toKey(getAuthority(), code);
    }

    /**
     * Trims the authority scope, if present. For example if this factory is an
     * EPSG authority factory and the specified code start with the "EPSG:"
     * prefix, then the prefix is removed. Otherwise, the string is returned
     * unchanged (except for leading and trailing spaces).
     * 
     * @param code
     *            The code to trim.
     * @return The code without the authority scope.
     */
    protected String trimAuthority(String code) {
        return toKey(code);
    }

    /**
     * Creates an exception for an unknown authority code. This convenience
     * method is provided for implementation of {@code createXXX} methods.
     * 
     * @param type
     *            The GeoAPI interface that was to be created (e.g.
     *            {@code CoordinateReferenceSystem.class}).
     * @param code
     *            The unknow authority code.
     * @return An exception initialized with an error message built from the
     *         specified informations.
     */
    protected final NoSuchAuthorityCodeException noSuchAuthorityCode(
            final Class type, final String code) {
        final InternationalString authority = getAuthority().getTitle();
        return new NoSuchAuthorityCodeException(Errors.format(
                ErrorKeys.NO_SUCH_AUTHORITY_CODE_$3, code, authority, Utilities
                        .getShortName(type)), authority.toString(), code);
    }

    /**
     * The authority body of the objects this factory provides.
     */
    public abstract Citation getAuthority();

    
    public Set getAuthorityCodes(Class type) throws FactoryException {
        Set codes = (Set) cache.get(type);
        if (codes == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    codes = worker.getAuthorityCodes(type);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return codes;
    }

    public abstract InternationalString getDescriptionText(String code)
            throws FactoryException;

    public IdentifiedObject createObject(String code) throws FactoryException {
        final String key = toKey(code);
        IdentifiedObject obj = (IdentifiedObject) cache.get(key);
        if (obj == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    obj = worker.createObject(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return obj;
    }

    //
    // CRSAuthority
    //
    public synchronized CompoundCRS createCompoundCRS(final String code)
            throws FactoryException {
        final String key = toKey(code);
        CompoundCRS crs = (CompoundCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createCompoundCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public CoordinateReferenceSystem createCoordinateReferenceSystem(String code)
            throws FactoryException {
        final String key = toKey(code);
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) cache
                .get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createCoordinateReferenceSystem(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public DerivedCRS createDerivedCRS(String code) throws FactoryException {
        final String key = toKey(code);
        DerivedCRS crs = (DerivedCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createDerivedCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public EngineeringCRS createEngineeringCRS(String code)
            throws FactoryException {
        final String key = toKey(code);
        EngineeringCRS crs = (EngineeringCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createEngineeringCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public GeocentricCRS createGeocentricCRS(String code)
            throws FactoryException {
        final String key = toKey(code);
        GeocentricCRS crs = (GeocentricCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createGeocentricCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public GeographicCRS createGeographicCRS(String code)
            throws FactoryException {
        final String key = toKey(code);
        GeographicCRS crs = (GeographicCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createGeographicCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public ImageCRS createImageCRS(String code) throws FactoryException {
        final String key = toKey(code);
        ImageCRS crs = (ImageCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createImageCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public ProjectedCRS createProjectedCRS(String code) throws FactoryException {
        final String key = toKey(code);
        ProjectedCRS crs = (ProjectedCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createProjectedCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public TemporalCRS createTemporalCRS(String code) throws FactoryException {
        final String key = toKey(code);
        TemporalCRS crs = (TemporalCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createTemporalCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    public VerticalCRS createVerticalCRS(String code) throws FactoryException {
        final String key = toKey(code);
        VerticalCRS crs = (VerticalCRS) cache.get(key);
        if (crs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    crs = worker.createVerticalCRS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return crs;
    }

    //
    // CSAuthority
    //
    public CartesianCS createCartesianCS(String code) throws FactoryException {
        final String key = toKey(code);
        CartesianCS cs = (CartesianCS) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createCartesianCS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    public CoordinateSystem createCoordinateSystem(String code)
            throws FactoryException {
        final String key = toKey(code);
        CoordinateSystem cs = (CoordinateSystem) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createCoordinateSystem(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    // sample implemenation with get/test
    public CoordinateSystemAxis createCoordinateSystemAxis(String code)
            throws FactoryException {
        final String key = toKey(code);
        CoordinateSystemAxis axis = (CoordinateSystemAxis) cache.get(key);
        if (axis == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    axis = worker.createCoordinateSystemAxis(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return axis;
    }

    public CylindricalCS createCylindricalCS(String code)
            throws FactoryException {
        final String key = toKey(code);
        CylindricalCS cs = (CylindricalCS) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createCylindricalCS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    public EllipsoidalCS createEllipsoidalCS(String code)
            throws FactoryException {
        final String key = toKey(code);
        EllipsoidalCS cs = (EllipsoidalCS) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createEllipsoidalCS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    public PolarCS createPolarCS(String code) throws FactoryException {
        final String key = toKey(code);
        PolarCS cs = (PolarCS) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createPolarCS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    public SphericalCS createSphericalCS(String code) throws FactoryException {
        final String key = toKey(code);
        SphericalCS cs = (SphericalCS) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createSphericalCS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    public TimeCS createTimeCS(String code) throws FactoryException {
        final String key = toKey(code);
        TimeCS cs = (TimeCS) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createTimeCS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    public Unit createUnit(String code) throws FactoryException {
        final String key = toKey(code);
        Unit unit = (Unit) cache.get(key);
        if (unit == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    unit = worker.createUnit(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return unit;
    }

    public VerticalCS createVerticalCS(String code) throws FactoryException {
        final String key = toKey(code);
        VerticalCS cs = (VerticalCS) cache.get(key);
        if (cs == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    cs = worker.createVerticalCS(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return cs;
    }

    //
    // DatumAuthorityFactory
    //
    public Datum createDatum(String code) throws FactoryException {
        final String key = toKey(code);
        Datum datum = (Datum) cache.get(key);
        if (datum == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    datum = worker.createDatum(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return datum;
    }

    public Ellipsoid createEllipsoid(String code) throws FactoryException {
        final String key = toKey(code);
        Ellipsoid ellipsoid = (Ellipsoid) cache.get(key);
        if (ellipsoid == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    ellipsoid = worker.createEllipsoid(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return ellipsoid;
    }

    public EngineeringDatum createEngineeringDatum(String code)
            throws FactoryException {
        final String key = toKey(code);
        EngineeringDatum datum = (EngineeringDatum) cache.get(key);
        if (datum == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    datum = worker.createEngineeringDatum(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return datum;
    }

    public GeodeticDatum createGeodeticDatum(String code)
            throws FactoryException {
        final String key = toKey(code);
        GeodeticDatum datum = (GeodeticDatum) cache.get(key);
        if (datum == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    datum = worker.createGeodeticDatum(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return datum;
    }

    public ImageDatum createImageDatum(String code) throws FactoryException {
        final String key = toKey(code);
        ImageDatum datum = (ImageDatum) cache.get(key);
        if (datum == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    datum = worker.createImageDatum(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return datum;
    }

    public PrimeMeridian createPrimeMeridian(String code)
            throws FactoryException {
        final String key = toKey(code);
        PrimeMeridian datum = (PrimeMeridian) cache.get(key);
        if (datum == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    datum = worker.createPrimeMeridian(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return datum;
    }

    public TemporalDatum createTemporalDatum(String code)
            throws FactoryException {
        final String key = toKey(code);
        TemporalDatum datum = (TemporalDatum) cache.get(key);
        if (datum == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    datum = worker.createTemporalDatum(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return datum;
    }

    public VerticalDatum createVerticalDatum(String code)
            throws FactoryException {
        final String key = toKey(code);
        VerticalDatum datum = (VerticalDatum) cache.get(key);
        if (datum == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    datum = worker.createVerticalDatum(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return datum;
    }

    public CoordinateOperation createCoordinateOperation(String code)
            throws FactoryException {
        final String key = toKey(code);
        CoordinateOperation operation = (CoordinateOperation) cache.get(key);
        if (operation == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    operation = worker.createCoordinateOperation(code);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return operation;
    }

    public synchronized Set/* <CoordinateOperation> */createFromCoordinateReferenceSystemCodes(
            final String sourceCode, final String targetCode)
            throws FactoryException {

        final Object key = ObjectCaches.toKey(getAuthority(), sourceCode,
                targetCode);
        Set operations = (Set) cache.get(key);
        if (operations == null) {
            try {
                AbstractCachedAuthorityFactory worker = null;
                try {
                    worker = (AbstractCachedAuthorityFactory) getPool().borrowObject();
                    operations = worker.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
                } finally {
                    getPool().returnObject(worker);
                }
            } catch (FactoryException e) {
                throw e;
            } catch (Exception e) {
                throw new FactoryException(e);
            }
        }
        return operations;
    }

    /**
     * Creates the objects, subclasses of AbstractCachedAuthorityFactory, which
     * are held by the ObjectPool.  This implementation simply delegates each
     * method to the subclass.
     * 
     * @author Cory Horner (Refractions Research)
     */
    private class AuthorityPoolableObjectFactory implements PoolableObjectFactory {
        
        AuthorityPoolableObjectFactory() {
        }

        public void activateObject(Object obj) throws Exception {
            activateWorker(obj);
        }

        public void destroyObject(Object obj) throws Exception {
            destroyWorker(obj);
        }

        public Object makeObject() throws Exception {
            return makeWorker();
        }

        public void passivateObject(Object obj) throws Exception {
            passivateWorker(obj);
        }

        public boolean validateObject(Object obj) {
            return validateWorker(obj);
        }
        
    }
 
    /**
     * Reinitialize an instance to be returned by the pool.
     */
    protected abstract void activateWorker(Object obj) throws Exception;

    /**
     * Destroys an instance no longer needed by the pool.
     */
    protected abstract void destroyWorker(Object obj) throws Exception;

    /**
     * Creates an instance that can be returned by the pool.
     */
    protected abstract Object makeWorker() throws Exception;

    /**
     * Uninitialize an instance to be returned to the pool.
     */
    protected abstract void passivateWorker(Object obj) throws Exception;

    /**
     * Ensures that the instance is safe to be returned by the pool.
     */
    protected abstract boolean validateWorker(Object obj);
}
