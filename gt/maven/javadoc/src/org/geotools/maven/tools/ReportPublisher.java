/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.maven.tools;

// J2SE dependencies
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
 * java -classpath ~/svnroot/geotools/maven/target/classes org.geotools.maven.tools.ReportPublisher
 * ~/svnroot/geotools ~/maven/reports
 * </code></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ReportPublisher implements Runnable {
    /**
     * The source directory.
     */
    private final File source;

    /**
     * The target directory.
     */
    private final File target;

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
    private final Collection modules = new HashSet();

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
        this.source = new File(source);
        this.target = new File(target);
    }

    /**
     * Process all files.
     */
    public void run() {
        process(source, 0);
        try {
            writeIndex();
        } catch (IOException e) {
            Logger.getLogger("org.geotools.maven").log(Level.WARNING, "Failed to write index.html", e);
        }
    }

    /**
     * Process all files in the specified directory.
     *
     * @param directory The directory to process.
     * @param depth The directory depth, from 0 to {@link #maxDepth} inclusive.
     */
    private void process(final File directory, int depth) {
        final File candidate = new File(directory, searchFor);
        if (candidate.isDirectory()) {
            final String module = directory.getName();
            move(candidate, new File(target, module));
            modules.add(module);
        }
        // Now scan subdirectories.
        if (++depth <= maxDepth) {
            final File[] content = directory.listFiles();
            for (int i=0; i<content.length; i++) {
                final File c = content[i];
                if (c.isDirectory() && !c.isHidden()) {
                    final String name = c.getName();
                    if (!name.equalsIgnoreCase(".svn") && !name.equalsIgnoreCase("_svn")) {
                        process(c, depth);
                    }
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
    private static void move(final File source, final File target) {
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
        final File[] content = source.listFiles();
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
                    warning(s, "move");
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
     * Prints a warning when an operation failed.
     */
    private static void warning(final File file, final String operation) {
        Logger.getLogger("org.geotools.maven").warning("Failed to "+operation+" file "+file);
    }

    /**
     * Write an index with the list of all modules processed.
     */
    private void writeIndex() throws IOException {
        final String modules[] = (String[]) this.modules.toArray(new String[this.modules.size()]);
        Arrays.sort(modules);
        index = new BufferedWriter(new FileWriter(new File(target, "index.html")));
        writeLine(0, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        writeLine(0, "<html>");
        writeLine(1, "<head>");
        writeLine(2, "<title>Maven reports for the Geotools project</title>");
        writeLine(1, "</head>");
        writeLine(1, "<body>");
        writeLine(2, "<h1>Maven reports for the Geotools project</h1>");
        writeLine(2, "<h2>All modules, plugins and extensions</h2>");
        writeLine(2, "<p><ul>");
        for (int i=0; i<modules.length; i++) {
            final String module = modules[i];
            writeLine(3, "<li><a href=\"" + module + "/index.html\">" + module + "</a></li>");
        }
        writeLine(2, "</ul></p>");
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
