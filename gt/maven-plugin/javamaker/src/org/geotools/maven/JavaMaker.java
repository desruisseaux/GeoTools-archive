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
import java.io.IOException;

// Maven and Plexus dependencies
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;


/**
 * Compiles Java interfaces from OpenOffice IDL files. In an ideal world, this plugin should
 * executes {@code idlc} on the {@code *.idl} files, then {@code regmerge} on the generated
 * {@code *.urd} files, then {@code javamaker} on the generated {@code *.rdb} files. However,
 * since the above mentioned tools are native and would require a manual installation on all
 * developers machine, current version just copies a pre-compiled class file. This copy must
 * occurs after the compilation phase (in order to overwrite the files generated by {@code javac}),
 * which is why the usual resources mechanism doesn't fit.
 *
 * @goal generate
 * @phase process-classes
 * @description Copies .class files generated from OpenOffice IDL
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo A future version should executes {@code idlc}, {@code regmerge} and {@code javamaker}
 *       from an OpenOffice SDK installation, and fallback on current behavior (just copy files)
 *       if no OpenOffice SDK is found.
 */
public class JavaMaker extends AbstractMojo {
    /**
     * Directory where the .class file(s) are located.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private String sourceDirectory;

    /**
     * Directory where the output Java files will be located.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private String outputDirectory;

    /**
     * Copies the {@code .class} files generated by OpenOffice.
     *
     * @throws MojoExecutionException if the plugin execution failed.
     */
    public void execute() throws MojoExecutionException {
        final int n;
        try {
            n = copyClasses(new File(sourceDirectory), new File(outputDirectory));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy *.class files.", e);
        }
        getLog().info("Copied "+n+" pre-compiled class files");
    }

    /**
     * Copies {@code *.class} files from source directory to output directory. The output
     * directory structure should already exists. It should be the case if all sources files
     * have been compiled before this method is invoked.
     *
     * @return The number of files copied.
     */
    private static int copyClasses(final File sourceDirectory,
                                   final File outputDirectory) throws IOException
    {
        int n = 0;
        final String[] filenames = sourceDirectory.list();
        for (int i=0; i<filenames.length; i++) {
            final String filename = filenames[i];
            final File file = new File(sourceDirectory, filename);
            if (file.isFile()) {
                if (filename.endsWith(".class") || filename.endsWith(".CLASS")) {
                    FileUtils.copyFileToDirectory(file, outputDirectory);
                    n++;
                }
            } else if (file.isDirectory()) {
                n += copyClasses(file, new File(outputDirectory, filename));
            }
        }
        return n;
    }
}
