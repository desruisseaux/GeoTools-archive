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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;

/**
 * <p>
 * Feature Buffer ... acts as a FeatureReader by making itself as a seperate 
 * thread prior starting execution with the SAX Parser.
 * </p>
 *
 * @author dzwiers
 */
public class FCBuffer extends Thread implements FeatureReader{
    /** Last feature is in the buffer */
    public static final int FINISH = -1;
    public static final int STOP = -2;
    
    protected Logger logger = Logger.getLogger("org.geotools.xml.gml");
    
    // positive number is the number of feature to parse before yield
    protected int state = 0;
    private Feature[] features;
    private int end,size,head;
    private URI document; // for run

    /*
     * Should not be called
     */
    private FCBuffer() {
    }

    /**
     * Creates a new FCBuffer object.
     *
     * @param capacity buffer feature capacity
     */
    protected FCBuffer(URI document, int capacity) {
        features = new Feature[capacity];
        this.document = document;
        end = size = head = 0;
    }
    
    /**
     * 
     * 
     * @return The buffer size
     */
    public int getSize(){
        return size;
    }

    /**
     * 
     * 
     * @return The buffer capacity
     */
    public int getCapacity(){
        return features.length;
    }
    
    /**
     * 
     * <p>
     * Adds a feature to the buffer 
     * </p>
     *
     * @param f Feature to add
     * @return false if unable to add the feature
     */
    protected boolean addFeature(Feature f){
        if(featureType == null)
            featureType = f.getFeatureType();
        if(size >= features.length)
            return false;
        features[end] = f;
        end++;
        if(end == features.length)
            end = 0;
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
     * @return
     */
    public static FeatureReader getFeatureReader(URI document, int capacity){
        FCBuffer fc = new FCBuffer(document,capacity);
        fc.start(); // calls run
        return fc;
    }
    
    private FeatureType featureType;
    
    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        while(featureType==null && state!=FINISH)
            yield(); // let the parser run ... this is being called from 
        			 // the original thread
        if(state==FINISH)
            return null;
        else
            return featureType;
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        if(exception != null)
            throw new IOException(exception.toString());
        size --;
        Feature f =  features[head++];
        if(head == features.length)
            head = 0;
        return f;
    }

    private SAXException exception = null;
    
    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if(exception instanceof StopException)
            return false;
        if(exception != null)
            throw new IOException(exception.toString());
        int t = features.length/3;
        logger.finest("hasNext "+size+" "+t);
        while (size<=t && state != FINISH){
            logger.finest("waiting for parser");
            Thread.yield();
        }
        if(state == FINISH)
            return !(size == 0);
        if(size == 0)
            throw new IOException("There was an error");
        return true;
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        state = STOP; // note for the sax parser
    }
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        HashMap hints = new HashMap();
        hints.put(GMLComplexTypes.STREAM_HINT,this);
        try{
            DocumentFactory.getInstance(document,hints);
            // start parsing until buffer part full, then yield();
        }catch(StopException e){
            exception = e;
        }catch(SAXException e){
            exception = e;
        }
        
    }
}

class StopException extends SAXException{
    StopException(){
        super("Stopping");
    }
}
