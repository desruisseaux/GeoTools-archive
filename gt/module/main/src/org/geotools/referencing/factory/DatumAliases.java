/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005 Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

// OpenGIS dependencies
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.util.GenericName;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.util.LocalName;
import org.geotools.util.ScopedName;
import org.geotools.resources.XArray;


/**
 * A simple class that determines if a datum name is in our list of aliases and
 * returns the aliases (as {@linkplain GenericName generic names}) for a name.
 * The default implementation is backed by the text file "{@code DatumAliasesTable.txt}".
 * The first line in this text file most be the authority names. All other lines are the
 * aliases.
 *
 * @author Rueben Schulz
 * @version $Id$
 */
public class DatumAliases extends AbstractFactory {
    /**
     * The default file for alias table.
     */
    private static final String ALIAS_TABLE = "DatumAliasesTable.txt";

    /**
     * The column separators in the file to parse.
     */
    private static final String SEPARATORS = ",";

    /**
     * The URL of the alias table. This file is read by {@link #load} when first needed.
     */
    private final URL aliasURL;

    /**
     * A map of our datum aliases. Keys are alias names in lower-case, and values are
     * either {@code String[]} or {@code GenericName[]}. In order to reduce the amount
     * of objects created, all values are initially {@code String[]} objects. They are
     * converted to {@code GenericName[]} only when first needed.
     */
    private Map/*<String,CharSequence[]>*/ aliasMap;

    /**
     * The authorities. This is the first line in the alias table.
     * This array is constructed by {@link #load} when first needed.
     */
    private LocalName[] authorities;

    /**
     * The underlying datum factory. If {@code null}, a default factory will be fetch
     * from {@link FactoryFinder} when first needed. A default value can't be set at
     * construction time, since all factories may not be registered at this time.
     */
    private DatumFactory factory;

    /**
     * Constructs a new datum factory with the default backing factory and alias table.
     */
    public DatumAliases() {
        aliasURL = org.geotools.resources.DatumAliases.class.getResource(ALIAS_TABLE);
        if (aliasURL == null) {
            throw new NoSuchElementException(ALIAS_TABLE);
        }
    }

    /**
     * Constructs a new datum factory which delegates this work to the specified factory.
     * The aliases table is read from the specified URL. The fist line in this file most
     * be the authority names. All other names are aliases.
     *
     * @param factory  The factory to use for datum creation.
     * @param aliasURL The url to the alias table.
     */
    public DatumAliases(final DatumFactory factory, final URL aliasURL) {
        this.factory  = factory;
        this.aliasURL = aliasURL;
        ensureNonNull("factory",  factory );
        ensureNonNull("aliasURL", aliasURL);
    }

    /**
     * Returns the first datum factory other than {@code this}.
     *
     * @throws NoSuchElementException if there is no such factory.
     */
    private DatumFactory getDatumFactory() throws NoSuchElementException {
        if (factory == null) {
            DatumFactory candidate;
            final Iterator it=FactoryFinder.getDatumFactories().iterator();
            do candidate = (DatumFactory) it.next();
            while (candidate == this);
            factory = candidate;
        }
        return factory;
    }

    /**
     * Read the next line from the specified input stream, skipping all blank
     * and comment lines. Returns {@code null} on end of stream.
     */
    private static String readLine(final BufferedReader in) throws IOException {
        String line;
        do line = in.readLine();
        while (line!=null && ((line=line.trim()).length()==0 || line.charAt(0)=='#'));
        return line;
    }

    /**
     * Clears all currently loaded alias and read again the
     * {@code DatumAliasesTable.txt} file into {@link #aliasMap}.
     *
     * @throws IOException if the loading failed.
     */
    private void load() throws IOException {
        assert Thread.holdsLock(this);
        final BufferedReader in = new BufferedReader(new InputStreamReader(aliasURL.openStream()));
        aliasMap = new HashMap();
        /*
         * Parses the title line. This line contains authority names as column titles.
         * The authority names will be used as the scope for each identifiers to be
         * created.
         */
        String line = readLine(in);
        if (line != null) {
            final List elements/*<CharSequence>*/ = new ArrayList();
            StringTokenizer st = new StringTokenizer(line, SEPARATORS);
            while (st.hasMoreTokens()) {
                final String name = st.nextToken().trim();
                elements.add(name.length()!=0 ? new LocalName(name) : null);
            }
            authorities = (LocalName[]) elements.toArray(new LocalName[elements.size()]);
            /*
             * Parses all aliases. They are stored as arrays of strings for now, but will be
             * converted to array of generic names by {@link #getAliases} when first needed.
             * If the alias belong to an authority (which should be true in most cases), a
             * scoped name will be created at this time.
             */
            while ((line=readLine(in)) != null) {
                elements.clear();
                st = new StringTokenizer(line, SEPARATORS);
                while (st.hasMoreTokens()) {
                    final String alias = st.nextToken().trim();
                    elements.add(alias.length()!=0 ? alias : null);
                }
                // Trim trailing null values only (we must keep other null values).
                for (int i=elements.size(); --i>=0;) {
                    if (elements.get(i) != null) break;
                    elements.remove(i);
                }
                final String[] names = (String[]) elements.toArray(new String[elements.size()]);
                for (int i=0; i<names.length; i++) {
                    final String name = names[i];
                    final String[] previous = (String[]) aliasMap.put(name.toLowerCase(), names);
                    if (previous!=null && !Arrays.equals(previous, names)) {
                        LOGGER.warning("Inconsistent aliases for datum \""+name+"\".");
                    }
                }
            }
        }
        in.close();
    }
    
    /**
     * Returns the aliases, as a {@code GenericName[]} array, for the given name. This
     * method returns the internal array (not a clone); do not modify the returned array.
     *
     * @param name Datum alias name to lookup.
     * @return An {@code GenericName[]} array of datum aliases for the given name or 
     *         {@code null} if the name is not in our list of aliases.
     */
    private synchronized GenericName[] getAliases(String name) {
        assert Thread.holdsLock(this);
        if (aliasMap == null) try {
            load();
        } catch (IOException exception) {
            // TODO
            return null;
        }
        name = name.trim();
        CharSequence[] aliases = (CharSequence[]) aliasMap.get(name.toLowerCase());
        if (aliases == null) { 
            return null;
        }
        if (aliases instanceof GenericName[]) {
            return (GenericName[]) aliases;
        }
        /*
         * Transforms an array of strings into an array of generic names.
         * The new array replaces the old one of all aliases enumerated in
         * the array (not just the requested one).
         */
        int count = 0;
        GenericName[] names = new GenericName[aliases.length];
        for (int i=0; i<aliases.length; i++) {
            final CharSequence alias = aliases[i];
            if (alias != null) {
                if (count < authorities.length) {
                    final LocalName authority = authorities[count];
                    if (authority != null) {
                        names[count++] = new ScopedName(authority, alias);
                        continue;
                    }
                }
                names[count++] = new LocalName(alias);
            }
        }
        names = (GenericName[]) XArray.resize(names, count);
        for (int i=0; i<names.length; i++) {
            final String alias = names[i].asLocalName().toString();
            final CharSequence[] previous = (CharSequence[]) aliasMap.put(alias.toLowerCase(), names);
            assert Arrays.equals(aliases, previous);
        }
        return names;
    }
}
