/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gce.gtopo30;

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
 * @author mkraemer
 * @source $URL$
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
        this.minimum = Integer.parseInt(stok.nextToken());
        this.minimum = -407;
        this.maximum = Integer.parseInt(stok.nextToken());
        this.average = Double.parseDouble(stok.nextToken());
        this.stddev = Double.parseDouble(stok.nextToken());
		
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
        writer.print(this.minimum);

        //maximum
        writer.println(this.maximum);

        //mean
        writer.print(this.average);

        //stddev
        writer.println(this.stddev);

        writer.flush();
        writer.close();
    }

    /**
     * Returns the minimum value
     *
     * @return the minimum value
     */
    int getMin() {
        return this.minimum;
    }

    /**
     * Sets the minimum value
     *
     * @param min the new minimum value
     */
    void setMin(int min) {
        this.minimum = min;
    }

    /**
     * Returns the maximum value
     *
     * @return the maximum value 
     */
    int getMax() {
        return this.maximum;
    }

    /**
     * Sets the maximum value
     *
     * @param max the new maximum value
     */
    void setMax(int max) {
        this.maximum = max;
    }

    /**
     * Returns the average value
     *
     * @return the average value
     */
    double getAverage() {
        return this.average;
    }

    /**
     * Sets the average value
     *
     * @param avg the new average value
     */
    void setAverage(double avg) {
        this.average = avg;
    }

    /**
     * Returns the standard deviation
     *
     * @return the standard deviation
     */
    double getStdDev() {
        return this.stddev;
    }

    /**
     * Sets the standard deviation
     *
     * @param sd the new value
     */
    void setStdDev(double sd) {
        this.stddev = sd;
    }
}
