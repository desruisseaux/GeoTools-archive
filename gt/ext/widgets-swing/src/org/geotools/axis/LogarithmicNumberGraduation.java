/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2000, Institut de Recherche pour le D�veloppement
 * (C) 1999, P�ches et Oc�ans Canada
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
package org.geotools.axis;

// Dependencies
import java.util.Locale;
import javax.units.Unit;


/**
 * A graduation using numbers on a logarithmic axis.
 *
 * @since 2.0
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LogarithmicNumberGraduation extends NumberGraduation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8514854171546232887L;

    /**
     * Contructs a new logarithmic graduation with the supplied units.
     */
    public LogarithmicNumberGraduation(final Unit unit) {
        super(unit);
    }

    /**
     * Constructs or reuses an iterator. This method override
     * the default {@link NumberGraduation} implementation.
     */
    NumberIterator getTickIterator(final TickIterator reuse, final Locale locale) {
        if (reuse instanceof LogarithmicNumberIterator) {
            final NumberIterator it = (NumberIterator) reuse;
            it.setLocale(locale);
            return it;
        } else {
            return new LogarithmicNumberIterator(locale);
        }
    }
}
