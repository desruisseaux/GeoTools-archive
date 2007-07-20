/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004 TOPP - www.openplans.org
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
 *    Created on Apr 27, 2004
 */
package org.geotools.validation.relate;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
/**
 * @author Pati
 *
 * <b>Puropse:</b><br>
 * <p>
 * Tests to see if a Geometry touches another Geometry.
 * 
 * <b>Description:</b><br>
 * <p>
 * If only one Geometry is given, then this test checks to see if it 
 * touches to itself.
 * </p>
 * 
 * <b>Usage:</b><br>
 * <p>
 * 
 * </p>
 * @source $URL$
 */
public class TouchesIntegrity extends RelationIntegrity {
	private static final Logger LOGGER = Logger.getLogger("org.geotools.validation");
	
	
	/**
	 * OverlapsIntegrity Constructor
	 * 
	 */
	public TouchesIntegrity()
	{
		super();
	}
	
	
	/* (non-Javadoc)
	 * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map, com.vividsolutions.jts.geom.Envelope, org.geotools.validation.ValidationResults)
	 */
	public boolean validate(Map layers, Envelope envelope,
			ValidationResults results) throws Exception 
	{
		LOGGER.finer("Starting test "+getName()+" ("+getClass().getName()+")" );
		String typeRef1 = getGeomTypeRefA();
		LOGGER.finer( typeRef1 +": looking up FeatureSource " );    	
		FeatureSource geomSource1 = (FeatureSource) layers.get( typeRef1 );
		LOGGER.finer( typeRef1 +": found "+ geomSource1.getSchema().getTypeName() );
		
		String typeRef2 = getGeomTypeRefB();
		if (typeRef2 == EMPTY || typeRef1.equals(typeRef2))
			return validateSingleLayer(geomSource1, isExpected(), results, envelope);
		else
		{
			LOGGER.finer( typeRef2 +": looking up FeatureSource " );        
			FeatureSource geomSource2 = (FeatureSource) layers.get( typeRef2 );
			LOGGER.finer( typeRef2 +": found "+ geomSource2.getSchema().getTypeName() );
			return validateMultipleLayers(geomSource1, geomSource2, isExpected(), results, envelope);
		}	
	
	}


	/**
	 * <b>validateMultipleLayers Purpose:</b> <br>
	 * <p>
	 * This validation tests for a geometry touches another geometry. 
	 * Uses JTS' Geometry.touches(Geometry) method.
	 * Returns true if the DE-9IM intersection matrix for the two Geometrys is FT*******, F**T***** or F***T****.
	 * </p>
	 * 
	 * <b>Description:</b><br>
	 * <p>
	 * The function filters the FeatureSources using the given bounding box.
	 * It creates iterators over both filtered FeatureSources. It calls touches() using the
	 * geometries in the FeatureSource layers. Tests the results of the method call against
	 * the given expected results. Returns true if the returned results and the expected results 
	 * are true, false otherwise.
	 * 
	 * </p>
	 * 
	 * Author: bowens<br>
	 * Created on: Apr 27, 2004<br>
	 * @param featureSourceA - the FeatureSource to pull the original geometries from. 
	 * @param featureSourceB - the FeatureSource to pull the other geometries from.
	 * @param expected - boolean value representing the user's expected outcome of the test
	 * @param results - ValidationResults
	 * @param bBox - Envelope - the bounding box within which to perform the touches()
	 * @return boolean result of the test
	 * @throws Exception - IOException if iterators improperly closed

	 */
	private boolean validateMultipleLayers(	FeatureSource featureSourceA, 
											FeatureSource featureSourceB, 
											boolean expected, 
											ValidationResults results, 
											Envelope bBox) 
	throws Exception
	{
		boolean success = true;
		
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		Filter filter = null;

		//JD: fix this!!
		//filter = (Filter) ff.createBBoxExpression(bBox);

		FeatureCollection collectionA = featureSourceA.getFeatures(filter);
		FeatureCollection collectionB = featureSourceB.getFeatures(filter);
		
		FeatureIterator fr1 = null;
		FeatureIterator fr2 = null;
		try 
		{
			fr1 = collectionA.features();
			if (fr1 == null)
				return false;
						
			while (fr1.hasNext())
			{
				Feature f1 = fr1.next();
				Geometry g1 = f1.getPrimaryGeometry();
				fr2 = collectionB.features();
				try {
    				while (fr2 != null && fr2.hasNext())
    				{
    					Feature f2 = fr2.next();
    					Geometry g2 = f2.getPrimaryGeometry();
    					if(g1.touches(g2) != expected )
    					{
    						results.error( f1, f1.getPrimaryGeometry().getGeometryType()+" "+getGeomTypeRefA()+" touches "+getGeomTypeRefB()+"("+f2.getID()+"), Result was not "+expected );
    						success = false;
    					}
    				}
                }
                finally {
                    collectionB.close( fr2 );
                }
			}
		}finally
		{
            collectionA.close( fr1 );            
		}
				
		return success;
	}



	/**
	 * <b>validateSingleLayer Purpose:</b> <br>
	 * <p>
	 * This validation tests for a geometry that touches on itself. 
	 * Uses JTS' Geometry.touches(Geometry) method.
	 * Returns true if the DE-9IM intersection matrix for the two Geometrys is FT*******, F**T***** or F***T****.
	 * </p>
	 * 
	 * <b>Description:</b><br>
	 * <p>
	 * The function filters the FeatureSource using the given bounding box.
	 * It creates iterators over the filtered FeatureSource. It calls touches() using the
	 * geometries in the FeatureSource layer. Tests the results of the method call against
	 * the given expected results. Returns true if the returned results and the expected results 
	 * are true, false otherwise.
	 * 
	 * </p>	
	 * 
	 * Author: bowens<br>
	 * Created on: Apr 27, 2004<br>
	 * @param featureSourceA - the FeatureSource to pull the original geometries from. 
	 * @param expected - boolean value representing the user's expected outcome of the test
	 * @param results - ValidationResults
	 * @param bBox - Envelope - the bounding box within which to perform the touches()
	 * @return boolean result of the test
	 * @throws Exception - IOException if iterators improperly closed
	 */
	private boolean validateSingleLayer(FeatureSource featureSourceA, 
										boolean expected, 
										ValidationResults results, 
										Envelope bBox) 
	throws Exception
	{
		boolean success = true;
		
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		Filter filter = null;

		//JD: fix this !!
		//filter = (Filter) ff.createBBoxExpression(bBox);

		FeatureCollection collection = featureSourceA.getFeatures(filter);
		
		FeatureIterator fr1 = null;
		FeatureIterator fr2 = null;
		try 
		{
			fr1 = collection.features();

			if (fr1 == null)
				return false;
					
			while (fr1.hasNext())
			{
				Feature f1 = fr1.next();
				Geometry g1 = f1.getPrimaryGeometry();
				fr2 = collection.features();
				try {
    				while (fr2 != null && fr2.hasNext())
    				{
    					Feature f2 = fr2.next();
    					Geometry g2 = f2.getPrimaryGeometry();
    					if (!f1.getID().equals(f2.getID()))	// if they are the same feature, move onto the next one
    					{
    						if(g1.touches(g2) != expected )
    						{
    							results.error( f1, f1.getPrimaryGeometry().getGeometryType()+" "+getGeomTypeRefA()+" touches "+getGeomTypeRefA()+"("+f2.getID()+"), Result was not "+expected );
    							success = false;
    						}
    					}
    				}
                }
                finally {
                    collection.close( fr2 );
                }
			}
		}
        finally {
            collection.close( fr1 );
		}
		
		return success;
	}	
}
