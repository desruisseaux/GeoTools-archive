/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// Collections
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Resource compiler. This class is run from the command line at compile time only.
 * {@code ResourceCompiler} scans for {@code .properties} files in the current directory and copies
 * their content to {@code .utf} files using UTF8 encoding. It also checks for key validity and
 * checks values for {@link MessageFormat} compatibility. Finally, it creates a {@code FooKeys.java}
 * source file declaring resource keys as integer constants.
 * <p>
 * This class <strong>must</strong> be run from the root of Java source files.
 * <p>
 * {@code ResourceCompiler} and all {@code FooKeys} classes don't need to be included in the final
 * JAR file. They are used at compile time only and no other classes should keep reference to them.
 *
 * @since 2.0
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ResourceCompiler implements Comparator {
    /**
     * Extension for properties files.
     */
    private static final String PROPERTIES_EXT = ".properties";

    /**
     * Extension for resource files.
     */
    private static final String RESOURCES_EXT = ".utf";

    /**
     * Prefix for argument count in resource key names. For example, a resource
     * expecting one argument may have a key name like "HELLO_$1".
     */
    private static final String ARGUMENT_COUNT_PREFIX = "_$";

    /**
     * Integer IDs allocated to resource keys. This map will be shared for all languages
     * of a given resource bundle.
     */
    private final Map/*<Integer,String>*/ allocatedIDs = new HashMap();

    /**
     * Resource keys and their localized values. This map will be cleared for each language
     * in a resource bundle.
     */
    private final Map/*<String,String>*/ resources = new HashMap();

    /**
     * The output stream for printing message.
     */
    private final PrintWriter out;

    /**
     * Constructs a new {@code ResourceCompiler}. This method will immediately look for
     * a {@code FooKeys.class} file. If one is found, integer keys are loaded in order to
     * reuse the same values.
     *
     * @param  out The output stream for printing message.
     * @param  bundleClass The resource bundle base class
     *         (e.g. <code>{@linkplain org.geotools.resources.i18n.Vocabulary}.class}</code>).
     * @throws IOException if an input/output operation failed.
     */
    private ResourceCompiler(final PrintWriter out, final Class bundleClass) throws IOException {
        this.out = out;
        try {
            final String classname = toKeyClass(bundleClass.getName());
            final Field[] fields = Class.forName(classname).getFields();
            out.print("Loading ");
            out.println(classname);
            /*
             * Copies all fields into {@link #allocatedIDs} map.
             */
            Field.setAccessible(fields, true);
            for (int i=fields.length; --i>=0;) {
                final Field field = fields[i];
                final String  key = field.getName();
                try {
                    final Object ID = field.get(null);
                    if (ID instanceof Integer) {
                        allocatedIDs.put((Integer)ID, key);
                    }
                } catch (IllegalAccessException exception) {
                    final File source = new File(classname.replace('.','/') + ".class");
                    warning(source, key, "Access denied", exception);
                }
            }
        } catch (ClassNotFoundException exception) {
            /*
             * 'VocabularyKeys.class' doesn't exist. This is okay (probably normal).
             * We will create 'VocabularyKeys.java' later using automatic key values.
             */
        }
    }

    /**
     * Returns the class name for the keys. For example if {@code bundleClass} is
     * {@code "org.geotools.resources.i18n.Vocabulary"}, then this method returns
     * {@code "org.geotools.resources.i18n.VocabularyKeys"}.
     */
    private static String toKeyClass(String bundleClass) {
        if (bundleClass.endsWith("s")) {
            bundleClass = bundleClass.substring(0, bundleClass.length()-1);
        }
        return bundleClass + "Keys";
    }

    /**
     * Loads all properties from a {@code .properties} file. Resource keys are checked for naming
     * conventions (i.e. resources expecting some arguments must have a key name ending with
     * {@code "_$n"} where {@code "n"} is the number of arguments). This method transforms resource
     * values into legal {@link MessageFormat} patterns when necessary.
     *
     * @param  file The properties file to read.
     * @throws IOException if an input/output operation failed.
     */
    private void loadPropertyFile(final File file) throws IOException {
        final InputStream input = new FileInputStream(file);
        final Properties properties = new Properties();
        properties.load(input);
        input.close();
        resources.clear();
        for (final Iterator it=properties.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final String key      = (String) entry.getKey();
            final String value    = (String) entry.getValue();
            /*
             * Checks key and value validity.
             */
            if (key.trim().length() == 0) {
                warning(file, key, "Empty key.", null);
                continue;
            }
            if (value.trim().length() == 0) {
                warning(file, key, "Empty value.", null);
                continue;
            }
            /*
             * Checks if the resource value is a legal MessageFormat pattern.
             */
            final MessageFormat message;
            try {
                message = new MessageFormat(toMessageFormatString(value));
            } catch (IllegalArgumentException exception) {
                warning(file, key, "Bad resource value", exception);
                continue;
            }
            /*
             * Checks if the expected arguments count (according to naming conventions)
             * matches the arguments count found in the MessageFormat pattern.
             */
            final int argumentCount;
            final int index = key.lastIndexOf(ARGUMENT_COUNT_PREFIX);
            if (index < 0) {
                argumentCount = 0;
                resources.put(key, value); // Text will not be formatted using MessageFormat.
            } else try {
                String suffix = key.substring(index + ARGUMENT_COUNT_PREFIX.length());
                argumentCount = Integer.parseInt(suffix);
                resources.put(key, message.toPattern());
            } catch (NumberFormatException exception) {
                warning(file, key, "Bad number in resource key", exception);
                continue;
            }
            final int expected = message.getFormats().length;
            if (argumentCount != expected) {
                final String suffix = ARGUMENT_COUNT_PREFIX + expected;
                warning(file, key, "Key name should ends with \""+suffix+"\".", null);
                continue;
            }
        }
        /*
         * Allocates an ID for each new key.
         */
        final String[] keys = (String[]) resources.keySet().toArray(new String[resources.size()]);
        Arrays.sort(keys, this);
        int freeID = 0;
        for (int i=0; i<keys.length; i++) {
            final String key = keys[i];
            if (!allocatedIDs.containsValue(key)) {
                Integer ID;
                do {
                    ID = new Integer(freeID++);
                } while (allocatedIDs.containsKey(ID));
                allocatedIDs.put(ID, key);
            }
        }
    }

    /**
     * Write UTF file. Method {@link #loadPropertyFile} should be invoked beforehand to
     * {@code writeUTFFile}.
     *
     * @param  file The destination file.
     * @throws IOException if an input/output operation failed.
     */
    private void writeUTFFile(final File file) throws IOException {
        final int count = allocatedIDs.isEmpty() ? 0 : ((Integer) Collections.max(allocatedIDs.keySet())).intValue()+1;
        final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        out.writeInt(count);
        for (int i=0; i<count; i++) {
            final String value = (String) resources.get(allocatedIDs.get(new Integer(i)));
            out.writeUTF((value!=null) ? value : "");
        }
        out.close();
    }

    /**
     * Changes a "normal" text string into a pattern compatible with {@link MessageFormat}.
     * The main operation consists of changing ' for '', except for '{' and '}' strings.
     */
    private static String toMessageFormatString(final String text) {
        int level =  0;
        int last  = -1;
        final StringBuffer buffer = new StringBuffer(text);
search: for (int i=0; i<buffer.length(); i++) { // Length of 'buffer' will vary.
            switch (buffer.charAt(i)) {
                /*
                 * Left and right braces take us up or down a level.  Quotes will only be doubled
                 * if we are at level 0.  If the brace is between quotes it will not be taken into
                 * account as it will have been skipped over during the previous pass through the
                 * loop.
                 */
                case '{' : level++; last=i; break;
                case '}' : level--; last=i; break;
                case '\'': {
                    /*
                     * If a brace ('{' or '}') is found between quotes, the entire block is
                     * ignored and we continue with the character following the closing quote.
                     */
                    if (i+2<buffer.length() && buffer.charAt(i+2)=='\'') {
                        switch (buffer.charAt(i+1)) {
                            case '{': i+=2; continue search;
                            case '}': i+=2; continue search;
                        }
                    }
                    if (level <= 0) {
                        /*
                         * If we weren't between braces, we must double the quotes.
                         */
                        buffer.insert(i++, '\'');
                        continue search;
                    }
                    /*
                     * If we find ourselves between braces, we don't normally need to double
                     * our quotes.  However, the format {0,choice,...} is an exception.
                     */
                    if (last>=0 && buffer.charAt(last)=='{') {
                        int scan=last;
                        do if (scan>=i) continue search;
                        while (Character.isDigit(buffer.charAt(++scan)));
                        final String choice=",choice,";
                        final int end=scan+choice.length();
                        if (end<buffer.length() && buffer.substring(scan, end).equalsIgnoreCase(choice)) {
                            buffer.insert(i++, '\'');
                            continue search;
                        }
                    }
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Prints a message to the output stream.
     *
     * @param file      File that produced the error, or {@code null} if none.
     * @param key       Resource key that produced the error, or {@code null} if none.
     * @param message   The message string.
     * @param exception An optional exception that is the cause of this warning.
     */
    private void warning(final File file,      final String key,
                         final String message, final Exception exception)
    {
        out.print("ERROR ");
        if (file != null) {
            String filename = file.getPath();
            if (filename.endsWith(PROPERTIES_EXT)) {
                filename = filename.substring(0, filename.length()-PROPERTIES_EXT.length());
            }
            out.print('(');
            out.print(filename);
            out.print(')');
        }
        out.print(": ");
        if (key != null) {
            out.print('"');
            out.print(key);
            out.print('"');
        }
        out.println();
        out.print(message);
        if (exception != null) {
            out.print(": ");
            out.print(exception.getLocalizedMessage());
        }
        out.println();
        out.println();
        out.flush();
    }

    /**
     * Writes {@code count} spaces to the {@code out} stream.
     * @throws IOException if an input/output operation failed.
     */
    private static void writeWhiteSpaces(final Writer out, int count) throws IOException {
        while (--count>=0) {
            out.write(' ');
        }
    }

    /**
     * Creates a source file for resource keys.
     *
     * @param  bundleClass The resource bundle base class
     *         (e.g. <code>{@linkplain org.geotools.resources.i18n.Vocabulary}.class}</code>).
     * @throws IOException if an input/output operation failed.
     */
    private void writeJavaSource(final Class bundleClass) throws IOException {
        final String fullname    = toKeyClass(bundleClass.getName());
        final String classname   = fullname.substring(fullname.lastIndexOf('.')+1);
        final String packageName = fullname.substring(0, fullname.lastIndexOf('.'));
        final File          file = new File(fullname.replace('.', '/') + ".java");
        final BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("/*\n"                                                             +
                  " * Geotools - OpenSource mapping toolkit\n"                       +
                  " * (C) 2003, Geotools Project Managment Committee (PMC)\n"        +
                  " *\n"                                                             +
                  " *     THIS IS AN AUTOMATICALLY GENERATED FILE. DO NOT EDIT!\n"   +
                  " *     Generated with: org.geotools.resources.ResourceCompiler\n" +
                  " */\n");
        out.write("package ");
        out.write(packageName);
        out.write(";\n\n\n");
        out.write("/**\n"                                                                  +
                  " * Resource keys. This class is used when compiling sources, but\n"     +
                  " * no dependencies to {@code ResourceKeys} should appear in any\n"      +
                  " * resulting class files.  Since Java compiler inlines final integer\n" +
                  " * values, using long identifiers will not bloat constant pools of\n"   +
                  " * classes compiled against the interface, provided that no class\n"    +
                  " * implements this interface.\n"                                        +
                  " *\n"                                                                   +
                  " * @see org.geotools.resources.ResourceBundle\n"                        +
                  " * @see org.geotools.resources.ResourceCompiler\n"                      +
                  " */\n");
        out.write("public final class "); out.write(classname); out.write(" {\n");
        out.write("    private "); out.write(classname); out.write("() {\n");
        out.write("    }\n");
        out.write("\n");
        final Map.Entry[] entries = (Map.Entry[]) allocatedIDs.entrySet().toArray(new Map.Entry[allocatedIDs.size()]);
        Arrays.sort(entries, this);
        int maxLength = 0;
        for (int i=entries.length; --i>=0;) {
            final int length = ((String) entries[i].getValue()).length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        for (int i=0; i<entries.length; i++) {
            final String key = (String) entries[i].getValue();
            final String ID  = entries[i].getKey().toString();
            writeWhiteSpaces(out, 4);
            out.write("public static final int ");
            out.write(key);
            writeWhiteSpaces(out, maxLength-key.length());
            out.write(" = ");
            writeWhiteSpaces(out, 5-ID.length());
            out.write(ID);
            out.write(";\n");
        }
        out.write("}\n");
        out.close();
    }

    /**
     * Compares two resource keys. Object {@code o1} and {@code o2} are usually {@link String}
     * objects representing resource keys (for example, "{@code MISMATCHED_DIMENSION}"), but
     * may also be {@link Map.Entry}.
     */
    public int compare(Object o1, Object o2) {
        if (o1 instanceof Map.Entry) o1 = ((Map.Entry) o1).getValue();
        if (o2 instanceof Map.Entry) o2 = ((Map.Entry) o2).getValue();
        final String key1 = (String) o1;
        final String key2 = (String) o2;
        return key1.compareTo(key2);
    }

    /**
     * Scans the package for resources.
     *
     * @param  out The output stream for printing message.
     * @param  bundleClass The resource bundle base class
     *         (e.g. <code>{@linkplain org.geotools.resources.i18n.Vocabulary}.class}</code>).
     * @throws IOException if an input/output operation failed.
     */
    private static void scanForResources(final PrintWriter out, final Class bundleClass) throws IOException {
        String classname = bundleClass.getName();
        final File directory = new File(classname.replace('.', '/')).getParentFile();
        if (!directory.isDirectory()) {
            out.print('"');
            out.print(directory.getPath());
            out.println("\" is not a directory.");
            return;
        }
        classname = classname.substring(classname.lastIndexOf('.')+1);
        ResourceCompiler compiler = null;
        final File[] content = directory.listFiles();
        for (int i=0; i<content.length; i++) {
            final File file = content[i];
            final String filename = file.getName();
            if (filename.startsWith(classname) && filename.endsWith(PROPERTIES_EXT)) {
                if (compiler == null) {
                    compiler = new ResourceCompiler(out, bundleClass);
                }
                compiler.loadPropertyFile(file);
                String path = file.getPath();
                path = path.substring(0, path.length()-PROPERTIES_EXT.length()) + RESOURCES_EXT;
                compiler.writeUTFFile(new File(path));
            }
        }
        if (compiler != null) {
            compiler.writeJavaSource(bundleClass);
        }
    }

    /**
     * Run the resource compiler.
     *
     * @param  args List of fully-qualified class name. If none, then a default set is used.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final PrintWriter out = arguments.out;
        args = arguments.getRemainingArguments(0);
        if (args.length == 0) {
            args = new String[] {
                org.geotools.resources.i18n.Descriptions.class.getName(),
                org.geotools.resources.i18n.Vocabulary  .class.getName(),
                org.geotools.resources.i18n.Logging     .class.getName(),
                org.geotools.resources.i18n.Errors      .class.getName()
            };
        }
        for (int i=0; i<args.length; i++) {
            try {
                scanForResources(out, Class.forName(args[i]));
            } catch (ClassNotFoundException exception) {
                out.println(exception.getLocalizedMessage());
            } catch (IOException exception) {
                out.println(exception.getLocalizedMessage());
            }
        }
        out.flush();
    }
}
