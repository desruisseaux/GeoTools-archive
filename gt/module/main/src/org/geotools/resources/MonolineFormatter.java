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
 * A formatter writting log messages on a single line. This formatter is used by
 * Geotools 2 instead of {@link SimpleFormatter}. The main difference is that
 * this formatter use only one line per message instead of two. For example, a
 * message formatted by <code>MonolineFormatter</code> looks like:
 *
 * <blockquote><pre>
 * FINE core - A log message logged with level FINE from the "org.geotools.core" logger.
 * </pre></blockquote>
 *
 * By default, <code>MonolineFormatter</code> display only the level and the
 * message.  Additional fields can be formatted if {@link #setTimeFormat} or
 * {@link #setSourceFormat} methods are invoked with a non-null argument. The
 * format can also be set from the <code>jre/lib/logging.properties</code>
 * file. For example, user can cut and paste the following properties into
 * <code>logging.properties</code>:
 *
 * <blockquote><pre>
 * ############################################################
 * # Properties for the Geotools's MonolineFormatter.
 * # By default, the monoline formatter display only the level
 * # and the message. Additional fields can be specified here:
 * #
 * #   time:  If set, writes the time ellapsed since the initialization.
 * #          The argument specifies the output pattern. For example, the
 * #          pattern HH:mm:ss.SSSS display the hours, minutes, seconds
 * #          and milliseconds.
 * #
 * #  source: If set, writes the source logger or the source class name.
 * #          The argument specifies the type of source to display. Valid
 * #          values are none, logger:short, logger:long, class:short and
 * #          class:long.
 * ############################################################
 * org.geotools.resources.MonolineFormatter.time = HH:mm:ss.SSS
 * org.geotools.resources.MonolineFormatter.source = class:short
 * </pre></blockquote>
 *
 * If the <code>MonolineFormatter</code> is wanted for the whole system
 * (not just the <code>org.geotools</code> packages) with level FINE (for
 * example), then the following properties can be defined as below:
 *
 * <blockquote><pre>
 * java.util.logging.ConsoleHandler.formatter = org.geotools.resources.MonolineFormatter
 * java.util.logging.ConsoleHandler.encoding = Cp850
 * java.util.logging.ConsoleHandler.level = FINE
 * </pre></blockquote>
 *
 * @version $Id: MonolineFormatter.java,v 1.14 2003/07/24 14:11:20 desruisseaux Exp $
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
