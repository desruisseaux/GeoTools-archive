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
import java.net.URL;

import org.geotools.data.arcgrid.ArcGridFormat;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public class StupidNestedMetadataImpl extends AbstractMetadata implements
        StupidNestedMetadata {

    /** 
     * @see org.geotools.data.arcgrid.test.StupidNestedMetadata#getFileData()
     */
    public FileMetadata getFileData() {
        URL resource = TestFileMetadataImpl.class.getResource("testdata/ArcGrid.asc");
        File f=new File(resource.getFile());
        return new FileMetadataImpl(f, new ArcGridFormat());
    }

    /** 
     * @see org.geotools.metadata.StupidNestedMetadata#getData()
     */
    public String getData() {
        return "Hello this is data";
    }

}
