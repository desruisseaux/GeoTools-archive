/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.referencing;

// J2SE direct dependencies
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.Factory;
import org.opengis.referencing.AuthorityFactory;

// Geotools dependencies
import org.geotools.factory.FactoryRegistry;
import org.geotools.io.TableWriter;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Prints a list of factory. This is used for {@link FactoryFinder#listProviders}
 * implementation only.
 *
 * @source $URL$
 * @version $Id$
 * @author Desruisseaux
 */
final class FactoryPrinter implements Comparator {
    /**
     * The printer instance.
     */
    public static final FactoryPrinter DEFAULT = new FactoryPrinter();

    /**
     * Do not allows instantiation of this class except the singleton constant.
     */
    private FactoryPrinter() {
    }

    /**
     * Compares two factories for order. This is used for sorting out the factories
     * before to display them.
     */
    public int compare(final Object object1, final Object object2) {
        final Class/*<Factory>*/ factory1 = (Class) object1;
        final Class/*<Factory>*/ factory2 = (Class) object2;
        final boolean isAuthority1 = AuthorityFactory.class.isAssignableFrom(factory1);
        final boolean isAuthority2 = AuthorityFactory.class.isAssignableFrom(factory2);
        if (isAuthority1 && !isAuthority2) return +1;
        if (isAuthority2 && !isAuthority1) return -1;
        return 0;
    }

    /**
     * List all available factory implementations in a tabular format. For each factory interface,
     * the first implementation listed is the default one. This method provides a way to check the
     * state of a system, usually for debugging purpose.
     *
     * @param  FactoryRegistry Where the factories are registered.
     * @param  out The output stream where to format the list.
     * @param  locale The locale for the list, or {@code null}.
     * @throws IOException if an error occurs while writting to {@code out}.
     */
    public void list(final FactoryRegistry registry, final Writer out, final Locale locale)
            throws IOException
    {
        /*
         * Gets the categories in some sorted order. We put the authority factories last.
         */
        final List categories = new ArrayList();
        for (final Iterator it=registry.getCategories(); it.hasNext();) {
            categories.add(it.next());
        }
        Collections.sort(categories, this);
        /*
         * Prints the table header.
         */
        final Vocabulary resources = Vocabulary.getResources(locale);
        final TableWriter table  = new TableWriter(out, " \u2502 ");
        table.setMultiLinesCells(true);
        table.writeHorizontalSeparator();
        table.write(resources.getString(VocabularyKeys.FACTORY));
        table.nextColumn();
        table.write(resources.getString(VocabularyKeys.AUTHORITY));
        table.nextColumn();
        table.write(resources.getString(VocabularyKeys.IMPLEMENTATIONS));
        table.writeHorizontalSeparator();
        final StringBuffer vendors = new StringBuffer();
        for (final Iterator it=categories.iterator(); it.hasNext();) {
            /*
             * Writes the category name (CRSFactory, DatumFactory, etc.)
             */
            final Class category = (Class) it.next();
            table.write(Utilities.getShortName(category));
            table.nextColumn('.');
            /*
             * Writes the authority, if applicable.
             */
            for (final Iterator providers=registry.getServiceProviders(category); providers.hasNext();) {
                if (vendors.length() != 0) {
                    table  .write ('\n');
                    vendors.append('\n');
                }
                final Factory provider = (Factory)providers.next();
                final Citation vendor = provider.getVendor();
                vendors.append(vendor.getTitle().toString(locale));
                if (provider instanceof AuthorityFactory) {
                    final Citation authority   = ((AuthorityFactory) provider).getAuthority();
                    final Iterator identifiers = authority.getIdentifiers().iterator();
                    final String   identifier  = identifiers.hasNext() ? identifiers.next().toString()
                                               : authority.getTitle().toString(locale);
                    table.write(identifier);
                }
            }
            /*
             * Writes the vendors.
             */
            table.nextColumn();
            table.write(vendors.toString());
            vendors.setLength(0);
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        table.flush();
    }
}
