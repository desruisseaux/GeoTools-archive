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
/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
/*
 * Created on Jun 28, 2004
 *
 */
package org.geotools.validation;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultRepository;

import junit.framework.TestCase;

/**
 * ValidatorTest<br>
 * @author bowens<br>
 * Created Jun 28, 2004<br>
 * @version <br>
 * 
 * <b>Puropse:</b><br>
 * <p>
 * DOCUMENT ME!!
 * </p>
 * 
 * <b>Description:</b><br>
 * <p>
 * DOCUMENT ME!!
 * </p>
 * 
 * <b>Usage:</b><br>
 * <p>
 * DOCUMENT ME!!
 * </p>
 */
public class ValidatorTest extends TestCase 
{
	
	TestFixture fixture;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fixture = new TestFixture();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		fixture = null;
	}

	public void testRepositoryGeneration()
	{
		//DefaultRepository dataRepository = new DefaultRepository();
		DefaultRepository repo = fixture.repository;
		Set ds = repo.getDataStores();
		assertTrue(ds!=null);
		assertTrue(ds.size() > 0);
		System.out.println("num datasotres = " + ds.size());
		assertNotNull(repo.datastore("cite"));			// fails
		Iterator it = ds.iterator();
		while (it.hasNext())
		{
			Object o = it.next();
			assertNotNull(o);							// fails
			System.out.println(o.getClass());
			DataStore d = (DataStore) o;
			String[] typeNames = null;
			try {
				typeNames = d.getTypeNames();
				System.out.println(typeNames.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (int i=0; i<typeNames.length; i++)
			{
				if (typeNames != null)
					System.out.println(typeNames[i]);
			}
		}
	}
	
	public void testFeatureValidation() {
	}

	public void testIntegrityValidation() {
	}

}
