/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.resources;


/**
 * A formatter writting log messages on a single line.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the <code>org.geotools.util</code> package.
 */
public class MonolineFormatter extends org.geotools.util.MonolineFormatter {
    /**
     * Construct a default <code>MonolineFormatter</code>.
     */
    public MonolineFormatter() {
        super();
    }

    /**
     * Construct a <code>MonolineFormatter</code>.
     *
     * @param base   The base logger name. This is used for shortening the logger 
     *               name when formatting message. For example, if the base 
     *               logger name is "org.geotools" and a log record come from 
     *               the "org.geotools.core" logger, it will be formatted as 
     *               "[LEVEL core]" (i.e. the "org.geotools" part is ommited).
     */
    public MonolineFormatter(final String base) {
        super(base);
    }
}
