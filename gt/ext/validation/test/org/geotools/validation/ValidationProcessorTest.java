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
 */
package org.geotools.validation;

import java.util.HashMap;

import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.validation.spatial.IsValidGeometryValidation;

/**
 * ValidationProcessorTest purpose.
 * <p>
 * Description of ValidationProcessorTest ...
 * <p>
 * Capabilities:
 * <ul>
 * </li></li>
 * </ul>
 * Example Use:
 * <pre><code>
 * ValidationProcessorTest x = new ValidationProcessorTest(...);
 * </code></pre>
 * 
 * @author bowens, Refractions Research, Inc.
 * @author $Author: sploreg $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class ValidationProcessorTest extends DataTestCase {
	MemoryDataStore store;

	ValidationProcessor processor;
	RoadNetworkValidationResults results;
	
	
	/**
	 * Constructor for ValidationProcessorTest.
	 * @param arg0
	 */
	public ValidationProcessorTest(String arg0) {
		super(arg0);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		store = new MemoryDataStore();
		store.addFeatures( roadFeatures );
		store.addFeatures( riverFeatures );
		processor = new ValidationProcessor();		
		results = new RoadNetworkValidationResults();		
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		store = null;
		super.tearDown();
	}

	public void testIsValidFeatureValidation() throws Exception
	{
		IsValidGeometryValidation geom = new IsValidGeometryValidation();
		geom.setName( "IsValidGeometry");
		geom.setDescription("IsValid geomtry test for Junit Test +"+getName());
		geom.setTypeRef("*");
		processor.addValidation( geom );
				
		// test the correct roads
		processor.runFeatureTests( "dataStoreId", this.roadType, DataUtilities.reader(this.roadFeatures), results);
		assertTrue(results.getFailedMessages().length == 0);
		
		// test the broken road
		// make an incorrect line
		try {
			this.newRoad = this.roadType.create(new Object[] {
				new Integer(2), line(new int[] { 1, 2, 1, 2}), "r4"
			}, "road.rd4");
		} catch (IllegalAttributeException e) {}
		
		Feature[] singleRoad = new Feature[1];
		singleRoad[0] = this.newRoad;
		FeatureCollection features = DataUtilities.collection(singleRoad);
		processor.runFeatureTests( "dataStoreId", this.roadType, DataUtilities.reader(features), results);
		assertTrue( results.getFailedMessages().length > 0 );


		// run integrity tests
		// make a map of FeatureSources
		HashMap map = new HashMap();
		String[] typeNames = this.store.getTypeNames();
		for (int i=0; i<typeNames.length; i++)
			map.put(typeNames[i], this.store.getFeatureSource(typeNames[i]));
		map.put("newThing", this.store.getFeatureSource(typeNames[0]));
			
		processor.runIntegrityTests(null, map, null, results);
		assertTrue(results.getFailedMessages().length > 0);
		/*
		String[] messages = validationResults.getFailedMessages();
		for (int i=0; i<validationResults.getFailedMessages().length; i++)
			System.out.println(messages[i]);
		*/	
			
			
	}
	
	

}
