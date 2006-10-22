/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.maven;

// J2SE dependencies
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Maven and Plexus dependencies
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;


// Note: javadoc in class and fields descriptions must be XHTML.
/**
 * Creates a <code>.uno.pkg</code> package for <a href="http://www.openoffice.org">OpenOffice</a>
 * addins.
 * 
 * @goal unopkg
 * @phase package
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class UnoPkg extends AbstractMojo implements FilenameFilter {
    /**
     * Directory where the source files are located. The plugin will looks for the
     * <code>META-INF/manifest.xml</code> and <code>*.rdb</code> files in this directory.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private String sourceDirectory;

    /**
     * Directory where the output <code>uno.pkg</code> file will be located.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * The name for the <code>uno.pkg</code> file to create.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * Project dependencies.
     *
     * @parameter expression="${project.artifacts}"
     * @required
     */
    private Set/*<Artifact>*/ dependencies;

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
        return name.endsWith(".jar") || name.endsWith(".JAR") ||
               name.endsWith(".rdb") || name.endsWith(".RDB");
    }

    /**
     * Generates the {@code .uno.pkg} file from all {@code .jar} files found in the target directory.
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
     * Creates the {@code .uno.pkg} file.
     */
    private void createPackage() throws IOException {
        final String  manifestName = "META-INF/manifest.xml";
        final File outputDirectory = new File(this.outputDirectory);
        final File         zipFile = new File(outputDirectory, finalName + ".uno.pkg");
        final File    manifestFile = new File(sourceDirectory, manifestName);
        final File[]          jars = outputDirectory.listFiles(this);
        final File[]          rdbs = new File(sourceDirectory).listFiles(this);
        final ZipOutputStream  out = new ZipOutputStream(new FileOutputStream(zipFile));
        if (manifestFile.isFile()) {
            copy(manifestFile, out, manifestName);
        }
        /*
         * Copies the RDB files.
         */
        for (int i=0; i<rdbs.length; i++) {
            copy(rdbs[i], out, null);
        }
        /*
         * Copies the JAR (and any additional JARs provided in the output directory).
         */
        for (int i=0; i<jars.length; i++) {
            copy(jars[i], out, null);
        }
        /*
         * Copies the dependencies.
         */
        if (dependencies != null) {
            for (final Iterator it=dependencies.iterator(); it.hasNext();) {
                final Artifact artifact = (Artifact) it.next();
                final String scope = artifact.getScope();
                if (scope.equalsIgnoreCase(Artifact.SCOPE_COMPILE) ||
                    scope.equalsIgnoreCase(Artifact.SCOPE_RUNTIME))
                {
                    copy(artifact.getFile(), out, null);
                }
            }
        }
        out.close();
    }

    /**
     * Copies the content of the specified file to the specified output stream.
     */
    private static void copy(final File file, final ZipOutputStream out, String name)
            throws IOException
    {
        if (name == null) {
            name = file.getName();
        }
        final ZipEntry entry = new ZipEntry(name);
        out.putNextEntry(entry);
        final InputStream in = new FileInputStream(file);
        final byte[] buffer = new byte[64*1024];
        int length;
        while ((length = in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.closeEntry();
    }
}
