/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.resources;

// J2SE dependencies
import java.nio.ByteBuffer;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Utility class for managing memory mapped buffers. 
 * 
 * @version $Id$
 * @author Andres Aimes
 */
public class NIOUtilities {
    /**
     * <code>true</code> if a warning has already been logged.
     */
    private static boolean warned = false;

    /**
     * Do not allows instantiation of this class.
     *
     * @task TODO: This constructor will become private when <code>NIOBufferUtils</code>
     *             will have been removed.
     */
    protected NIOUtilities() {
    }

    /**
     * Really closes a <code>MappedByteBuffer</code> without the need to wait for garbage
     * collection. Any problems with closing a buffer on Windows (the problem child in this
     * case) will be logged as <code>SEVERE</code> to the logger of the package name. To
     * force logging of errors, set the System property "org.geotools.io.debugBuffer" to "true".
     *
     * @param  buffer The buffer to close.
     * @return true if the operation was successful, false otherwise.
     *
     * @see java.nio.MappedByteBuffer
     */
    public static boolean clean(final ByteBuffer buffer) {
        if (buffer == null || ! buffer.isDirect() ) {
            return false;
        }
        Boolean b = (Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Boolean success = Boolean.FALSE;
                try {
                    Method getCleanerMethod = buffer.getClass().getMethod("cleaner", null);
                    getCleanerMethod.setAccessible(true);
                    Object cleaner = getCleanerMethod.invoke(buffer,  null);
                    Method clean = cleaner.getClass().getMethod("clean", null);
                    clean.invoke(cleaner, null);
                    success = Boolean.TRUE;
                } catch (Exception e) {
                    // This really is a show stopper on windows
                    if (isLoggable()) {
                        log(e, buffer);
                    }
                }
                return success;
            }
        });

        return b.booleanValue();
    }

    /**
     * Check if a warning message should be logged.
     */
    private static synchronized boolean isLoggable() {
        return !warned && (
                System.getProperty("org.geotools.io.debugBuffer", "false").equalsIgnoreCase("true") ||
                System.getProperty("os.name").indexOf("Windows") >= 0 );
    }

    /**
     * Log a warning message.
     */
    private static synchronized void log(final Exception e, final ByteBuffer buffer) {
        warned = true;
        String message = "Error attempting to close a mapped byte buffer : " + buffer.getClass().getName()
                       + "\n JVM : " + System.getProperty("java.version")
                       + ' '         + System.getProperty("java.vendor");
        Logger.getLogger("org.geotools.io").log(Level.SEVERE, message, e);
    }
}
