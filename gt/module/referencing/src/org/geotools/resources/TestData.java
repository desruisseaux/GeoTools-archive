/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.resources;

// J2SE dependencies
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Provides access to {@code test-data} directories associated with JUnit tests.
 * <p>
 * We have chosen "{@code test-data}" to follow the javadoc "{@code doc-files}" convention
 * of ensuring that data directories don't look anything like normal java packages.
 * </p>
 * <p>
 * Example:
 * <pre>
 * class MyClass {
 *     public void example() {
 *         Image testImage = new ImageIcon(TestData.getResource(this, "test.png")).getImage();
 *         Reader reader = TestData.getReader(this, "script.xml");
 *     }
 * }
 * </pre>
 * Where:
 * <ul>
 *   <li>{@code MyClass.java}<li>
 *   <li>{@code test-data/test.png}</li>
 *   <li>{@code test-data/script.xml}</li>
 * </ul>
 * </p>
 * <p>
 * By convention you should try and locate {@code test-data} near the JUnit test
 * cases that uses it.
 * </p>
 *
 * @since 2.0
 * @version $Id$
 * @author James McGill
 * @author Simone Giannecchiin (simboss)
 * @author Martin Desruisseaux
 *
 * @todo It should be possible to move this class in the {@code sample-data} module.
 */
public final class TestData implements Runnable {
    /**
     * The test data directory.
     */
    private static final String DIRECTORY = "test-data";

    /**
     * Encoding of URL path.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * The file to deletes at shutdown time. {@link File#deleteOnExit} alone doesn't seem
     * suffisient since it will preserve any overwritten files.
     */
    private static final List toDelete = new LinkedList();

    /**
     * Register the thread to be automatically executed at shutdown time.
     * This thread will delete all temporary files registered in {@link #toDelete}.
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new TestData(), "Test data cleaner"));
    }

    /**
     * Do not allow instantiation of this class.
     */
    private TestData() {
    }

    /**
     * Locates named test-data resource for caller. <strong>Note:</strong> Consider using the
     * <code>{@link #url url}(caller, name)</code> method instead if the resource should always
     * exists.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  name resource name in {@code test-data} directory.
     * @return URL or {@code null} if the named test-data could not be found.
     *
     * @see #url
     */
    public static URL getResource(final Object caller, String name) {
        if (name == null || (name=name.trim()).length() == 0) {
            name = DIRECTORY;
        } else {
            name = DIRECTORY + '/' + name;
        }
        if (caller != null) {
            final Class c = (caller instanceof Class) ? (Class) caller : caller.getClass();
            return c.getResource(name);
        } else {
            return Thread.currentThread().getContextClassLoader().getResource(name);
        }
    }

    /**
     * Access to <code>{@linkplain #getResource getResource}(caller, path)</code> as a non-null
     * {@link URL}. At the difference of {@code getResource}, this method throws an exception if
     * the resource is not found. This provides a more explicit explanation about the failure
     * reason than the infamous {@link NullPointerException}.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  path Path to file in {@code test-data}.
     * @return The URL to the {@code test-data} resource.
     * @throws FileNotFoundException if the resource is not found.
     *
     * @since 2.2
     */
    public static URL url(final Object caller, final String path) throws FileNotFoundException {
        final URL url = getResource(caller, path);
        if (url == null) {
            throw new FileNotFoundException("Could not locate test-data: " + path);
        }
        return url;
    }

    /**
     * Access to <code>{@linkplain #getResource getResource}(caller, path)</code> as a non-null
     * {@link File}. You can access the {@code test-data} directory with:
     *
     * <blockquote><pre>
     * TestData.file(MyClass.class, null);
     * </pre></blockquote>
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  path Path to file in {@code test-data}.
     * @return The file to the {@code test-data} resource.
     * @throws FileNotFoundException if the file is not found.
     * @throws IOException if the resource can't be fetched for an other reason.
     */
    public static File file(final Object caller, final String path) throws IOException {
        final URL url = url(caller, path);
        final File file = new File(URLDecoder.decode(url.getPath(), ENCODING));
        if (!file.exists()) {
            throw new FileNotFoundException("Could not locate test-data: " + path);
        }
        return file;
    }

    /**
     * Creates a temporary file with the given name. The file will be created in the
     * {@code test-data} directory and will be deleted on exit.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  A base name for the temporary file.
     * @return The temporary file in the {@code test-data} directory.
     * @throws IOException if the file can't be created.
     */
    public static File temp(final Object caller, final String name) throws IOException {
        final File testData = file(caller, null);
        final int split = name.lastIndexOf('.');
        final String prefix = (split < 0) ? name : name.substring(0,split);
        final String suffix = (split < 0) ? null : name.substring(split+1);
        final File tmp = File.createTempFile(prefix, '.'+suffix, testData);
        deleteOnExit(tmp);
        return tmp;
    }

    /**
     * Provides a non-null {@link InputStream} for named test data.
     * It is the caller responsability to close this stream after usage.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  name of test data to load.
     * @return The input stream.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     *
     * @since 2.2
     */
    public static InputStream openStream(final Object caller, final String name)
            throws IOException
    {
        return new BufferedInputStream(url(caller, name).openStream());
    }

