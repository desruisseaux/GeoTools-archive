/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A decorator that logs the Input to the Logger as input is accessed.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class LogInputStream extends InputStream {
    private InputStream delegate;
    private Level level;

    StringBuffer buffer=new StringBuffer("Input: "); 
    
    public LogInputStream( InputStream in, Level logLevel ) {
        this.delegate=in;
        this.level=logLevel;
    }

    public void close() throws IOException {
        delegate.close();
        Logger.getLogger("org.geotools.data.wfs").log(level, buffer.toString());
        buffer=new StringBuffer("Input: ");
    }

    public int read() throws IOException {
        int result=delegate.read();
        buffer.append((char) result); 
        return result;
    }


}
