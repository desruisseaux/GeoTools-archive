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

import org.geotools.metadata.Metadata;
import org.opengis.coverage.grid.Format;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public interface FileMetadata extends Metadata {
    /** 
     * @see org.opengis.catalog.MetadataEntity#getName()
     */
    public String getName() ;
    
    /**
     * @return Returns the extension.
     */
    public String getExtension();
    /**
     * @return Returns the format.
     */
    public Format getFormat();
    /**
     * @return Returns the lastModified.
     */
    public long getLastModified();
    /**
     * @return Returns the path.
     */
    public String getPath();
}
