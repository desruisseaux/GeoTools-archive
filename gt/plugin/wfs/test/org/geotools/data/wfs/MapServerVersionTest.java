package org.geotools.data.wfs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.IllegalFilterException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Capture issue http://jira.codehaus.org/browse/GEOT-575
 * 
 * @author jgarnett
 */
public class MapServerVersionTest extends TestCase {

    private URL mapserver44 = null;
    private URL mapserver46 = null;
    private URL version = null;
    
    public MapServerVersionTest() throws MalformedURLException {
        mapserver44 = new URL("http://mapserver.refractions.net/cgi-bin/mapserv44?map=/home/www/mapserv/maps/elections-wms.map&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetCapabilities");
        mapserver46 = new URL("http://mapserver.refractions.net/cgi-bin/mapserv46?map=/home/www/mapserv/maps/elections-wms.map&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetCapabilities");
        //version = mapserver46; // uncomment to test
    }
    
    public void testFeatureTypeGET() throws NoSuchElementException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureType( version,true,false,0);
    }
    public void testFeatureTypePOST() throws NoSuchElementException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureType( version,false,true,0);
    }
    
    public void testFeatureReaderGET() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        WFSDataStoreReadTest.doFeatureReader( version,true,false,0);
    }
    
    // POST is buggy ... and returns HTML, doesn't seem to recognize the request
    //        
    public void testFeatureReaderPOST() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        WFSDataStoreReadTest.doFeatureReader( version,false, true,0);
    }

    public void testFeatureReaderWithFilterGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(version,true,false,0);
    }
    public void testFeatureReaderWithFilterPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(version,false,true,0);
    } 
    
    public void testFeatureReaderWithBBoxGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        Envelope bbox = new Envelope(952432.055579,928103.623676,1223280.254035,1261364.647505);
        // this is from <ID>PRO</ID>
        WFSDataStoreReadTest.doFeatureReaderWithBBox(version,true,false,0,bbox);
    }
    public void testFeatureReaderWithBBoxPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        Envelope bbox = new Envelope(952432.055579,928103.623676,1223280.254035,1261364.647505);        
        WFSDataStoreReadTest.doFeatureReaderWithBBox(version,false,true,0,bbox);
    }    
}