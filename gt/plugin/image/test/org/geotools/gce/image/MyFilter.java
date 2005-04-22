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
package org.geotools.gce.image;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Helper class used to filter files basing the decision on the file
 * extensions. It is not so smart I know.... :-(
 *
 * @author giannecchini TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
class MyFileFilter implements FilenameFilter {
    private HashSet extensions = new HashSet();

    public MyFileFilter() {
        String[] extensions = new String[] {
                ".gif", ".jpg", ".jpeg", ".tif", ".tiff", ".png"
            };
        this.setExtensions(extensions);
    }

    public MyFileFilter(String[] extensions) {
        this.setExtensions(extensions);
    }

    /**
     * Sets the extensions that are allowed
     *
     * @param extensions
     */
    private void setExtensions(String[] extensions) {
        if (extensions != null) {
            this.extensions.clear();

            for (int i = 0; i < extensions.length; i++)
                this.extensions.add(extensions[i]);
        }
    }

    /**
     * Checks whether or not a file is acceptable following the conditions
     * stated by the given extensions.
     *
     * @param file DOCUMENT ME!
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean accept(File file, String name) {
        if (this.extensions.size() > 0) {
            Iterator it = this.extensions.iterator();

            while (it.hasNext())

                if (name.endsWith((String) it.next())) {
                    return true;
                }
        }

        return false;
    }
}
