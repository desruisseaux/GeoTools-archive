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
package org.geotools.data.coverage.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.factory.FactoryFinder;
import org.opengis.coverage.grid.Format;

/**
 * Enable programs to find all available grid format implementations.
 * 
 * <p>
 * In order to be located by this finder datasources must provide an
 * implementation of the {@link GridFormatFactorySpi} interface.
 * </p>
 * 
 * <p>
 * In addition to implementing this interface datasouces should have a services
 * file:<br/><code>META-INF/services/org.geotools.data.GridFormatFactorySpi</code>
 * </p>
 * 
 * <p>
 * The file should contain a single line which gives the full name of the
 * implementing class.
 * </p>
 * 
 * <p>
 * Example:<br/><code>org.geotools.data.mytype.MyTypeDataStoreFacotry</code>
 * </p>
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/main/src/org/geotools/data/coverage/grid/GridFormatFinder.java $
 */
public final class GridFormatFinder {
	private GridFormatFinder() {
	}

	/**
	 * Finds all implemtaions of DataStoreFactory which have registered using
	 * the services mechanism, and that have the appropriate libraries on the
	 * classpath.
	 * 
	 * @return An iterator over all discovered datastores which have registered
	 *         factories, and whose available method returns true.
	 */
	public static Set getAvailableFormats() {
		final Set available = new HashSet();
		final Iterator it = FactoryFinder.factories(GridFormatFactorySpi.class);
		GridFormatFactorySpi factory;
		while (it.hasNext()) {
			factory = (GridFormatFactorySpi) it.next();

			if (factory.isAvailable()) {
				available.add(factory);
			}
		}

		return available;
	}

	public static Format[] getFormatArray() {

		GridFormatFactorySpi element;
		Set formats = GridFormatFinder.getAvailableFormats();
		List formatSet = new ArrayList(formats.size());
		for (Iterator iter = formats.iterator(); iter.hasNext();) {
			element = (GridFormatFactorySpi) iter.next();
			formatSet.add(element.createFormat());
		}
		return (Format[]) formatSet.toArray(new Format[formatSet.size()]);
	}

	public static Format findFormat(Object o) {
		Format[] formats = getFormatArray();
		final int length = formats.length;
		for (int i = 0; i < length; i++) {
			Format f = formats[i];
			if (((AbstractGridFormat) f).accepts(o))
				return f;
		}
		return null;
	}
}
