/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
 *
 *    Created on Apr 26, 2004
 */
package org.geotools.data.coverage.grid.stream.test;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;

import junit.framework.TestCase;

import org.geotools.data.coverage.grid.stream.IOExchange;
import org.geotools.resources.TestData;
/**
 * @author jeichar
 * @source $URL$
 */
public class IOExchangeTest extends TestCase{
        final String TEST_FILE = "ArcGrid.asc";
        IOExchange mExchange=null;;
        /**
         * @param name
         */
        public IOExchangeTest(String name) {
                super(name);
        }
        /**
         * Test that getIOExchange() returns an object
         *
         */
        public void testGetIOExchange() {
                mExchange = IOExchange.getIOExchange();
                assertNotNull(mExchange);
        }
        /**
         * Test that various types of source objects return a reader
         *
         */
        public void testGetReader() throws Exception {
                mExchange = IOExchange.getIOExchange();
                Reader reader;

                // Test URL
                URL fileURL = TestData.getResource(this, TEST_FILE);
                reader=mExchange.getReader(fileURL);
                assertNotNull(reader);
                assertTrue(reader.ready());
                int char1=reader.read();

                //Test String
                String fileString=URLDecoder.decode(fileURL.toString(),"UTF-8");
                reader=mExchange.getReader(fileString);
                assertNotNull(reader);
                assertTrue(reader.ready());
                assertEquals(char1,reader.read());

                //Test File
                File file=TestData.file(this, TEST_FILE);
                boolean ex=file.exists();
                reader=mExchange.getReader(file);
                assertNotNull(reader);
                assertTrue(reader.ready());
                assertEquals(char1,reader.read());

                //Test InputStream
                InputStream in=fileURL.openStream();
                reader=mExchange.getReader(in);
                assertNotNull(reader);
                assertTrue(reader.ready());
                assertEquals(char1,reader.read());

                //Test Reader
                reader=mExchange.getReader(mExchange.getReader(fileURL));
                assertNotNull(reader);
                assertTrue(reader.ready());
                assertEquals(char1,reader.read());

        }
}
