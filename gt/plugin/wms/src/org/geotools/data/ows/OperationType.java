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
package org.geotools.data.ows;

import java.net.URL;


public class OperationType {
    protected Object formats;
    private URL get;
    private URL post;

    /**
     * @return Returns the formats.
     */
    public Object getFormats() {
        return formats;
    }

    /**
     * @param formats The formats to set.
     */
    public void setFormats(Object formats) {
        this.formats = formats;
    }

    /**
     * @return Returns the get.
     */
    public URL getGet() {
        return get;
    }

    /**
     * @param get The get to set.
     */
    public void setGet(URL get) {
        this.get = get;
    }

    /**
     * @return Returns the post.
     */
    public URL getPost() {
        return post;
    }

    /**
     * @param post The post to set.
     */
    public void setPost(URL post) {
        this.post = post;
    }
}
