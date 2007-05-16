package org.geotools.geometry.xml;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:joel@lggi.com">Joel Skelton</a>
 */
public class RunStoredTest extends TestCase {
    private static final Logger LOG = Logger.getLogger("org.geotools.geometry");

    private static String TEST_DIRECTORY = "src/main/resources/org/geotools/test-data/xml/geometry";
    //TODO: use TestData.copy and acquire files from sample-data module
    
    private FilenameFilter xmlFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".xml");
        }
    };

    /**
     * Load and run all test files.
     * @throws IOException 
     */
    public void testGeometriesFromXML() throws IOException {
        GeometryTestParser parser = new GeometryTestParser();
        File dir = new File(TEST_DIRECTORY);
        if (dir.isDirectory()) {
            for (File testFile : dir.listFiles(xmlFilter)) {
            	LOG.info("Loading test description file:" + testFile);
                FileInputStream inputStream = new FileInputStream(testFile);
                InputSource inputSource = new InputSource(inputStream);
                GeometryTestContainer tests = parser.parseTestDefinition(inputSource);
                assertTrue("Failed test(s) in: " + testFile.getName(), tests.runAllTestCases());
            }
        }
    }
}
