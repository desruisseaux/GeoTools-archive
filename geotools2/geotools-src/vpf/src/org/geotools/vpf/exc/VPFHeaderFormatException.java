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

package org.geotools.vpf.exc;

import java.io.IOException;


/**
 * Class VPFHeaderFormatException.java is responsible for
 *
 * <p>
 * Created: Tue Jan 21 15:12:10 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */
public class VPFHeaderFormatException extends VPFDataException {
    /**
     * Creates a new VPFHeaderFormatException object.
     */
    public VPFHeaderFormatException() {
        super();
    }

    /**
     * Creates a new VPFHeaderFormatException object.
     *
     * @param message DOCUMENT ME!
     */
    public VPFHeaderFormatException(String message) {
        super(message);
    }
}


// VPFHeaderFormatException
