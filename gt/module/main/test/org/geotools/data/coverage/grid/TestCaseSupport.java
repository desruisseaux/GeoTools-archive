/*
 * TestCaseSupport.java
 *
 * Created on April 30, 2003, 12:16 PM
 */

package org.geotools.data.coverage.grid;

import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author  Ian Schneider
 */
public abstract class TestCaseSupport extends TestCase {

    /** Creates a new instance of TestCaseSupport */
    public TestCaseSupport() {
      super();
    }
    
    /** Creates a new instance of TestCaseSupport */
    public TestCaseSupport(String name) {
      super(name);
    }
  
  protected URL getTestResource(String name) {
    URL r = TestCaseSupport.class.getResource("test-data/" + name);
    if (r == null)
      throw new RuntimeException("Could not locate resource : " + name);
    return r;
  }
  
  protected InputStream getTestResourceAsStream(String name) {
    InputStream in = TestCaseSupport.class.getResourceAsStream("test-data/" + name);
    if (in == null)
      throw new RuntimeException("Could not locate resource : " + name);
    return in;
  }
  
  protected ReadableByteChannel getTestResourceChannel(String name) {
    return java.nio.channels.Channels.newChannel(getTestResourceAsStream(name));
  }
  
  public static Test suite(Class c) {
    return new TestSuite(c);
  }
  
}
