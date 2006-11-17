/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import junit.framework.TestCase;
import org.geotools.TestData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


public abstract class FIDTestCase extends TestCase {
	protected final String TYPE_NAME = "archsites";
	
    protected File backshp;
    protected File backdbf;
    protected File backshx;
    protected File backprj;
    protected File backqix;
    String filename;
    protected File fixFile;

    protected void setUp() throws Exception {
        backshp = File.createTempFile("FIDTests", ".shp");

        String name = backshp.getAbsolutePath();
        filename = name.substring(0, name.lastIndexOf('.'));
        backdbf = new File(filename + ".dbf");
        backshx = new File(filename + ".shx");
        backprj = new File(filename + ".prj");
        backqix = new File(filename + ".qix");

        backdbf.deleteOnExit();
        backshx.deleteOnExit();
        backprj.deleteOnExit();
        backshp.deleteOnExit();
        backqix.deleteOnExit();
        fixFile = new File(filename + ".fix");
        fixFile.deleteOnExit();

        copyFiles();
    }

    protected void tearDown() throws Exception {
        if (backdbf.exists()) {
            backdbf.delete();
        }

        if (backprj.exists()) {
            backprj.delete();
        }

        if (backshp.exists()) {
            backshp.delete();
        }

        if (backshx.exists()) {
            backshx.delete();
        }

        if (backqix.exists()) {
            backqix.delete();
        }

        if (fixFile.exists()) {
            fixFile.delete();
        }
    }

    private void copyFiles() throws Exception {
        if (backshp.exists()) {
            backshp.delete();
        }

        if (backshp.exists()) {
            backshp.delete();
        }

        if (backshp.exists()) {
            backshp.delete();
        }

        if (backprj.exists()) {
            backprj.delete();
        }

        if (backqix.exists()) {
            backqix.delete();
        }

        copy(TestData.url("shapes/"+TYPE_NAME+".shp"), backshp);
        copy(TestData.url("shapes/"+TYPE_NAME+".dbf"), backdbf);
        copy(TestData.url("shapes/"+TYPE_NAME+".shx"), backshx);
        copy(TestData.url("shapes/"+TYPE_NAME+".prj"), backprj);
    }

    void copy(URL src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = src.openStream();
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }
    }
}
