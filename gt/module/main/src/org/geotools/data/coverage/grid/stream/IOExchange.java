/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 * Created on Apr 26, 2004
 */
package org.geotools.data.coverage.grid.stream;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;


/**
 * This class is used to exchange between different input and output sources
 * and destinations If a format, for example arcgrid, needs a Reader object
 * then IOExchange will create one from the Source object
 *
 * @author jeichar
 */
public class IOExchange {
    private IOExchange() {
    }

    /**
     * Factory method for creating a new IOExchange
     *
     * @return an instance of a IOExchange
     */
    public static IOExchange getIOExchange() {
        return new IOExchange();
    }

    /**
     * Uses the source object to create a Reader object for client
     *
     * @param source An object that identifies an input Current Implementation
     *        accepts: Reader, URL, String, File, InputStream
     *
     * @return Returns a Reader that reads from the source identified by the
     *         source object
     *
     * @throws IOException If Source object cannot be identified or if an
     *         unexpected problem occurs.
     */
    public Reader getReader(Object source) throws IOException {
        if (source instanceof Reader) {
            return (Reader) source;
        }

        if (source instanceof InputStream) {
            return new InputStreamReader((InputStream) source);
        }

        if (source instanceof String) {
            String urlString = (String) source;
            URL url = new URL(urlString);

            return new InputStreamReader(url.openStream());
        }

        if (source instanceof URL) {
            URL url = (URL) source;

            return new InputStreamReader(url.openStream());
        }

        if (source instanceof File) {
            return new FileReader((File) source);
        }

        if (source instanceof ReadableByteChannel) {
            return Channels.newReader((ReadableByteChannel) source, "UTF-8");
        }

        throw new IOException(
            "Source Object is not recognized as one of the accepted source objects");
    }

    /**
     * Takes a destination object and converts it to it's appropriate subclass
     * of Writer.
     * 
     * <p>
     * Takes the destination object and casts it to one of: Writer (Returns
     * Writer) OutputStream (Returns OutputStreamWriter) String (Returns
     * OutputStreamWriter) URL (Returns OutputStreamWriter) File (Returns
     * FileWriter)  It then constructs a new Writer out of the destination and
     * returns it.
     * </p>
     *
     * @param destination the destination object to be converted
     *
     * @return a new Writer that can write to the destination
     *
     * @throws IOException if the destination cannot be converted to a Writer
     */
    public Writer getWriter(Object destination) throws IOException {
        if (destination instanceof Writer) {
            return (Writer) destination;
        }

        if (destination instanceof OutputStream) {
            return new OutputStreamWriter((OutputStream) destination);
        }

        if (destination instanceof String) {
            String urlString = (String) destination;
            URL url = new URL(urlString);

            return new OutputStreamWriter(url.openConnection().getOutputStream());
        }

        if (destination instanceof URL) {
            URL url = (URL) destination;

            return new OutputStreamWriter(url.openConnection().getOutputStream());
        }

        if (destination instanceof File) {
            return new FileWriter((File) destination);
        }

        if (destination instanceof WritableByteChannel) {
            return Channels.newWriter((WritableByteChannel) destination, "UTF-8");
        }

        throw new IOException(
            "destination Object is not recognized as one of the accepted destination objects");
    }

    /**
     * Takes a destination object and creates a PrintWriter from it
     *
     * @param destination the destination object to be converted
     *
     * @return a new PrintWriter that can write to the destination
     *
     * @throws IOException if the destination cannot be converted to a Writer
     */
    public PrintWriter getPrintWriter(Object destination)
        throws IOException {
        if (destination instanceof Writer) {
            return new PrintWriter((Writer) destination);
        }

        if (destination instanceof OutputStream) {
            return new PrintWriter((OutputStream) destination);
        }

        if (destination instanceof String) {
            String urlString = (String) destination;
            URL url = new URL(urlString);

            return new PrintWriter(url.openConnection().getOutputStream());
        }

        if (destination instanceof URL) {
            URL url = (URL) destination;

            return new PrintWriter(url.openConnection().getOutputStream());
        }

        if (destination instanceof File) {
            FileWriter fw = new FileWriter((File) destination);

            return new PrintWriter(fw);
        }

        if (destination instanceof WritableByteChannel) {
            return new PrintWriter(Channels.newWriter(
                    (WritableByteChannel) destination, "UTF-8"));
        }

        throw new IOException(
            "destination Object is not recognized as one of the accepted destination objects");
    }

    /**
     * When implemented this method will determine the format of a source
     * object
     *
     * @param arg0 The source object
     *
     * @return an integer indicating a particular format
     */
    public static int determineSourceFormat(Object arg0) {
        return 0;
    }
}
