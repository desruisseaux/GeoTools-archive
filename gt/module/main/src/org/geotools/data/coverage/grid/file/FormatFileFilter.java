/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.data.coverage.grid.file;

import org.geotools.data.coverage.grid.Format;
import java.io.File;
import java.io.FileFilter;


/**
 * DOCUMENT ME!
 *
*  @author Jesse Eichar
 * @author $author$ (Last Modified)
 * @version $Revision: 1.9 $
 */
public class FormatFileFilter implements FileFilter {
    Format[] formats;

    /**
     * Creates a new FormatFileFilter object.
     *
     * @param f DOCUMENT ME!
     */
    public FormatFileFilter(Format[] f) {
        formats = f;
    }

    /**
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File pathname) {
        for (int i = 0; i < formats.length; i++) {
            Format format = formats[i];

            if (format.accepts(pathname)) {
                return true;
            }
        }

        return false;
    }
}
