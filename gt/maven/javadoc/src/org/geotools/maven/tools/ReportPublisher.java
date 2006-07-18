/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.maven.tools;

// J2SE dependencies
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Moves the content of all {@code target/site} directories to the specified location.
 * This class is more efficient than {@code mvn site:deploy} when the target directory
 * lives on the same machine than the build directory. It also avoid a bunch of problem
 * related to the unreliable {@code scp} behavior in Maven 2 (as of December 2005).
 * <p>
 * <b>Example:</b> suppose that an Unix box has the following directory layout:
 * <ul>
 *   <li><p>{@code "~/svnroot/geotools"} is the root of a Geotools checkout from SVN
 *       (as {@code svn checkout svn.geotools.org/geotools/trunk/gt/ geotools}).</p></li>
 *
 *   <li><p>The Maven reports need to be moved into the {@code "~/maven/reports"} directory for
 *       publication on the web (where {@code "~/maven/} may be a symbolic link toward some
 *       {@code "/var/www/"} directory).</p></li>
 * </ul>
 * Then the reports may be moved (rather than copied) using the following command line
 * (after running {@code mvn install} at least once):
 *
 * <blockquote><code>
 * java -classpath ~/svnroot/geotools/maven/javadoc/target/classes
 * org.geotools.maven.tools.ReportPublisher ~/svnroot/geotools ~/maven/reports
 * </code></blockquote>
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ReportPublisher implements Runnable, FilenameFilter {
    /**
     * The source directory.
     */
    private final File sourceBase;

    /**
     * The target directory.
     */
    private final File targetBase;

    /**
     * The subdirectory to search for.
     */
    private final String searchFor = "target/site";

    /**
     * The maximum directory depth. Used in order to avoid to scan the (very numerous)
     * project subdirectories. The default value is 2, which is okay for fetching the
     * {@code module/api/target/site} directory for example.
     */
    private final int maxDepth = 2;

    /**
     * The list of modules processed.
     */
    private final Collection modules = new TreeSet();

    /**
     * The stream where to write the {@code index.html} file. Used by {@link #writeIndex} only.
     */
    private transient Writer index;

    /**
     * The line separator to use for writting into the {@link #index} streams.
     */
    private final String lineSeparator = System.getProperty("line.separator", "\n");

    /**
     * Run from the command line. See class description for a description of expected arguments.
     */
    public static void main(final String args[]) {
        final ReportPublisher r;
        switch (args.length) {
            case 2: {
                r = new ReportPublisher(args[0], args[1]);
                break;
            }
            default: {
                System.out.println("Expected argument: root of Geotools sources.");
                return;
            }
        }
        r.run();
    }

    /**
     * Creates an instance of this class.
     *
     * @param source The source directory.
     * @param target The target directory.
     */
    public ReportPublisher(final String source, final String target) {
        sourceBase = new File(source);
        targetBase = new File(target);
    }

    /**
     * Returns {@code true} if the specified name is not a SVN directory.
     */
    public boolean accept(final File directory, final String name) {
        return !name.equalsIgnoreCase(".svn") && !name.equalsIgnoreCase("_svn");
    }

    /**
     * Process all files.
     */
    public void run() {
        process(sourceBase, 0);
        try {
            writeIndex();
        } catch (IOException e) {
            Logger.getLogger("org.geotools.maven").log(Level.WARNING, "Failed to write index.html", e);
        }
    }

    /**
     * Gets the module relative path for the specifed source directory and depth.
     */
    private static String getModulePath(File source, int depth) {
        final StringBuffer buffer = new StringBuffer();
        while (--depth >= 0) {
            buffer.insert(0, '/');
            buffer.insert(0, source.getName());
            source = source.getParentFile();
        }
        return buffer.toString();
    }

    /**
     * Process all files in the specified directory.
     *
     * @param source The directory to process.
     * @param depth  The directory depth, from 0 to {@link #maxDepth} inclusive.
     */
    private void process(final File source, int depth) {
        final String modulePath = getModulePath(source, depth);
        final String moduleName = new File(modulePath).getName();
        final File   candidate  = new File(source, searchFor);
        if (candidate.isDirectory()) {
            move(candidate, new File(targetBase, moduleName));
            modules.add(modulePath);
        }
        // Now scan subdirectories.
        if (++depth <= maxDepth) {
            final File[] content = source.listFiles(this);
            for (int i=0; i<content.length; i++) {
                final File c = content[i];
                if (c.isDirectory() && !c.isHidden()) {
                    process(c, depth);
                }
            }
        }
    }

    /**
     * Moves all files from the source directory to the target directory.
     * Target directories will be created as needed.
     * Source directories will be deleted after the move.
     *
     * @param source The source directory.
     * @param target The target directory.
     */
    private void move(final File source, final File target) {
        if (target.isFile()) {
            if (!target.delete()) {
                warning(target, "delete");
            }
        }
        if (!target.isDirectory()) {
            if (!target.mkdir()) {
                warning(target, "mkdir");
            }
        }
        final File[] content = source.listFiles(this);
        for (int i=0; i<content.length; i++) {
            final File s = content[i];
            final File t = new File(target, s.getName());
            if (s.isFile()) {
                if (t.exists()) {
                    if (!t.delete()) {
                        warning(t, "delete");
                    }
                }
                if (!s.renameTo(t)) {
                    // Unable to use 'renameTo'. Copy and delete.
                    try {
                        copy(s, t);
                        if (!s.delete()) {
                            warning(s, "delete");
                        }
                    } catch (IOException e) {
                        warning(s, "move");
                    }
                }
                continue;
            }
            if (s.isDirectory() && !s.isHidden()) {
                move(s, t);
                continue;
            }
        }
        if (!source.delete()) {
            warning(source, "rmdir");
        }
    }

    /**
     * Copies {@code source} to {@code target}. Note: no need for a buffered input/output
     * streams, since we are already using a buffer of type {@code byte[4096]}.
     */
    private static void copy(final File source, final File target) throws IOException {
        final InputStream  in  = new FileInputStream (source);
        final OutputStream out = new FileOutputStream(target);
        final byte[]    buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        out.close();
        in.close();
    }

    /**
     * Prints a warning when an operation failed.
     */
    private static void warning(final File file, final String operation) {
        Logger.getLogger("org.geotools.maven").warning("Failed to "+operation+" file "+file);
    }

    /**
     * Writes an index with the list of all modules processed.
     */
    private void writeIndex() throws IOException {
        final String[] prefix = {"module/", "plugin/", "ext/",       "maven/"       };
        final String[] titles = {"Modules", "Plugins", "Extensions", "Maven plugins"};
        final int width = 100 / prefix.length;
        index = new BufferedWriter(new FileWriter(new File(targetBase, "index.html")));
        writeLine(0, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        writeLine(0, "<html>");
        writeLine(1, "<head>");
        writeLine(2, "<title>Maven reports for the Geotools project</title>");
        writeLine(1, "</head>");
        writeLine(1, "<body>");
        writeLine(2, "<h1>Maven reports for the Geotools project</h1>");
        writeLine(2, "<p><table cellpadding=\"6\" border=\"1\">");
        writeLine(3, "<tr>");
        for (int i=0; i<titles.length; i++) {
            writeLine(4, "<th bgcolor=\"palegreen\" width=\"" + width + "%\">" + titles[i] + "</th>");
        }
        writeLine(3, "</tr><tr>");
        for (int column=0; column<prefix.length; column++) {
            final String p = prefix[column];
            writeLine(4, "<td nowrap witdh=\"" + width + "%\" valign=\"top\"><ul>");
            for (final Iterator it=modules.iterator(); it.hasNext();) {
                final String module = (String) it.next();
                if (module.startsWith(p)) {
                    final String name = new File(module).getName();
                    writeLine(5, "<li><a href=\"" + name + "/index.html\">" + name + "</a></li>");
                    it.remove();
                }
            }
            writeLine(4, "</ul></td>");
        }
        writeLine(3, "</tr>");
        writeLine(2, "</table>");
        writeLine(1, "</body>");
        writeLine(0, "</html>");
        index.close();
        index = null;
    }

    /**
     * Writes a line to {@link #writer}. This is a helper method for {@link #writeIndex} only.
     */
    private void writeLine(int indent, final String line) throws IOException {
        indent *= 2;
        while (--indent >= 0) {
            index.write(' ');
        }
        index.write(line);
        index.write(lineSeparator);
    }
}
