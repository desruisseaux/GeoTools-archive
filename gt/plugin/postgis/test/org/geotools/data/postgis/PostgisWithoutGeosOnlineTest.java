package org.geotools.data.postgis;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import org.geotools.data.FeatureReader;

import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;

import com.vividsolutions.jts.geom.Envelope;


/**
 * This test should be run against a postgis instance that does not 
 * have GEOS installed.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class PostgisWithoutGeosOnlineTest extends AbstractPostgisDataTestCase {

	public PostgisWithoutGeosOnlineTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public String getFixtureFile() {
		return "nogeos.properties"; 
	}
	
	public void _testBboxQuery() throws Exception {
//		get the bounding box for each feature
		List bbox = new ArrayList();
		List fids = new ArrayList();
		FeatureCollection fc = data.getFeatureSource("road").getFeatures();
		for (Iterator itr = fc.iterator(); itr.hasNext();) {
			Feature f = (Feature)itr.next();
			bbox.add(f.getDefaultGeometry().getEnvelopeInternal());
			fids.add(f.getID());
		}
		
		//query each feature
		FeatureType type = data.getSchema("road");
		FilterFactory ff = FilterFactory.createFilterFactory();
		
		for (int i = 0; i < bbox.size(); i++) {
			Envelope box = (Envelope)bbox.get(i);
			String fid = (String)fids.get(i);
			
			GeometryFilter filter = 
				ff.createGeometryFilter(GeometryFilter.GEOMETRY_BBOX);
			
			filter.addLeftGeometry(ff.createAttributeExpression(type,"geom"));
			filter.addRightGeometry(ff.createBBoxExpression(box));
			
			FeatureReader reader = 
				data.getFeatureReader(type,filter,Transaction.AUTO_COMMIT);
			boolean found = false;
			for (; reader.hasNext();) {
				Feature f = reader.next();
				if (fid.equals(f.getID()))
					found = true;
			}
			reader.close();
			assertTrue(found);
		}
	}
	
	public void testBboxQueryWithLooseBBOX() throws Exception {
		data.setLooseBbox(true);
		_testBboxQuery();
	}
	
	public void testBboxQueryWithoutLooseBBOX() throws Exception {
		data.setLooseBbox(false);
		_testBboxQuery();
	}
}
