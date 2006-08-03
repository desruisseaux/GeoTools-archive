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
    
    public LogWriterDecorator( Writer w, Level logLevel ) {
        this.delegate=w;
        this.level=logLevel;
    }

    public synchronized void close() throws IOException {
        delegate.close();
        Logger.getLogger("org.geotools.data.wfs").log(level, buffer.toString());
        buffer=new StringBuffer("Output: ");
    }

    public synchronized void flush() throws IOException {
        delegate.flush();
        Logger.getLogger("org.geotools.data.wfs").log(level, buffer.toString());
        buffer=new StringBuffer("Output: ");
    }

    public synchronized void write( char[] cbuf, int off, int len ) throws IOException {
        char[] msg = new char[len];
        System.arraycopy(cbuf, off, msg, 0, len);
        buffer.append(msg);
        delegate.write(cbuf, off, len);
    }

}