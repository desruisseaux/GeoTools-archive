/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.gui.swing;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;


/**
 * Browse through the registered JAI operations.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.image} package.
 */
public class RegisteredOperationBrowser extends org.geotools.gui.swing.image.RegisteredOperationBrowser {
    /**
     * Constructs a new operation browser for the default {@link JAI} instance.
     */
    public RegisteredOperationBrowser() {
        super();
    }
    
    /**
     * Constructs a new operation browser for the specified operation registry.
     *
     * @param registry The operation registry to use for fetching operations.
     */
    public RegisteredOperationBrowser(final OperationRegistry registry) {
        super(registry);
    }
}
