/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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

// Standard I/O
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;


/**
 * A writer that put line number in front of every line.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NumberedLineWriter extends FilterWriter {
    /**
     * A default numbered line writer to the {@linkplain System#out standard output stream}.
     * The {@link #close} method on this stream will only flush it without closing it.
     */
    public static final PrintWriter OUT =
            new PrintWriter(new Uncloseable(Arguments.getWriter(System.out)), true);

    /**
     * A stream that can never been closed. Used only for wrapping the
     * {@linkplain System#out standard output stream}.
     */
    private static final class Uncloseable extends NumberedLineWriter {
        /** Constructs a stream. */
        public Uncloseable(final Writer out) {
            super(out);
        }

        /** Flush the stream without closing it. */
        public void close() throws IOException {
            flush();
        }
    }

    /**
     * The with reserved for line numbers (not counting the space for "[ ]" brackets).
     */
    private int width = 3;

    /**
     * The current line number.
     */
    private int current = 1;

    /**
     * <code>true</code> if we are about to write a new line.
     */
    private boolean newLine = true;

    /**
     * <code>true</code> if we are waiting for a '\n' character.
     */
    private boolean waitLF;

    /**
     * Constructs a stream which will write line number in front of each line.
     *
     * @param out a Writer object to provide the underlying stream.
     */
    public NumberedLineWriter(final Writer out) {
        super(out);
    }

    /**
     * Returns the current line number.
     */
    public int getLineNumber() {
        return current;
    }

    /**
     * Sets the current line number.
     *
     * @param line The current line number.
     */
    public void setLineNumber(final int line) {
        synchronized (lock) {
            this.current = line;
        }
    }

    /**
     * Write the specified character. Line number will be written if we are at the
     * begining of a new line.
     *
     * @throws IOException If an I/O error occurs
     */
    private void doWrite(final int c) throws IOException {
        if (newLine && (c!='\n' || !waitLF)) {
            final String number = String.valueOf(current++);
            out.write('[');
            out.write(Utilities.spaces(width - number.length()));
            out.write(number);
            out.write("] ");
        }
        out.write(c);
        if ((newLine = (c=='\r' || c=='\n')) == true) {
            waitLF = (c=='\r');
        }
    }

    /**
     * Writes a single character.
     *
     * @throws IOException If an I/O error occurs
     */
    public void write(final int c) throws IOException {
        synchronized (lock) {
            doWrite(c);
        }
    }
    
    /**
     * Writes a portion of an array of characters.
     *
     * @param  buffer  Buffer of characters to be written
     * @param  offset  Offset from which to start reading characters
     * @param  length  Number of characters to be written
     * @throws IOException  If an I/O error occurs
     */
    public void write(final char[] buffer, int offset, final int length) throws IOException {
        final int upper = offset + length;
        synchronized (lock) {
CHECK:      while (offset < upper) {
                if (newLine) {
                    doWrite(buffer[offset++]);
                    continue;
                }
                final int lower = offset;
                do {
                    final char c = buffer[offset];
                    if (c=='\r' || c=='\n') {
                        out.write(buffer, lower, offset-lower);
                        doWrite(c);
                        offset++;
                        continue CHECK;
                    }
                } while (++offset < upper);
                out.write(buffer, lower, offset-lower);
                break;
            }
        }
    }
    
    /**
     * Writes a portion of a string.
     *
     * @param  string  String to be written
     * @param  offset  Offset from which to start reading characters
     * @param  length  Number of characters to be written
     * @throws IOException  If an I/O error occurs
     */
    public void write(final String string, int offset, final int length) throws IOException {
        final int upper = offset + length;
        synchronized (lock) {
CHECK:      while (offset < upper) {
                if (newLine) {
                    doWrite(string.charAt(offset++));
                    continue;
                }
                final int lower = offset;
                do {
                    final char c = string.charAt(offset);
                    if (c=='\r' || c=='\n') {
                        out.write(string, lower, offset-lower);
                        doWrite(c);
                        offset++;
                        continue CHECK;
                    }
                } while (++offset < upper);
                out.write(string, lower, offset-lower);
                break;
            }
        }
    }
}
