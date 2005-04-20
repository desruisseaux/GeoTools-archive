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
/*
 * TestCaseSupport.java
 *
 * Created on April 30, 2003, 12:16 PM
 */
package org.geotools.data.gtopo30;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.zip.*;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 * @author Simone Giannecchini
 */
public abstract class TestCaseSupport extends TestCase {
    /**
     * Creates a new instance of TestCaseSupport
     *
     * @param name DOCUMENT ME!
     */
    public TestCaseSupport(String name) {
        super(name);
    }

    protected File getFile(String name) {
        java.net.URL base = getClass().getResource("testData/");

        try {
            return new File(URLDecoder.decode(base.getPath(), "UTF-8"), name);
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new RuntimeException("Unable to decode file path ", uee);
        }
    }

    protected URL getTestResource(String name) {
        URL r = TestCaseSupport.class.getResource("testData/" + name);

        if (r == null) {
            throw new RuntimeException("Could not locate resource : " + name);
        }

        return r;
    }

    protected InputStream getTestResourceAsStream(String name) {
        InputStream in = TestCaseSupport.class.getResourceAsStream("testData/"
                + name);

        if (in == null) {
            throw new RuntimeException("Could not locate resource : " + name);
        }

        return in;
    }

    protected ReadableByteChannel getTestResourceChannel(String name) {
        return java.nio.channels.Channels.newChannel(getTestResourceAsStream(
                name));
    }

    public static Test suite(Class c) {
        return new TestSuite(c);
    }

    /**
     * Unzip a file.
     *
     * @param name
     * @param outPath output path for the files
     *
     * @throws Exception
     */
    protected void unzipFile(String name, String outPath)
        throws Exception {
        if (name != null) {
            Unzip zip = new Unzip();
            zip.unzipFile(name, outPath);
        }
    }

    //small utiliy to unzip files
    class Unzip {
        public final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
            byte[] buffer = new byte[4096];
            int len;

            while ((len = in.read(buffer)) >= 0)
                out.write(buffer, 0, len);

            in.close();
            out.close();
        }

        public final void unzipFile(String file, String outPath)
            throws Exception {
            Enumeration entries;
            ZipFile zipFile = null;

            try {
                zipFile = new ZipFile(file);

                entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();

                    if (entry.isDirectory()) {
                        // Assume directories are stored parents first then children.
                        // This is not robust!!!
                        (new File(outPath + entry.getName())).mkdir();

                        continue;
                    }

                    //files
                    copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(
                            new FileOutputStream(outPath + entry.getName())));
                }

                zipFile.close();
            } catch (Exception e) {
                throw e;
            }
        }
    }
}
