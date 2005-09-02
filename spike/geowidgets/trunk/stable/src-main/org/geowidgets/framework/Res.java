/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework;

import java.text.MessageFormat;
import java.util.*;

/** Messages
 * 
 * @author Matthias Basler
 */
public abstract class Res {
    protected static final String HTML_START = "<html><page>"; //$NON-NLS-1$
    protected static final String HTML_BREAK = " </p>"; //$NON-NLS-1$
    protected static final String HTML_END = "</page></html>"; //$NON-NLS-1$

    /** If a key doesn't exist, a <code>MissingResourceException</code> is thrown. */
    public static final int CONFIG_THROW_EXCEPTIONS = 0;
    /** If a key doesn't exist, the key is returned instead of a localized string. */
    public static final int CONFIG_RETURN_KEY = 1;
    /** If a key doesn't exist, the key is returned instead of a localized string.
     * Additionally the exception is stored in the a public vector "exceptions".
     * A debugging/logging tool could then read out this list and provide a
     * list of missing keys. */
    public static final int CONFIG_STORE_EXEPTIONS = 2;
    /** Public list for exceptions captured during localization. */
    public static List<MissingResourceException> exceptions
            = new Vector<MissingResourceException>();

    // TODO Check if this really works as expected. 
    protected static int exHandling = 0;

    /** @return a constant that describes how missing localization resources
     * are treated. Review the CONFIG_xxx constants for details. */
    public int getExceptionHandling() {
        return this.exHandling;
    }

    /** @param exceptionHandling use one of the provided CONFIG_xxx constants
     * to determine how <code>MissingResourceExceptions</code> shall get handled.*/
    public void setExceptionHandling(int exceptionHandling) {
        this.exHandling = exceptionHandling;
    }

    //Ressources
    /** The default resource bundle in GeoWidgets. All reusable strings and
     * error messages should be registered here. New widgets or widget groups
     * should add their own resource bundle to this list. */
    public static final ResourceBundle WIDGETS = ResourceBundle
            .getBundle("org.geowidgets.framework.Widgets");//$NON-NLS-1$
    /** The resource bundle for the unit dropdown widget. */
    public static final ResourceBundle UNITS = ResourceBundle
            .getBundle("org.geowidgets.units.model.Units");//$NON-NLS-1$
    /** The resource bundle for the CRS assembly widgets. */
    public static final ResourceBundle CRS = ResourceBundle
            .getBundle("org.geowidgets.crs.model.CRS");//$NON-NLS-1$

    /** @return a localized string from the WIDGETS resource bundle.
     * @param key the key for the localized String.
     * @throws MissingResourceException, only if
     * getExceptionHandling() = CONFIG_THROW_EXCEPTIONS. Otherwise
     * returns the key as result. */
    public static String get(String key) {
        try {
            return WIDGETS.getString(key);
        } catch (MissingResourceException e) {
            if (exHandling == CONFIG_THROW_EXCEPTIONS) throw e;
            System.err.println(e.getLocalizedMessage());
            return key;
        }
    }

    /** @return a localized string from a specified resource bundle.
     * @param rb the Resource bundle to use. You may use one of the constants
     * provided in this convenience class.
     * @param key the key for the localized String.
     * @throws MissingResourceException, only if
     * getExceptionHandling() = CONFIG_THROW_EXCEPTIONS. Otherwise
     * returns the key as result. */
    public static String get(ResourceBundle rb, String key) {
        try {
            return rb.getString(key);
        } catch (MissingResourceException e) {
            if (exHandling == CONFIG_THROW_EXCEPTIONS) throw e;
            System.err.println(e.getLocalizedMessage());
            return key;
        }
    }

    /** @return a localized string from a specified resource bundle.
     * @param rb the Resource bundle to use. You may use one of the constants
     * provided in this convenience class.
     * @param key the key for the localized String.
     * The key can contain placeholders, in which the argument is inserted.
     * @param arg0 the first placeholder: "{0}"
     * @throws MissingResourceException, only if
     * getExceptionHandling() = CONFIG_THROW_EXCEPTIONS. Otherwise
     * returns the key as result. */
    public static String get(ResourceBundle rb, String key, String arg0) {
        try {
            return MessageFormat.format(rb.getString(key), new Object[] { arg0 });
        } catch (MissingResourceException e) {
            if (exHandling == CONFIG_THROW_EXCEPTIONS) throw e;
            System.err.println(e.getLocalizedMessage());
            return key;
        }
    }

    /** @return a localized string from a specified resource bundle.
     * @param rb the Resource bundle to use. You may use one of the constants
     * provided in this convenience class.
     * @param key the key for the localized String.
     * The key can contain placeholders, in which the argument is inserted.
     * @param arg0 the first placeholder: "{0}"
     * @param arg1 the second placeholder: "{1}"
     * @throws MissingResourceException, only if
     * getExceptionHandling() = CONFIG_THROW_EXCEPTIONS. Otherwise
     * returns the key as result. */
    public static String get(ResourceBundle rb, String key, String arg0, String arg1) {
        try {
            return MessageFormat.format(rb.getString(key), new Object[] { arg0, arg1 });
        } catch (MissingResourceException e) {
            if (exHandling == CONFIG_THROW_EXCEPTIONS) throw e;
            System.err.println(e.getLocalizedMessage());
            return key;
        }
    }

    /** @return a localized string from a specified resource bundle.
     * @param rb the Resource bundle to use. You may use one of the constants
     * provided in this convenience class.
     * @param key the key for the localized String.
     * The key can contain line breaks (<p>). The page and body tags
     * are added automatically within this function.
     * @throws MissingResourceException, only if
     * getExceptionHandling() = CONFIG_THROW_EXCEPTIONS. Otherwise
     * returns the key as result. */
    public static String getHTML(ResourceBundle rb, String key) {
        try {
            return HTML_START + rb.getString(key) + HTML_END;
        } catch (MissingResourceException e) {
            if (exHandling == CONFIG_THROW_EXCEPTIONS) throw e;
            System.err.println(e.getLocalizedMessage());
            return key;
        }
    }
}