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

import java.net.URL;

public class Capability {

    public static final int GET_CAPABILITIES = 1;
    public static final int DESCRIBE_FEATURE_TYPE = 2;
    public static final int TRANSACTION = 4;
    public static final int GET_FEATURE = 8;
    public static final int GET_FEATURE_WITH_LOCK = 16;
    public static final int LOCK_FEATURE = 32;
    
    private URL get;
    private URL post;
    private int type;
    private String[] formats; // only applicable for DescribeFeatureType and GetFeature
    
    /**
     * @return Returns the formats.
     */
    public String[] getFormats() {
        return formats;
    }
    /**
     * @return Returns the get.
     */
    public URL getGet() {
        return get;
    }
    /**
     * @return Returns the post.
     */
    public URL getPost() {
        return post;
    }
    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }
    /**
     * @param formats The formats to set.
     */
    public void setFormats(String[] formats) {
        this.formats = formats;
    }
    /**
     * @param get The get to set.
     */
    public void setGet(URL get) {
        this.get = get;
    }
    /**
     * @param post The post to set.
     */
    public void setPost(URL post) {
        this.post = post;
    }
    /**
     * @param type The type to set.
     */
    public void setType(int type) {
        this.type = type;
    }
}
