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
 */
package org.geotools.data.ows;

import java.net.URL;
import java.util.List;


/**
 *
 * @source $URL$
 */
public class OperationType {
	protected String name;
	protected List formats;
	protected URL get;
	protected URL post;

    /**
     * DOCUMENT ME!
     *
     * @return Returns the formats.
     */
    public List getFormats() {
        return formats;
    }

    /**
     * DOCUMENT ME!
     *
     * @param formats The formats to set.
     */
    public void setFormats(List formats) {
        this.formats = formats;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the get.
     */
    public URL getGet() {
        return get;
    }

    /**
     * DOCUMENT ME!
     *
     * @param get The get to set.
     */
    public void setGet(URL get) {
        this.get = get;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the post.
     */
    public URL getPost() {
        return post;
    }

    /**
     * DOCUMENT ME!
     *
     * @param post The post to set.
     */
    public void setPost(URL post) {
        this.post = post;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
