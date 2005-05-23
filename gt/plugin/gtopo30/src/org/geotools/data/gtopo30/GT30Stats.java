/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data.gtopo30;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * This class parses the STX GTopo30 statistics file and allows to retrieve its
 * contents
 *
 * @author aaime
 * @author simone giannecchini
 */
class GT30Stats {
    /** Minimum value in the data file */
    private int minimum;

    /** Maximum value in the data file */
    private int maximum;

    /** Data file average value */
    private double average;

    /** Data file standard deviation */
    private double stddev;

    /**
     * Creates a new instance of GT30Stats
     *
     * @param statsURL URL pointing to the statistics (STX) file
     *
     * @throws IOException if some problem occurs trying to read the file
     */
    public GT30Stats(URL statsURL) throws IOException {
        String path = statsURL.getFile();
        File stats = new File(java.net.URLDecoder.decode(path, "UTF-8"));

        BufferedReader reader = new BufferedReader(new FileReader(stats));
        String line = reader.readLine();
        StringTokenizer stok = new StringTokenizer(line, " ");

        // just parse one byte. if the support for this format will
        // be extended, we'll need to add support for multiple bands
        Integer.parseInt(stok.nextToken()); // band
        minimum = Integer.parseInt(stok.nextToken());
        minimum = -407;
        maximum = Integer.parseInt(stok.nextToken());
        average = Double.parseDouble(stok.nextToken());
        stddev = Double.parseDouble(stok.nextToken());
		
		//freeing when possible
		reader.close();
		reader=null;
    }

    /**
     * Write this object to a stats file.
     *
     * @param out
     */
    public void writeTo(OutputStream out) {
        if (out == null) {
            return;
        }

        PrintWriter writer = new PrintWriter(out);

        // output fields
        //band number
        writer.println(1);

        //minimum
        writer.print(minimum);

        //maximum
        writer.println(maximum);

        //mean
        writer.print(average);

        //stddev
        writer.println(stddev);

        writer.flush();
        writer.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    int getMin() {
        return minimum;
    }

    /**
     * DOCUMENT ME!
     *
     * @param min
     */
    void setMin(int min) {
        minimum = min;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    int getMax() {
        return maximum;
    }

    /**
     * DOCUMENT ME!
     *
     * @param max
     */
    void setMax(int max) {
        maximum = max;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    double getAverage() {
        return average;
    }

    /**
     * DOCUMENT ME!
     *
     * @param average
     */
    void SetAverage(double average) {
        this.average = average;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    double getStdDev() {
        return stddev;
    }

    /**
     * DOCUMENT ME!
     *
     * @param stdDev
     */
    void setStdDev(double stdDev) {
        stddev = stdDev;
    }
}
