
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
import org.geotools.data.DefaultQuery;
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
import org.geotools.filter.IllegalFilterException;

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
    
    public FidFilter doInsert(DataStore ds) throws NoSuchElementException, IOException, IllegalAttributeException{
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ds.getTypeNames()[0]);
    	fs.setTransaction(t);
    	FeatureReader fr = fs.getFeatures().reader();
    	int count1 = 0;
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}
    	DefaultQuery q = new DefaultQuery(ds.getTypeNames()[1]);
    	q.setMaxFeatures(5);
    	fs.addFeatures(ds.getFeatureReader(q,t));
    	
    	fr = fs.getFeatures().reader();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++; fr.next();
    	}
    	assertTrue(count2>count1);
    	
    	t.commit();
    	
    	fr = fs.getFeatures().reader();
    	int count3 = 0;
    	while(fr.hasNext()){
    		count3 ++; fr.next();
    	}
    	assertTrue(count2==count3);
    	
    	WFSTransactionState ts = (WFSTransactionState)t.getState(ds);
    	FidFilter ff = FilterFactory.createFilterFactory().createFidFilter();
    	String[] fids = ts.getFids();
    	assertNotNull(fids);
    	for(int i=0;i<fids.length;i++)
    		ff.addFid(fids[i]);
    	return ff;
    }
    
    public void doDelete(DataStore ds, FidFilter ff) throws NoSuchElementException, IOException, IllegalAttributeException{
    	assertNotNull("doInsertFailed?",ff);
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ds.getTypeNames()[0]);
    	fs.setTransaction(t);
    	FeatureReader fr = fs.getFeatures().reader();
    	int count1 = 0;
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}
    		
    	fs.removeFeatures(ff);
    	
    	fr = fs.getFeatures().reader();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++; fr.next();
    	}
    	assertTrue(count2>count1);
    	
    	t.commit();
    	
    	fr = fs.getFeatures().reader();
    	int count3 = 0;
    	while(fr.hasNext()){
    		count3 ++; fr.next();
    	}
    	assertTrue(count2==count3);
    }
    
    public void doUpdate(DataStore ds) throws IllegalFilterException, FactoryConfigurationError, NoSuchElementException, IOException, IllegalAttributeException{
    	Transaction t = new DefaultTransaction();
    	FeatureStore fs = (FeatureStore)ds.getFeatureSource(ds.getTypeNames()[0]);
    	fs.setTransaction(t);
    	
    	FeatureType ft = fs.getSchema();
    	AttributeType at = ft.getAttributeType(0);
    	
    	CompareFilter f = FilterFactory.createFilterFactory().createCompareFilter(Filter.COMPARE_EQUALS);
    	f.addLeftValue(FilterFactory.createFilterFactory().createAttributeExpression(ft,at.getName()));
    	f.addRightValue(FilterFactory.createFilterFactory().createLiteralExpression("3"));
    	
    	FeatureReader fr = fs.getFeatures(f).reader();
    	
    	int count1 = 0;
    	while(fr.hasNext()){
    		count1 ++; fr.next();
    	}
    		
    	fs.modifyFeatures(at,"3",Filter.NONE);
    	
    	fr = fs.getFeatures(f).reader();
    	int count2 = 0;
    	while(fr.hasNext()){
    		count2 ++; fr.next();
    	}
    	assertTrue(count2>count1);
    	
    	t.commit();
    	
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
        DataStore get = getDataStore(url,false);
        DataStore post = getDataStore(url,true);
        
        FidFilter fg = doInsert(get);FidFilter fp = doInsert(post);
        doDelete(get,fg);doDelete(post,fp);
        doUpdate(get);doUpdate(post);
    }
    
}
