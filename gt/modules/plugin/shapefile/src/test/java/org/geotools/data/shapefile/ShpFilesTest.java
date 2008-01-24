package org.geotools.data.shapefile;

import static org.geotools.data.shapefile.ShpFileType.DBF;
import static org.geotools.data.shapefile.ShpFileType.PRJ;
import static org.geotools.data.shapefile.ShpFileType.SHP;
import static org.geotools.data.shapefile.ShpFileType.SHX;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.TestCase;

public class ShpFilesTest extends TestCase {

    public static Map<ShpFileType, File> createFiles(String string,
            ShpFileType[] values) throws IOException {
        Map<ShpFileType, File> files = new HashMap<ShpFileType, File>();

        File baseFile = File.createTempFile(string,
                values[0].extensionWithPeriod);
        baseFile.createNewFile();
        baseFile.deleteOnExit();

        files.put(values[0], baseFile);

        String baseFileName = values[0].toBase(baseFile);

        for (int i = 1; i < values.length; i++) {
            ShpFileType type = values[i];
            File file = new File(baseFileName + type.extensionWithPeriod);
            file.createNewFile();
            file.deleteOnExit();
            files.put(type, file);
        }

        return files;
    }

    public void testShapefileFilesAll() throws Exception {
        Map<ShpFileType, File> expected = createFiles("testShapefileFilesAll",
                ShpFileType.values());

        File file = expected.values().iterator().next();
        ShpFiles shapefiles = new ShpFiles(file);

        assertEqualMaps(expected, shapefiles.getFileNames());
    }

    public void testURLStringConstructor() throws Exception {
        Map<ShpFileType, File> expected = createFiles(
                "testURLStringConstructor", ShpFileType.values());

        File file = expected.values().iterator().next();
        ShpFiles shapefiles = new ShpFiles(file.toURI().toURL()
                .toExternalForm());

        assertEqualMaps(expected, shapefiles.getFileNames());
    }

    public void testFileStringConstructor() throws Exception {
        Map<ShpFileType, File> expected = createFiles(
                "testFileStringConstructor", ShpFileType.values());

        File file = expected.values().iterator().next();
        ShpFiles shapefiles = new ShpFiles(file.getPath());

        assertEqualMaps(expected, shapefiles.getFileNames());
    }

    public void testShapefileFilesSome() throws Exception {
        Map<ShpFileType, File> expected = createFiles("testShapefileFilesSome",
                new ShpFileType[] { SHP, DBF, SHX, PRJ });

        File prj = expected.remove(PRJ);

        ShpFiles shapefiles = new ShpFiles(prj);

        assertEqualMaps(expected, shapefiles.getFileNames());
    }

    public void testBadFormat() throws Exception {
        try {
            new ShpFiles("SomeName.woo");
            fail("The file is not one of the files types associated with a shapefile therefore the ShapefileFiles class should not be constructable");
        } catch (IllegalArgumentException e) {
            // good
        }
    }

    public void testNonFileURLs() throws IOException {
        Map<ShpFileType, URL> expected = new HashMap<ShpFileType, URL>();
        String base = "http://www.geotools.org/testFile";
        ShpFileType[] types = ShpFileType.values();
        for (ShpFileType type : types) {
            expected.put(type, new URL(base + type.extensionWithPeriod));
        }

        ShpFiles shapefiles = new ShpFiles(expected.get(SHP));

        Map<ShpFileType, String> files = shapefiles.getFileNames();

        Set<Entry<ShpFileType, URL>> expectedEntries = expected.entrySet();
        for (Entry<ShpFileType, URL> entry : expectedEntries) {
            assertEquals(entry.getValue().toExternalForm(), files.get(entry
                    .getKey()));
        }
    }

    public void testFileURLs() throws Exception {
        Map<ShpFileType, File> expected = createFiles("testShapefileFilesAll",
                ShpFileType.values());

        File file = expected.values().iterator().next();
        ShpFiles shapefiles = new ShpFiles(file.toURI().toURL());

        assertEqualMaps(expected, shapefiles.getFileNames());
    }

    private void assertEqualMaps(Map<ShpFileType, File> expected,
            Map<ShpFileType, String> files) throws MalformedURLException {

        Set<Entry<ShpFileType, File>> expectedEntries = expected.entrySet();
        for (Entry<ShpFileType, File> entry : expectedEntries) {
            assertEquals(entry.getValue().toURI().toURL().toExternalForm(),
                    files.get(entry.getKey()));
        }
    }

}
