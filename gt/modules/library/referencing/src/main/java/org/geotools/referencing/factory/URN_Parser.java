/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Geomatys
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
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Iterator;
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.Factory;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.metadata.iso.citation.CitationImpl;


/**
 * Split a URN into its {@link #type} and {@link #version} parts for {@link URN_AuthorityFactory}.
 * This class must be immutable in order to avoid the need for synchronization in the authority
 * factory.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class URN_Parser {
    /**
     * The parts separator in the URN.
     */
    private static final char SEPARATOR = ':';

    /**
     * The parsed code as full URN.
     */
    public final String urn;

    /**
     * The type part of the URN ({@code "crs"}, {@code "cs"}, {@code "datum"}, <cite>etc</cite>).
     */
    public final String type;

    /**
     * The authority part of the URN (typically {@code "EPSG"}).
     */
    public final String authority;

    /**
     * The version part of the URN, or {@code null} if none.
     */
    public final String version;

    /**
     * The code part of the URN.
     */
    public final String code;

    /**
     * Parses the specified URN.
     *
     * @param urnBases
     *          The begining parts of the URN, typically {@code "urn:ogc:def:"} and
     *          {@code "urn:x-ogc:def:"}. All elements in the array are treated as synonymous.
     *          Those parts are up to, but do not include, de type part ({@code "crs"},
     *          {@code "cs"}, {@code "datum"}, <cite>etc.</cite>). They must include a trailing
     *          separator ({@code :}).
     * @param urn
     *          The URN to parse.
     * @throws NoSuchIdentifierException if the URN syntax is invalid.
     *
     * @todo Implementation should be replaced by some mechanism using {@code GenericName}
     *       (at least the call to {@code String.regionMatches}) otherwise this method will
     *       fails if there is spaces around the separator.
     */
    public URN_Parser(final String[] urnBases, final String urn) throws NoSuchIdentifierException {
        this.urn = urn;
        final String code = urn.trim();
        String type = urn; // To be really assigned later.
        for (int i=0; i<urnBases.length; i++) {
            final String urnBase = urnBases[i];
            final int typeStart = urnBase.length();
            if (code.regionMatches(true, 0, urnBase, 0, typeStart)) {
                final int typeEnd = code.indexOf(SEPARATOR, typeStart);
                if (typeEnd >= 0) {
                    type = code.substring(typeStart, typeEnd).trim();
                    final URN_Type candidate = URN_Type.get(type);
                    if (candidate != null) {
                        final int nameEnd = code.indexOf(SEPARATOR, typeEnd + 1);
                        if (nameEnd >= 0) {
                            final int lastEnd = code.lastIndexOf(SEPARATOR);
                            this.version   = (lastEnd <= nameEnd) ? null
                                           : code.substring(nameEnd + 1, lastEnd).trim();
                            this.authority = code.substring(typeEnd + 1, nameEnd).trim();
                            this.code      = code.substring(lastEnd + 1).trim();
                            this.type      = candidate.name;
                            return;
                        }
                    }
                }
            }
        }
        throw new NoSuchIdentifierException(
                Errors.format(ErrorKeys.ILLEGAL_IDENTIFIER_$1, type), type);
    }

    /**
     * Add the separator character to the specified string, if not already presents.
     */
    static String addSeparator(String urnBase) {
        urnBase = (urnBase!=null) ? urnBase.trim() : "";
        final int length = urnBase.length();
        if (length == 0 || urnBase.charAt(length - 1) != SEPARATOR) {
            urnBase += SEPARATOR;
        }
        return urnBase;
    }

    /**
     * Returns the version of the specified factory, or {@code null} if unknown.
     */
    private static String getVersion(final AuthorityFactory factory) {
        if (factory instanceof Factory) {
            final Object version = ((Factory) factory).getImplementationHints().get(Hints.VERSION);
            if (version instanceof CharSequence) {
                return version.toString();
            }
        }
        return null;
    }

    /**
     * Creates the authority citation for this factory, derived from the supplied factory.
     * This is used by {@link #URN_AuthorityFactory(AuthorityFactory, String)} constructor.
     *
     * @param factory The factory to wrap.
     * @param urnBase The begining part of the URN, typically {@code "URN:OGC:DEF"}.
     */
    static Citation getAuthority(final AuthorityFactory factory, final String urnBase) {
        final StringBuffer buffer = new StringBuffer(addSeparator(urnBase));
        final int                    baseOffset        = buffer.length();
        final String                 version           = getVersion(factory);
        final Citation               sourceCitation    = factory.getAuthority();
        final CitationImpl           targetCitation    = new CitationImpl(sourceCitation);
        final Collection/*<String>*/ sourceIdentifiers = sourceCitation.getIdentifiers();
        final Collection/*<String>*/ targetIdentifiers = targetCitation.getIdentifiers();
        targetIdentifiers.clear();
        for (final Iterator it=sourceIdentifiers.iterator(); it.hasNext();) {
            final String name = (String) it.next();
            for (int i=0; i<URN_Type.MAIN.length; i++) {
                final URN_Type type = URN_Type.MAIN[i];
                if (type.isInstance(factory)) {
                    buffer.setLength(baseOffset);
                    buffer.append(type.name).append(SEPARATOR).append(name);
                    if (version != null) {
                        buffer.append(SEPARATOR).append(version);
                    }
                    targetIdentifiers.add(buffer.toString());
                }
            }
        }
        return (Citation) targetCitation.unmodifiable();
    }

    /**
     * Returns the concatenation of the {@linkplain #authority} and the {@linkplain #code}.
     */
    public String getAuthorityCode() {
        return authority + SEPARATOR + code;
    }

    /**
     * Returns the last part of the URN (starting with the {@linkplain #type})
     * for debugging purpose.
     */
    public String toString() {
        return type + SEPARATOR + authority + SEPARATOR + version + SEPARATOR + code;
    }
}
