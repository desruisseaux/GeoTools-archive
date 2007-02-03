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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.util.Arrays;
import java.io.PrintWriter;

// OpenGIS dependencies
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.OperationNotFoundException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.resources.Arguments;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * Implementation of the {@link DefaultFactory#main} methods. Exists as a separated class
 * in order to reduce the class loading for applications (which typically don't want to run
 * this main method).
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Console {
    /**
     * The hints for this factory. Null for now, but may be different in a future version.
     */
    private static final Hints HINTS = null;

    /**
     * Constructs an object from the EPSG database and print its WKT (Well Know Text) to
     * the standard output. This method can be invoked from the command line. For example:
     *
     * <blockquote><pre>
     * java org.geotools.referencing.factory.epsg.DefaultFactory 4181
     * </pre></blockquote>
     *
     * Should print:
     *
     * <blockquote><pre>
     * GEOGCS["Luxembourg 1930", DATUM["Luxembourg 1930", <FONT face="Arial">etc...</FONT>
     * </pre></blockquote>
     *
     * The following optional arguments are supported:
     *
     * <blockquote>
     *   <strong>{@code -encoding} <var>charset</var></strong><br>
     *       Sets the console encoding for this application output. This value has
     *       no impact on the data exchanged with the EPSG database.
     *
     *   <strong>{@code -transform}</strong><br>
     *       Output the math transforms between every pairs of CRS.
     * </blockquote>
     *
     * @param args A list of EPSG code to display.
     *             An arbitrary number of codes can be specified on the command line.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final boolean     printMT = arguments.getFlag("-transform");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        final PrintWriter out = arguments.out;
        final char[] separator = new char[79];
        Arrays.fill(separator, '\u2500');
        /*
         * Constructs and prints each object. In the process, keep all coordinate reference systems.
         * They will be used later for printing math transforms. This is usefull in order to check
         * if the EPSG database provides enough information that Geotools know about for creating
         * the coordinate operation.
         */
        int count = 0;
        final CoordinateReferenceSystem[] crs = new CoordinateReferenceSystem[args.length];
        try {
            AuthorityFactory factory = null;
            try {
                for (int i=0; i<args.length; i++) {
                    if (factory == null) {
                        factory = FactoryFinder.getCRSAuthorityFactory("EPSG", HINTS);
                        if (factory instanceof AbstractAuthorityFactory) {
                            out.println(((AbstractAuthorityFactory) factory).getBackingStoreDescription());
                        }
                    }
                    final Object object = factory.createObject(args[i]);
                    out.println();
                    out.println(separator);
                    out.println();
                    out.println(object);
                    if (object instanceof CoordinateReferenceSystem) {
                        crs[count++] = (CoordinateReferenceSystem) object;
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
                            out.println();
                            final CoordinateOperation op;
                            try {
                                op = opFactory.createOperation(crs[i], crs[j]);
                            } catch (OperationNotFoundException exception) {
                                out.println(exception.getLocalizedMessage());
                                continue;
                            }
                            out.println(op);
                            out.println();
                            out.println(op.getMathTransform());
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
                if (factory instanceof AbstractAuthorityFactory) {
                    ((AbstractAuthorityFactory) factory).dispose();
                }
            }
        } catch (Exception exception) {
            out.flush();
            exception.printStackTrace(arguments.err);
            return;
        }
        out.flush();
    }
}
