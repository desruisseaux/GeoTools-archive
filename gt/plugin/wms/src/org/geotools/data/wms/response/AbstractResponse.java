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
package org.geotools.data.wms.response;

import java.io.InputStream;

/**
 * @author Richard Gould, Refractions Research
 */
public class AbstractResponse {

	protected InputStream inputStream;
	protected String contentType;

	public AbstractResponse(String contentType, InputStream inputStream) {
		this.inputStream = inputStream;
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

}
