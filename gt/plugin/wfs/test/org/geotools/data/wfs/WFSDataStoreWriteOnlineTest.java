
package org.geotools.data.wfs;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;

/**
 * <p> 
 * Needs two featureTypes on the server, with the same data types ... will add the second set to the first set. 
 * </p>
 * @author dzwiers
 *
 */
public class WFSDataStoreWriteOnlineTest extends TestCase {
    public void testEmpty() throws NoSuchElementException, IOException, IllegalAttributeException, FactoryConfigurationError{
//        URL u = new URL("http://localhost:8080/geoserver/wfs");
//        WFSDataStore ds = getDataStore(u);
//        FeatureType ft = ds.getSchema("states");
//        Feature f = ds.getFeatureReader("states").next();
//        doDelete(ds,ft,FilterFactoryFinder.createFilterFactory().createFidFilter(f.getID()));
//        FeatureCollection fc = DefaultFeatureCollections.newCollection();
//        fc.add(f);
//        doInsert(ds,ft,(new CollectionDataStore(fc)).getFeatureReader("states"));
    }

    
    public WFSDataStoreWriteOnlineTest(){
        Logger.global.setLevel(Level.SEVERE);
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
    	FidFilter ff = FilterFactoryFinder.createFilterFactory().createFidFilter();
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
    	
    	CompareFilter f = FilterFactoryFinder.createFilterFactory().createCompareFilter(FilterType.COMPARE_EQUALS);
    	f.addLeftValue(FilterFactoryFinder.createFilterFactory().createAttributeExpression(ft,at.getName()));
    	f.addRightValue(FilterFactoryFinder.createFilterFactory().createLiteralExpression(3.0));

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
