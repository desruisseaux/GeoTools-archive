/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * A writer that copy all output to an other stream. This writer can be used for perfoming
 * an exact copy of what is sent to an other writer. For example, it may be used for echoing
 * to the standard output the content sent to a file. This writer is usefull for debugging
 * purpose.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EchoWriter extends FilterWriter {
    /**
     * The echo writer.
     */
    private final Writer echo;

    /**
     * Creates a writer that will echo to the {@linkplain System#out standard output}.
     * Each line to that standard output will be {@linkplain NumberedLineWriter numbered}.
     *
     * @param main The main stream.
     */
    public EchoWriter(final Writer main) {
        super(main);
        this.echo = NumberedLineWriter.OUT;
    }

    /**
     * Creates a copy writter for the specified stream.
     *
     * @param main The main stream.
     * @param echo The echo stream.
     */
    public EchoWriter(final Writer main, final Writer echo) {
        super(main);
        this.echo = echo;
    }

    /**
     * Write a single character.
     *
     * @throws IOException  If an I/O error occurs
     */
    public void write(final int c) throws IOException {
        synchronized (lock) {
            out .write(c);
            echo.write(c);
        }
    }

    /**
     * Write an array of characters.
     *
     * @param  cbuf  Buffer of characters to be written
     * @throws IOException  If an I/O error occurs
     */
    public void write(final char[] cbuf) throws IOException {
        synchronized (lock) {
            out .write(cbuf);
            echo.write(cbuf);
        }
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Buffer of characters to be written
     * @param  off   Offset from which to start reading characters
     * @param  len   Number of characters to be written
     * @throws IOException  If an I/O error occurs
     */
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        synchronized (lock) {
            out .write(cbuf, off, len);
            echo.write(cbuf, off, len);
        }
    }

    /**
     * Write a string.
     *
     * @param  str  String to be written
     * @throws IOException  If an I/O error occurs
     */
    public void write(final String str) throws IOException {
        synchronized (lock) {
            out .write(str);
            echo.write(str);
        }
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  A String
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     * @throws IOException  If an I/O error occurs
     */
    public void write(final String str, final int off, final int len) throws IOException {
        synchronized (lock) {
            out .write(str, off, len);
            echo.write(str, off, len);
        }
    }

    /**
     * Flush both streams.
     *
     * @throws  IOException  If an I/O error occurs
     */
    public void flush() throws IOException {
        synchronized (lock) {
            out .flush();
            echo.flush();
        }
    }

    /**
     * Close the main stream, If this object has been constructed with the
     * {@linkplain #EchoWriter(Writer) one argument constructor} (i.e.
     * if the echo stream is the {@linkplain System#out standard output}),
     * then the echo stream will not be closed. Otherwise it will be closed too.
     *
     * @throws  IOException  If an I/O error occurs
     */
    public void close() throws IOException {
        synchronized (lock) {
            out .close();
            echo.close(); // Overriden with an uncloseable version for System.out.
        }
    }
}
