/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.maven;

// J2SE dependencies
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Maven and Plexus dependencies
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;


/**
 * Creates a {@code .uno.pkg} package for OpenOffice addins.
 * 
 * @goal unopkg
 * @phase package
 * @execute phase="package:jar"
 * @description Creates a uno.pkg package for OpenOffice addins.
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo We need to copy the {@code .jar} dependencies as well (e.g.
 *       {@code epsg-hsql-2.2-SNAPSHOT.jar}). This is hard to do without Mojo javadoc.
 *       We hope to improve this plugin once the Mojo javadoc is published.
 */
public class UnoPkg extends AbstractMojo implements FilenameFilter {
    /**
     * Directory where the source files are located. The plugin will looks for
     * the {@code META-INF/manifest.xml} file in this directory.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private String sourceDirectory;

    /**
     * Directory where the output uno.pkg file will be located.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * The name for the uno.pkg file to create.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * The Maven project running this plugin.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   directory the directory in which the file was found.
     * @param   name      the name of the file.
     */
    public boolean accept(final File directory, final String name) {
        return name.endsWith(".jar") || name.endsWith(".JAR");
    }

    /**
     * Generates the ZIP file from all {@code .jar} files found in the target directory.
     *
     * @throws MojoExecutionException if the plugin execution failed.
     */
    public void execute() throws MojoExecutionException {
        try {
            createPackage();
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating the uno.pkg file.", e);
        }
    }

    /**
     * Implementation of the {@link #execute} method.
     */
    private void createPackage() throws IOException {
        final String  manifestName = "META-INF/manifest.xml";
        final File outputDirectory = new File(this.outputDirectory);
        final File         zipFile = new File(outputDirectory, finalName + ".uno.pkg");
        final File    manifestFile = new File(sourceDirectory, manifestName);
        final File[]          jars = outputDirectory.listFiles(this);
        final ZipOutputStream  out = new ZipOutputStream(new FileOutputStream(zipFile));
        if (manifestFile.isFile()) {
            final ZipEntry entry = new ZipEntry(manifestName);
            out.putNextEntry(entry);
            copy(manifestFile, out);
            out.closeEntry();
        }
        for (int i=0; i<jars.length; i++) {
            final File jar = jars[i];
            final ZipEntry entry = new ZipEntry(jar.getName());
            out.putNextEntry(entry);
            copy(jar, out);
            out.closeEntry();
        }
        out.close();
    }

    /**
     * Copies the content of the specified file to the specified output stream.
     */
    private static void copy(final File file, final OutputStream out) throws IOException {
        final InputStream in = new FileInputStream(file);
        final byte[] buffer = new byte[64*1024];
        int length;
        while ((length = in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
        }
        in.close();
    }
}
