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
package org.geotools.data.wms.response;

import java.io.InputStream;


/**
 * Process GetMapResponse.
 * 
 * <p>
 * Assume this is a placeholder allowing other code access to the InputStream?
 * It would be nice if this class actually provided a real object either
 * returned Image, SVG XML Document or a resolved GridCoverage in a manner
 * similar to GetCapabilities.
 * </p>
 *
 * @author Richard Gould, Refractions Research
 */
public class GetMapResponse extends AbstractResponse {
    public GetMapResponse(String contentType, InputStream response) {
        super(contentType, response);
    }
}