    /**
     * Provides a {@link BufferedReader} for named test data. The buffered reader is provided as
     * an {@link LineNumberReader} instance, which is useful for displaying line numbers where
     * error occur. It is the caller responsability to close this reader after usage.
     *
     * @param  caller The class of the object associated with named data.
     * @param  name of test data to load.
     * @return The buffered reader.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     *
     * @since 2.2
     */
    public static LineNumberReader openReader(final Object caller, final String name)
            throws IOException
    {
        return new LineNumberReader(new InputStreamReader(url(caller, name).openStream()));
    }

    /**
     * Provides a {@link java.io.BufferedReader} for named test data.
     * It is the caller responsability to close this reader after usage.
     *
     * @param  caller The class of the object associated with named data.
     * @param  name of test data to load.
     * @return The reader, or {@code null} if the named test data are not found.
     * @throws IOException if an error occurs during an input operation.
     *
     * @deprecated Use {@link #openReader} instead. The {@code openReader} method throws an
     *  exception if the resource is not found, instead of returning null. This make debugging
     *  easier, since it replaces infamous {@link NullPointerException} by a more explicit error
     *  message during tests. Furthermore, the {@code openReader} name make it more obvious that
     *  the stream is not closed automatically and is also consistent with other method names in
     *  this class.
     */
    public static BufferedReader getReader(final Object caller, final String name)
            throws IOException
    {
        final URL url = getResource(caller, name);
        if (url == null) {
            return null; // echo handling of getResource( ... )
        }
        return new BufferedReader(new InputStreamReader(url.openStream()));
    }

    /**
     * Provides a channel for named test data. It is the caller responsability to close this
     * chanel after usage.
     *
     * @param  caller The class of the object associated with named data.
     * @param  name of test data to load.
     * @return The chanel.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     *
     * @since 2.2
     */
    public static ReadableByteChannel openChannel(final Object caller, final String name) throws IOException {
        final URL url = url(caller, name);
        final File file = new File(URLDecoder.decode(url.getPath(), ENCODING));
        if (file.exists()) {
            return new RandomAccessFile(file, "r").getChannel();
        }
        return Channels.newChannel(url.openStream());
    }

    /**
     * Unzip a file in the {@code test-data} directory. The zip file content is inflated in place,
     * i.e. are inflated files are written in the same {@code test-data} directory. If a file to be
     * inflated already exists in the {@code test-data} directory, then the existing file left
     * untouched and the corresponding ZIP entry is silently skipped. This approach avoid the
     * overhead of inflating the same files many time if this {@code unzipFile} method is invoked
     * before every tests.
     * <p>
     * All inflated files will be automatically {@linkplain File#deleteOnExit deleted on exit}.
     * Callers don't need to worry about cleanup.
     *
     * @param  caller The class of the object associated with named data.
     * @param  name The file name to unzip in place.
     * @throws FileNotFoundException if the specified zip file is not found.
     * @throws IOException if an error occurs during an input or output operation.
     *
     * @since 2.2
     */
    public static void unzipFile(final Object caller, final String name) throws IOException {
        final File        file    = file(caller, name);
        final File        parent  = file.getParentFile().getAbsoluteFile();
        final ZipFile     zipFile = new ZipFile(file);
        final Enumeration entries = zipFile.entries();
        final byte[]      buffer  = new byte[4096];
        while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            final File path = new File(parent, entry.getName());
            if (path.exists()) {
                continue;
            }
            final File directory = path.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            // Copy the file. Note: no need for a BufferedOutputStream,
            // since we are already using a buffer of type byte[4096].
            final InputStream  in  = zipFile.getInputStream(entry);
            final OutputStream out = new FileOutputStream(path);
            deleteOnExit(path);
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
        }
        zipFile.close();
    }

    /**
     * Adds a file to delete on exit.
     */
    private static void deleteOnExit(final File file) {
        file.deleteOnExit();
        synchronized (toDelete) {
            toDelete.add(file);
        }
    }

    /**
     * Deletes all temporary files. This method is invoked automatically at shutdown time and
     * should not be invoked directly. It is public only as an implementation side effect.
     */
    public void run() {
        int iteration = 5; // Maximum number of iterations
        synchronized (toDelete) {
            while (!toDelete.isEmpty()) {
                if (--iteration < 0) {
                    break;
                }
                /*
                 * Before to try to delete the files, invokes the finalizers in a hope to close
                 * any input streams that the user didn't explicitly closed. Leaving streams open
                 * seems to occurs way too often in our test suite...
                 */
                System.gc();
                System.runFinalization();
                for (final Iterator it=toDelete.iterator(); it.hasNext();) {
                    final File f = (File) it.next();
                    try {
                        if (f.delete()) {
                            it.remove();
                            continue;
                        }
                    } catch (SecurityException e) {
                        if (iteration == 0) {
                            System.err.print(Utilities.getShortClassName(e));
                            System.err.print(": ");
                        }
                    }
                    // Can't use logging, since logger are not available anymore at shutdown time.
                    if (iteration == 0) {
                        System.err.print("Can't delete ");
                        System.err.println(f);
                    }
                }
            }
        }
    }
}
