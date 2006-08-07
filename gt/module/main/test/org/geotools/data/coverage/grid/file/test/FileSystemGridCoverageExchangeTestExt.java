/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.data.coverage.grid.file.FileSystemGridCoverageExchange;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;





/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @source $URL$
 */
public class FileSystemGridCoverageExchangeTestExt extends TestCase {

	File root;
	FileSystemGridCoverageExchange exchange;



	private void init() throws Exception{
		root = TestData.copy(this, "arcgrid/ArcGrid.asc");
		exchange=new FileSystemGridCoverageExchange();
		exchange.setRecursive(true);
		exchange.setDataSource(root);
	}


	public void skippedtestFileSystemGridCoverageExchange()throws Exception {
		init();
		File file = root;
		URL url = file.toURL();
		
		Iterator iter=exchange.getFiles().iterator();
		assertTrue(iter.hasNext());
		Object f= iter.next();
//		assertEquals( url.getFile(), f.getDataName() );

		iter=exchange.getFiles().iterator();
		assertTrue(iter.hasNext());
		f= iter.next();
//		assertTrue(f.getResource() instanceof File );
	}


	public void skippedtestGetFormats() throws Exception{
		init();
		Format[] formats=exchange.getFormats();
		assertNotNull(formats);
		//assertTrue(formats.length>0); //TODO: GridFormatFinder is broken 
		Format arcgrid=null;
		for (int i = 0; i < formats.length; i++) {
			Format format = formats[i];
			if(format.getName().equals("ArcGrid"))
				arcgrid=format;
		}
		assertNotNull(arcgrid);
	}

//	public void testQuery()throws Exception {
//		init();
//
//		Expr expr=Exprs.meta("Name");
//		QueryRequest query=new QueryRequest(expr);
//
//		QueryResult result=exchange.query(new DefaultQueryDefinition(query));
//		assertNotNull(result);
//		assertEquals(2, result.getNumEntries());
//	}

//	public void testGetReader() throws Exception {
//		init();
//
//		Expr expr=Exprs.meta("Name");
//		QueryRequest query=new QueryRequest(expr);
//
//		QueryResult result=exchange.query(new DefaultQueryDefinition(query));
//
//		GridCoverageReader reader=exchange.getReader(result.getEntry(0));
//		assertNotNull(reader);
//	}

	public void skippedtestGetWriter() throws Exception{
		init();
		Format[] formats=exchange.getFormats();
		GridCoverageWriter writer=exchange.getWriter(root, formats[0]);
	}


}
