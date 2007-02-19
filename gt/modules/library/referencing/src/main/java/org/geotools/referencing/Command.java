/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.NumberFormat;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.io.TableWriter;
import org.geotools.io.IndentedLineWriter;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.resources.Arguments;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;

/**
 * Implementation of the {@link CRS#main} method. Exists as a separated class in order
 * to reduce the class loading for applications that don't want to run this main method.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Command {
    /**
     * The hints for the factory to fetch. Null for now, but may be different in a future version.
     */
    private static final Hints HINTS = null;

    /**
     * {@code true} if colors are enabled.
     */
    private static boolean colors = false;

    /**
     * The authority factory.
     */
    private final AuthorityFactory factory;

    /**
     * Creates an instance of the specified authority.
     */
    private Command(final String authority) {
        factory = (authority == null) ? CRS.getAuthorityFactory(false) :
                FactoryFinder.getCRSAuthorityFactory(authority, HINTS);
    }

    /**
     * The separator to put between WKT.
     */
    private static char[] getSeparator() {
        final char[] separator = new char[79];
        Arrays.fill(separator, '\u2500');
        return separator;
    }

    /**
     * Prints usage.
     */
    private static void help(final PrintWriter out) {
        out.println("Display informations about CRS identified by authority codes.");
        out.println("Usage: java org.geotools.referencing.CRS [options] [codes]");
        out.println("Options:");
        out.println(" -authority=ARG : Uses the specified authority factory (default to all).");
        out.println(" -bursawolfs    : Lists Bursa-Wolf parameters for the specified CRS.");
        out.println(" -codes         : Lists all available CRS codes with their description.");
        out.println(" -factories     : Lists all availables CRS authority factories.");
        out.println(" -help          : Prints this message.");
        out.println(" -locale=ARG    : Formats texts in the specified locale.");
        out.println(" -operations    : Prints all available coordinate operations between a pair of CRS.");
        out.println(" -transform     : Prints the preferred math transform between a pair of CRS.");
    }

    /**
     * Prints all objects as WKT. This is the default behavior when no option is specified.
     */
    private void list(final PrintWriter out, final String[] args) throws FactoryException {
        char[] separator = null;
        for (int i=0; i<args.length; i++) {
            if (separator == null) {
                separator = getSeparator();
            } else {
                out.println(separator);
            }
            out.println(factory.createObject(args[i]));
        }
    }

    /**
     * Lists all authority codes.
     */
    private void codes(final PrintWriter out) throws FactoryException {
        final Set codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        final TableWriter table = new TableWriter(out);
        table.writeHorizontalSeparator();
        table.write(Vocabulary.format(VocabularyKeys.CODE));
        table.nextColumn();
        table.write(Vocabulary.format(VocabularyKeys.DESCRIPTION));
        table.writeHorizontalSeparator();
        for (final Iterator it=codes.iterator(); it.hasNext();) {
            final String code = (String) it.next();
            table.write(code);
            table.nextColumn();
            try {
                final InternationalString description = factory.getDescriptionText(code);
                if (description != null) {
                    table.write(description.toString());
                }
            } catch (NoSuchAuthorityCodeException e) {
                table.write(e.getLocalizedMessage());
            }
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        try {
            table.flush();
        } catch (IOException e) {
            // Should never happen, since we are backed by PrintWriter
            throw new AssertionError(e);
        }
    }

    /**
     * Lists all CRS authority factories.
     */
    private static void factories(final PrintWriter out) {
        final Set/*<Citation>*/ done = new HashSet();
        final TableWriter   table = new TableWriter(out, " \u2502 ");
        final TableWriter   notes = new TableWriter(out, " ");
        int noteCount = 0;
        notes.setMultiLinesCells(true);
        table.setMultiLinesCells(true);
        table.writeHorizontalSeparator();
        table.write(Vocabulary.format(VocabularyKeys.AUTHORITY));
        table.nextColumn();
        table.write(Vocabulary.format(VocabularyKeys.DESCRIPTION));
        table.nextColumn();
        table.write(Vocabulary.format(VocabularyKeys.NOTE));
        table.writeHorizontalSeparator();
        for (final Iterator it=FactoryFinder.getCRSAuthorityFactories(null).iterator(); it.hasNext();) {
            AuthorityFactory factory = (AuthorityFactory) it.next();
            final Citation authority = factory.getAuthority();
            final Iterator identifiers = authority.getIdentifiers().iterator();
            if (!identifiers.hasNext()) {
                // No identifier. Scan next authorities.
                continue;
            }
            if (!done.add(authority)) {
                // Already done. Scans next authorities.
                continue;
            }
            table.write((String) identifiers.next());
            table.nextColumn();
            table.write(authority.getTitle().toString().trim());
            if (factory instanceof AbstractAuthorityFactory) {
                String description;
                try {
                    description = ((AbstractAuthorityFactory) factory).getBackingStoreDescription();
                } catch (FactoryException e) {
                    description = e.getLocalizedMessage();
                }
                if (description != null) {
                    final String n = String.valueOf(++noteCount);
                    table.nextColumn();
                    table.write('('); table.write(n); table.write(')');
                    notes.write('('); notes.write(n); notes.write(')');
                    notes.nextColumn();
                    notes.write(description.trim());
                    notes.nextLine();
                }
            }
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        try {
            table.flush();
            notes.flush();
        } catch (IOException e) {
            // Should never happen, since we are backed by PrintWriter.
            throw new AssertionError(e);
        }
    }

    /**
     * Prints the bursa-wolfs parameters for the specified CRS.
     */
    private void bursaWolfs(final PrintWriter out, final String[] args) throws FactoryException {
        final NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        final TableWriter table = new TableWriter(out);
        table.writeHorizontalSeparator();
        final String[] titles = {
            Vocabulary.format(VocabularyKeys.TARGET),
            "dx", "dy", "dz", "ex", "ey", "ez", "ppm"
        };
        for (int i=0; i<titles.length; i++) {
            table.write(titles[i]);
            table.nextColumn();
            table.setAlignment(TableWriter.ALIGN_CENTER);
        }
        table.writeHorizontalSeparator();
        for (int i=0; i<args.length; i++) {
            IdentifiedObject object = factory.createObject(args[i]);
            if (object instanceof CoordinateReferenceSystem) {
                object = CRSUtilities.getDatum((CoordinateReferenceSystem) object);
            }
            if (object instanceof DefaultGeodeticDatum) {
                final BursaWolfParameters[] params =
                        ((DefaultGeodeticDatum) object).getBursaWolfParameters();
                for (int j=0; j<params.length; j++) {
                    final BursaWolfParameters p = params[j];
                    final boolean useColors = colors &&
                            CRS.equalsIgnoreMetadata(DefaultGeodeticDatum.WGS84, p.targetDatum);
                    table.setAlignment(TableWriter.ALIGN_LEFT);
                    table.write(p.targetDatum.getName().getCode());
                    table.nextColumn();
                    table.setAlignment(TableWriter.ALIGN_RIGHT);
                    double v;
                    for (int k=0; k<7; k++) {
                        switch (k) {
                            case 0: v = p.dx;  break;
                            case 1: v = p.dy;  break;
                            case 2: v = p.dz;  break;
                            case 3: v = p.ex;  break;
                            case 4: v = p.ey;  break;
                            case 5: v = p.ez;  break;
                            case 6: v = p.ppm; break;
                            default: throw new AssertionError(k);
                        }
                        table.write(nf.format(v));
                        table.nextColumn();
                    }
                    table.nextLine();
                }
                table.writeHorizontalSeparator();
            }
        }
        try {
            table.flush();
        } catch (IOException e) {
            // Should never happen, since we are backed by PrintWriter
            throw new AssertionError(e);
        }
    }

    /**
     * Prints the operations between every pairs of the specified authority code.
     */
    private void operations(final PrintWriter out, final String[] args) throws FactoryException {
        if (!(factory instanceof CoordinateOperationAuthorityFactory)) {
            return;
        }
        final CoordinateOperationAuthorityFactory factory =
                (CoordinateOperationAuthorityFactory) this.factory;
        char[] separator = null;
        for (int i=0; i<args.length; i++) {
            for (int j=i+1; j<args.length; j++) {
                final Set/*<CoordinateOperation>*/ op;
                op = factory.createFromCoordinateReferenceSystemCodes(args[i], args[j]);
                for (final Iterator it=op.iterator(); it.hasNext();) {
                    final CoordinateOperation operation = (CoordinateOperation) it.next();
                    if (separator == null) {
                        separator = getSeparator();
                    } else {
                        out.println(separator);
                    }
                    out.println(operation);
                }
            }
        }
    }

    /**
     * Prints the math transforms between every pairs of the specified authority code.
     */
    private void transform(final PrintWriter out, final String[] args) throws FactoryException {
        if (!(factory instanceof CRSAuthorityFactory)) {
            return;
        }
        final CRSAuthorityFactory factory = (CRSAuthorityFactory) this.factory;
        final CoordinateOperationFactory opFactory =
                FactoryFinder.getCoordinateOperationFactory(HINTS);
        char[] separator = null;
        for (int i=0; i<args.length; i++) {
            final CoordinateReferenceSystem crs1 = factory.createCoordinateReferenceSystem(args[i]);
            for (int j=i+1; j<args.length; j++) {
                final CoordinateReferenceSystem crs2 = factory.createCoordinateReferenceSystem(args[j]);
                final CoordinateOperation op;
                try {
                    op = opFactory.createOperation(crs1, crs2);
                } catch (OperationNotFoundException exception) {
                    out.println(exception.getLocalizedMessage());
                    continue;
                }
                if (separator == null) {
                    separator = getSeparator();
                } else {
                    out.println(separator);
                }
                out.println(op.getMathTransform());
            }
        }
    }

    /**
     * Dispose the factory.
     */
    private void dispose() throws FactoryException {
        if (factory instanceof AbstractAuthorityFactory) {
            ((AbstractAuthorityFactory) factory).dispose();
        }
    }

    /**
     * Implementation of {@link CRS#main}.
     */
    public static void execute(String[] args) {
        final Arguments arguments = new Arguments(args);
        final PrintWriter out = arguments.out;
        Locale.setDefault(arguments.locale);
        colors = arguments.getFlag("-colors");
        if (arguments.getFlag("-help")) {
            args = arguments.getRemainingArguments(0);
            help(out);
            return;
        }
        if (arguments.getFlag("-factories")) {
            args = arguments.getRemainingArguments(0);
            factories(out);
            return;
        }
        final String authority = arguments.getOptionalString("-authority");
        final Command command = new Command(authority);
        try {
            if (arguments.getFlag("-codes")) {
                args = arguments.getRemainingArguments(0);
                command.codes(out);
            } else if (arguments.getFlag("-bursawolfs")) {
                args = arguments.getRemainingArguments(Integer.MAX_VALUE);
                command.bursaWolfs(out, args);
            } else if (arguments.getFlag("-operations")) {
                args = arguments.getRemainingArguments(2);
                command.operations(out, args);
            } else if (arguments.getFlag("-transform")) {
                args = arguments.getRemainingArguments(2);
                command.transform(out, args);
            } else {
                args = arguments.getRemainingArguments(Integer.MAX_VALUE);
                command.list(out, args);
            }
            out.flush();
            command.dispose();
        } catch (FactoryException exception) {
            out.flush();
            arguments.err.println(exception.getLocalizedMessage());
        } catch (Exception exception) {
            out.flush();
            exception.printStackTrace(arguments.err);
        }
    }
}
