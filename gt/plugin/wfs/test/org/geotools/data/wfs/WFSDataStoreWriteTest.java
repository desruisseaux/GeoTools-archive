
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

    public static WFSDataStore getDataStore(URL server) throws IOException{
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,server);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(100000));
        return (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);
    }
    
    public static FidFilter doInsert(DataStore ds,FeatureType ft,FeatureReader insert) throws NoSuchElementException, IOException, IllegalAttributeException{
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
    
    public static void doDelete(DataStore ds,FeatureType ft, FidFilter ff) throws NoSuchElementException, IllegalAttributeException, IOException{
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
    
    public static void doUpdate(DataStore ds,FeatureType ft) throws IllegalFilterException, FactoryConfigurationError, NoSuchElementException, IOException, IllegalAttributeException{
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ft.getTypeName());
    	fs.setTransaction(t);
    	
    	AttributeType at = ft.getAttributeType("LAND_KM");
    	assertNotNull("Attribute LAND_KM does not exist",at);
    	
    	CompareFilter f = FilterFactory.createFilterFactory().createCompareFilter(FilterType.COMPARE_EQUALS);
    	f.addLeftValue(FilterFactory.createFilterFactory().createAttributeExpression(ft,at.getName()));
    	f.addRightValue(FilterFactory.createFilterFactory().createLiteralExpression(3.0));

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
    		count2 ++;
//    		System.out.println(fr.next());
    		fr.next();
    	}
//System.out.println("Read 1 == "+count1+" Read 2 == "+count2);
    	assertTrue("Read 1 == "+count1+" Read 2 == "+count2,count2>count1);

    	System.out.println("Update Commit");
    	t.commit();
    	
    	assertTrue(((WFSTransactionState)t.getState(ds)).getFids()!=null);

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
}
