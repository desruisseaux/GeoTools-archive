/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.postgis;

import java.io.IOException;

import org.geotools.data.DataSourceException;

/**
 * Simple struct used to specify with revision must be extracted, and eventually from which branch
 * (even if at the moment we don't have branches around).
 * 
 * @author aaime
 * @since 2.4
 * 
 */
class RevisionInfo {
    long revision;

    /**
     * RevisionInfo for the last revision in "trunk"
     */
    public RevisionInfo() {
        revision = Long.MAX_VALUE;
    }

    public RevisionInfo(long revision) {
        this.revision = revision;
    }

    /**
     * Parses a version specification into a RevisionInfo object
     * 
     * @param version
     * @throws IOException
     */
    public RevisionInfo(String version) throws IOException {
        if (version == null || version.trim().equals("")) {
            revision = Long.MAX_VALUE;
        } else {
            try {
                revision = Long.parseLong(version);
            } catch (NumberFormatException e) {
                throw new DataSourceException("Unsupported revision format '" + version + "'", e);
            }
        }
    }

    public boolean isLast() {
        return revision == Long.MAX_VALUE;
    }

    public String toVersion() {
        if (isLast())
            return "";
        else
            return String.valueOf(revision);
    }
    
    public String toString() {
        if (isLast())
            return "LATEST";
        else
            return String.valueOf(revision);
    }
}