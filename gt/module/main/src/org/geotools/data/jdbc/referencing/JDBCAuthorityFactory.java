/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data.jdbc.referencing;

import java.util.Set;

import org.geotools.referencing.FactoryFinder;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
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
import org.opengis.util.InternationalString;

/**
 * @author jeichar
 */
public class JDBCAuthorityFactory implements CRSAuthorityFactory {

    CRSFactory factory;
    
    /**
     * Construct <code>PostgisAuthorityFactory</code>.
     *
     */
    public JDBCAuthorityFactory() {
        factory=FactoryFinder.getCRSFactory();
    }
    
    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createCoordinateReferenceSystem(java.lang.String)
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createCompoundCRS(java.lang.String)
     */
    public CompoundCRS createCompoundCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createDerivedCRS(java.lang.String)
     */
    public DerivedCRS createDerivedCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createEngineeringCRS(java.lang.String)
     */
    public EngineeringCRS createEngineeringCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createGeographicCRS(java.lang.String)
     */
    public GeographicCRS createGeographicCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createGeocentricCRS(java.lang.String)
     */
    public GeocentricCRS createGeocentricCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createImageCRS(java.lang.String)
     */
    public ImageCRS createImageCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createProjectedCRS(java.lang.String)
     */
    public ProjectedCRS createProjectedCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createTemporalCRS(java.lang.String)
     */
    public TemporalCRS createTemporalCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.crs.CRSAuthorityFactory#createVerticalCRS(java.lang.String)
     */
    public VerticalCRS createVerticalCRS( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.AuthorityFactory#getObjectFactory()
     */
    public ObjectFactory getObjectFactory() {
        return factory;
    }

    /**
     * @see org.opengis.referencing.AuthorityFactory#getAuthority()
     */
    public Citation getAuthority() {
        return null;
    }

    /**
     * @see org.opengis.referencing.AuthorityFactory#getAuthorityCodes(java.lang.Class)
     */
    public Set getAuthorityCodes( Class arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.AuthorityFactory#getDescriptionText(java.lang.String)
     */
    public InternationalString getDescriptionText( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.AuthorityFactory#createObject(java.lang.String)
     */
    public Object createObject( String arg0 ) throws FactoryException {
        return null;
    }

    /**
     * @see org.opengis.referencing.Factory#getVendor()
     */
    public Citation getVendor() {
        return null;
    }

	

}
