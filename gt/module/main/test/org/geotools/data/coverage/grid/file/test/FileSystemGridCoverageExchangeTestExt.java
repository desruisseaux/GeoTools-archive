
package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;

import junit.framework.TestCase;


import org.opengis.catalog.CatalogEntry;
import org.opengis.catalog.QueryResult;

import org.geotools.catalog.DefaultQueryDefinition;
import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.coverage.grid.file.FileSystemGridCoverageExchange;
import org.geotools.expr.Expr;
import org.geotools.expr.Exprs;
import org.geotools.catalog.Query;
import org.geotools.resources.TestData;





/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileSystemGridCoverageExchangeTestExt extends TestCase {

	File root;
	FileSystemGridCoverageExchange exchange;
	
		
	
	private void init() throws Exception{
		URL url=TestData.getResource(this,"ArcGrid.asc");
		root=new File(new URI(URLDecoder.decode(url.toString(),"UTF-8"))).getParentFile();
		exchange=new FileSystemGridCoverageExchange();
		exchange.setRecursive(true);
		exchange.setDataSource(root);
	}
	

	public void testFileSystemGridCoverageExchange()throws Exception {
		URL url=TestData.getResource(this,"ArcGrid.asc");
		exchange= new FileSystemGridCoverageExchange();
		assertNotNull(exchange);
		
		exchange.add(new File(new URI(URLDecoder.decode(url.toString(),"UTF-8"))));
		Iterator iter=exchange.iterator();
		assertTrue(iter.hasNext());
		CatalogEntry f= (CatalogEntry)iter.next();
		assertEquals( url.getFile(), f.getDataName() );
		
		init();
		iter=exchange.iterator();
		assertTrue(iter.hasNext());
		f= (CatalogEntry)iter.next();
		assertTrue(f.getResource() instanceof File );

	}


	public void testGetFormats() throws Exception{
		init();
		Format[] formats=exchange.getFormats();
		assertNotNull(formats);
		assertTrue(formats.length>0);
		Format arcgrid=null;
		for (int i = 0; i < formats.length; i++) {
			Format format = formats[i];
			if(format.getName().equals("ArcGrid"))
				arcgrid=format;
		}
		assertNotNull(arcgrid);
	}

	public void testQuery()throws Exception {
		init();
		
		Expr expr=Exprs.meta("Name");
		Query query=new Query(expr);
		
		QueryResult result=exchange.query(new DefaultQueryDefinition(query));
		assertNotNull(result);
		assertEquals(2, result.getNumEntries());
	}

	public void testGetReader() throws Exception {
		init();

		Expr expr=Exprs.meta("Name");
		Query query=new Query(expr);
		
		QueryResult result=exchange.query(new DefaultQueryDefinition(query));
		
		GridCoverageReader reader=exchange.getReader(result.getEntry(0));
		assertNotNull(reader);
	}

	public void testGetWriter() throws Exception{
		init();
		Format[] formats=exchange.getFormats();
		GridCoverageWriter writer=exchange.getWriter(root, formats[0]);
	}


}
