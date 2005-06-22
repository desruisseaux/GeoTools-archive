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
package org.geotools.data.mif;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypes;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;
import org.geotools.filter.parser.ParseException;
import org.geotools.resources.TestData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFTestUtils {
    public static final int SRID = 26591;
    public static final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(
                PrecisionModel.FLOATING_SINGLE), SRID);
    public static final String coordsysClause = "Earth Projection 8, 87, \"m\", 9, 0, 0.9996, 1500000, 0 Bounds (-6746230.6469, -9998287.38389) (9746230.6469, 9998287.38389)";

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String getDataPath() {
        try {
            return TestData.file(MIFTestUtils.class, null).getAbsolutePath()
            + "/";
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param inMif DOCUMENT ME!
     * @param outMif DOCUMENT ME!
     *
     * @throws FileNotFoundException
     */
    public static void copyMif(String inMif, String outMif)
        throws FileNotFoundException {
        File path = new File(getDataPath());

        copyFileUsingChannels(MIFFile.getFileHandler(path, inMif, ".mif", true),
            new File(getDataPath() + outMif + ".mif"));
        copyFileUsingChannels(MIFFile.getFileHandler(path, inMif, ".mid", true),
            new File(getDataPath() + outMif + ".mid"));
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String copyFileUsingChannels(File in, File out) {
        String res = "";

        try {
            FileChannel sourceChannel = new FileInputStream(in).getChannel();
            FileChannel destinationChannel = new FileOutputStream(out)
                .getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
            sourceChannel.close();
            destinationChannel.close();
        } catch (Exception e) {
            res = e.getMessage();
        }

        return res;
    }

    /**
     * DOCUMENT ME!
     *
     * @param mifName MIF file to be deleted (no extension)
     */
    public static void safeDeleteMif(String mifName) {
        File f;

        try {
            f = MIFFile.getFileHandler(new File(getDataPath()), mifName,
                    ".mif", false);

            if (f.exists()) {
                f.delete();
            }

            f = MIFFile.getFileHandler(new File(getDataPath()), mifName,
                    ".mid", false);

            if (f.exists()) {
                f.delete();
            }
        } catch (FileNotFoundException e) {
        }
    }

    /**
     * Deletes temporary files in test-data
     */
    public static void cleanFiles() {
        safeDeleteMif("grafo_new");
        safeDeleteMif("grafo_out");
        safeDeleteMif("mixed_wri");
        safeDeleteMif("grafo_append");
        safeDeleteMif("newschema");
        safeDeleteMif("mixed_fs");
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     * @param logger DOCUMENT ME!
     */
    public static void printFeature(Feature f, Logger logger) {
        print(f.toString(), logger);
    }

    /**
     * Utility print method
     *
     * @param msg DOCUMENT ME!
     * @param logger DOCUMENT ME!
     */
    public static void print(String msg, Logger logger) {
        logger.fine(msg);
    }

    /**
     * DOCUMENT ME!
     *
     * @param ft DOCUMENT ME!
     * @param logger DOCUMENT ME!
     */
    public static void printSchema(FeatureType ft, Logger logger) {
        print(ft.getTypeName(), logger);

        AttributeType[] attrs = ft.getAttributeTypes();

        for (int i = 0; i < attrs.length; i++) {
            print("   " + attrs[i].getName() + " - "
                + attrs[i].getType().toString() + "("
                + AttributeTypes.getFieldLength(attrs[i], 0) + ")", logger);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param dbtype DOCUMENT ME!
     * @param path DOCUMENT ME!
     * @param uri DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static HashMap getParams(String dbtype, String path, URI uri) {
        HashMap params = new HashMap();

        params.put("dbtype", dbtype);
        params.put("path", path);

        if (uri != null) {
            params.put("namespace", uri);
        }

        params.put(MIFDataStore.PARAM_FIELDCASE, "upper");
        params.put(MIFDataStore.PARAM_GEOMNAME, "the_geom");
        params.put(MIFDataStore.PARAM_GEOMFACTORY, MIFTestUtils.geomFactory);
        params.put(MIFDataStore.HCLAUSE_COORDSYS, MIFTestUtils.coordsysClause);

        return params;
    }

    /**
     * Duplicates a given feature type
     *
     * @param inFeatureType
     * @param typeName
     *
     * @return
     *
     * @throws SchemaException
     */
    protected static FeatureType duplicateSchema(FeatureType inFeatureType,
        String typeName) throws SchemaException {
        FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance(typeName);

        for (int i = 0; i < inFeatureType.getAttributeCount(); i++) {
            builder.addType(inFeatureType.getAttributeType(i));
        }

        return builder.getFeatureType();
    }

    /**
     * DOCUMENT ME!
     *
     * @param expression DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Filter parseFilter(String expression) {
        try {
            return (Filter) ExpressionBuilder.parse(expression);
        } catch (ParseException e) {
            return Filter.ALL;
        }
    }
}
