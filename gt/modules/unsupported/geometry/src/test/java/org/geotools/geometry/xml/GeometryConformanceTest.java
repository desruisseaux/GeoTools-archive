/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.geometry.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.test.TestData;
import org.xml.sax.InputSource;

/**
 * This TestSuite picks up each JTS test and applies it to the provided
 * Geometry*Factory.
 */
public class GeometryConformanceTest extends TestSuite {

    public static Test suite() {
        GeometryTestParser parser = new GeometryTestParser();

        GeometryConformanceTest suite = new GeometryConformanceTest();

        File dir;
        try {
            dir = TestData.file(GeometryConformanceTest.class, "LineTests.xml")
                    .getParentFile();

            File tests[] = dir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.toString().endsWith(".xml");
                }
            });
            for (int i = 0; i < tests.length; i++) {
                File testFile = tests[i];
                InputStream inputStream = testFile.toURL().openStream();
                try {
                    InputSource inputSource = new InputSource(inputStream);
                    GeometryTestContainer container = parser
                            .parseTestDefinition(inputSource);
                    
                    container.addToTestSuite( testFile.getName(), suite );
                }
                catch( Exception eek){
                    //eek.printStackTrace();
                } finally {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return suite;
    }
}
