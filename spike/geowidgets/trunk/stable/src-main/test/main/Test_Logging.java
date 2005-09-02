package test.main;

import java.util.logging.*;

import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.logging.LoggerFactory;

public class Test_Logging {


    public static void main(String[] args) {
        System.out.println("Test 1: Get the default handlers.");
        LoggerFactory logFactory = GWFactoryFinder.getLoggerFactory();
        Logger logger = logFactory.getLogger();
        Handler[] handlers = logger.getHandlers();
        if (handlers.length > 0){
            for (Handler handler : handlers) System.out.println(handler.toString());
        } else 
            System.out.println("No handlers yet configured.");
        
        System.out.println("Test 2: Configure the logger to print FINE messages. ");
        System.out.println("You should see a message saying \"You just logged to FINE level.\"");
        logger.fine("  You just logged to FINE level.");
    }

}
