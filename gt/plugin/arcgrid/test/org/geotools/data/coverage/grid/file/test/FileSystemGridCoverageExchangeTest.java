/*
 * Created on Jun 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.arcgrid.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.geotools.catalog.CatalogEntry;
import org.geotools.catalog.DefaultQueryDefinition;
import org.geotools.catalog.QueryResult;
import org.geotools.data.arcgrid.ArcGridReader;
import org.geotools.data.gridcoverage.FileSystemGridCoverageExchange;
import org.geotools.expr.Expr;
import org.geotools.expr.Exprs;
import org.geotools.gc.exchange.GridCoverageReader;
import org.geotools.gc.exchange.GridCoverageWriter;
import org.geotools.metadata.Query;
import org.opengis.coverage.grid.Format;



/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileSystemGridCoverageExchangeTest extends TestCaseSupport {

	File root;
	FileSystemGridCoverageExchange exchange;
	
	/**
	 * @param name
	 */
	public FileSystemGridCoverageExchangeTest(String name) {
		super(name);
	}

	
	
	private void init(){
		URL url=getTestResource("ArcGrid.asc");
		root=new File(url.getFile()).getParentFile();
		exchange=new FileSystemGridCoverageExchange(root,false);
	}
	

	public void testFileSystemGridCoverageExchange() {
		URL url=getTestResource("ArcGrid.asc");
		exchange= new FileSystemGridCoverageExchange(null, false);
		assertNotNull(exchange);
		
		exchange.add(new File(url.getFile()));
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


	public void testGetFormats() {
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

	public void testQuery() {
		init();
		
		Expr expr=Exprs.meta("Name");
		Query query=new Query(expr);
		
		QueryResult result=exchange.query(new DefaultQueryDefinition(query));
		assertNotNull(result);
		assertEquals(2, result.getNumEntries());
	}

	public void testGetReader() throws IOException {
		init();

		Expr expr=Exprs.meta("Name");
		Query query=new Query(expr);
		
		QueryResult result=exchange.query(new DefaultQueryDefinition(query));
		
		GridCoverageReader reader=exchange.getReader(result.getEntry(0));
		assertNotNull(reader);
		assertEquals(ArcGridReader.class, reader.getClass());
	}

	public void testGetWriter() throws IOException{
		init();
		Format[] formats=exchange.getFormats();
		GridCoverageWriter writer=exchange.getWriter(root, formats[0]);
	}


}
