
package org.geotools.data.wfs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;


import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * <p> 
 * Needs two featureTypes on the server, with the same data types ... will add the second set to the first set. 
 * </p>
 * @author dzwiers
 *
 */
public class WFSDataStoreWriteTest extends TestCase {
    
    public WFSDataStoreWriteTest(){
        Logger.global.setLevel(Level.SEVERE);
    }

    private DataStore getDataStore(URL server, boolean isPost) throws IOException{
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.GET_CAPABILITIES_URL.key,server);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(100000));
        if(isPost){
            m.put(WFSDataStoreFactory.USE_POST.key,Boolean.TRUE);
            m.put(WFSDataStoreFactory.USE_GET.key,Boolean.FALSE);
        }
        else {
            m.put(WFSDataStoreFactory.USE_GET.key,Boolean.TRUE);
            m.put(WFSDataStoreFactory.USE_POST.key,Boolean.FALSE);
        }
        return (new WFSDataStoreFactory()).createNewDataStore(m);
    }
    
    public FidFilter doInsert(DataStore ds,FeatureType ft,FeatureReader insert) throws NoSuchElementException, IOException, IllegalAttributeException{
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ft.getTypeName());
    	fs.setTransaction(t);
    	System.out.println("Insert Read 1");
    	FeatureReader fr = fs.getFeatures().reader();
    	int count1 = 0;
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}
    	System.out.println("Insert Add Features");
    	fs.addFeatures(insert);

    	System.out.println("Insert Read 2");
    	fr = fs.getFeatures().reader();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++; fr.next();
    	}
    	assertTrue(count2>count1);

    	System.out.println("Insert Commit");
    	t.commit();

    	System.out.println("Insert Read 3");
    	fr = fs.getFeatures().reader();
    	int count3 = 0;
    	while(fr.hasNext()){
    		count3 ++; fr.next();
    	}
    	assertTrue("Read 1 == "+count1+", Read 2 == "+count2+" but Read 3 = "+count3,count2==count3);
    	
    	WFSTransactionState ts = (WFSTransactionState)t.getState(ds);
    	FidFilter ff = FilterFactory.createFilterFactory().createFidFilter();
    	String[] fids = ts.getFids();
    	assertNotNull(fids);
    	for(int i=0;i<fids.length;i++)
    		ff.addFid(fids[i]);
    	return ff;
    }
    
    public void doDelete(DataStore ds,FeatureType ft, FidFilter ff) throws NoSuchElementException, IllegalAttributeException, IOException{
    	assertNotNull("doInsertFailed?",ff);
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ft.getTypeName());
    	fs.setTransaction(t);
    	
    	System.out.println("Delete Read 1");
    	FeatureReader fr = fs.getFeatures().reader();
    	int count1 = 0;
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}

    	System.out.println("Delete Remove "+ff);
    	fs.removeFeatures(ff);

    	System.out.println("Delete Read 2");
    	fr = fs.getFeatures().reader();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++;
    		if(count2<5)
    			System.out.println("# == "+count2+" "+fr.next().getID());
    		else
    			fr.next();
    	}
    	assertTrue("Read 1 == "+count1+" Read 2 == "+count2,count2<count1);

    	System.out.println("Delete Commit");
    	t.commit();

    	System.out.println("Delete Read 3");
    	fr = fs.getFeatures().reader();
    	int count3 = 0;
    	while(fr.hasNext()){
    		count3 ++; fr.next();
    	}
    	assertTrue(count2==count3);
    }
    
    public void doUpdate(DataStore ds,FeatureType ft) throws IllegalFilterException, FactoryConfigurationError, NoSuchElementException, IOException, IllegalAttributeException{
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ft.getTypeName());
    	fs.setTransaction(t);
    	
    	AttributeType at = ft.getAttributeType("LAND_KM");
    	assertNotNull("Attribute LAND_KM does not exist",at);
    	
    	CompareFilter f = FilterFactory.createFilterFactory().createCompareFilter(FilterType.COMPARE_EQUALS);
    	f.addLeftValue(FilterFactory.createFilterFactory().createAttributeExpression(ft,at.getName()));
    	f.addRightValue(FilterFactory.createFilterFactory().createLiteralExpression("3"));

    	System.out.println("Update Read 1");
    	FeatureReader fr = fs.getFeatures(f).reader();
    	
    	int count1 = 0;
    	if(fr!=null)
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}

    	System.out.println("Update Modify");
    	fs.modifyFeatures(at,"3",Filter.NONE);

    	System.out.println("Update Read 2");
    	fr = fs.getFeatures(f).reader();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++; fr.next();
    	}
    	assertTrue("Read 1 == "+count1+" Read 2 == "+count2,count2>count1);

    	System.out.println("Update Commit");
    	t.commit();

    	System.out.println("Update Read 3");
    	fr = fs.getFeatures(f).reader();
    	int count3 = 0;
    	while(fr.hasNext()){
    		count3 ++; fr.next();
    	}
    	assertTrue(count2==count3);
    	
    	// cleanup
    	fs.modifyFeatures(at,"1",Filter.NONE);
    	t.commit();
    }
    
    public void testGeoServer() throws NoSuchElementException, IllegalFilterException, FactoryConfigurationError, IOException, IllegalAttributeException{
        URL url = new URL("http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities");

        DataStore post = getDataStore(url,true);
        FeatureType ft = post.getSchema("topp:states");

		GeometryFactory gf = new GeometryFactory();
        MultiPolygon mp = gf.createMultiPolygon(new Polygon[]{gf.createPolygon(gf.createLinearRing(new Coordinate[]{new Coordinate(-88.071564,37.51099), new Coordinate(-88.467644,37.400757), new Coordinate(-90.638329,42.509361), new Coordinate(-89.834618,42.50346),new Coordinate(-88.071564,37.51099)}),new LinearRing[]{})});
        mp.setUserData("http://www.opengis.net/gml/srs/epsg.xml#4326");
        
        Object[] attrs = {
        		mp,
        		"MyStateName",
				"70",
				"Refrac",
				"RR",
				new Double(180),
				new Double(18),
				new Double(220),
				new Double(80),
				new Double(20),
				new Double(40),
				new Double(180),
				new Double(90),
				new Double(100),
				new Double(40),
				new Double(80),
				new Double(40),
				new Double(180),
				new Double(90),
				new Double(70),
				new Double(70),
				new Double(60),
				new Double(10)	
        };
        
        System.out.println(attrs[0]);
        Feature f = ft.create(attrs);
        
        FeatureReader inserts = DataUtilities.reader(new Feature[] {f});
        FidFilter fp = doInsert(post,ft,inserts);
        // geoserver does not return the correct fid here ... 
        // get the 3rd feature ... and delete it?
        
        inserts.close();
        inserts = post.getFeatureReader(new DefaultQuery(ft.getTypeName()),Transaction.AUTO_COMMIT);
        int i = 0;
        while(inserts.hasNext() && i<3){
        	f = inserts.next();i++;
        }
        inserts.close();
        fp = FilterFactory.createFilterFactory().createFidFilter(f.getID());
        
        doDelete(post,ft,fp);
        doUpdate(post,ft);
    }
    
}
