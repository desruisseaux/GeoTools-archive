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

import org.geotools.catalog.DefaultMetadataEntity;
import org.geotools.data.coverage.grid.Format;
import java.io.File;


/**
 * A simple implementation of FileMetadata
 *
 * @author jeichar
 *
 */
public class FileMetadataImpl extends DefaultMetadataEntity implements FileMetadata {
    String name;
    String path;
    String extension;
    Format format;
    long lastModified;
    File file;

    /**
     * Create a FileMetadata Instance
     * @param file the file the metadata will describe
     * @param format the format type of the file
     *                 "unkown" is the format is unknown
     *                 Static fields should be used
     */
    public FileMetadataImpl(File file, Format format) {
        assert file.exists();
        path = file.getPath();
        name = file.getName();
        extension = name.substring((name.lastIndexOf('.') + 1));
        lastModified = file.lastModified();
        this.format = format;
        this.file = file;
    }

    /**
     * @see org.opengis.catalog.MetadataEntity#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @return Returns the associated file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return Returns the format.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * @return Returns the lastModified.
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param format The format to set.
     */
    public void setFormat(Format format) {
        this.format = format;
    }
}
