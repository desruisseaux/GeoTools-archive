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
package org.geotools.data.coverage.grid.file.test;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 * @source $URL$
 */
public class StupidNestedMetadataImpl implements
        StupidNestedMetadata {

    /** 
     * @see org.geotools.metadata.StupidNestedMetadata#getData()
     */
    public String getData() {
        return "Hello this is data";
    }

    /* (non-Javadoc)
     * @see org.geotools.metadata.StupidNestedMetadata#getFileData()
     */
    public StupidFileData getFileData() {
        // TODO Auto-generated method stub
        return new StupidFileDataImpl();
    }

}
