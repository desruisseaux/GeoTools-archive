
package org.geotools.data.wfs.demo;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.feature.IllegalAttributeException;

/**
 * <p> 
 * An example use of the WFS data store
 * </p>
 * @author dzwiers Refractions Research
 *
 */
public class WFSDemo {
    public static void main(String[] args){
        System.out.println("WFS Demo");
        try{
            URL url = new URL("http://www2.dmsolutions.ca/cgi-bin/mswfs_gmap?version=1.0.0&request=getcapabilities&service=wfs");
        
            Map m = new HashMap();
            m.put(WFSDataStoreFactory.GET_CAPABILITIES_URL.key,url);
            m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(10000));
            m.put(WFSDataStoreFactory.USE_GET.key,Boolean.TRUE);

            DataStore wfs = (new WFSDataStoreFactory()).createNewDataStore(m);
            Query query = new DefaultQuery(wfs.getTypeNames()[1]);
            FeatureReader ft = wfs.getFeatureReader(query,Transaction.AUTO_COMMIT);
            int count = 0;
            while(ft.hasNext())
                if(ft.next()!=null)
                    count++;
            System.out.println("Found "+count+" features");
        }catch(IOException e){
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IllegalAttributeException e) {
            e.printStackTrace();
        }
    }
}
