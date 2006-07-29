
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
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
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
 * @source $URL$
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
    
    public static FidFilter doInsert(DataStore ds,FeatureType ft,FeatureCollection insert) throws NoSuchElementException, IOException, IllegalAttributeException{
    	Transaction t = new DefaultTransaction();
    	WFSFeatureStore fs = (WFSFeatureStore)ds.getFeatureSource(ft.getTypeName());
    	fs.setTransaction(t);
    	System.out.println("Insert Read 1");
    	FeatureIterator fr = fs.getFeatures().features();
    	int count1 = 0;
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}
        fr.close();
    	System.out.println("Insert Add Features");
    	fs.addFeatures(insert);

    	System.out.println("Insert Read 2");
    	fr = fs.getFeatures().features();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++; fr.next();
    	}
        fr.close();
    	assertEquals(count1+insert.size(), count2);

    	System.out.println("Insert Commit");
    	t.commit();

    	System.out.println("Insert Read 3");
    	fr = fs.getFeatures().features();
    	int count3 = 0;
    	while(fr.hasNext()){
    		count3 ++; fr.next();
    	}
        fr.close();
    	assertEquals(count2,count3);
    	
    	WFSTransactionState ts = (WFSTransactionState)t.getState(ds);
    	FidFilter ff = FilterFactoryFinder.createFilterFactory().createFidFilter();
    	String[] fids = ts.getFids(ft.getTypeName());
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
    	FeatureIterator fr = fs.getFeatures().features();
    	int count1 = 0;
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}
        fr.close();

    	System.out.println("Delete Remove "+ff);
    	fs.removeFeatures(ff);

    	System.out.println("Delete Read 2");
    	fr = fs.getFeatures().features();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++;
    		if(count2<5)
    			System.out.println("# == "+count2+" "+fr.next().getID());
    		else
    			fr.next();
    	}
        fr.close();
    	assertTrue("Read 1 == "+count1+" Read 2 == "+count2,count2<count1);

    	System.out.println("Delete Commit");
    	t.commit();

    	System.out.println("Delete Read 3");
    	fr = fs.getFeatures().features();
    	int count3 = 0;
    	while(fr.hasNext()){
    		count3 ++; fr.next();
    	}
        fr.close();
    	assertTrue(count2==count3);
    }
    
    public static void doUpdate(DataStore ds,FeatureType ft, String attributeToChange, Object newValue ) throws IllegalFilterException, FactoryConfigurationError, NoSuchElementException, IOException, IllegalAttributeException{
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ft.getTypeName());
    	fs.setTransaction(t);
    	
    	AttributeType at = ft.getAttributeType(attributeToChange);
    	assertNotNull("Attribute "+attributeToChange+" does not exist",at);
    	
    	CompareFilter f = FilterFactoryFinder.createFilterFactory().createCompareFilter(FilterType.COMPARE_EQUALS);
    	f.addLeftValue(FilterFactoryFinder.createFilterFactory().createAttributeExpression(at.getName()));
    	f.addRightValue(FilterFactoryFinder.createFilterFactory().createLiteralExpression(newValue));

    	System.out.println("Update Read 1");
    	FeatureIterator fr = fs.getFeatures(f).features();
    	
    	int count1 = 0;
    	Object oldValue=null;
        if(fr!=null)
    	while(fr.hasNext()){
    		count1 ++; oldValue=fr.next().getAttribute(attributeToChange);
    	}

        fr.close();
    	System.out.println("Update Modify");
    	fs.modifyFeatures(at,newValue,Filter.NONE);

    	System.out.println("Update Read 2");
    	fr = fs.getFeatures(f).features();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++;
//    		System.out.println(fr.next());
    		fr.next();
    	}
        fr.close();
//System.out.println("Read 1 == "+count1+" Read 2 == "+count2);
    	assertTrue("Read 1 == "+count1+" Read 2 == "+count2,count2>count1);

    	System.out.println("Update Commit");
        try {
            t.commit();

            assertTrue(((WFSTransactionState) t.getState(ds)).getFids(ft.getTypeName()) != null);

            System.out.println("Update Read 3");
            fr = fs.getFeatures(f).features();
            int count3 = 0;
            while( fr.hasNext() ) {
                count3++;
                fr.next();
            }
            fr.close();
            assertEquals(count2, count3);
        } finally {
            // cleanup
            fs.modifyFeatures(at, oldValue, Filter.NONE);
            t.commit();
        }
    }
}
