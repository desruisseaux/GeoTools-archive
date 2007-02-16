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
import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.IOException;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.Datum;
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
        out.println("Usage: java org.geotools.referencing.CRS [options] [codes]");
        out.println("Options:");
        out.println(" -help          : Prints this message.");
        out.println(" -authority=ARG : Uses the specified authority factory (default to all).");
        out.println(" -list          : List all available CRS codes with their description.");
        out.println(" -bursawolfs    : List Bursa-Wolf parameters for the specified CRS.");
        out.println(" -operations    : Prints all available coordinate operations between pairs of CRS.");
        out.println(" -transform     : Prints the preferred math transform between pairs of CRS.");
        out.println();
        out.println("Examples:");
        out.println("  java org.geotools.referencing.CRS EPSG:4326 EPSG:4181");
        out.println("  java org.geotools.referencing.CRS -authority=EPSG 4326 4181");
    }

    /**
     * Prints the backing store description, if any.
     */
    private void description(final PrintWriter out) throws FactoryException {
        if (factory instanceof AbstractAuthorityFactory) {
            String description = ((AbstractAuthorityFactory) factory).getBackingStoreDescription();
            if (description != null) {
                out.println(description);
            }
        }
    }

    /**
     * Lists all authority codes.
     */
    private void list(final PrintWriter out) throws FactoryException, IOException {
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
        table.flush();
    }

    /**
     * Prints the operations between every pairs of the specified authority code.
     */
    private void operations(final PrintWriter out, final String[] args) throws FactoryException {
        if (!(factory instanceof CoordinateOperationAuthorityFactory)) {
            return;
        }
        char[] separator = null;
        final CoordinateOperationAuthorityFactory factory =
                (CoordinateOperationAuthorityFactory) this.factory;
        for (int i=0; i<args.length; i++) {
            for (int j=1; j<args.length; j++) {
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
        final String    authority = arguments.getOptionalString("-authority");
        final boolean  bursawolfs = arguments.getFlag          ("-bursawolfs");
        final boolean     printMT = arguments.getFlag          ("-transform");
        final boolean  operations = arguments.getFlag          ("-operations");
        final boolean     listCRS = arguments.getFlag          ("-list");
        final boolean        help = arguments.getFlag          ("-help");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        final PrintWriter out = arguments.out;
        if (help) {
            help(out);
            return;
        }
        final char[] separator = getSeparator();
        /*
         * Constructs and prints each object. In the process, keep all coordinate reference systems.
         * They will be used later for printing math transforms. This is usefull in order to check
         * if the EPSG database provides enough information that Geotools know about for creating
         * the coordinate operation.
         */
        int count = 0;
        final CoordinateReferenceSystem[] crs = new CoordinateReferenceSystem[args.length];
        try {
            Command command = null;
            if (listCRS) {
                command = new Command(authority);
                command.list(out);
                return;
            }
            if (operations) {
                command = new Command(authority);
                command.operations(out, args);
                return;
            }
            try {
                for (int i=0; i<args.length; i++) {
                    if (command == null) {
                        command = new Command(authority);
                        command.description(out);
                    } else {
                        out.println(separator);
                    }
                    final Object object = command.factory.createObject(args[i]);
                    out.println(object);
                    if (object instanceof CoordinateReferenceSystem) {
                        final CoordinateReferenceSystem ref = (CoordinateReferenceSystem) object;
                        crs[count++] = ref;
                        if (bursawolfs) {
                            final Datum datum = CRSUtilities.getDatum(ref);
                            if (datum instanceof DefaultGeodeticDatum) {
                                out.println("  Bursa-Wolf parameters:");
                                final IndentedLineWriter w = new IndentedLineWriter(out);
                                w.setIndentation(4);
                                final BursaWolfParameters[] params =
                                        ((DefaultGeodeticDatum) datum).getBursaWolfParameters();
                                for (int j=0; j<params.length; j++) {
                                    w.write(params[j].toString());
                                    w.write(System.getProperty("line.separator", "\n"));
                                }
                                w.flush();
                            }
                        }
                    }
                }
                /*
                 * If the user asked for math transforms, prints them now. We will try all possible
                 * combinaisons. If an operation is not found (usually because not yet implemented
                 * in Geotools), prints the message on a single line (not the whole stack trace) and
                 * continue. Other kinds of error will stop the process.
                 */
                if (printMT) {
                    final CoordinateOperationFactory opFactory =
                            FactoryFinder.getCoordinateOperationFactory(HINTS);
                    for (int i=0; i<count; i++) {
                        for (int j=i+1; j<count; j++) {
                            out.println(separator);
                            final CoordinateOperation op;
                            try {
                                op = opFactory.createOperation(crs[i], crs[j]);
                            } catch (OperationNotFoundException exception) {
                                out.println(exception.getLocalizedMessage());
                                continue;
                            }
                            out.println(op);
                            out.println("  Transform: ");
                            final IndentedLineWriter w = new IndentedLineWriter(out);
                            w.setIndentation(4);
                            w.write(op.getMathTransform().toString());
                            w.flush();
                        }
                    }
                }
            } finally {
                /*
                 * It is possible to dispose the factory right after CRS creation and before math
                 * transform creation.  However, it is better to dispose after math transforms in
                 * order to avoid opening and closing the database connection twice (the operation
                 * factory too may uses this factory).
                 */
                command.dispose();
            }
        } catch (Exception exception) {
            out.flush();
            exception.printStackTrace(arguments.err);
            return;
        }
        out.flush();
    }
}
