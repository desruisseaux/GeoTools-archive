/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004 Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.crs;

import java.util.Properties;
import java.net.URL;
import java.io.IOException;
import java.util.Set;

import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import org.geotools.referencing.FactoryFinder;

/**
 * Default implementation for a coordinate reference system authority factory backed
 * by the EPSG property file. This gives most of the benifits of using the EPSG 
 * database backed authority factory, in a nice, portable property file.
 *
 * @version $Id$
 * @author Jody Garnett
 * @author Rueben Schulz
 */
//not quite sure how I am going to create a new factory (what should the geoapi method be)
public class CRSEPSGPropertyFileFactory implements CRSAuthorityFactory {
   
//would be nice to cache crs objects for codes that have already been requested    
    
    /**
     * The default coordinate system authority factory.
     * Will be constructed only when first requested.
     */
    protected static CRSEPSGPropertyFileFactory DEFAULT;
    
    protected Properties epsg = new Properties();
    
    //object factory
    protected CRSFactory crsFactory;
        
    protected CRSEPSGPropertyFileFactory() {
        this(FactoryFinder.getCRSFactory());
    }
    
    protected CRSEPSGPropertyFileFactory(final CRSFactory factory ) {
    	this(factory, CRSEPSGPropertyFileFactory.class.getResource("epsg.properties"));
    }
    
    /** 
     *
     */
    protected CRSEPSGPropertyFileFactory(final CRSFactory factory,
                       	 URL definition) {
        this.crsFactory = factory;
        
        //super(factory);
        //Info.ensureNonNull("definition", definition );
        System.out.println("url: " + definition);
        try {
            epsg.load( definition.openStream() );
        }
        catch (IOException io ){
            // could not load properties file
            //probably want to throw a factory error
            System.out.println("Could not load properties file");
            
        }
    }
    
    /**
     * Returns a default coordinate system factory backed by the EPSG property file.
     * 
     * @return The default factory.
     * @throws SQLException if the connection to the database can't be etablished.
     */
    public synchronized static CRSAuthorityFactory getDefault() {
        if (DEFAULT == null) {        	
            DEFAULT = new CRSEPSGPropertyFileFactory();
        }            
        return DEFAULT;
    }
       
    public CoordinateReferenceSystem createCoordinateReferenceSystem(String code) 
        throws FactoryException 
    {
        if (code == null) {
            return null;
        }
        if (code.startsWith("EPSG:")) { // EPSG:26907
            code = code.substring(5);
        }
        code = code.trim();
        String wkt = epsg.getProperty( code );
        if( wkt == null ) {
            throw new FactoryException("Unknonwn EPSG code: '"+code+"'" );
        }
        return crsFactory.createFromWKT(wkt);
    }
    
    public Object createObject(String code) throws FactoryException {
        return (Object) createCoordinateReferenceSystem(code);
    }
    
    public ProjectedCRS createProjectedCRS(String code) throws FactoryException {
        return (ProjectedCRS) createCoordinateReferenceSystem(code);
    }
    
    public GeographicCRS createGeographicCRS(String code) throws FactoryException {
         return (GeographicCRS) createCoordinateReferenceSystem(code);
    }    
    
    public org.opengis.metadata.citation.Citation getAuthority() {
        return org.geotools.metadata.citation.Citation.EPSG;
    }
    
//clazz is the class of codes to return (to filter set)
//will want to filter wkt strings based on "PROJCS" and "GEOGCS"  
    public Set getAuthorityCodes(Class clazz) throws FactoryException {
//wrong
        return epsg.keySet();        
    }
    
    public ObjectFactory getObjectFactory() {
        return crsFactory;        
    }
    
    public org.opengis.metadata.citation.Citation getVendor() {
         return org.geotools.metadata.citation.Citation.GEOTOOLS;
    }
    
    public org.opengis.util.InternationalString getDescriptionText(String str) throws FactoryException {
//should implement this (return the crs name)        
        throw new FactoryException("Not implemented");
    } 
    
    public org.opengis.referencing.crs.CompoundCRS createCompoundCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
        
    public org.opengis.referencing.crs.DerivedCRS createDerivedCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.EngineeringCRS createEngineeringCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.GeocentricCRS createGeocentricCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.ImageCRS createImageCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.TemporalCRS createTemporalCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
    
    public org.opengis.referencing.crs.VerticalCRS createVerticalCRS(String str) throws FactoryException {
        throw new FactoryException("Not implemented");
    }
}
