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
package org.geotools.data.wfs;

import org.geotools.data.FeatureReader;
import org.geotools.data.wfs.Action.InsertAction;
import org.geotools.data.wfs.Action.UpdateAction;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.gml.FCBuffer;
import org.geotools.xml.gml.GMLComplexTypes;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class WFSFeatureReader extends FCBuffer {
    private InputStream is = null;
    private WFSTransactionState ts = null;
    private Feature next = null;
    private int insertSearchIndex = -1;

    private WFSFeatureReader(InputStream is, int capacity, int timeout,
        WFSTransactionState trans) {
        //document may be null
        super(null, capacity, timeout);
        this.is = is;
        ts = trans;
    }

    public static FeatureReader getFeatureReader(URI document, int capacity,
        int timeout, WFSTransactionState transaction) throws SAXException {
        HttpURLConnection hc;

        try {
            hc = (HttpURLConnection) document.toURL().openConnection();

            return getFeatureReader(hc.getInputStream(), capacity, timeout,
                transaction);
        } catch (MalformedURLException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        } catch (IOException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }
    }

    public static WFSFeatureReader getFeatureReader(InputStream is,
        int capacity, int timeout, WFSTransactionState transaction)
        throws SAXException {
        WFSFeatureReader fc = new WFSFeatureReader(is, capacity, timeout,
                transaction);
        fc.start(); // calls run

        if (fc.exception != null) {
            throw fc.exception;
        }

        return fc;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        HashMap hints = new HashMap();
        hints.put(GMLComplexTypes.STREAM_HINT, this);

        try {
            try {
                DocumentFactory.getInstance(is, hints, logger.getLevel());
                is.close();

                // start parsing until buffer part full, then yield();
            } catch (StopException e) {
                exception = e;
                state = STOP;
                is.close();
                yield();
            } catch (SAXException e) {
                exception = e;
                state = STOP;
                is.close();
                yield();
            }
        } catch (IOException e) {
            logger.warning(e.toString());
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            return true;
        }

        try {
            loadElement();
        } catch (NoSuchElementException e) {
            return false;
        } catch (IllegalAttributeException e) {
            return false;
        }

        return next != null;
    }

    private boolean loadElement()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        if (ts == null) {
            while ((next == null) && super.hasNext())
                next = super.next();
        } else {
            List l = ts.getActions();

            while ((next == null) && super.hasNext()) {
                next = super.next();

                if ((ts != null) && (next != null)) {
                    // 	check to make sure it wasn't deleted
                    // 	check for updates
                    Iterator i = l.iterator();

                    while (i.hasNext() && (next != null)) {
                        Action a = (Action) i.next();

                        if ((a.getType() == Action.DELETE)
                                && a.getFilter().contains(next)) {
                            next = null;
                        } else {
                            if ((a.getType() == Action.UPDATE)
                                    && a.getFilter().contains(next)) {
                                // update the feature
                                UpdateAction ua = (UpdateAction) a;
                                String[] propNames = ua.getPropertyNames();

                                for (int j = 0; j < propNames.length; j++) {
                                    next.setAttribute(propNames[j],
                                        ua.getProperty(propNames[j]));
                                }
                            }
                        }
                    }
                }
            }

            if ((insertSearchIndex < l.size()) && (next == null)) {
                // look for an insert then
                // advance one spot
                insertSearchIndex = (insertSearchIndex + 1);

                while ((insertSearchIndex < l.size()) && (next == null)) {
                    Action a = (Action) l.get(insertSearchIndex);

                    if (a.getType() == Action.INSERT) {
                        InsertAction ia = (InsertAction) a;
                        next = ia.getFeature();

                        //run thorough the rest to look for deletes / mods
                        int i = insertSearchIndex + 1;

                        while ((i < l.size()) && (next != null)) {
                            a = (Action) l.get(i);

                            if ((a.getType() == Action.DELETE)
                                    && a.getFilter().contains(next)) {
                                next = null;
                            } else {
                                if ((a.getType() == Action.UPDATE)
                                        && a.getFilter().contains(next)) {
                                    // update the feature
                                    UpdateAction ua = (UpdateAction) a;
                                    String[] propNames = ua.getPropertyNames();

                                    for (int j = 0; j < propNames.length;
                                            j++) {
                                        next.setAttribute(propNames[j],
                                            ua.getProperty(propNames[j]));
                                    }
                                }
                            }

                            i++;
                        }
                    }
                    insertSearchIndex++;
                }
            }
        }

        return next != null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (next == null) {
            loadElement(); // load it

            if (next == null) {
                throw new NoSuchElementException();
            }
        }

        Feature r = next;
        next = null;

        return r;
    }
}
