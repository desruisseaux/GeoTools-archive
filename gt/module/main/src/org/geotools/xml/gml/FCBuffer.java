/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.xml.gml;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.logging.Logger;


/**
 * <p>
 * Feature Buffer ... acts as a FeatureReader by making itself as a seperate
 * thread prior starting execution with the SAX Parser.
 * </p>
 *
 * @author dzwiers
 */
public class FCBuffer extends Thread implements FeatureReader {
    /** Last feature is in the buffer */
    public static final int FINISH = -1;

    /** DOCUMENT ME!  */
    public static final int STOP = -2;

    /** DOCUMENT ME!  */
    protected static Logger logger = Logger.getLogger("org.geotools.xml.gml");

    // positive number is the number of feature to parse before yield

    /** DOCUMENT ME!  */
    protected int state = 0;
    private Feature[] features;
    private int end;
    private int size;
    private int head;
    private URI document; // for run
    private FeatureType featureType;
    protected SAXException exception = null;

    /*
     * Should not be called
     */
    private FCBuffer() {
    }

    /**
     * Creates a new FCBuffer object.
     *
     * @param document DOCUMENT ME!
     * @param capacity buffer feature capacity
     */
    protected FCBuffer(URI document, int capacity) {
        features = new Feature[capacity];
        this.document = document;
        end = size = head = 0;
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
     * <p>
     * Adds a feature to the buffer
     * </p>
     *
     * @param f Feature to add
     *
     * @return false if unable to add the feature
     */
    protected boolean addFeature(Feature f) {
        if (featureType == null) {
            featureType = f.getFeatureType();
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
     * @throws SAXException
     */
    public static FeatureReader getFeatureReader(URI document, int capacity) throws SAXException {
        FCBuffer fc = new FCBuffer(document, capacity);
        fc.start(); // calls run

        if(fc.exception != null)
            throw fc.exception;
        
        return fc;
    }

    /**
     * @throws SAXException
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        // TODO put a real counter here
//        int t = 100;
        while ((featureType == null) && (state != FINISH && state != STOP)){// && t>0){
            yield(); // let the parser run ... this is being called from 
//            t --;
        }
//        if(t<=0){
//            exception = new SAXException("Timeout");
//            state = STOP;
//        }

        // the original thread
        if (state == FINISH || state == STOP) {
            return null;
        } else {
            return featureType;
        }
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (exception != null) {
            state = STOP;
            throw new IOException(exception.toString());
        }

        size--;

        Feature f = features[head++];

        if (head == features.length) {
            head = 0;
        }

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
            throw new IOException(exception.toString());
        }

        logger.finest("hasNext " + size);

        // TODO put a real counter here
//        int t=1000;
        while ((size <= 1) && (state != FINISH) && (state != STOP) ){//&& t>0) {

            if (exception != null) {
                state = STOP;
                throw new IOException(exception.toString());
            }
            logger.finest("waiting for parser");
            Thread.yield();
//            t --;
        }
//        if(t<=0){
//            state = STOP;
//            throw new IOException("Timeout");
//        }

        if (state == STOP) {
            return false;
        }

        if (state == FINISH) {
            return !(size == 0);
        }

        if (size == 0) {
            state = STOP;
            if(exception!=null)
                throw new IOException(exception.toString());
            throw new IOException("There was an error");
        }

        return true;
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        state = STOP; // note for the sax parser
        // TODO better here !!!
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        HashMap hints = new HashMap();
        hints.put(GMLComplexTypes.STREAM_HINT, this);

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
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.9 $
     */
    public static class StopException extends SAXException {
        StopException() {
            super("Stopping");
        }
    }
}

