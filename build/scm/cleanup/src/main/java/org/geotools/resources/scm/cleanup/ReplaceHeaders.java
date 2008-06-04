/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.resources.scm.cleanup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.console.CommandLine;
import org.geotools.console.Option;


/**
 * Adapts the header of a file with the {@code OSGeo} format.<br/>
 * According to the changes done, a file can have different status :
 * <ul>
 *   <li>Skipped : if no changes are done on this file. It could happens if the file does not
 *                 contain any header, or if the file is already well formated.</li>
 *   <li>Suspicious : if too many changes are done on this file, it is classed as suspicious,
 *                    and the user should have a look to see the proposal changes.</li>
 *   <li>Copyright problems : if a copyright red is not present in the list of copyright that
 *                            we know what to do with.</li>
 *   <li>Changed correctly : if all changes seem correct.</li>
 * </ul>
 *
 * HOW TO USE THIS FILE:
 *
 * 0) cd root of checkout (trunk/)
 *
 * 1) compile (mvn clean install)
 *
 * 2) cp build/scm/cleanup/target/cleanup-2.5-SNAPSHOT.jar target/binaries/.
 *
 * 3) java -jar target/binaries/cleanup-2.5-SNAPSHOT.jar \
 *        org.geotools.resources.scm.cleanup.ReplaceHeaders -info -input "path/to/dir"
 *    where the path must be in quotes. For example, use "modules/library/metadata"
 *
 * The options to run are:<br/>
 * REQUIRED
 * <ul>
 *   <li>-input "dir-or-file" blah blah WARNING will clobber files in place</li>
 * </ul>
 * OPTIONAL
 * <ul>
 *   <li>-help   --- gives usage</li>
 *   <li>-info   --- run in information only mode; will not write any file</li>
 *   <li>-output "path/to/existing/folder-or-file" will recreate a file tree of modified files</li>
 * </ul>
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public final class ReplaceHeaders extends CommandLine {

    private final static Logger LOGGER = Logger.getLogger("org.geotools.resources.scm.cleanup");

    private final String FIRST_LINE = "GeoTools - The Open Source Java GIS Tookit";
    private final String OSGEO      = "Open Source Geospatial Foundation (OSGeo)";

    private static final int CURRENT_YEAR = 2008;

    /**
     * Files that do not contain any change. They probably should be handled
     * by hand.
     */
    private final List<String> unchangedFiles = new ArrayList<String>();

    /**
     * Files which need to be verified, because they contain lots of changed.
     */
    private final List<String> suspiciousFiles = new ArrayList<String>();

    /**
     * Files which have copyright problems. This could happen to a file that has a
     * copyright not listed in the {@link #COPYRIGHTS_FOR_MARTIN} map.
     */
    private final List<String> copyrightProblemsFiles = new ArrayList<String>();

    /**
     * Files which are correctly changed by this process.
     */
    private final List<String> correctlyChangedFiles = new ArrayList<String>();

    /**
     * Mapping between copyrights full text and the abbreviation to put in a
     * {@code @author Martin Desruisseaux} annotation.
     */
    private static final Map<String,String> COPYRIGHTS_FOR_MARTIN =
            new HashMap<String,String>(3);
    static {
        COPYRIGHTS_FOR_MARTIN.put("Pêches et Océans Canada", "PMO");
        COPYRIGHTS_FOR_MARTIN.put("Fisheries and Oceans Canada", "PMO");
        COPYRIGHTS_FOR_MARTIN.put("Institut de Recherche pour le Développement", "IRD");
    }

    /**
     * A set of copyrights that the process can suppress, because we know what to do
     * with them, and they can be suppressed.
     */
    private static final Set<String> RECOGNIZED_COPYRIGHTS = new HashSet<String>(10);
    static {
        RECOGNIZED_COPYRIGHTS.add("Geotools Project Managment Committee (PMC)");
        RECOGNIZED_COPYRIGHTS.add("GeoTools Project Managment Committee (PMC)");
        RECOGNIZED_COPYRIGHTS.add("Geotools Project Management Committee (PMC)");
        RECOGNIZED_COPYRIGHTS.add("GeoTools Project Management Committee (PMC)");
        RECOGNIZED_COPYRIGHTS.add("Centre for Computational Geography");
        RECOGNIZED_COPYRIGHTS.add("Pêches et Océans Canada");
        RECOGNIZED_COPYRIGHTS.add("Fisheries and Oceans Canada");
        RECOGNIZED_COPYRIGHTS.add("Institut de Recherche pour le Développement");
        RECOGNIZED_COPYRIGHTS.add("Geomatys");
        RECOGNIZED_COPYRIGHTS.add("Open Source Geospatial Foundation (OSGeo)");
        /*RECOGNIZED_COPYRIGHTS.add("Frank Warmerdam");
        RECOGNIZED_COPYRIGHTS.add("Gerald Evenden");*/
    }

    /**
     * Number of files that have some changes in their headers.
     */
    private int numFilesChanged = 0;

    /**
     * Command-line option for the input file or directory to manage.
     */
    @Option(description="Input file or directory.", mandatory=true)
    private String input;

    /**
     * Command-line option for the output file or directory.
     */
    @Option(description="Output file or directory.")
    private String output;

    /**
     * Command-line option to define whether the script is launched in information mode
     * (read-only) or in writing mode.
     */
    @Option(description="Only displays information about changes (write nothing).")
    private boolean info;


    /**
     * Replace existing header of the Geotools source code with the {@code OSGeo}
     * copyright.
     *
     * @param args The command line arguments. Should contain a {@code -input} value, at
     *             least.
     * @throws FileNotFoundException if the input/output files do not exist.
     * @throws IOException
     */
    public ReplaceHeaders(final String[] args) throws FileNotFoundException, IOException {
        super(args);
        final File in = new File(input);
        if (!in.exists()) {
            throw new FileNotFoundException("Input file does not exists.");
        }
        if (info) {
            System.out.println("xxxxxx  INFORMATION MODE  xxxxxx");
            System.out.println(" /!\\ Nothing will be written /!\\ \n");
        } else {
            System.out.println("xxxxxx  WRITING MODE  xxxxxx\n");
            System.out.println("File(s) changed :");
        }
        if (in.isDirectory()) {
            final FileFilter javaFilter = new FileFilter() {
                public boolean accept(File arg0) {
                    return arg0.getName().endsWith(".java") ||
                           arg0.isDirectory();
                }
            };
            browseFiles(in, javaFilter);
        } else {
            fixHeaders(in, (output == null) ? in : new File(output));
        }
        writeSummary();
    }

    /**
     * Recursively browse all files in a directory, and launch the
     * {@link #fixHeaders(java.io.File, java.io.File)} process.
     *
     * @param root Root directory where to begin the process.
     * @param filter A filter to extract only the chosen files. Here selects only
     *               java files.
     * @throws FileNotFoundException if the input/output files do not exist.
     * @throws IOException
     */
    private void browseFiles(final File root, final FileFilter filter)
            throws FileNotFoundException, IOException
    {
        for (final File candidate : root.listFiles(filter)) {
            if (candidate.isDirectory()) {
                browseFiles(candidate, filter);
            } else {
                fixHeaders(candidate,
                    (output == null) ? candidate : new File(output));
            }
        }
    }

    /**
     * Corrects the header of an input file, and returns the output file that will contain
     * the result.
     *
     * @param input  The input file to read.
     * @param output The output file where to write the result, or {@code null} to
     *               overwrite same files.
     * @throws FileNotFoundException if the input/output files do not exist.
     * @throws IOException
     */
    private void fixHeaders(final File input, final File output) throws FileNotFoundException,
            IOException
    {
        final InputStreamReader inputStream = new InputStreamReader(new FileInputStream(input));
        final BufferedReader reader         = new BufferedReader(inputStream);
        final StringBuilder textIn          = new StringBuilder();
        final StringBuilder textOut         = new StringBuilder();
        // Fix a default initial value, which should be greater than copyright dates found in
        // the java class header. The current year is then chosen.
        int startCopyright = CURRENT_YEAR;
        int linesDeleted = 0, linesChanged = 0, linesWithCopyright = 0;
        try {
            String line;
            // Defines whether the (c) value has been
            boolean hasCopyright = false;
            final Map<String,Integer> copyrightsRed = new HashMap<String,Integer>();
            final Set<String> unknowCopyrights = new HashSet<String>();

            final String geotoolsOld = "GeoTools - OpenSource mapping toolkit";


            /* *****************************************************************
             * Reading part
             */
            while ((line = reader.readLine()) != null) {
                textIn.append(line).append("\n");
                if (line.contains(geotoolsOld)) {
                    linesChanged++;
                    textOut.append(line.replaceAll(geotoolsOld, FIRST_LINE)).append("\n");
                    continue;
                }
                // Lines like " *    (C) 2005"
                if (line.matches("(\\s)+\\*(\\s)+\\([cC]\\)(\\s)+[0-9]{4}(.)*")) {
                    final String copyrightName = getCopyrightFullText(line);
                    if (!RECOGNIZED_COPYRIGHTS.contains(copyrightName)) {
                        textOut.append(line).append("\n");
                        unknowCopyrights.add(copyrightName);
                    }
                    linesWithCopyright++;
                    hasCopyright = true;
                    copyrightsRed.put(copyrightName, getCopyrigthStartTime(line));
                    continue;
                }
                if (hasCopyright) {
                    textOut.append(" *    (C) ");
                    startCopyright = Collections.min(copyrightsRed.values());
                    if (startCopyright < CURRENT_YEAR) {
                        textOut.append(startCopyright).append("-");
                    }
                    textOut.append(CURRENT_YEAR).append(", ").append(OSGEO).append("\n");
                    hasCopyright = false;
                    linesDeleted = linesWithCopyright - 1;
                    linesChanged++;
                    continue;
                }

                ////////////////////////////////////////////////////////
                // Block specific for Martin's @author tagline
                // Tests whether there is a copyright defined in the header,
                // for which Martin wants to add to the {@code author} annotation.
                final String authorMartin = "@author Martin Desruisseaux";
                if (line.contains(authorMartin) && !copyrightsRed.isEmpty()) {
                    String copyrightReplacement = "";
                    for (String foundC : copyrightsRed.keySet()) {
                        if (COPYRIGHTS_FOR_MARTIN.get(foundC) != null) {
                            if (copyrightReplacement.equals("IRD") && COPYRIGHTS_FOR_MARTIN.get(foundC).equals("PMO")) {
                                copyrightReplacement = "PMO, IRD";
                            } else {
                                if (!copyrightReplacement.equals("")) {
                                    copyrightReplacement += ", " + COPYRIGHTS_FOR_MARTIN.get(foundC);
                                } else {
                                    copyrightReplacement = COPYRIGHTS_FOR_MARTIN.get(foundC);
                                }
                            }
                        }
                    }
                    // Tests whether a copyright requires to add something to Martin's name.
                    if (!copyrightReplacement.equals("")) {
                        textOut.append(line.replaceAll(authorMartin,
                                authorMartin + " (" + copyrightReplacement) + ")").append("\n");
                        linesChanged++;
                        continue;
                    }
                }
                //
                textOut.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            /* *****************************************************************
             * Analysis part
             */
            if (textIn.toString().contentEquals(textOut.toString())) {
                unchangedFiles.add(input.getAbsolutePath());
                return;
            }
            // Specify the status of the current file by putting it in the matching list.
            numFilesChanged++;
            if (!unknowCopyrights.isEmpty()) {
                if (!info) {
                    System.out.println("/!\\ Copyright problems /!\\ ==> " + input.getAbsolutePath());
                    System.out.println("\t\t\t\t|__\tLines deleted : " + linesDeleted +
                            "\tLines changed : " + linesChanged);
                }
                for (String unknownCopyright : unknowCopyrights) {
                    copyrightProblemsFiles.add(input.getAbsolutePath());
                    if (!info) {
                        System.out.println("\t\t\t\tUnknown copyright \"" + unknownCopyright +
                                "\". You should handle it by hand.");
                    }
                }
            }
            // If too many changes are done on a file, it is considered as suspect, which means the user
            // should have a look to this file to verify that all proposal changes are rigth.
            if (linesChanged > 4 || linesDeleted > 2) {
                if (!info) {
                    System.out.println("???     Suspicious     ??? ==> " + input.getAbsolutePath());
                    System.out.println("\t\t\t\t|__\tLines deleted : " + linesDeleted +
                            "\tLines changed : " + linesChanged);
                }
                suspiciousFiles.add(input.getAbsolutePath());
            } else {
                if (!info) {
                    System.out.println("||| Changed correctly  ||| ==> " + input.getAbsolutePath() +
                            "\tLines deleted : " + linesDeleted + "\tLines changed : " + linesChanged);
                }
                correctlyChangedFiles.add(input.getAbsolutePath());
            }

            /* *****************************************************************
             * Writing part (only if the script is launched in the writing mode)
             */
            if (!info) {
                final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(output));
                final BufferedWriter buffer = new BufferedWriter(writer);
                buffer.append(textOut.toString());
                buffer.close();
            }
        } finally {
            reader.close();
            inputStream.close();
        }
    }

    /**
     * Returns the full text of a line containing a copyright, without the dates
     * information, or {@code null} if the line specified does not contain any date
     * (and so is not a copyright line).
     *
     * @param line A line which contains a copyright.
     */
    private static String getCopyrightFullText(final String line) {
        final String[] lineSplittedOnNumbers = line.split("[0-9]{4}(,)?");
        return (lineSplittedOnNumbers.length > 0) ?
            lineSplittedOnNumbers[lineSplittedOnNumbers.length - 1].trim() :
            null;
    }

    /**
     * Returns the start time of a copyright contained in a line, or {@code -1} if
     * the line given does not contain any copyright information.
     *
     * @param line A line which contains a copyright.
     */
    private static int getCopyrigthStartTime(final String line) {
        final String[] lineSplittedOnSpace = line.split("\\W");
        int i = 0;
        while (i < lineSplittedOnSpace.length && !lineSplittedOnSpace[i].matches("[0-9]{4}")) {
            i++;
        }
        if (i == lineSplittedOnSpace.length) {
            // Should never happened. Copyrights always have date information.
            return -1;
        }
        return Integer.valueOf(lineSplittedOnSpace[i]);
    }

    /**
     * Write a summary of what have been done in the writing mode, or what it will do in the
     * information mode.
     */
    private void writeSummary() {
        System.out.println("===========================================================");
        System.out.println("========                 Summary                   ========");
        System.out.println("===========================================================");
        if (numFilesChanged == 0) {
            System.out.println("= No file changed");
        } else {
            System.out.println("= " + numFilesChanged + " file(s) with changes");

            System.out.println("=\t+ " + suspiciousFiles.size() + " suspicious file(s)");
            if (info) {
                for (String candidate : suspiciousFiles) {
                    System.out.println("=\t\t" + candidate);
                }
            }
            System.out.println("=\t+ " + correctlyChangedFiles.size() + " file(s) correctly changed");
            if (info) {
                for (String candidate : correctlyChangedFiles) {
                    System.out.println("=\t\t" + candidate);
                }
            }
            System.out.println("= ");
            System.out.println("= " + copyrightProblemsFiles.size() + " file(s) have copyright problems");
            if (info) {
                for (String candidate : copyrightProblemsFiles) {
                    System.out.println("=\t" + candidate);
                }
            }
        }
        System.out.println("=");
        final int size = unchangedFiles.size();
        if (size != 0) {
            System.out.println("= " + size + " file(s) skipped (no changes or no header found)");
            for (final String path : unchangedFiles) {
                System.out.println("=\t" + path);
            }
        } else {
            System.out.println("= No file skipped.");
        }
    }

    /**
     * Just launches the process, with arguments specified by user.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new ReplaceHeaders(args);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}
