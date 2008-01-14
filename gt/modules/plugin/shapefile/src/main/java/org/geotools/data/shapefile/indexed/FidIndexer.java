/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.shapefile.indexed;

import static org.geotools.data.shapefile.ShpFileType.*;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.shp.IndexFile;

/**
 * Creates a .fix file (fid index).
 * 
 * @author Jesse
 */
public class FidIndexer {
    static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.data.shapefile");

    /**
     * Generates the FID index file for the shpFile
     */
    public static synchronized void generate(URL shpURL) throws IOException {
        generate(new ShpFiles(shpURL));
    }

    /**
     * Generates the FID index file for the shpFiles
     */
    public static void generate(ShpFiles shpFiles) throws IOException {
        LOGGER.fine("Generating fids for " + shpFiles.get(SHP));
        IndexedFidWriter writer = null;
        IndexFile indexFile = null;

        try {
            indexFile = new IndexFile(shpFiles, false);

            writer = new IndexedFidWriter(shpFiles);

            for (int i = 0, j = indexFile.getRecordCount(); i < j; i++) {
                writer.next();
            }

        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } finally {
                if (indexFile != null) {
                    indexFile.close();
                }
            }
        }
    }

}
