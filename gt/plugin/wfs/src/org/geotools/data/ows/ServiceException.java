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
package org.geotools.data.ows;

import org.xml.sax.SAXException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class ServiceException extends SAXException {
    private String code = "";
    private String locator = null;

    private ServiceException() {
    }

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(String msg, String code, String locator) {
        super(msg + ((code == null) ? "" : code));
        this.code = code;
        this.locator = locator;
    }

    public String getCode() {
        return code;
    }

    public String getLocator() {
        return locator;
    }
}
