/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

// OpenGIS dependencies
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Transform vertical coordinates using coefficients from the
 * <A HREF="http://earth-info.nima.mil/GandG/wgs84/gravitymod/">Earth Gravitational Model</A>.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Pierre Cardinal
 * @author Martin Desruisseaux
 */
public class EarthGravitationalModel extends VerticalTransform {
    /**
     * WGS 84 semi-major axis
     */
    private static final double semiMajor = 6378137;

    /**
     * The first Eccentricity Squared (e²) for WGS 84 ellipsoid.
     */
    private static double esq = 0.00669437999013;

    /**
     * Theoretical (Normal) Gravity at the Equator (on the Ellipsoid).
     */
    private static final double grava = 9.7803267714;

    /**
     * Theoretical (Normal) Gravity Formula Constant.
     */
    private static final double star = 0.001931851386;

    /**
     * WGS 84 Earth's Gravitational Constant w/ atmosphere
     */
    private static final double rkm = 3.986004418e14;

    /**
     * (TODO)
     */
    private final int nmax;

    /**
     * (TODO)
     */
    private final int[] ivLocArray;

    /**
     * (TODO)
     */
    private final double[] aClenshawArray, bClenshawArray, cnmGeopCoef, snmGeopCoef, as;

    /**
     * Temporary buffer for use by {@link #height} only. Allocated once for ever
     * for avoiding to many objects creation / destruction.
     */
    private final double[] sht, cr, sr, t11, t12, s11, s12;
    

    /**
     * Creates a model with the default amount (TODO).
     */
    private EarthGravitationalModel() {
        this(180);
    }

    /**
     * Creates a model with the specified amount of (TODO).
     */
    private EarthGravitationalModel(final int nmax) {
        this.nmax = nmax;
        final int cleanshawLength = (((nmax + 3) * (nmax + 4)) / 2) + 1;
        final int  geopCoefLength = (((nmax + 1) * (nmax + 2)) / 2) + 1;
        aClenshawArray = new double[cleanshawLength];
        bClenshawArray = new double[cleanshawLength];
        cnmGeopCoef    = new double[geopCoefLength];
        snmGeopCoef    = new double[geopCoefLength];
        ivLocArray     = new int   [nmax + 4];
        as             = new double[nmax + 3];
        sht            = new double[nmax + 2];
        cr             = new double[nmax + 1];
        sr             = new double[nmax + 1];
        t11            = new double[nmax + 3];
        t12            = new double[nmax + 3];
        s11            = new double[nmax + 3];
        s12            = new double[nmax + 3];
    }

    /**
     * Loads the coefficients from the specified ASCII file and initialize the internal
     * <cite>clenshaw arrays</cite>.
     *
     * @param  filename The filename (e.g. {@code "WGS84.cof"}, relative to this class directory.
     * @throws IOException if the file can't be read or has an invalid content.
     */
    private void load(final String filename) throws IOException {
        final InputStream stream = EarthGravitationalModel.class.getResourceAsStream(filename);
        if (stream == null) {
            throw new FileNotFoundException(filename);
        }
        final LineNumberReader in = new LineNumberReader(new InputStreamReader(stream, "ISO-8859-1"));
        String line;
        while ((line = in.readLine()) != null) {
            final StringTokenizer tokens = new StringTokenizer(line);
            try {
                final int    n    = Integer.parseInt   (tokens.nextToken());
                final int    m    = Integer.parseInt   (tokens.nextToken());
                final double cbar = Double .parseDouble(tokens.nextToken());
                final double sbar = Double .parseDouble(tokens.nextToken());
                final int   ll    = ((n * (n + 1)) / 2) + m + 1;
                cnmGeopCoef[ll]   = cbar;
                snmGeopCoef[ll]   = sbar;
            } catch (RuntimeException cause) {
                /*
                 * Catch the following exceptions:
                 *   - NoSuchElementException      if a line has too few numbers.
                 *   - NumberFormatException       if a number can't be parsed.
                 *   - IndexOutOfBoundsException   if 'n' or 'm' values are illegal.
                 */
                final IOException exception = new IOException(Errors.format(
                        ErrorKeys.BAD_LINE_IN_FILE_$2, filename, new Integer(in.getLineNumber())));
                exception.initCause(cause);
                throw exception;
            }
        }
        in.close();
        initialize();
    }

