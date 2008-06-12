/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.openoffice;

// OpenOffice dependencies
import com.sun.star.uno.XInterface;
import com.sun.star.beans.XPropertySet;


/**
 * Services from the {@link org.geotools.nature} package to be exported to
 * <A HREF="http://www.openoffice.org">OpenOffice</A>.
 *
 * This interface is derived from the {@code XNature.idl} file using the {@code javamaker}
 * tool provided in OpenOffice SDK, and disassembling the output using the {@code javap} tool
 * provided in Java SDK. This source file exists mostly for javadoc purpose and in order to keep
 * IDE happy. The {@code .class} file compiled from this source file <strong>MUST</strong> be
 * overwritten by the {@code .class} file generated by {@code javamaker}.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface XNature extends XInterface {
    /**
     * Returns the noon time (in GMT) when the Sun reach its highest point.
     *
     * @param xOptions  Provided by OpenOffice.
     * @param latitude  The latitude of observation point, in degrees.
     * @param longitude The longitude of observation point, in degrees.
     * @param time      The observation date.
     */
    double getNoonTime(XPropertySet xOptions, double latitude, double longitude, double time);

    /**
     * Returns the Sun's elevation angle in degrees.
     *
     * @param xOptions  Provided by OpenOffice.
     * @param latitude  The latitude of observation point, in degrees.
     * @param longitude The longitude of observation point, in degrees.
     * @param time      The observation date and time, in GMT.
     */
    double getElevation(XPropertySet xOptions, double latitude, double longitude, double time);

    /**
     * Returns the Sun's azimuth in degrees.
     *
     * @param xOptions  Provided by OpenOffice.
     * @param latitude  The latitude of observation point, in degrees.
     * @param longitude The longitude of observation point, in degrees.
     * @param time      The observation date and time, in GMT.
     */
    double getAzimuth(XPropertySet xOptions, double latitude, double longitude, double time);

    /**
     * Returns the tropical year length in days.
     *
     * @param xOptions  Provided by OpenOffice.
     * @param time A date that contains the year.
     */
    double getTropicalYearLength(XPropertySet xOptions, double time);

    /**
     * Returns the synodic month length in days.
     *
     * @param xOptions  Provided by OpenOffice.
     * @param time A date that contains the month.
     */
    double getSynodicMonthLength(XPropertySet xOptions, double time);

    /**
     * Computes sea water density as a function of salinity, temperature and pressure.
     *
     * @param  salinity    Salinity PSS-78 (0 to 42).
     * @param  temperature Temperature ITS-68 (-2 to 40°C).
     * @param  pressure    Pressure in decibars (0 to 10<sup>5</sup> dbar),
     *                     not including atmospheric pressure.
     * @return Density (kg/m³).
     */
    double getSeaWaterDensity(XPropertySet xOptions,
                              double       salinity,
                              double       temperature,
                              double       pressure);

    /**
     * Computes the fusion temperature (melting point) as a function of salinity and pressure.
     *
     * @param  salinity    Salinity PSS-78.
     * @param  pressure    Pressure in decibars, not including atmospheric pressure.
     */
    double getSeaWaterMeltingPoint(XPropertySet xOptions,
                                   double       salinity,
                                   double       pressure);

    /**
     * Computes the sound velocity in sea water as a function of salinity, temperature and pressure.
     *
     * @param  salinity    Salinity PSS-78.
     * @param  temperature Temperature ITS-68.
     * @param  pressure    Pressure in decibars, not including atmospheric pressure.
     */
    double getSeaWaterSoundVelocity(XPropertySet xOptions,
                                    double       salinity,
                                    double       temperature,
                                    double       pressure);

    /**
     * Computes the saturation in disolved oxygen (µmol/kg) as a function of salinity and
     * temperature.
     *
     * @param  salinity    Salinity PSS-78.
     * @param  temperature Temperature ITS-68.
     */
    double getSeaWaterSaturationO2(XPropertySet xOptions,
                                   double       salinity,
                                   double       temperature);
}
