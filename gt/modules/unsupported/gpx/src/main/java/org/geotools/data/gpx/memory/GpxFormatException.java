/*
 * Created on 2006.11.21.
 *
 * $Id$
 *
 */
package org.geotools.data.gpx.memory;

public class GpxFormatException extends Exception {
    public GpxFormatException(String message) {
        super(message);
    }

    public GpxFormatException(String message, Exception e) {
        super(message, e);
    }
}
