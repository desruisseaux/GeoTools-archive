package org.geotools.gui.widget;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.geotools.gui.tools.AbstractTool;
//import org.geotools.gui.widget.FrameWidget;
import org.geotools.map.Context;
/**
 * Factory for constructing Tool classes.
 * @version $Id: WidgetFactory.java,v 1.4 2003/03/22 10:33:43 camerons Exp $
 * @author Cameron Shorter
 * @deprecated
 */
public abstract class WidgetFactory {
    /**
     * The logger 
     */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.widget.WidgetFactory");

    private static WidgetFactory factory=null;

    /**
     * Create an instance of the factory.
     * @return An instance of WidgetFactory, or null if WidgetFactory could not
     * be created.
     */
    public static WidgetFactory createFactory(){
        if (factory==null){
            String factoryClass = System.getProperty("WidgetFactoryImpl");
            LOGGER.fine("loaded property = " + factoryClass);
            if(factoryClass != null && factoryClass != ""){
                factory = createFactory(factoryClass); 
            }
            if (factory==null) {
                factory = createFactory(
                    "org.geotools.gui.swing.WidgetFactoryImpl");
            }
        }
        return factory;
    }

    /**
     * Create an instance of the factory.
     * @return An instance of the Factory, or null if the Factory could not
     * be created.
     * @param factoryClass The name of the factory, eg:
     * "org.geotools.gui.swing.WidgetFactoryImpl".
     */
    public static WidgetFactory createFactory(String factoryClass) {
        try{
            return factory = (WidgetFactory)Class.forName(
                    factoryClass).newInstance();
        } catch (ClassNotFoundException e){
            LOGGER.warning("createFactory failed to find implementation "
                    + factoryClass+ " , "+e);
        } catch (InstantiationException e){
            LOGGER.warning("createFactory failed to insantiate implementation "
                    + factoryClass+" , "+e);
        } catch (IllegalAccessException e){
            LOGGER.warning("createFactory failed to access implementation "
                    + factoryClass+" , "+e);
        }
        return null;
    }

    /**
     * Create an instance of MapPane.
     */
    public abstract org.geotools.gui.widget.MapPane createMapPane(
            AbstractTool tool,
            Context context) throws IllegalArgumentException;
    
    /**
     * Create an instance of FrameWidget.
     */
    public abstract FrameWidget createFrameWidget();
}
