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
package org.geotools.data.shapefile;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileReader;

/**
 * 
 * Allows access the the ShapefileReaders.
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class ShapefileUtil {
	public static ShapefileReader getShpReader(ShapefileDataStore ds) throws IOException{
		return new ShapefileReader(ds.getReadChannel(ds.shpURL));
	}
	public static DbaseFileReader getDBFReader(ShapefileDataStore ds) throws IOException{
		return new DbaseFileReader(ds.getReadChannel(ds.dbfURL));
	}
	
	public static ReadableByteChannel getShpReadChannel(ShapefileDataStore ds) throws IOException{
		return ds.getReadChannel(ds.shpURL);
	}
}
