/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.coverage.grid.file;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.opengis.coverage.grid.Format;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Used to map between Format objects, FormatFactories and file types
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class FormatManager {
    private static FormatManager manager;
    private HashMap formatMap = new HashMap();

    private FormatManager() {
        Iterator iter = GridFormatFinder.getAvailableFormats();

        while (iter.hasNext()) {
            GridFormatFactorySpi factory = (GridFormatFactorySpi) iter.next();
            formatMap.put(factory.createFormat(), factory);
        }
    }

    /**
     * gets the singleton FormatManager
     *
     * @return the FormatManager
     */
    public static FormatManager getFormatManager() {
        if (manager == null) {
            manager = new FormatManager();
        }

        return manager;
    }

    /**
     * Gets a list of all the known formats
     *
     * @return A list of the known formats
     */
    public Format[] getFormats() {
        Format[] format = new Format[formatMap.size()];

        return (Format[]) formatMap.keySet().toArray(format);
    }

    /**
     * Gets the format of the file f if it is known
     *
     * @param f A GridCoverage file
     *
     * @return A Format that represents the format of the file 
     * 		null if file format is not known.
     */
    public Format getFormat(File f) {
        Set entries = formatMap.entrySet();

        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            Map.Entry element = (Map.Entry) iter.next();
            GridFormatFactorySpi factory = (GridFormatFactorySpi) element
                .getValue();

            try {
                if (factory.accepts(f.toURL())) {
                    return (Format) element.getKey();
                }
            } catch (MalformedURLException e) {
            }
        }

        return null;
    }

    /**
     * Returns a factory that is associated with the format 
     *
     * @param f A format object
     *
     * @return A factory that is associated with the format 
     */
    public GridFormatFactorySpi getFactory(Format f) {
        return (GridFormatFactorySpi) formatMap.get(f);
    }

    /**
     * gets a File filter that will accepts all files that are of known GridCoverage formats
     *
     * @return a File filter that will accepts all files that are of known GridCoverage formats
     */
    public java.io.FileFilter getFileFilter() {
        return new FormatFileFilter();
    }

    private class FormatFileFilter implements java.io.FileFilter {
        /*
         * (non-Javadoc)
         *
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File pathname) {
            Collection factories = formatMap.values();

            for (Iterator iter = factories.iterator(); iter.hasNext();) {
                GridFormatFactorySpi factory = (GridFormatFactorySpi) iter.next();

                try {
                    if (factory.accepts(pathname.toURL())) {
                        return true;
                    }
                } catch (MalformedURLException e) {
                }
            }

            return false;
        }
    }
}
