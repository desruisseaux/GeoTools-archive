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
package org.geotools.data.wms.capabilities;

import java.util.List;

/**
 * @author rgould
 *
 * Represents available HTTP methods such as <gets> and <posts>
 */
public class HTTP {
    /** A list of type Get */
    private List gets;
    
    /** A list of type Post */
    private List posts;
    

    /**
     * @param gets A list of Get requests
     * @param posts A list of Post requests
     */
    public HTTP(List gets, List posts) {
        super();
        this.gets = gets;
        this.posts = posts;
    }
    public List getGets() {
        return gets;
    }
    public void setGets(List gets) {
        this.gets = gets;
    }
    public List getPosts() {
        return posts;
    }
    public void setPosts(List posts) {
        this.posts = posts;
    }
}
