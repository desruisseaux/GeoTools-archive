package org.geotools.data;

import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class DiffWriterTest extends TestCase {

	DiffFeatureWriter writer;
	private Point geom;
	private FeatureType type;
	
	protected void setUp() throws Exception {
        type = DataUtilities.createType("default", "name:String,*geom:Geometry");
        GeometryFactory fac=new GeometryFactory();
        geom = fac.createPoint(new Coordinate(10,10));

        Diff diff=new Diff();
		diff.add("1", type.create(new Object[]{ "diff1", geom }, "1"));
		diff.modify("original", type.create(new Object[]{ "diff2", geom }, "original"));
		FeatureReader reader=new TestReader(type,type.create(new Object[]{ "original", geom }, "original") );
		writer=new DiffFeatureWriter(reader, diff){

			protected void fireNotification(int eventType, Envelope bounds) {
				// 
			}
			
		};
	}

	public void testRemove() throws Exception {
		writer.next();
		Feature feature=writer.next();
		writer.remove();
		assertNull(writer.diff.added.get(feature.getID()));
	}

	public void testHasNext() throws Exception {
		assertTrue(writer.hasNext());
		assertEquals(2, writer.diff.added.size()+writer.diff.modified2.size());
		writer.next();
		assertTrue(writer.hasNext());
		assertEquals(2, writer.diff.added.size()+writer.diff.modified2.size());
		writer.next();
		assertFalse(writer.hasNext());
		assertEquals(2, writer.diff.added.size()+writer.diff.modified2.size());
	}
	
	public void testWrite() throws IOException, Exception {
		while( writer.hasNext() ){
			writer.next();
		}
		
		Feature feature=writer.next();
		feature.setAttribute("name", "new1");
		
		writer.write();
		assertEquals(2, writer.diff.added.size() );
		feature=writer.next();
		feature.setAttribute("name", "new2");
		
		writer.write();
		
		assertEquals(3, writer.diff.added.size() );
	}


}
