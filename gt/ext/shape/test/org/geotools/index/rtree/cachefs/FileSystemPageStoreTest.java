package org.geotools.index.rtree.cachefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import junit.framework.TestCase;

import org.geotools.index.DataDefinition;
import org.geotools.index.TreeException;
import org.geotools.index.rtree.cachefs.FileSystemPageStore;

/**
 * @author Tommaso Nolli
 */
public class FileSystemPageStoreTest extends TestCase {

	/**
	 * Constructor for FileSystemPageStoreTest.
	 * @param arg0
	 */
	public FileSystemPageStoreTest(String arg0) {
		super(arg0);
	}

	/*
	 * Test for void FileSystemPageStore(File)
	 */
	public void testFileSystemPageStoreFile() throws Exception {
        File file = File.createTempFile("geotools2", ".grx");
        try {
            FileSystemPageStore fps = new FileSystemPageStore(file);
            fail("Cannot create a FileSystemPageStore without a " +
                 "DataDefinition");
        } catch (TreeException e) {
            // Ok, the file must exist
        }
    }

	/*
	 * Test for void FileSystemPageStore(File, DataDefinition)
	 */
	public void testFileSystemPageStoreFileDataDefinition()
    throws Exception
    {
        File file = File.createTempFile("geotools2", ".grx");
        DataDefinition dd = new DataDefinition("US-ASCII");
        try {
            FileSystemPageStore fps = new FileSystemPageStore(file, dd);
            fail("Cannot use an empty DataDefinition");
        } catch (TreeException e) {
            // OK
        }
        

        dd.addField(Integer.class);
        FileSystemPageStore fps = new FileSystemPageStore(file, dd);
        fps.close();
	}

	/*
	 * Test for void FileSystemPageStore(File, DataDefinition, int, int, short)
	 */
	public void testFileSystemPageStoreFileDataDefinitionintintshort()
    throws Exception
    {
        File file = File.createTempFile("geotools2", ".grx");
        DataDefinition dd = new DataDefinition("US-ASCII");
        dd.addField(Integer.class);
        FileSystemPageStore fps = null;
        try {
            fps = new FileSystemPageStore(file,
                                          dd,
                                          10,
                                          10,
                                          FileSystemPageStore.SPLIT_LINEAR);
            fail("MinNodeEntries must be <= MaxNodeEntries / 2");
        } catch (TreeException e) {
            // OK
        }
        
        try {
            fps = new FileSystemPageStore(file,
                                          dd,
                                          10,
                                          5,
                                          (short)1000);
            fail("SplitAlgorithm not supported");
        } catch (TreeException e) {
            // OK
        }
        
        fps = new FileSystemPageStore(file,
                                      dd,
                                      50,
                                      25,
                                      FileSystemPageStore.SPLIT_QUADRATIC);
        fps.close();
        
        OutputStream out = new FileOutputStream(file);
        out.write(SecureRandom.getSeed(50));
        out.close();
        
        try {
            fps = new FileSystemPageStore(file,
                                          dd,
                                          10,
                                          5,
                                          FileSystemPageStore.SPLIT_QUADRATIC);
            fail("File must not exist");
        } catch (TreeException e) {
            // OK
        }
        
	}

}
