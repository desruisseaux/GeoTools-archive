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
package org.geotools.gce.arcgrid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.InputStream;

import java.nio.channels.ReadableByteChannel;

/**
 *
 * @author  Ian Schneider
 * @source $URL$
 */
public abstract class TestCaseSupport extends TestCase {
    /** Creates a new instance of TestCaseSupport */
    public TestCaseSupport(String name) {
        super(name);
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
}
