/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools;

// J2SE dependencies
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;


/**
 * Provides access to the common {@code test-data} directories provided in the
 * {@code sample-data} module. This directory is shared by test suites in other
 * modules.
 * <p>
 * This file has to live in the {@code org.geotools} root package in order to
 * get access to the {@code org/geotools/test-data} directory. If you don't
 * need this directory, then use the {@link org.geotools.resources.TestData}
 * class provided in the {@code org.geotools.resources} directory.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @tutorial http://www.geotools.org/display/GEOT/5.8+Test+Data
 */
public final class TestData extends org.geotools.resources.TestData {
    /**
     * Do not allow instantiation of this class.
     */
    private TestData() {
    }

    /**
     * Access to <code>{@linkplain #getResource getResource}(name)</code> as a non-null
     * {@link URL}. At the difference of {@code getResource}, this method throws an exception if
     * the resource is not found. This provides a more explicit explanation about the failure
     * reason than the infamous {@link NullPointerException}.
     *
     * @param  name Path to file in {@code org/geotools/test-data}.
     * @return The URL to the {@code test-data} resource.
     * @throws FileNotFoundException if the resource is not found.
     */
    public static URL url(final String name) throws FileNotFoundException {
        return url(TestData.class, name);
    }

    /**
     * Access to <code>{@linkplain #getResource getResource}(name)</code> as a non-null
     * {@link File}. You can access the {@code test-data} directory with:
     *
     * <blockquote><pre>
     * TestData.file(null);
     * </pre></blockquote>
     *
     * @param  name Path to file in {@code org/geotools/test-data}.
     * @return The file to the {@code test-data} resource.
     * @throws FileNotFoundException if the file is not found.
     * @throws IOException if the resource can't be fetched for an other reason.
     */
    public static File file(final String name) throws IOException {
        return file(TestData.class, name);
    }

    /**
     * Provides a non-null {@link InputStream} for named test data.
     * It is the caller responsability to close this stream after usage.
     *
     * @param  name Path to file in {@code org/geotools/test-data}.
     * @return The input stream.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     */
    public static InputStream openStream(final String name) throws IOException {
        return openStream(TestData.class, name);
    }

    /**
     * Provides a {@link BufferedReader} for named test data. The buffered reader is provided as
     * an {@link LineNumberReader} instance, which is useful for displaying line numbers where
     * error occur. It is the caller responsability to close this reader after usage.
     *
     * @param  name Path to file in {@code org/geotools/test-data}.
     * @return The buffered reader.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     */
    public static LineNumberReader openReader(final String name) throws IOException {
        return openReader(TestData.class, name);
    }

    /**
     * Provides a channel for named test data. It is the caller responsability to close this
     * chanel after usage.
     *
     * @param  name Path to file in {@code org/geotools/test-data}.
     * @return The chanel.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     */
    public static ReadableByteChannel openChannel(final String name) throws IOException {
        return openChannel(TestData.class, name);
    }

    /**
     * Copies the named resources from the {@code sample-data} module to the {@code test-data}
     * directory in an other module. For example if {@code TestData.copy(this, "foo.txt")} is
     * invoked inside a test suite in the {@code org.geotools.foo} package, then this method
     * copies {@code org/geotools/test-data/foo.txt} (from {@code sample-data} module) to
     * {@code org/geotools/foo/test-data/foo.txt} (in the {@code foo} module).
     * <p>
     * This method is useful when a test case needs to access a resource through a {@link File},
     * for example because it want to open it using {@link java.io.RandomAccess}. Because the
     * resources provided in the {@code sample-data} module are available to other modules as
     * a JAR file, other modules can only access them through an {@link URL} unless they copy
     * them in their own {@code test-data} directory.
     * <p>
     * If the named file already exists in the caller {@code test-data} directory, then this
     * method does nothing. It make it safe to invoke this method many time in a test suite,
     * since this method should not copy the file more than once for a given JVM execution.
     * The file will be {@linkplain File#deleteOnExit deleted on exit}.
     *
     * @param  caller Calling class or object used to locate the destination {@code test-data}.
     * @param  name Path to file in {@code org/geotools/test-data}.
     * @return The file to the <code>org/geotools/<strong>caller-package</strong>/test-data</code>
     *         resource copy, returned for convenience.
     * @throws FileNotFoundException if the file is not found.
     * @throws IOException if the resource can't be fetched for an other reason.
     */
    public static File copy(final Object caller, final String name) throws IOException {
        final File path      = new File(name);
        final File directory = new File(file(caller, null), path.getParent());
        final File file      = new File(directory, path.getName());
        if (!file.exists()) {
            if (directory.mkdirs()) {
                deleteOnExit(directory);
            }
            final InputStream   in = openStream(name);
            final OutputStream out = new FileOutputStream(file);
            final byte[]    buffer = new byte[4096];
            deleteOnExit(file);
            int count;
            while ((count = in.read(buffer)) >= 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
        }
        return file;
    }
}
