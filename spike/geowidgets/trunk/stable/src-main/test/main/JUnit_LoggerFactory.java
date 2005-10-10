package test.main;

import java.util.logging.*;

import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.logging.LoggerFactory;

import junit.framework.TestCase;

public class JUnit_LoggerFactory extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("************************************");
    }
    
    /** Test method for 'LoggerFactory.getLogger()'
     */
    public void testLocalizedLogging() {
        System.out.println("Get the logger and print a localized word.");
        LoggerFactory logFactory = GWFactoryFinder.getLoggerFactory();
        Logger logger = logFactory.getLogger();

        System.out.println("You should see \"Cause\" or a translation of this word.");
        logger.log(Level.INFO, "err.Cause");
    }    
}
