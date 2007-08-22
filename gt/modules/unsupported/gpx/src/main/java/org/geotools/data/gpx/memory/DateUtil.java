/*
 * Created on 2005.07.05.
 *
 * $Id$
 *
 */
package org.geotools.data.gpx.memory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtil {
    /*
     * Date buzergalo metodusok.
     *
     * Mivel a SimpleDateFormat nem thread safe, ezert synchronized blokkokba kerultek.
     * ezzel viszont szuk keresztmetszetei lehetnek a rendszernek, de mivel a konverzio
     * idoigenye valoszinuleg elenyeszik a tobbi lepes idoigenye mellett, varhatoan nem lesz
     * problema ebbol. Ha megis, akkor kell csinalni egy DateFormat poolt.
     */

    // 2004-12-04T09:42:53Z
    private static DateFormat gpxTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static Date parseGpxTime(String dateStr) throws ParseException {
        synchronized (gpxTimeFormat) {
            return gpxTimeFormat.parse(dateStr);
        }
    }

    static String formatGpxTime(Date date) {
        synchronized (gpxTimeFormat) {
            return gpxTimeFormat.format(date);
        }
    }
}
