/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;


// Note: javadoc in class and fields descriptions must be XHTML.
/**
 * Copies <code>.jar</code> files in a single directory. Dependencies are copied as well,
 * except if already presents.
 *
 * @goal collect
 * @phase package
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class JarCollector extends AbstractMojo {
    /**
     * The sub directory to create inside the "target" directory.
     */
    private static final String SUB_DIRECTORY = "binaries";

    /**
     * The directory where JARs are to be copied. It should
     * be the "target" directory of the parent {@code pom.xml}.
     */
    private String collectDirectory;

    /**
     * Directory containing the generated JAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * Name of the generated JAR.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String jarName;

    /**
     * Project dependencies.
     *
     * @parameter expression="${project.artifacts}"
     * @required
     */
    private Set<Artifact> dependencies;

    /**
     * The Maven project running this plugin.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The list of JAR names read from {@code "GtJars.txt"} file.
     */
    private final List<String> names = new ArrayList<String>();

    /**
     * Copies the {@code .jar} files to the collect directory.
     *
     * @throws MojoExecutionException if the plugin execution failed.
     */
    public void execute() throws MojoExecutionException {
        /*
         * Gets the parent "target" directory.
         */
        MavenProject parent = project;
        while (parent.hasParent()) {
            parent = parent.getParent();
        }
        collectDirectory = parent.getBuild().getDirectory();
        /*
         * Now collects the JARs.
         */
        try {
            collect();
        } catch (IOException e) {
            throw new MojoExecutionException("Error collecting the JAR file.", e);
        }
    }

    /**
     * Implementation of the {@link #execute} method.
     */
    private void collect() throws MojoExecutionException, IOException {
        /*
         * Make sure that we are collecting the JAR file from a module which produced
         * such file. Some modules use pom packaging, which do not produce any JAR file.
         */
        final File jarFile = new File(outputDirectory, jarName + ".jar");
        if (!jarFile.isFile()) {
            return;
        }
        /*
         * Get the "target" directory of the parent pom.xml and make sure it exists.
         */
        File collect = new File(collectDirectory);
        if (!collect.exists()) {
            if (!collect.mkdir()) {
                throw new MojoExecutionException("Failed to create target directory.");
            }
        }
        if (collect.getCanonicalFile().equals(jarFile.getParentFile().getCanonicalFile())) {
            /*
             * The parent's directory is the same one than this module's directory.
             * In other words, this plugin is not executed from the parent POM. Do
             * not copy anything, since this is not the place where we want to
             * collect the JAR files.
             */
            return;
        }
        /*
         * Creates a "binaries" subdirectory inside the "target" directory.
         */
        collect = new File(collect, SUB_DIRECTORY);
        if (!collect.exists()) {
            if (!collect.mkdir()) {
                throw new MojoExecutionException("Failed to create binaries directory.");
            }
        }
        int count = 1;
        FileUtils.copyFileToDirectory(jarFile, collect);
        if (dependencies != null) {
            fillNamesFromResources();
            for (final Artifact artifact : dependencies) {
                final String scope = artifact.getScope();
                if (scope != null &&  // Maven 2.0.6 bug?
                   (scope.equalsIgnoreCase(Artifact.SCOPE_COMPILE) ||
                    scope.equalsIgnoreCase(Artifact.SCOPE_RUNTIME)))
                {
                    final File file = artifact.getFile();
                    if (artifact.getGroupId().startsWith("org.geotools")) {
                        final String finalName = prefixedName(file);
                        FileUtils.copyFile(file, new File(collect, finalName));
                    } else {
                        final File copy = new File(collect, file.getName());
                        if (!copy.exists()) {
                            /*
                             * Copies the dependency only if it was not already copied. Note that
                             * the module's JAR was copied inconditionnaly above (because it may
                             * be the result of a new compilation). If a Geotools JAR from the
                             * dependencies list changed, it will be copied inconditionnaly when
                             * the module for this JAR will be processed by Maven.
                             */
                            FileUtils.copyFileToDirectory(file, collect);
                            count++;
                        }
                    }
                }
            }
        }
        getLog().info("Copied "+count+" JAR to parent directory.");
    }

    /**
     * Fill the list of jar names with the values found in the resource file.
     *
     * @throws IOException if the reading of the resource file fails.
     */
    private void fillNamesFromResources() throws IOException {
        final InputStream input = getClass().getResourceAsStream("GtJars.txt");
        final BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = buffer.readLine()) != null) {
            names.add(line.trim());
        }
        buffer.close();
    }

    /**
     * Tests a file name with the ones present in the resource file, and returns the
     * shortest matching name in the resource file.
     *
     * @param  file The file to test against the names in the resource file.
     * @return The shortest name with a {@code gt-} prefix, or a generated name
     *         if no matching name was found in the text file.
     */
    private String prefixedName(final File file) {
        final String fileName = file.getName();
        String match = null;
        for (final String candidateName : names) {
            if (candidateName.endsWith(fileName)) {
                if (match == null) {
                    match = candidateName;
                } else {
                    // There is already a name that has matched previously, we just keep
                    // the name which is the shortest between them.
                    if (candidateName.length() < match.length()) {
                        match = candidateName;
                    }
                }
            }
        }
        if (match == null) {
            match = "gt-" + fileName;
        }
        return match;
    }
}
