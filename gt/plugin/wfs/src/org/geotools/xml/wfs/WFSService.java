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
package org.geotools.xml.wfs;

/**
 * DOCUMENT ME!
 *
 * @author Norman Barker To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Generation - Code and
 *         Comments
 */
public class WFSService {
    private String name = null;
    private String title = null;
    private String keywords = null;
    private String onlineResource = null;
    private String fees = null;
    private String accessConstraints = null;

    /**
     * DOCUMENT ME!
     *
     * @return Returns the fees.
     */
    public String getFees() {
        return fees;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fees The fees to set.
     */
    public void setFees(String fees) {
        this.fees = fees;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the keywords.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param keywords The keywords to set.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the onlineResource.
     */
    public String getOnlineResource() {
        return onlineResource;
    }

    /**
     * DOCUMENT ME!
     *
     * @param onlineResource The onlineResource to set.
     */
    public void setOnlineResource(String onlineResource) {
        this.onlineResource = onlineResource;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the accessConstraints.
     */
    public String getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * DOCUMENT ME!
     *
     * @param accessConstraints The accessConstraints to set.
     */
    public void setAccessConstraints(String accessConstraints) {
        this.accessConstraints = accessConstraints;
    }
}
