/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

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
