
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;

import org.geotools.data.FeatureReader;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.gml.FCBuffer;
import org.geotools.xml.gml.GMLComplexTypes;
import org.xml.sax.SAXException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSFeatureReader extends FCBuffer {
    
    private InputStream is = null;
    private WFSFeatureReader(InputStream is, int capacity){
        //document may be null
        super(null,capacity);
        this.is = is;
    }
    
    public static FeatureReader getFeatureReader(URI document, int capacity) throws SAXException {
        HttpURLConnection hc;
        try {
            hc = (HttpURLConnection)document.toURL().openConnection();
            WFSFeatureReader fc = new WFSFeatureReader(hc.getInputStream(), capacity);
        	fc.start(); // calls run

            if(fc.exception != null)
                throw fc.exception;
            
        	return fc;
        } catch (MalformedURLException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        } catch (IOException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }
    }
    
    public static FeatureReader getFeatureReader(InputStream is, int capacity) throws SAXException {
        WFSFeatureReader fc = new WFSFeatureReader(is, capacity);
        fc.start(); // calls run
        if(fc.exception != null)
            throw fc.exception;
        return fc;
    }


    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        HashMap hints = new HashMap();
        hints.put(GMLComplexTypes.STREAM_HINT, this);

        try {
            DocumentFactory.getInstance(is, hints, logger.getLevel());

            // start parsing until buffer part full, then yield();
        } catch (StopException e) {
            exception = e;
            state = STOP;
            yield();
        } catch (SAXException e) {
            exception = e;
            state = STOP;
            yield();
        }
    }
}
