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
package org.geotools.data.shapefile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A read-write lock for shapefiles so that OS file locking exceptions will not ruin an attempt to update a shapefile.
 * 
 * On windows there are often operating system locking conflicts when writing to a shapefile.  In order to 
 * not have exceptions thrown everytime a write is made, geotools has implemented file locking for shapefiles.
 * 
 * @author jeichar
 */
public class Lock {
    public final static int DELAY = 200;
    Logger logger = Logger.getLogger("ShapefileLogger.Lock");
    int level = 0;

    {
        logger.setLevel(Level.FINE);
    }

    /**
     * Called by shapefileReader before a read is started and before an IOStream is openned.
     * 
     * @throws IOException
     */
    public synchronized void startRead() throws IOException {
        while (level < 0) {
            try {
                wait(DELAY);
            } catch (InterruptedException e) {
                throw (IOException) new IOException().initCause(e);
            }
        }

        assertTrue(level > -1);
        level++;
        logger.fine("Start Read Lock:" + level);
    }

    private void assertTrue(boolean b) {
        if (!b) {
            throw new AssertionError();
        }
    }

    /**
     * Called by ShapefileReader after a read is complete and after the IOStream is closed.
     */
    public synchronized void endRead() {
        assertTrue(level > 0);
        level--;
        logger.fine("End Read Lock:" + level);
        notifyAll();
    }
    /**
     * Called by ShapefileDataStore before a write is started and before an IOStream is openned.
     * 
     * @throws IOException
     */
    public synchronized void startWrite() throws IOException {
        while (level != 0) {
            try {
                wait(DELAY);
            } catch (InterruptedException e) {
                throw (IOException) new IOException().initCause(e);
            }
        }

        assertTrue(level == 0);
        level = -1;
        logger.fine("Start Write Lock:" + level);
    }

    /**
     * Called by ShapefileDataStore after a write is complete and after the IOStream is closed.
     */
    public synchronized void endWrite() {
        assertTrue(level == -1);
        level = 0;
        logger.fine("End Write Lock:" + level);
        notifyAll();
    }
}
