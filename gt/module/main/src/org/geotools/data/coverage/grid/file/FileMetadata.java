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
package org.geotools.data.coverage.grid.file;

import org.geotools.catalog.MetadataEntity;
import org.geotools.data.coverage.grid.Format;


/**
 * A simple unstandard metadata that describes files
 *
 * @author jeichar
 *
 */
public interface FileMetadata extends MetadataEntity {
    /**
     * Returns the File name
     * @return Returns the File name
     */
    public String getName();

    /**
     * Returns the extension of the file.
     * @return Returns the extension of the file.
     */
    public String getExtension();

    /**
     * Returns the format of the file if it is known;
     * @return Returns the format.
     * 		Null if format is not known
     */
    public Format getFormat();

    /**
     * Returns the date the file was last modified.
     * 
     * @return Returns the date the file was last modified.
     */
    public long getLastModified();

    /**
     * Returns the path of the file.
     * 
     * @return Returns the path of the file.
     */
    public String getPath();
}
