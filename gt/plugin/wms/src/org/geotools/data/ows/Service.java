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


/**
 * @author rgould
 *
 * This is a data model for the OGC WMS Service metadata. Feel free to extend it
 * to other OGC services.
 *
 * Name, Title and OnlineResource are Required. Everything else is optional.
 */
public class Service {
    /** The name of the Service (machine readible, typically one word) - Required */
    private String name;

    /** The title for the service (human readible) - Required */
    private String title;

    /** The URL pointing to where this Service can be accessed - Required */
    private URL onlineResource;

    /** Keywords that apply to the Service. Can be used for searching, etc */
    private String[] keywordList;

    /** Abstract allows a description providing more information about the Service */
    private String _abstract;

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public String[] getKeywordList() {
        return keywordList;
    }

    public void setKeywordList(String[] keywordList) {
        this.keywordList = keywordList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getOnlineResource() {
        return onlineResource;
    }

    public void setOnlineResource(URL onlineResource) {
        this.onlineResource = onlineResource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
