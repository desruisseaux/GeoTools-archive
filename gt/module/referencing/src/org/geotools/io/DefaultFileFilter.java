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
package org.geotools.io;

// J2SE dependencies
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.regex.Pattern;


/**
 * A {@link FileFilter} implementation using Unix-style wildcards.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.0
 */
public class DefaultFileFilter extends javax.swing.filechooser.FileFilter
                            implements FileFilter, FilenameFilter
{
    /**
     * The description of this filter, usually for graphical user interfaces.
     */
    private final String description;

    /**
     * The pattern to matchs to filenames.
     */
    private final Pattern pattern;

    /**
     * Construct a file filter for the specified pattern.
     * Pattern may contains the "*" and "?" wildcards.
     *
     * @param pattern The pattern (e.g. "*.png").
     */
    public DefaultFileFilter(final String pattern) {
        this(pattern, new File(pattern).getName());
    }

    /**
     * Construct a file filter for the specified pattern
     * and description. Pattern may contains the "*" and
     * "?" wildcards.
     *
     * @param pattern The pattern (e.g. "*.png").
     * @param description The description of this filter,
     *        usually for graphical user interfaces.
     */
    public DefaultFileFilter(final String pattern, final String description) {
        this.description = description.trim();
        final int length = pattern.length();
        final StringBuffer buffer = new StringBuffer(length+8);
        for (int i=0; i<length; i++) {
            final char c = pattern.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                switch (c) {
                    case '?': buffer.append('.' ); continue;
                    case '*': buffer.append(".*"); continue;
                    default : buffer.append('\\'); break;
                }
            }
            buffer.append(c);
        }
        this.pattern = Pattern.compile(buffer.toString());
    }

    /**
     * Returns the description of this filter.
     * For example: "PNG images"
     */
    public String getDescription() {
        return description;
    }

    /**
     * Tests if a specified file matches the pattern.
     *
     * @param  file The file to be tested.
     * @return {@code true} if and only if the name matches the pattern.
     */
    public boolean accept(final File file) {
        return (file!=null) && pattern.matcher(file.getName()).matches();
    }

    /**
     * Tests if a specified file matches the pattern.
     *
     * @param  dir    the directory in which the file was found.
     * @param  name   the name of the file.
     * @return {@code true} if and only if the name matches the pattern.
     */
    public boolean accept(File dir, String name) {
        return (name!=null) && pattern.matcher(name).matches();
    }
}
