/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.logging;

import java.util.logging.*;

import org.geotools.util.MonolineFormatter;

/** LoggerFactory is the central point to get the Logger from. This logger
 * is initialized here and a console handler is added. Subclasses may override
 * the <code>initLogger</code> method to customize logging behaviour. Afterwards
 * the subclass must be registered as the default <code>LoggerFactory</code>
 * at the <code>GWFactoryFinder</code>.
 *  
 * @author Matthias Basler
 */
public class LoggerFactory {
    /** The default <code>LoggerFactory</code>.*/
    protected static LoggerFactory me = new LoggerFactory();

    /** @return The default logger factory for the GeoWidgets. */
    public static LoggerFactory getDefault() {
        return me;
    }

    /** Creates the instance of this singleton class. */
    protected LoggerFactory() {
        initLogger();
    }

    //------------------------------------------------------------------------------
    //Get logger
    /** The path to the resource bundle to be used for translating log messages.*/
    protected String bundle = "org.geowidgets.framework.widgets"; //$NON-NLS-1$
    /** The default logging level. Messages below this level are not shown. */
    Level level = Level.FINE;

    /** @return The default logger for the GeoWidgets. It uses the <code>Widgets</code>
     *  resource bundle to localize messages. */
    public Logger getLogger() {
        return Logger.getLogger("org.geowidgets", bundle); //$NON-NLS-1$
    }

    /** Called on construction time. Gives the possibility to set the Logger's
     * level and handlers. This default implementation sets the level to FINE
     * and registers a Console handler with a MonolineFormatter.
     */
    protected void initLogger() {
        Logger logger = Logger.getLogger("org.geowidgets", bundle); //$NON-NLS-1$
        logger.setLevel(level);

        ConsoleHandler cHandler = new ConsoleHandler();
        cHandler.setLevel(Level.ALL);
        cHandler.setFormatter(new MonolineFormatter());
        logger.addHandler(cHandler);
    }

}
