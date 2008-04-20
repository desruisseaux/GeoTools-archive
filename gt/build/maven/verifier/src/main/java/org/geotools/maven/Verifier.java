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

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;


// Note: javadoc in class and fields descriptions must be XHTML.
/**
 * Verifies the compliance of <code>pom.xml</code> files with GeoTools conventions.
 * Module name must matches the directory name except for <code>"gt-"</code> prefix.
 * The prefix must be presents only in modules producing JAR files, except the ones
 * used only as Maven plugins.
 *
 * @goal verify
 * @phase verify
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Verifier extends AbstractMojo {
    /**
     * Name of the generated JAR.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String jarName;

    /**
     * The Maven project running this plugin.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Verifies the compliance of current pom.xml with GeoTools conventions.
     *
     * @throws MojoExecutionException if a convention is violated.
     */
    public void execute() throws MojoExecutionException {
        ensureNameConsistency();
    }

    /**
     * Ensures that the project is in a directory of the same name than the artifactId.
     * The "gt-" prefix is expected for modules producing a JAR, except the ones in the
     * "org.geotools.maven" group since they are used for GeoTools build only. We do not
     * prefix fully-qualified artifactId like "org.w3.xlink" neither.
     */
    private void ensureNameConsistency() throws MojoExecutionException {
        String groupId = project.getGroupId();
        checkGroup(groupId);
        final MavenProject parent = project.getParent();
        if (parent == null) {
            // If there is no parent, we are the root "org.geotools" pom.xml.
            // Do not check the directory name since it can be anything.
            return;
        }
        File directory = project.getFile().getParentFile();
        if (!isSvnCheckout(directory)) {
            // If we are not in a SVN checkout, the user may have modified the
            // directory layout has he wish. Do not check.
            return;
        }
        String artifactId = project.getArtifactId();
        String expectedId = directory.getName();
        boolean isJAR = project.getPackaging().equals("jar");
        if (!exclude(artifactId)) {
            if (isJAR && !groupId.startsWith("org.geotools.maven") && artifactId.indexOf('.') < 0) {
                expectedId = "gt-" + expectedId;
            }
            if (!expectedId.equals(artifactId)) {
                throw new MojoExecutionException("Invalid <artifactId> \"" + artifactId +
                        "\". Expected \"" + expectedId + "\" (derived from the directory name).");
            }
        }
        /*
         * Ensures that the project is inheriting from the right parent. For example if the
         * module inherit from "org.geotools.library", then we requires the module to lives
         * as a subdirectory of "libraries" parent directory. This condition doesn't apply
         */
        groupId = parent.getGroupId();
        checkGroup(groupId);
        artifactId = parent.getArtifactId();
        if (!exclude(artifactId)) do {
            directory = directory.getParentFile();
            if (directory == null) {
                throw new MojoExecutionException("Inheriting from \"" + artifactId +
                        "\" but not a \"" + artifactId + "\" subdirectory.");
            }
            if (!isSvnCheckout(directory)) {
                // Same reason than above.
                return;
            }
            expectedId = directory.getName();
        } while (!artifactId.equals(expectedId));
        // We have found the proper directory, so we are done.
    }

    /**
     * Ensures that the given group ID is a geotools one.
     */
    private static void checkGroup(final String groupId) throws MojoExecutionException {
        if (!groupId.equals("org.geotools") && !groupId.startsWith("org.geotools.")) {
            throw new MojoExecutionException("Invalid <groupId>: \"" + groupId +
                    "\". It must be \"org.geotools\" or a submodule.");
        }
    }

    /**
     * Returns {@code true} if the given directory has been obtained by a SVN checkout.
     * The checks for directory names will be disabled otherwise.
     */
    private static boolean isSvnCheckout(File directory) {
        directory = directory.getParentFile();
        if (directory == null) {
            return false;
        }
        return new File(directory, ".svn").isDirectory();
    }

    /**
     * Returns {@code true} if the given artifactId should be excluded from the check.
     */
    private static boolean exclude(final String artifactId) {
        return artifactId.equals("arcsde-plugin") ||
               artifactId.equals("gt-arcsde") ||
               artifactId.equals("gt-xsd-wfs") ||
               artifactId.equals("geotools"); // Always excluded from directory check.
    }
}
