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
package org.geotools.metadata;

import java.io.File;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public class FileMetadataImpl extends AbstractMetadata implements FileMetadata {
    String name;
    String path;
    String extension;
    String format;
    long lastModified;
    File file;
    
    /**
     * Create a FileMetadata Instance
     * @param file the file the metadata will describe
     * @param format the format type of the file
     * 		"unkown" is the format is unknown
     * 		Static fields should be used
     */
    FileMetadataImpl( File file, String format ){
        assert file.exists();
        path=file.getPath();
        name=file.getName();
        extension=name.substring((name.lastIndexOf('.')+1));
        lastModified = file.lastModified();
        this.format=format;
        this.file=file;
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
    public String getFormat() {
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
    public void setFormat(String format) {
        this.format = format;
    }

}
