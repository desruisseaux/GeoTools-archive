/*
 * TestData.java
 *
 * Created on May 24, 2004, 4:52 PM
 */

package org.geotools.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author  jamesm
 */
public class TestData {
    
    public static final BufferedReader getReader(final Object host, final String name) throws IOException {
        return new BufferedReader(
        new InputStreamReader(
        host.getClass().getResource("test-data/"+name).openStream()));
    }
    
    public static final URL getResource(final Object host, final String name) throws IOException {
       URL base = host.getClass().getResource("test-data/");
       return new URL(base + name);
    }
    
    
}
