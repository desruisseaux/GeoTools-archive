package org.geotools.data.shapefile.dbf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class DBFFileIndexerTest extends TestCase {

	DbaseFileReader reader;
	DBFFileIndexer indexer;
	
	
	protected void setUp() throws Exception {
		reader = 
			new DbaseFileReader(new FileInputStream(getFile()).getChannel());
		indexer = new DBFFileIndexer(getFile().toURL());
	}
	
	protected void tearDown() throws Exception {
		reader.close();
	}
//	
//	public void testIndex() throws Exception {
//		indexer.index();
//		
//		assertTrue(getIndexFile().exists());
//	}
	
//	public void testReadIndex() throws Exception {
//		indexer.index();
//		
//		DBFFileIndexer.Reader idxReader = new DBFFileIndexer.Reader(
//			getIndexFile().toURL()
//		);
//		IndexedDbaseFileReader r1 = new IndexedDbaseFileReader(
//			new FileInputStream(getFile()).getChannel(),null
//		);
//		IndexedDbaseFileReader r2 = new IndexedDbaseFileReader(
//			new FileInputStream(getFile()).getChannel(),idxReader
//		);
//		DbaseFileHeader header = reader.getHeader();
//		int nfields = header.getNumFields();
//		int nrecs = header.getNumRecords();
//		
//		try {
//			for (int i = 1; i <= nrecs; i++) {
//				r1.goTo(i);
//				r2.goTo(i);
//				DbaseFileReader.Row row1 = r1.readRow();
//				DbaseFileReader.Row row2 = r2.readRow();
//				
//				for (int j = 0; j < nfields; j++) {
//					char type = header.getFieldType(j);
//					if (type == 'C' || type == 'c') {
//						String s1 = (String)row1.read(j);
//						String s2 = (String)row2.read(j);
//						assertEquals(s1,s2);
//					}
//				}
//			}
//		}
//		finally {
//			r1.close();
//			r2.close();
//		}
//	}
	
	public void testReadTime() throws Exception {
		indexer.index();
		IndexedDbaseFileReader r1 = new IndexedDbaseFileReader(
			new FileInputStream(getFile()).getChannel(),null
		);
		
		DbaseFileHeader header = reader.getHeader();
		int nfields = header.getNumFields();
		int nrecs = header.getNumRecords();
		
		try {
//			long t1 = System.currentTimeMillis();
//			
//			for (int i = 1; i <= nrecs; i++) {
//				r1.goTo(i);
//				DbaseFileReader.Row row1 = r1.readRow();
//				
//				for (int j = 0; j < nfields; j++) {
//					char type = header.getFieldType(j);
//					if (type == 'C' || type == 'c') {
//						row1.read(j);
//					}
//				}
//			}
//			
//			long t2 = System.currentTimeMillis();
//			System.out.println((t2-t1)/1000d);
//			
			r1.close();
			
			DBFFileIndexer.Reader idxReader = new DBFFileIndexer.Reader(
				getIndexFile().toURL()
			);
			IndexedDbaseFileReader r2 = new IndexedDbaseFileReader(
				new FileInputStream(getFile()).getChannel(),idxReader
			);
			//t1 = System.currentTimeMillis();
			for (int i = 1; i <= nrecs; i++) {
				r2.goTo(i);
				DbaseFileReader.Row row2 = r2.readRow();
				
				for (int j = 0; j < nfields; j++) {
					char type = header.getFieldType(j);
					if (type == 'C' || type == 'c') {
						row2.read(j);
					}
				}
			}
			//t2 = System.currentTimeMillis();
			//System.out.println((t2-t1)/1000d);
			r2.close();
		}
		finally {
		
		}
	}
	
	File getFile() {
		return new File("/data/devel/geoserver/cite/confIndexedShapefile/data/featureTypes/cite_NRN/ROADSEG.dbf");
		//return new File("/data/devel/geoserver/cite/confIndexedShapefile/data/featureTypes/cite_Counties/countyp020.dbf");
	}
	
	File getIndexFile() {
		File dbf = getFile();
		
		String path = dbf.getAbsolutePath();
		path = path.substring(0, path.lastIndexOf('.')) + ".dix";
		
		return new File(path);
	}
}
