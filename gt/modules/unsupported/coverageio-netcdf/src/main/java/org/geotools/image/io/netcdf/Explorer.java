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
package org.geotools.image.io.netcdf;

// J2SE dependencies
import java.util.List;
import java.util.Iterator;
import java.io.File;
import java.io.Writer;
import java.io.IOException;

// NetCDF dependencies
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.Dimension;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.resources.Arguments;
import org.geotools.math.Statistics;


/**
 * Provides informations about the content of a NetCDF file. Command line options are:
 * <table>
 *   <tr>
 *     <td><code>-variable=</code><var>nom</var></td>
 *     <td>Prints informations about the specified variable.</td>
 *   </tr>
 * </table>
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Explorer {
    /**
     * Prints the content of the specified file into the specified stream. This method do not
     * print variable informations. Use {@link #dump(Variable,Writer)} for information about a
     * specific variable.
     */
    private static void dump(final NetcdfFile file, final Writer out) throws IOException {
        final TableWriter table = new TableWriter(out, " \u2502 ");
        //@SuppressWarnings("unchecked")
        final List/*<Variable>*/ variables = (List/*<Variable>*/) file.getVariables();
        table.nextLine('\u2500');
        table.write("Type\tName\tDimensions\tLengths");
        table.nextLine();
        table.nextLine('\u2500');
        for (final Iterator it=variables.iterator(); it.hasNext();) {
            final Variable v = (Variable) it.next();
            table.write(String.valueOf(v.getDataType()));
            table.nextColumn();
            table.write(v.getName());
            /*
             * The set of dimensions for a variable.
             */
            //@SuppressWarnings("unchecked")
            final List/*<Dimension>*/ dimensions = (List/*<Dimension>*/) v.getDimensions();
            for (int info=0; info<=1; info++) {
                table.nextColumn();
                boolean first = true;
                for (final Iterator it2=dimensions.iterator(); it2.hasNext();) {
                    final Dimension dim = (Dimension) it2.next();
                    final String text;
                    switch (info) {
                        case 0:  text = dim.getName(); break;
                        case 1:  text = String.valueOf(dim.getLength()); break;
                        default: throw new AssertionError(info);
                    }
                    if (first) {
                        first = false;
                    } else {
                        table.write(", ");
                    }
                    table.write(text);
                }
            }
            table.nextLine();
        }
        table.nextLine('\u2500');
        table.flush();
    }

    /**
     * Prints informations about the specified variable into the specified stream.
     */
    private static void dump(final Variable variable, final Writer out) throws IOException {
        final TableWriter table = new TableWriter(out, " \u2502 ");
        table.nextLine('\u2500');
        table.write("Dimension\tLength\tUnlimited");
        table.nextLine();
        table.nextLine('\u2500');
        //@SuppressWarnings("unchecked")
        final List/*<Dimension>*/ dimensions = (List/*<Dimension>*/) variable.getDimensions();
        for (final Iterator it=dimensions.iterator(); it.hasNext();) {
            final Dimension dim = (Dimension) it.next();
            table.write(dim.getName());
            table.nextColumn();
            table.setAlignment(TableWriter.ALIGN_RIGHT);
            table.write(String.valueOf(dim.getLength()));
            table.nextColumn();
            table.setAlignment(TableWriter.ALIGN_LEFT);
            table.write(String.valueOf(dim.isUnlimited()));
            table.nextLine();
        }
        table.nextLine('\u2500');
        table.flush();
        /*
         * Get statistics about the variable.
         */
        final Statistics stats = new Statistics();
        final Array      array = variable.read();
        final DataType   type  = variable.getDataType();
        final double nodata;
        if (type.equals(DataType.SHORT)) {
            nodata = Short.MAX_VALUE;
        } else {
            nodata = Double.NaN;
        }
        for (final IndexIterator it=array.getIndexIterator(); it.hasNext();) {
            double value = it.getDoubleNext();
            if (value == nodata) {
                value = Double.NaN;
            }
            stats.add(value);
        }
        out.write(String.valueOf(stats));
        out.flush();
    }

    /**
     * Executes from the command line.
     */
    public static void main(String[] args) throws IOException {
        final Arguments arguments = new Arguments(args);
        final String variable = arguments.getOptionalString("-variable");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        for (int i=0; i<args.length; i++) {
            final String filename = args[i];
            arguments.out.println(filename);
            final NetcdfFile file = NetcdfFile.open(filename);
            if (variable == null) {
                dump(file, arguments.out);
            } else {
                //@SuppressWarnings("unchecked")
                final List/*<Variable>*/ variables = (List/*<Variable>*/) file.getVariables();
                for (final Iterator it=variables.iterator(); it.hasNext();) {
                    final Variable v = (Variable) it.next();
                    if (v.getName().trim().equalsIgnoreCase(variable)) {
                        dump(v, arguments.out);
                    }
                }
            }
            file.close();
        }
    }
}
