/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.coverage.grid.file;

import java.io.File;
import java.io.FileFilter;

import org.opengis.coverage.grid.Format;
import org.geotools.data.coverage.grid.AbstractGridFormat;


/**
 * DOCUMENT ME!
 *
*  @author Jesse Eichar
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini (simboss)</a>
 * @author $author$ (Last Modified)
 * @source $URL$
 * @version $Revision: 1.9 $
 */
public class FormatFileFilter implements FileFilter {
    Format[] formats;
    boolean recursive=false;

    /**
     * Creates a new FormatFileFilter object.
     *
     * @param f DOCUMENT ME!
     */
    public FormatFileFilter(Format[] f, boolean recursive) {
        formats = f;
        this.recursive= recursive;
    }

    /**
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File pathname) {
		final int length = formats.length;
		Format format;
		for (int i = 0; i < length; i++) {
			format = formats[i];

			if (((AbstractGridFormat) format).accepts(pathname)
					|| (pathname.isDirectory() && this.recursive)) {
				return true;

			}
		}

		return false;
    }
}
