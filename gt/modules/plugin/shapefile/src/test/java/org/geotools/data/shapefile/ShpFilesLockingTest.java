package org.geotools.data.shapefile;

import static org.geotools.data.shapefile.ShpFileType.*;

import java.net.URL;

import junit.framework.TestCase;

import org.geotools.data.shapefile.ShpFiles.State;

public class ShpFilesLockingTest extends TestCase implements FileWriter {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getClass().getClassLoader().setDefaultAssertionStatus(true);
    }

    public void testAcquireRead1() throws Exception {
        ShpFiles shpFiles = new ShpFiles("http://somefile.com/shp.shp");

        URL url = shpFiles.acquireRead(DBF, this);
        assertEquals("http://somefile.com/shp.dbf", url.toExternalForm());
        assertEquals(1, shpFiles.numberOfLocks());
        FileWriter testWriter = new FileWriter() {

            public String id() {
                return "Other";
            }

        };

        // same thread should work
        Result<URL, State> result1 = shpFiles.tryAcquireRead(SHX, testWriter);
        assertEquals("http://somefile.com/shp.shx", result1.value
                .toExternalForm());
        assertEquals(2, shpFiles.numberOfLocks());

        // same thread should work
        Result<URL, State> result2 = shpFiles.tryAcquireRead(DBF, this);
        assertEquals("http://somefile.com/shp.dbf", result2.value
                .toExternalForm());
        assertEquals(3, shpFiles.numberOfLocks());

        shpFiles.unlockRead(result2.value, this);
        shpFiles.unlockRead(result1.value, testWriter);
        shpFiles.unlockRead(url, this);
    }

    public void testUnlockReadAssertion() throws Exception {
        ShpFiles shpFiles = new ShpFiles("http://somefile.com/shp.shp");

        URL url = shpFiles.acquireRead(DBF, this);
        assertEquals("http://somefile.com/shp.dbf", url.toExternalForm());
        assertEquals(1, shpFiles.numberOfLocks());
        FileWriter testWriter = new FileWriter() {

            public String id() {
                return "Other";
            }

        };

        // same thread should work
        Result<URL, State> result1 = shpFiles.tryAcquireRead(SHX, testWriter);
        assertEquals("http://somefile.com/shp.shx", result1.value
                .toExternalForm());
        assertEquals(2, shpFiles.numberOfLocks());

        try {
            shpFiles.unlockRead(result1.value, this);
            throw new RuntimeException(
                    "Unlock should fail because it is in the wrong reader");
        } catch (IllegalArgumentException e) {
            // good
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }

        shpFiles.unlockRead(result1.value, testWriter);
        shpFiles.unlockRead(url, this);
    }

    public void testUnlockWriteAssertion() throws Exception {
        ShpFiles shpFiles = new ShpFiles("http://somefile.com/shp.shp");

        URL url = shpFiles.acquireWrite(DBF, this);
        assertEquals("http://somefile.com/shp.dbf", url.toExternalForm());
        assertEquals(1, shpFiles.numberOfLocks());
        FileWriter testWriter = new FileWriter() {

            public String id() {
                return "Other";
            }

        };

        // same thread should work
        Result<URL, State> result1 = shpFiles.tryAcquireWrite(SHX, testWriter);
        assertEquals("http://somefile.com/shp.shx", result1.value
                .toExternalForm());
        assertEquals(2, shpFiles.numberOfLocks());

        try {
            shpFiles.unlockRead(result1.value, this);
            throw new RuntimeException(
                    "Unlock should fail because it is in the wrong reader");
        } catch (IllegalArgumentException e) {
            // good
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }

    }

    public String id() {
        return getClass().getName();
    }

}
