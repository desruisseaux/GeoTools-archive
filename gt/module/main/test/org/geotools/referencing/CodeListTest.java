/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing;

// J2SE dependencies
import java.io.*;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.lang.reflect.*;
import java.net.URLDecoder;
import java.util.logging.Logger;
import junit.framework.*;

// OpenGIS dependencies
import org.opengis.util.CodeList;
import org.opengis.referencing.cs.AxisDirection;


/**
 * Test all code list in all packages found.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CodeListTest extends TestCase implements FileFilter {
    /**
     * Construct a test case.
     */
    public CodeListTest(String testName) {
        super(testName);
    }
    
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CodeListTest.class);
    }
    
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Test {@link AxisDirection} operations.
     */
    public void testAxisDirection() {
        assertSame("SOUTH",  AxisDirection.NORTH,  AxisDirection.SOUTH .inverse());
        assertSame("NORTH",  AxisDirection.SOUTH,  AxisDirection.NORTH .inverse());
        assertSame("WEST",   AxisDirection.EAST,   AxisDirection.WEST  .inverse());
        assertSame("EAST",   AxisDirection.WEST,   AxisDirection.EAST  .inverse());
        assertSame("DOWN",   AxisDirection.UP,     AxisDirection.DOWN  .inverse());
        assertSame("UP",     AxisDirection.DOWN,   AxisDirection.UP    .inverse());
        assertSame("LEFT",   AxisDirection.RIGHT,  AxisDirection.LEFT  .inverse());
        assertSame("RIGHT",  AxisDirection.LEFT,   AxisDirection.RIGHT .inverse());
        assertSame("BOTTOM", AxisDirection.TOP,    AxisDirection.BOTTOM.inverse());
        assertSame("TOP",    AxisDirection.BOTTOM, AxisDirection.TOP   .inverse());
        assertSame("PAST",   AxisDirection.FUTURE, AxisDirection.PAST  .inverse());
        assertSame("FUTURE", AxisDirection.PAST,   AxisDirection.FUTURE.inverse());
        assertSame("OTHER",  AxisDirection.OTHER,  AxisDirection.OTHER .inverse());
        
        assertSame("SOUTH",  AxisDirection.NORTH,  AxisDirection.SOUTH .absolute());
        assertSame("NORTH",  AxisDirection.NORTH,  AxisDirection.NORTH .absolute());
        assertSame("WEST",   AxisDirection.EAST,   AxisDirection.WEST  .absolute());
        assertSame("EAST",   AxisDirection.EAST,   AxisDirection.EAST  .absolute());
        assertSame("DOWN",   AxisDirection.UP,     AxisDirection.DOWN  .absolute());
        assertSame("UP",     AxisDirection.UP,     AxisDirection.UP    .absolute());
        assertSame("LEFT",   AxisDirection.RIGHT,  AxisDirection.LEFT  .absolute());
        assertSame("RIGHT",  AxisDirection.RIGHT,  AxisDirection.RIGHT .absolute());
        assertSame("BOTTOM", AxisDirection.TOP,    AxisDirection.BOTTOM.absolute());
        assertSame("TOP",    AxisDirection.TOP,    AxisDirection.TOP   .absolute());
        assertSame("PAST",   AxisDirection.FUTURE, AxisDirection.PAST  .absolute());
        assertSame("FUTURE", AxisDirection.FUTURE, AxisDirection.FUTURE.absolute());
        assertSame("OTHER",  AxisDirection.OTHER,  AxisDirection.OTHER .absolute());
    }
    
    /**
     * Test the extensibility of some enums.
     */
    public void testExtensibility() {
        final AxisDirection[] dir = AxisDirection.values();
        final AxisDirection DUMMY = new AxisDirection("DUMMY");
        final AxisDirection[] dim = AxisDirection.values();
        assertEquals("Addition 1", dir.length+1, dim.length);
        assertEquals("Ordinal", dir.length, DUMMY.ordinal());
        assertSame("Last element", DUMMY, dim[dir.length]);
        for (int i=0; i<dir.length; i++) {
            assertSame("Element", dir[i], dim[i]);
        }
        assertTrue(toIgnore.add(DUMMY));
    }
    
    /**
     * Test all code list found in the system.
     */
    public void testCodeList() {
        final File base = defaultRootDirectory();
        scan(base, base);
    }
    
    /**
     * Filter the files to includes on the processing. Only the files with the ".class" extensions
     * will be loaded. Directory must be included too if recursive scanning is wanted.
     */
    public boolean accept(final File pathname) {
        final String name = pathname.getName();
        if (pathname.isDirectory()) {
            if (name.equals("org")) {
                return true;
            }
            if (name.equals("go")) {
                return false;
            }
            return pathname.getPath().indexOf("opengis") >= 0;
        }
        if (name.indexOf("SimpleEnumerationType") >= 0) {
            return false;
        }
        return name.trim().endsWith(".class");
    }
    
    /**
     * Returns the default root directory, or <code>null</code> if not found.
     * The default root directory is the one where the implementation of this
     * class is found, up to the 'org' package.
     */
    private File defaultRootDirectory() {
        final Class c = getClass();
        URL url = c.getClassLoader().getResource( "." );
        
        System.out.println( "here:"+url );
        if (url ==null || !url.getProtocol().trim().equalsIgnoreCase("file")){
        	return null; // bad developer no test not on filesystem
        }
        String path = url.getPath();
        if( path.charAt(0) == '/' && path.charAt(2) == ':' ){
        	path = path.substring(1);
        }
        System.out.println( "path:"+path );        
        try {
        	url = new URL( "file", null, 0, path );
        }
        catch (MalformedURLException ignore ){
        	return null;
        }
        System.out.println( "url:"+url );        
        File file = new File( path );
        System.out.println( "file:"+file );
        if( file.exists() && file.isDirectory() ){
        	return file;
        }
        url = c.getClassLoader().getResource(c.getName().replace('.','/')+".class");
        System.out.println( "class:"+url );
        if (url!=null && url.getProtocol().trim().equalsIgnoreCase("file")) {
            try{
                file = new File(URLDecoder.decode(url.toExternalForm(),"UTF-8"));
                System.out.println( "file:"+file );
                final String[] packages = c.getPackage().getName().split("\\.");
                for (int i=packages.length; --i>=0;) {
                	System.out.println( i+ " package:"+packages[i] );                	
                    file = file.getParentFile();
                    if (file==null || !file.getName().equals(packages[i])) {
                        fail("Wrong directory name: "+file);
                        return null;
                    }
                }
                file = file.getParentFile();
                System.out.println( "file:"+file );                
                return file;
            }
            catch(UnsupportedEncodingException uee){
                return null;
            }
        }
        return null;
    }
    
    /**
     * Scan the directory and all subdirectory for classes implementing {@link CodeList}.
     */
    private void scan(final File directory, final File base) {
        if (directory == null || !directory.exists()
        		|| !directory.isDirectory()) {
            Logger.getLogger("org.geotools").warning("No directory to scan:"+directory);
            return; // Just a warning; do not fails.
        }
        final StringBuffer buffer = new StringBuffer();
        final File[] files = directory.listFiles(this);
        for (int i=0; i<files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                scan(file, base);
                continue;
            }
            buffer.setLength(0);
            String name = file.getName();
            final int ext = name.lastIndexOf('.');
            if (ext >= 0) {
                name = name.substring(0, ext);
            }
            buffer.append(name);
            while ((file=file.getParentFile()) != null) {
                if (file.equals(base)) {
                    break;
                }
                buffer.insert(0, '.');
                buffer.insert(0, file.getName());
            }
            name = buffer.toString();
            try {
                process(Class.forName(name));
            } catch (ClassNotFoundException exception) {
                fail("Class not found: "+name);
            }
        }
    }
    
    /**
     * Process a class. Only code list will be procceded.
     */
    private void process(final Class classe) {
        if (!CodeList.class.isAssignableFrom(classe)) {
            return;
        }
        if (CodeList.class.equals(classe)) {
            return;
        }
        final String name = classe.getName();
        final CodeList[] codes;
        try {
            codes = (CodeList[]) classe.getMethod("values", null).invoke(null,null);
        } catch (NoSuchMethodException exception) {
            fail("No values() method in "+name);
            return;
        } catch (IllegalAccessException exception) {
            fail("values() method is not public in "+name);
            return;
        } catch (InvocationTargetException exception) {
            fail("values() method failed in "+name);
            return;
        } catch (ClassCastException exception) {
            fail("values() method returned wrong type in "+name);
            return;
        }
        assertNotNull("values() returned null", codes);
        for (int i=0; i<codes.length; i++) {
            final CodeList code = codes[i];
            assertNotNull("Null element", code);
            assertEquals("Not equals to itself", code, code);
            assertEquals("Wrong index", i, code.ordinal());
            final CodeList field;
            try {
                field = (CodeList) classe.getField(code.name()).get(null);
            } catch (NoSuchFieldException exception) {
                if (toIgnore.contains(code)) {
                    continue;
                }
                fail("No field "+code+" in "+name);
                return;
            } catch (IllegalAccessException exception) {
                fail("Field "+code+" is not public in "+name);
                return;
            } catch (ClassCastException exception) {
                fail("Field "+code+" has a wrong type in "+name);
                return;
            }
            assertSame("Wrong name", code, field);
            if (toIgnore.contains(code)) {
                fail("Code "+code+" was not expected in a field");
            }
        }
    }
    
    /**
     * List of enums to ignore in the {@link #process} operation.
     * Must be static because JUnit seems to recreate this object
     * for each test.
     */
    private static final Set toIgnore = new HashSet();
}
