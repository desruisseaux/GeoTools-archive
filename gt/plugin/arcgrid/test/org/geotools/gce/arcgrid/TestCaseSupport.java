/*
 * TestCaseSupport.java
 *
 * Created on April 30, 2003, 12:16 PM
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
