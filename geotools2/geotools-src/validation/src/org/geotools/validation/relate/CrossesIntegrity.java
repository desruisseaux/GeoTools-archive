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

/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.

 * Created on Apr 27, 2004
 *
 */
package org.geotools.validation.relate;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Pati
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CrossesIntegrity extends RelationIntegrity {
	private static final Logger LOGGER = Logger.getLogger("org.geotools.validation");
	
	
	/**
	 * OverlapsIntegrity Constructor
	 * 
	 */
	public CrossesIntegrity()
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
	 * DOCUMENT ME!!
	 * </p>
	 * 
	 * <b>Description:</b><br>
	 * <p>
	 * DOCUMENT ME!!
	 * </p>
	 * 
	 * Author: bowens<br>
	 * Created on: Apr 27, 2004<br>
	 * @param featureSourceA
	 * @param featureSourceB
	 * @param expected
	 * @param results
	 * @param bBox
	 * @return
	 * @throws Exception
	 */
	private boolean validateMultipleLayers(	FeatureSource featureSourceA, 
											FeatureSource featureSourceB, 
											boolean expected, 
											ValidationResults results, 
											Envelope bBox) 
	throws Exception
	{
		boolean success = true;
		
		FilterFactory ff = FilterFactory.createFilterFactory();
		Filter filter = null;

		filter = (Filter) ff.createBBoxExpression(bBox);

		FeatureResults featureResultsA = featureSourceA.getFeatures(filter);
		FeatureResults featureResultsB = featureSourceB.getFeatures(filter);
		
		FeatureReader fr1 = null;
		FeatureReader fr2 = null;
		try 
		{
			fr1 = featureResultsA.reader();

			if (fr1 == null)
				return false;
						
			while (fr1.hasNext())
			{
				Feature f1 = fr1.next();
				Geometry g1 = f1.getDefaultGeometry();
				fr2 = featureResultsB.reader();
				
				while (fr2 != null && fr2.hasNext())
				{
					Feature f2 = fr2.next();
					Geometry g2 = f2.getDefaultGeometry();
					if(g1.crosses(g2) != expected )
					{
						results.error( f1, f1.getDefaultGeometry().getGeometryType()+" "+getGeomTypeRefA()+" overlapped "+getGeomTypeRefB()+"("+f2.getID()+"), Result was not "+expected );
						success = false;
					}
				}		
			}
		}finally
		{
			/** Close the connections too the feature readers*/
			try {
				fr1.close();
				if (fr2 != null)
					fr2.close();
			} catch (IOException e4) {
				e4.printStackTrace();
				throw e4;
			}
		}
				
		return success;
	}



	/**
	 * <b>validateSingleLayer Purpose:</b> <br>
	 * <p>
	 * DOCUMENT ME!!
	 * </p>
	 * 
	 * <b>Description:</b><br>
	 * <p>
	 * DOCUMENT ME!!
	 * </p>
	 * 
	 * Author: bowens<br>
	 * Created on: Apr 27, 2004<br>
	 * @param featureSourceA
	 * @param expected
	 * @param results
	 * @param bBox
	 * @return
	 * @throws Exception
	 */
	private boolean validateSingleLayer(FeatureSource featureSourceA, 
										boolean expected, 
										ValidationResults results, 
										Envelope bBox) 
	throws Exception
	{
		boolean success = true;
		
		FilterFactory ff = FilterFactory.createFilterFactory();
		Filter filter = null;

		filter = (Filter) ff.createBBoxExpression(bBox);

		FeatureResults featureResults = featureSourceA.getFeatures(filter);
		
		FeatureReader fr1 = null;
		FeatureReader fr2 = null;
		try 
		{
			fr1 = featureResults.reader();

			if (fr1 == null)
				return false;
					
			while (fr1.hasNext())
			{
				Feature f1 = fr1.next();
				Geometry g1 = f1.getDefaultGeometry();
				fr2 = featureResults.reader();
				
				while (fr2 != null && fr2.hasNext())
				{
					Feature f2 = fr2.next();
					Geometry g2 = f2.getDefaultGeometry();
					if (!f1.getID().equals(f2.getID()))	// if they are the same feature, move onto the next one
					{
						if(g1.crosses(g2) != expected )
						{
							results.error( f1, f1.getDefaultGeometry().getGeometryType()+" "+getGeomTypeRefA()+" overlapped "+getGeomTypeRefA()+"("+f2.getID()+"), Result was not "+expected );
							success = false;
						}
					}
				}		
			}
		}finally
		{
			/** Close the connections too the feature readers*/
			try {
				fr1.close();
				if (fr2 != null)
					fr2.close();
			} catch (IOException e4) {
				e4.printStackTrace();
				throw e4;
			}
		}
		
		return success;
	}
}
