/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml.gml;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.XMLHandlerHints;
import org.xml.sax.SAXException;


/**
 * <p>
 * Feature Buffer ... acts as a FeatureReader by making itself as a seperate
 * thread prior starting execution with the SAX Parser.
 * </p>
 *
 * @author dzwiers
 * @source $URL$
 */
public class FCBuffer extends Thread implements FeatureReader {
    /** Last feature is in the buffer */
    public static final int FINISH = -1;
    
    /** DOCUMENT ME! */
    public static final int STOP = -2;

    /** DOCUMENT ME! */
    protected static Logger logger = getLogger();

    // positive number is the number of feature to parse before yield

    /** DOCUMENT ME! */
    protected int state = 0;
    private Feature[] features;

    private int end;
    private int size;
    private int head;
    private int timeout = 1000;
    private URI document; // for run
    protected SAXException exception = null;

    private FCBuffer() {
        // should not be called
    	super("Feature Collection Buffer");
    }

    /**
     * 
     * @param document
     * @param capacity
     * @param timeout
     * @param ft Nullable
     */
    protected FCBuffer(URI document, int capacity, int timeout, FeatureType ft) {
    	super("Feature Collection Buffer");
        features = new Feature[capacity];
        this.timeout = timeout;
        this.document = document;
        end = size = head = 0;
        this.ft = ft;
    }

    /**
     * Returns the logger to be used for this class.
     *
     * @todo Logger.setLevel(...) should not be invoked, because it override any user setting in
     *       {@code jre/lib/logging.properties}. Users should edit their properties file instead.
     *       If Geotools is too verbose below the warning level, then some log messages should
     *       probably be changed from Level.INFO to Level.FINE.
     */
    private static final Logger getLogger() {
        Logger l = Logger.getLogger("org.geotools.xml.gml");
        l.setLevel(Level.WARNING);
        return l;
    }

    /**
     * DOCUMENT ME!
     *
     * @return The buffer size
     */
    public int getSize() {
        return size;
    }

    /**
     * DOCUMENT ME!
     *
     * @return The buffer capacity
     */
    public int getCapacity() {
        return features.length;
    }

    /**
     * DOCUMENT ME!
     *
     * @return The buffer capacity
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * <p>
     * Adds a feature to the buffer
     * </p>
     *
     * @param f Feature to add
     *
     * @return false if unable to add the feature
     */
    protected boolean addFeature(Feature f) {
        if (ft == null) {
            ft = f.getFeatureType();
        }

        if (size >= features.length) {
            return false;
        }

        features[end] = f;
        end++;

        if (end == features.length) {
            end = 0;
        }

        size++;

        return true;
    }

    /**
     * <p>
     * The prefered method of using this class, this will return the Feature
     * Reader for the document specified, using the specified buffer capacity.
     * </p>
     *
     * @param document URL to parse
     * @param capacity
     *
     * @return
     *
     * @throws SAXException
     */
    public static FeatureReader getFeatureReader(URI document, int capacity)
        throws SAXException {
        return getFeatureReader(document,capacity,1000,null);
    }
    
    public static FeatureReader getFeatureReader(URI document, int capacity, FeatureType ft)
    throws SAXException {
        return getFeatureReader(document,capacity,1000,ft);
}

    public static FeatureReader getFeatureReader(URI document, int capacity,
        int timeout) throws SAXException {

        return getFeatureReader(document,capacity,timeout,null);
    }

    public static FeatureReader getFeatureReader(URI document, int capacity,
        int timeout, FeatureType ft) throws SAXException {
        FCBuffer fc = new FCBuffer(document, capacity, timeout,ft);
        fc.start(); // calls run

        if (fc.exception != null) {
            throw fc.exception;
        }

        if(fc.getFeatureType()!=null && fc.getFeatureType().getDefaultGeometry()!=null && fc.getFeatureType().getDefaultGeometry().getCoordinateSystem() == null){
                // load crs
//                Feature f = fc.peek();
                // TODO set crs here.
        }
        return fc;
    }

    protected FeatureType ft = null;

	private volatile Date lastUpdate;
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        if(ft != null)
            return ft;
        Date d = new Date(Calendar.getInstance().getTimeInMillis() + timeout);

        while ((ft == null) && ((state != FINISH) && (state != STOP))) {
            yield(); // let the parser run ... this is being called from 

            if (d.before(Calendar.getInstance().getTime())) {
                exception = new SAXException("Timeout");
                state = STOP;
            }
        }

        // the original thread
        if ((state == FINISH) || (state == STOP)) {
            return ft;
        }

        return ft;
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, NoSuchElementException {
        if (exception != null) {
            state = STOP;
            IOException e = new IOException(exception.toString());
            e.initCause(exception);
            throw e;
        }

        size--;

        Feature f = features[head++];

        if (head == features.length) {
            head = 0;
        }

        return f;
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature peek()
        throws IOException, NoSuchElementException {
        if (exception != null) {
            state = STOP;
            IOException e = new IOException(exception.toString());
            e.initCause(exception);
            throw e;
        }

        Feature f = features[head];
        return f;
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (exception instanceof StopException) {
            return false;
        }

        if (exception != null) {
            IOException e = new IOException(exception.toString());
            e.initCause(exception);
            throw e;
        }

        logger.finest("hasNext " + size);

        resetTimer();

        while ((size <= 1) && (state != FINISH) && (state != STOP)) { //&& t>0) {
        	
            if (exception != null) {
                state = STOP;
                IOException e = new IOException(exception.toString());
                e.initCause(exception);
                throw e;
            }

            logger.finest("waiting for parser");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                //just continue;
            }

            if (lastUpdate.before(new Date(Calendar.getInstance().getTimeInMillis() - timeout))) {
                exception = new SAXException("Timeout");
                state = STOP;
            }
        }

        if (state == STOP) {
            if (exception != null) {
            	IOException e = new IOException(exception.toString());
            	e.initCause(exception);
            	throw e;
            }

            return false;
        }

        if (state == FINISH) {
            return !(size == 0);
        }

        if (size == 0) {
            state = STOP;

            if (exception != null) {
                throw new IOException(exception.toString());
            }

            throw new IOException("There was an error");
        }

        return true;
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close(){
        state = STOP; // note for the sax parser
        interrupt();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        XMLHandlerHints hints = new XMLHandlerHints();
        initHints(hints);

        try {
            DocumentFactory.getInstance(document, hints);

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

    /**
     * Called before parsing the FeatureCollection.  Subclasses may override to set their custom hints.  
     */
    protected void initHints(XMLHandlerHints hints)  {
        hints.put(XMLHandlerHints.STREAM_HINT, this);
        hints.put(XMLHandlerHints.FLOW_HANDLER_HINT, new FCFlowHandler());
        if( this.ft!=null ){
        	hints.put("DEBUG_INFO_FEATURE_TYPE_NAME", ft.getTypeName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.9 $
     */
    public static class StopException extends SAXException {
        public StopException() {
            super("Stopping");
        }
    }
    
    public int getInternalState() {
    	return state;
    }

	public void resetTimer() {
		this.lastUpdate = Calendar.getInstance().getTime();		
	}
}
