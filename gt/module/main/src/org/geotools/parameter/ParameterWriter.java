/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.parameter;

// J2SE dependencies
import java.util.Date;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Locale;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.lang.reflect.Array;
import java.io.IOException;
import java.io.FilterWriter;
import java.io.Writer;

// OpenGIS dependencies
import org.opengis.util.GenericName;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.IdentifiedObject;

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.measure.Angle;
import org.geotools.measure.AngleFormat;
import org.geotools.resources.XArray;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Format {@linkplain ParameterDescriptorGroup parameter descriptors} or
 * {@linkplain ParameterValueGroup parameter values} in a tabular format.
 * This writer assumes a monospaced font and an encoding capable to provide
 * drawing box characters (e.g. unicode).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ParameterWriter extends FilterWriter {
    /**
     * The locale.
     */
    private Locale locale = Locale.getDefault();

    /**
     * The formatter to use for numbers. Will be created only when first needed.
     */
    private transient NumberFormat numberFormat;

    /**
     * The formatter to use for dates. Will be created only when first needed.
     */
    private transient DateFormat dateFormat;

    /**
     * The formatter to use for angles. Will be created only when first needed.
     */
    private transient AngleFormat angleFormat;

    /**
     * Creates a new formatter writting parameters to the
     * {@linkplain System#out default output stream}.
     */
    public ParameterWriter() {
        this(Arguments.getWriter(System.out));
    }

    /**
     * Creates a new formatter writting parameters to the specified output stream.
     */
    public ParameterWriter(final Writer out) {
        super(out);
    }

    /**
     * Print the elements of an operation to the
     * {@linkplain System#out default output stream}.
     * This is a convenience method for <code>new
     * ParameterWriter().{@linkplain #format(OperationMethod) format}(operation)</code>.
     */
    public static void print(final OperationMethod operation) {
        final ParameterWriter writer = new ParameterWriter();
        try {
            writer.format(operation);
        } catch (IOException exception) {
            // Should never happen, since we are writting to System.out.
            throw new AssertionError(exception);
        }
    }

    /**
     * Print the elements of a descriptor group to the
     * {@linkplain System#out default output stream}.
     * This is a convenience method for <code>new
     * ParameterWriter().{@linkplain #format(ParameterDescriptorGroup)
     * format}(descriptor)</code>.
     */
    public static void print(final ParameterDescriptorGroup descriptor) {
        final ParameterWriter writer = new ParameterWriter();
        try {
            writer.format(descriptor);
        } catch (IOException exception) {
            // Should never happen, since we are writting to System.out.
            throw new AssertionError(exception);
        }
    }

    /**
     * Print the elements of a parameter group to the
     * {@linkplain System#out default output stream}.
     * This is a convenience method for <code>new
     * ParameterWriter().{@linkplain #format(ParameterValueGroup)
     * format}(values)</code>.
     */
    public static void print(final ParameterValueGroup values) {
        final ParameterWriter writer = new ParameterWriter();
        try {
            writer.format(values);
        } catch (IOException exception) {
            // Should never happen, since we are writting to System.out.
            throw new AssertionError(exception);
        }
    }

    /**
     * Print the elements of an operation to the output stream.
     *
     * @param  operation The operation method to format.
     * @throws IOException if an error occured will writing to the stream.
     */
    public void format(final OperationMethod operation) throws IOException {
        synchronized (lock) {
            format(operation.getName().getCode(), operation.getParameters(), null);
        }
    }

    /**
     * Print the elements of a descriptor group to the output stream.
     *
     * @param  descriptor The descriptor group to format.
     * @throws IOException if an error occured will writing to the stream.
     */
    public void format(final ParameterDescriptorGroup descriptor) throws IOException {
        synchronized (lock) {
            format(descriptor.getName().getCode(), descriptor, null);
        }
    }

    /**
     * Print the elements of a parameter group to the output stream.
     *
     * @param  values The parameter group to format.
     * @throws IOException if an error occured will writing to the stream.
     */
    public void format(final ParameterValueGroup values) throws IOException {
        final ParameterDescriptorGroup descriptor =
             (ParameterDescriptorGroup) values.getDescriptor();
        synchronized (lock) {
            // TODO: remove cast when we will be allowe to use J2SE 1.5.
            format(descriptor.getName().getCode(), descriptor, values);
        }
    }

    /**
     * Implementation of public <code>format</code> methods.
     *
     * @param  name The group name, usually <code>descriptor.getCode().getName()</code>.
     * @param  descriptor The parameter descriptor. Should be equals to
     *         <code>values.getDescriptor()</code> if <code>values</code> is non null.
     * @param  values The parameter values, or <code>null</code> if none.
     * @throws IOException if an error occured will writing to the stream.
     */
    private void format(final String                   name,
                        final ParameterDescriptorGroup group,
                        final ParameterValueGroup      values)
            throws IOException
    {
        /*
         * Write the operation name (including aliases) before the table.
         */
        final String lineSeparator = System.getProperty("line.separator", "\n");
        out.write(' ');
        out.write(name);
        GenericName[] alias = group.getAlias();
        if (alias != null) {
            final int margin = name.length() + 3;
            int length = margin;
            boolean hasWrote = false;
            final Identifier identifier = group.getName();
            for (int i=0; i<alias.length; i++) {
                if (!identifier.equals(alias[i])) {
                    out.write(hasWrote ? "\", " : " (alias ");
                    String aliasName = alias[i].toInternationalString().toString(locale);
                    if ((length += aliasName.length()) >= 100) {
                        out.write(lineSeparator);
                        out.write(Utilities.spaces(margin));
                    }
                    out.write('"');
                    out.write(aliasName);
                    hasWrote = true;
                }
            }
            if (hasWrote) {
                out.write("\")");
            }
        }
        out.write(lineSeparator);
        /*
         * Format the table header (i.e. column names).
         */
        final Resources resources = Resources.getResources(locale);
        final TableWriter table = new TableWriter(out, " \u2502 ");
        table.setMultiLinesCells(true);
        table.writeHorizontalSeparator();
        table.write(resources.getString(ResourceKeys.NAME));
        table.nextColumn();
        table.write(resources.getString(ResourceKeys.CLASS));
        table.nextColumn();
        table.write("Minimum");  // TODO localize
        table.nextColumn();
        table.write("Maximum");  // TODO localize
        table.nextColumn();
        table.write(resources.getString((values==null) ? ResourceKeys.DEFAULT_VALUE
                                                       : ResourceKeys.VALUE));
        table.nextColumn();
        table.write("Units");  // TODO localize
        table.nextLine();
        table.nextLine('\u2550');
        /*
         * Format each element in the parameter group. If values were supplied, we will
         * iterate through the values instead of the descriptor. We do it that way because
         * the descriptor can't know which optional values are included and which one are
         * omitted.
         */
        List deferredGroups = null;
        final Object[] array1 = new Object[1];
        final Collection elements = (values!=null) ? values.values() : group.descriptors();
        for (final Iterator it=elements.iterator(); it.hasNext();) {
            final Object                     element = it.next();
            final GeneralParameterValue      generalValue;
            final GeneralParameterDescriptor generalDescriptor;
            if (values != null) {
                generalValue = (GeneralParameterValue) element;
                generalDescriptor = generalValue.getDescriptor();
            } else {
                generalValue = null;
                generalDescriptor = (GeneralParameterDescriptor) element;
            }
            /*
             * If the current element is a group, we will format it later (after
             * all ordinary elements) in order avoid breaking the table layout.
             */
            if (generalDescriptor instanceof ParameterDescriptorGroup) {
                if (deferredGroups == null) {
                    deferredGroups = new ArrayList();
                }
                deferredGroups.add(element);
                continue;
            }
            /*
             * Format the element name, including all alias (if any).
             * Each alias will be formatted on its own line.
             */
            final Identifier identifier = generalDescriptor.getName();
            table.write(identifier.getCode());
            alias = generalDescriptor.getAlias();
            if (alias != null) {
                for (int i=0; i<alias.length; i++) {
                    final GenericName a = alias[i];
                    if (!identifier.equals(a)) {
                        table.write(lineSeparator);
                        table.write(a.asLocalName().toInternationalString().toString(locale));
                    }
                }
            }
            table.nextColumn();
            /*
             * Format the current element as an ordinary descriptor. If we are iterating
             * over the descriptors rather than values, then the "value" column will be
             * filled with the default value specified in descriptors.
             */
            if (generalDescriptor instanceof ParameterDescriptor) {
                final ParameterDescriptor descriptor = (ParameterDescriptor) generalDescriptor;
                table.write(Utilities.getShortName(descriptor.getValueClass()));
                table.nextColumn();
                table.setAlignment(TableWriter.ALIGN_RIGHT);
                Object value = descriptor.getMinimumValue();
                if (value != null) {
                    table.write(formatValue(value));
                }
                table.nextColumn();
                value = descriptor.getMaximumValue();
                if (value != null) {
                    table.write(formatValue(value));
                }
                table.nextColumn();
                if (generalValue != null) {
                    value = ((ParameterValue) generalValue).getValue();
                } else {
                    value = descriptor.getDefaultValue();
                }
                /*
                 * Wraps the value in an array. Because it may be an array of primitive
                 * type, we can't cast to Object[]. Then, each array's element will be
                 * formatted on its own line.
                 */
                final Object array;
                if (value!=null && value.getClass().isArray()) {
                    array = value;
                } else {
                    array = array1;
                    array1[0] = value;
                }
                final int length = Array.getLength(array);
                for (int i=0; i<length; i++) {
                    value = Array.get(array, i);
                    if (value != null) {
                        if (i != 0) {
                            table.write(lineSeparator);
                        }
                        table.write(formatValue(value));
                    }
                }
                table.nextColumn();
                table.setAlignment(TableWriter.ALIGN_LEFT);
                value = descriptor.getUnit();
                if (value != null) {
                    table.write(value.toString());
                }
            }
            table.writeHorizontalSeparator();
        }
        table.flush();
        /*
         * Now format all groups deferred to the end of this table.
         * Most of the time, there is no such group.
         */
        if (deferredGroups != null) {
            for (final Iterator it=deferredGroups.iterator(); it.hasNext();) {
                final Object element = it.next();
                final ParameterValueGroup value;
                final ParameterDescriptorGroup descriptor;
                if (element instanceof ParameterValueGroup) {
                    value = (ParameterValueGroup) element;
                    descriptor = (ParameterDescriptorGroup) value.getDescriptor();
                    // TODO: remove cast when we will be allowed to use J2SE 1.5.
                } else {
                    value = null;
                    descriptor = (ParameterDescriptorGroup) element;
                }
                out.write(lineSeparator);
                format(name + '/' + descriptor.getName().getCode(), descriptor, value);
            }
        }
    }

    /**
     * Format a summary of a collection of {@linkplain IdentifiedObject identified objects}.
     * The summary contains the identifier name and alias aligned in a table.
     * .
     *
     * @param parameters The collection of parameters to format.
     * @throws IOException if an error occured will writing to the stream.
     */
    public void summary(final Collection parameters) throws IOException {
        /*
         * Prepare the list of alias before any write to the output stream.
         * We need to prepare the list first, because not all identified objects
         * may have generic names with the same scopes in the same order.
         *
         *   titles    -  The column number for each column title.
         *   names     -  The names (including alias) for each line.
         */
        final Map    titles = new LinkedHashMap/*<Object,Integer>*/();
        final List    names = new ArrayList/*<String[]>*/();
        final Locale locale = this.locale; // Protect from changes.
        titles.put(null, new Integer(0)); // Special value for the identifier column.
        for (final Iterator it=parameters.iterator(); it.hasNext();) {
            final IdentifiedObject element = (IdentifiedObject) it.next();
            final GenericName[] aliases = element.getAlias();
            String[] elementNames = new String[titles.size()];
            elementNames[0] = element.getName().getCode();
            if (aliases != null) {
                for (int i=0; i<aliases.length; i++) {
                    final GenericName alias = aliases[i];
                    final GenericName scope = alias.getScope();
                    final GenericName name  = alias.asLocalName();
                    final Object title;
                    if (scope != null) {
                        title = scope.toInternationalString().toString(locale);
                    } else {
                        title = new Integer(i);
                    }
                    Integer position = (Integer) titles.get(title);
                    if (position == null) {
                        position = new Integer(titles.size());
                        titles.put(title, position);
                    }
                    final int index = position.intValue();
                    if (index >= elementNames.length) {
                        elementNames = (String[]) XArray.resize(elementNames, index+1);
                    }
                    final String oldName = elementNames[index];
                    final String newName = name.toInternationalString().toString(locale);
                    if (oldName==null || oldName.length()>newName.length()) {
                        /*
                         * Keep the shortest string, since it is often a code used
                         * for identification (e.g. EPSG code). It also help to fit
                         * the table in the window's width.
                         */
                        elementNames[index] = newName;
                    }
                }
            }
            names.add(elementNames);
        }
        /*
         * Trim the columns that duplicates the identifier column (#0). This is
         * usually the case of the OGC column (usually #1), since we already use
         * OGC name as the main identifier in most cases.
         */
        final boolean[] hide = new boolean[titles.size()];
trim:   for (int column=hide.length; --column>=1;) {
            for (final Iterator it=names.iterator(); it.hasNext();) {
                final String[] alias = (String[]) it.next();
                if (alias.length > column) {
                    final String name = alias[column];
                    if (name!=null && !name.equals(alias[0])) {
                        // No need to looks at the next lines.
                        // Move to previous column.
                        continue trim;
                    }
                }
            }
            // A column duplicating the identifier column has been found.
            hide[column] = true;
        }
        /*
         * Write the table. The header will contains one column for each alias's
         * scope (or authority) declared in 'titles', in the same order.
         */
        int column = 0;
        synchronized (lock) {
            final TableWriter table = new TableWriter(out, " \u2502 ");
            table.setMultiLinesCells(true);
            table.writeHorizontalSeparator();
            for (final Iterator it=titles.keySet().iterator(); it.hasNext();) {
                final Object element = it.next();
                if (hide[column++]) {
                    continue;
                }
                final String title;
                if (element == null) {
                    title = "Identifier"; // TODO: localize
                } else if (element instanceof String) {
                    title = (String) element;
                } else {
                    title = "Alias " + element; // TODO: localize
                }
                table.write(title);
                table.nextColumn();
            }
            table.writeHorizontalSeparator();
            for (final Iterator it=names.iterator(); it.hasNext();) {
                final String[] aliases = (String[]) it.next();
                for (column=0; column<aliases.length; column++) {
                    if (hide[column]) {
                        continue;
                    }
                    final String alias = aliases[column];
                    if (alias != null) {
                        table.write(alias);
                    }
                    table.nextColumn();
                }
                table.nextLine();
            }
            table.writeHorizontalSeparator();
            table.flush();
        }
    }

    /**
     * Returns the current locale. Newly constructed <code>ParameterWriter</code>
     * use the {@linkplain Locale#getDefault system default}.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Set the locale to use for table formatting.
     */
    public void setLocale(final Locale locale) {
        synchronized (lock) {
            this.locale  = locale;
            numberFormat = null;
            dateFormat   = null;
            angleFormat  = null;
        }
    }

    /**
     * Format the specified value as a string. This method is automatically invoked
     * by <code>format(...)</code> methods. The default implementation format
     * {@link Number}, {@link Date} and {@link Angle} object according the
     * {@linkplain #getLocale current locale}. This method can been overriden if
     * more objects need to be formatted in a special way.
     *
     * @param  value the value to format.
     * @return The value formatted as a string.
     *
     * @todo Some code were commented out. This commented code should appears in
     *       a subclass of <code>ParameterWriter</code> to be provided in the
     *       grid coverage package.
     */
    protected String formatValue(final Object value) {
//        if (KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL.equals(value)) {
//            return "GRADIENT_MASK_SOBEL_HORIZONTAL";
//        }
//        if (KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL.equals(value)) {
//            return "GRADIENT_MASK_SOBEL_VERTICAL";
//        }
//        if (value instanceof GridCoverage) {
//            return ((GridCoverage) value).getName(null);
//        }
//        if (value instanceof Interpolation) {
//            return getInterpolationName((Interpolation) value);
//        }
//        if (value instanceof EnumeratedParameter) {
//            return ((EnumeratedParameter) value).getName();
//        }
//        if (value instanceof Color) {
//            final Color c = (Color) value;
//            return "RGB["+c.getRed()+','+c.getGreen()+','+c.getBlue()+']';
//        }

        if (value instanceof Number) {
            if (numberFormat == null) {
                numberFormat = NumberFormat.getNumberInstance(locale);
            }
            return numberFormat.format(value);
        }
        if (value instanceof Date) {
            if (dateFormat == null) {
                dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            }
            return dateFormat.format(value);
        }
        if (value instanceof Angle) {
            if (angleFormat == null) {
                angleFormat = AngleFormat.getInstance(locale);
            }
            return angleFormat.format(value);
        }
        return String.valueOf(value);
    }
}
