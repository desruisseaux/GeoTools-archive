/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Provides access to test-data directories associated
 * with junit tests.
 * <p>
 * We have chosen test-data to follow the javadoc "doc-files" convention 
 * of ensuring that data directories don't look anything like normal
 * java packages.
 * </p>
 * <p>
 * Example:
 * <pre><code>
 * class MyClass {
 *   public void example(){
 *     Image testImage =
 *       new ImageIcon( TestData.getResource( this, "test.png" ) ).getImage();
 *     Reader reader = TestData.getReader( this, "script.xml" );
 *   }
 * }
 * </code></pre>
 * Where:
 * <ul>
 * <li>MyClass.java<li>
 * <li>test-data/test.png</li>
 * <li>test-data/script.xml</li>
 * </ul>
 * </ul>
 * </p>
 * <p>
 * By convention you should try and locate testdata near the junit test
 * cases that uses it.
 * </p>
 * @version $Id$
 * @author James McGill
 */
public class TestData {
    /**
     * Provided a BufferedReader for named test data.
     * 
     * @param caller Object associated with named data
     * @param name of test data to load
     */
    public static final BufferedReader getReader(final Class caller, final String name) throws IOException {
    	URL url = caller.getResource("test-data/"+name);
    	if( url == null ) {
            return null; // echo handling of getResource( ... )    		
    	}
    	return new BufferedReader(new InputStreamReader( url.openStream()));
    }	
    /**
     * Provided a BufferedReader for named test data.
     * 
     * @param host Object associated with named data
     * @param name of test data to load
     */
    public static final BufferedReader getReader(final Object host, final String name) throws IOException {
    	URL url = host.getClass().getResource("test-data/"+name);
    	if( url == null ) {
            return null; // echo handling of getResource( ... )    		
    	}
    	return new BufferedReader(new InputStreamReader( url.openStream()));
    }

    // REVISIT: Should this be getURL() - or simply url
    // I tend to save getX method for accessors.
    /**
     * Locate named test-data resource for caller.
     *
     * @param caller Class used to locate test-data
     * @param name name of test-data 
     * @return URL or null of named test-data could not be found
     */
    public static final URL getResource(final Class caller, final String name) throws IOException {
    	if( name == null ){
    		return caller.getResource("test-data");
    	}
    	else {
    		return caller.getResource("test-data/"+name);
    	}
    }

    // REVISIT: Should this be getURL() - or simply url
    // I tend to save getX method for accessors.
    /** 
     * Locate named test-data resource for caller.
     * @param caller Object used to locate test-data
     * @param name name of test-data
     * 
     * @return URL or null of named test-data could not be found 
     */
    public static final URL getResource(final Object caller, final String name) throws IOException {
    	if( name == null ){
    		return caller.getClass().getResource("test-data");
    	}
    	else {
    		return caller.getClass().getResource("test-data/"+name);
    	}
    }
    /**
     * Access to getResource( caller, path ) as a File.
     * <p>
     * You can access the test-data directory with:
     * <pre><code>
     * TestData.file( this, null )
     * </code></pre>
     * </p>
     * @param caller Calling object used to locate test-data
     * @param path Path to file in testdata
     * @return File from test-data
     * @throws IOException
     */
    public static final File file( final Object caller, final String path ) throws IOException {
    	URL url = getResource( caller, path );
    	// Based SVGTest
    	File file = new File(java.net.URLDecoder.decode( url.getFile(),"UTF-8"));
    	if( !file.exists() ) {
    		throw new FileNotFoundException("Could not locate test-data: "+path );    		
    	}
    	return file;    	
    }
    public static final File temp( final Object caller, final String name ) throws IOException{
    	File testData = file( caller, null );
    	int split = name.lastIndexOf(".");
    	String prefix = split == -1 ? name : name.substring(0,split);
    	String suffix = split == -1 ? null : name.substring(split+1);
    	File tmp = File.createTempFile( prefix, suffix, testData );
    	tmp.deleteOnExit();    	
    	return tmp;
    }
}
