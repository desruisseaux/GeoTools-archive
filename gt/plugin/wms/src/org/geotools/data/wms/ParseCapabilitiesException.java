/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wms;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Exception thrown trying to parse GetCapabilities document.
 * <p>
 * Would really like to know where in the document the error occured (at least line number).
 * Also need to make use of a nested Exception (for things like a NumberFormatException).
 * </p>
 * Changed to SAXParseException as it fits out needs - alternatives were:
 * <ul>
 * <li>ParseException was based on String offset and did not allow nested Exceptions
 * <li>IOException to general, did not provide any offset or nested Exceptions.
 * </ul>
 * <p>
 * We may wish to retire this class and use SAXParseException directly.
 * </p>
 * @author Richard Gould, Refractions Research
 * @see org.xml.sax.SAXParseException
 * @see org.xml.sax.Locator
 */
public class ParseCapabilitiesException extends SAXParseException {

    private static final long serialVersionUID = 1L;

    /**
     * ParseCapabilitiesException exception at unknown location.
     * 
     * @param message
     */
    public ParseCapabilitiesException(String message) {
        super( message, null );        
    }
    
    /**
     * ParseCapabilitiesException exception at unknown location.
     * <p>
     * Used to wrap root cause to a ParseCapabilitiesException:
     * <pre><code>
     * try {
     *   ...
     * }
     * catch( NumberFormatException badNumber ){
     *    throw new ParseCapabilitiesException( badNumber ); 
     * }
     * </code></pre>
     * </p>
     * <p>
     * Error message will be provided by wraped cause.
     * </p>
     * @param cause Another exception to embed in this one
     */
    public ParseCapabilitiesException(Exception cause) {
        super( null, null, cause );
    }
    
    /**
     * ParseCapabilitiesException exception at unknown location.
     * <p>
     * Used to wrap root cause to a ParseCapabilitiesException:
     * <pre><code>
     * try {
     *   ...
     * }
     * catch( NumberFormatException nan ){
     *    throw new ParseCapabilitiesException("BBox minx invalid", nan ); 
     * }
     * </code></pre>
     * </p>
     * @param message The error or warning message, or null to use
     *                the message from the embedded exception
     * @param locator The locator object for the error or warning (may be
     *                null).
     * @param cause   Another exception to embed in this one
     */
    public ParseCapabilitiesException(String message, Locator locator, Exception cause) {
        super( message, locator, cause );
    }
    
}
