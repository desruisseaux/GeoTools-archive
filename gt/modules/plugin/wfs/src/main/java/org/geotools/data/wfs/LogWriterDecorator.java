/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A decorator that writes to the log as well as the wrapped writer.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class LogWriterDecorator extends Writer {

    private Writer delegate;
    private Level level;

    StringBuffer buffer=new StringBuffer("Output: ");
    private Logger logger; 
    
    public LogWriterDecorator( Writer w, Logger logger, Level logLevel ) {
        this.delegate=w;
        this.level=logLevel;
        this.logger=logger;
    }

    public synchronized void close() throws IOException {
        delegate.close();
        logger.log(level, buffer.toString());
        buffer=new StringBuffer("Output: ");
    }

    public synchronized void flush() throws IOException {
        delegate.flush();
        logger.log(level, buffer.toString());
        buffer=new StringBuffer("Output: ");
    }

    public synchronized void write( char[] cbuf, int off, int len ) throws IOException {
        char[] msg = new char[len];
        System.arraycopy(cbuf, off, msg, 0, len);
        buffer.append(msg);
        delegate.write(cbuf, off, len);
    }

}