    /**
     * Computes the <cite>clenshaw arrays</cite> after all coefficients have been read. This method
     * is separated step from the {@link #load} because we may want to load the coefficient from an
     * other source than an ASCII file. For example a binary file may be more efficient.
     */
    private final void initialize() {
        final int nmax3 = nmax + 3;
        for (int i=1; i<=nmax3; i++) {
            ivLocArray[i] = (i * (i-1)) / 2;
        }
        final double c2 = 108262.9989050e-8;
        final double[] c2n = new double[6];
        int sign = 1;
        double esqi = esq;
        for (int i=2; i<6; i++) {
            sign *= -1;
            esqi *= esq;
            c2n[i] = sign * ((3 * esqi) / (double) ((2*i + 1) * (2*i + 3))) * (1 - i + (5*i*c2 / esq));
        }
        cnmGeopCoef[ 4] += c2     / 2.2360679774997896964091736687313; // sqrt(5)
        cnmGeopCoef[11] += c2n[2] / 3;
        cnmGeopCoef[22] += c2n[3] / 3.6055512754639892931192212674705; // sqrt(13)
        cnmGeopCoef[37] += c2n[4] / 4.1231056256176605498214098559741; // sqrt(17)
        cnmGeopCoef[56] += c2n[5] / 4.5825756949558400065880471937280; // sqrt(21)

        for (int i=1; i<=nmax; i++) {
            as[i] = -Math.sqrt(1 + 1.0 / (2*i));
        }
        for (int i=0; i<=nmax; i++) {
            for (int j=i+1; j<=nmax; j++) {
                final int ll = ivLocArray[j+1] + i + 1;
                final int n  = 2*j + 1;
                final int ji = (j-i) * (j+i);
                aClenshawArray[ll] = -Math.sqrt(((n * (2*j - 1)) / (double) ji));
                bClenshawArray[ll] =  Math.sqrt(((n * (j+i - 1)) * (j-i - 1)) / (double) ((2*j - 3) * ji));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public double height(final double longitude, final double latitude) throws TransformException {
        /*
         * Note: no need to ensure that longitude is in [-180..+180°] range, because its value
         * is used only in trigonometric functions (sin / cos), which roll it as we would expect.
         * Latitude is used only in trigonometric functions as well.
         */
        final double ht       = 0;  // TODO: Why 0?
        final double phi      = Math.toRadians(latitude);
        final double sin_phi  = Math.sin(phi);
        final double sin2_phi = sin_phi * sin_phi;
        final double rni      = Math.sqrt(1 - esq*sin2_phi);
        final double rn       = semiMajor / rni;
        final double t22      = (rn + ht) * Math.cos(phi);
        final double x2y2     = t22 * t22;
        final double z1       = ((rn * (1 - esq)) + ht) * sin_phi;
        final double th       = (Math.PI / 2.0) - Math.atan(z1 / Math.sqrt(x2y2));
        final double y        = Math.sin(th);
        final double f1       = semiMajor / Math.sqrt(x2y2 + z1*z1);
        final double f2       = f1*f1;
        final double rlam     = Math.toRadians(longitude);

        sr[0] = 0;
        cr[0] = 1;
        sr[1] = Math.sin(rlam);
        cr[1] = Math.cos(rlam);

        for (int j=2; j<=nmax; j++) {
            sr[j] = (2.0 * cr[1] * sr[j-1]) - sr[j-2];
            cr[j] = (2.0 * cr[1] * cr[j-1]) - cr[j-2];
        }
        for (int k=nmax; k>=0; k--) {
            for (int l=nmax; l>=k; l--) {
                final int    ll  = ivLocArray[l+1] + k + 1;
                final int    ll2 = ivLocArray[l+2] + k + 1;
                final int    ll3 = ivLocArray[l+3] + k + 1;
                final double ta  = -aClenshawArray[ll2] * f1 * Math.cos(th);
                final double tb  =  bClenshawArray[ll3] * f2;
                s11[l] = (ta * s11[l + 1]) - (tb * s11[l + 2]) + cnmGeopCoef[ll];
                s12[l] = (ta * s12[l + 1]) - (tb * s12[l + 2]) + snmGeopCoef[ll];
            }
            t11[k] = s11[k];
            t12[k] = s12[k];
            sht[k] = (-as[k+1] * y * f1 * sht[k+1]) + (t11[k] * cr[k]) + (t12[k] * sr[k]);
        }
        return ((t11[0] + t12[0]) * f1 + (sht[1] * 1.7320508075688772935274463415059 * y * f2)) *
               rkm / semiMajor / ((grava * (1 + (star * sin2_phi))) / rni - (ht * 0.3086e-5));
    }

    /**
     * TODO: move that as a JUnit test.
     */
    public static void main(String[] args) throws Exception {
        EarthGravitationalModel gh = new EarthGravitationalModel();
        gh.load("WGS84.cof");
        double test = gh.height(45, 45); // 1,505
        System.out.println(test);
        test = gh.height(0, 45); // 46,908
        System.out.println(test);
    }
}
