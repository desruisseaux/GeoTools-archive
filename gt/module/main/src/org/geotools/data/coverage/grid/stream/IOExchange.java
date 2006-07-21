/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.coverage.grid.stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class is used to exchange between different input and output sources and
 * destinations If a format, for example arcgrid, needs a Reader object then
 * IOExchange will create one from the Source object
 * 
 * @author jeichar
 * @source $URL$
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
     * @param source
     *            An object that identifies an input Current Implementation
     *            accepts: Reader, URL, String, File, InputStream
     * 
     * @return Returns a Reader that reads from the source identified by the
     *         source object
     * 
     * @throws IOException
     *             If Source object cannot be identified or if an unexpected
     *             problem occurs.
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
     * Returns a Reader that wraps a GZIPInputStream. WARNING. If source is a
     * reader it assumes that the Reader is of the correct type. There is no way
     * to be certain aside from trying to read the data.
     * 
     * @param source
     *            An object that identifies an input Current Implementation.
     *            accepts: Reader, URL, String, File, InputStream Note: if
     *            source is a reader it MUST already wrap a GZIPInputStream.
     * @return Reader that wraps a GZIPInputStream.
     * @throws IOException
     *             if the destination cannot be converted to a Writer
     */
    public Reader getGZIPReader(Object source) throws IOException {
        InputStream in = null;
        if (source instanceof Reader) {
            return (Reader) source;
        }

        if (source instanceof GZIPInputStream) {
            in = (InputStream) source;
        }

        if (source instanceof InputStream) {
            in = new GZIPInputStream((InputStream) source);
        }

        if (source instanceof String) {
            String urlString = (String) source;
            URL url = new URL(urlString);

            in = new GZIPInputStream(url.openStream());
        }

        if (source instanceof URL) {
            URL url = (URL) source;
            
            in = new GZIPInputStream(url.openStream());
        }

        if (source instanceof File) {
            in = new GZIPInputStream(new FileInputStream((File) source));
        }

        if (source instanceof ReadableByteChannel) {
            in = new GZIPInputStream(Channels
                    .newInputStream((ReadableByteChannel) source));
        }

        if (in != null)
            return new InputStreamReader(new BufferedInputStream(in));
        else
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
     * FileWriter) It then constructs a new Writer out of the destination and
     * returns it.
     * </p>
     * 
     * @param destination
     *            the destination object to be converted
     * 
     * @return a new Writer that can write to the destination
     * 
     * @throws IOException
     *             if the destination cannot be converted to a Writer
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

            return new OutputStreamWriter(url.openConnection()
                    .getOutputStream());
        }

        if (destination instanceof URL) {
            URL url = (URL) destination;

            return new OutputStreamWriter(url.openConnection()
                    .getOutputStream());
        }

        if (destination instanceof File) {
            return new FileWriter((File) destination);
        }

        if (destination instanceof WritableByteChannel) {
            return Channels.newWriter((WritableByteChannel) destination,
                    "UTF-8");
        }

        throw new IOException(
                "destination Object is not recognized as one of the accepted destination objects");
    }

    /**
     * Takes a destination object and creates a PrintWriter from it
     * 
     * @param destination
     *            the destination object to be converted
     * 
     * @return a new PrintWriter that can write to the destination
     * 
     * @throws IOException
     *             if the destination cannot be converted to a Writer
     */
    public PrintWriter getPrintWriter(Object destination) throws IOException {
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
     * Returns a PrintWriter that wraps a GZIPOutputStream. WARNING. If source
     * is a PrintWriter or a Writer it assumes that stream is of the correct
     * type. There is no way to be certain aside from trying to write the data.
     * 
     * @param source
     *            An object that identifies an input Current Implementation.
     *            accepts: Writer, URL, String, File, OutputStream Note: if
     *            source is a Writer it MUST already wrap a GZIPOutputStream.
     * @return Writer that wraps a GZIPOutputStream.
     * @throws IOException
     *             if the destination cannot be converted to a Writer
     */
    public PrintWriter getGZIPPrintWriter(Object destination)
            throws IOException {
        if (destination instanceof PrintWriter) {
            return (PrintWriter) destination;
        }
        if (destination instanceof Writer) {
            return new PrintWriter((Writer) destination);
        }

        if (destination instanceof GZIPOutputStream) {
            return new PrintWriter((OutputStream) destination);
        }

        if (destination instanceof OutputStream) {
            return new PrintWriter(new GZIPOutputStream(
                    (OutputStream) destination));
        }

        if (destination instanceof String) {
            String urlString = (String) destination;
            URL url = new URL(urlString);
            OutputStream out = new GZIPOutputStream(url.openConnection()
                    .getOutputStream());
            return new PrintWriter(out);
        }

        if (destination instanceof URL) {
            URL url = (URL) destination;
            OutputStream out = new GZIPOutputStream(url.openConnection()
                    .getOutputStream());
            return new PrintWriter(out);
        }

        if (destination instanceof File) {
            OutputStream out = new GZIPOutputStream(new FileOutputStream(
                    (File) destination));
            return new PrintWriter(out);
        }

        if (destination instanceof WritableByteChannel) {
            OutputStream out = new GZIPOutputStream(Channels
                    .newOutputStream((WritableByteChannel) destination));
            return new PrintWriter(out);
        }

        throw new IOException(
                "destination Object is not recognized as one of the accepted destination objects");
    }

    /**
     * When implemented this method will determine the format of a source object
     * 
     * @param arg0
     *            The source object
     * 
     * @return an integer indicating a particular format
     */
    public static int determineSourceFormat(Object arg0) {
        return 0;
    }
}
