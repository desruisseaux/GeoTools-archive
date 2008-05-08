//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-hudson-3037-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.07.27 at 11:06:51 PM CDT 
//
package org.geotools.gpx.bean;

import java.util.Calendar;


/**
 *
 *          A geographic point with optional elevation and time.  Available for use by other schemas.
 *
 *
 * <p>Java class for ptType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ptType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ele" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
 *       &lt;attribute name="lon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class PtType {
    protected double ele;
    protected Calendar time;
    protected double lat;
    protected double lon;

    /**
     * Gets the value of the ele property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public double getEle() {
        return ele;
    }

    /**
     * Sets the value of the ele property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setEle(double value) {
        this.ele = value;
    }

    /**
     * Gets the value of the time property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setTime(Calendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the lat property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public double getLat() {
        return lat;
    }

    /**
     * Sets the value of the lat property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setLat(double value) {
        this.lat = value;
    }

    /**
     * Gets the value of the lon property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public double getLon() {
        return lon;
    }

    /**
     * Sets the value of the lon property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setLon(double value) {
        this.lon = value;
    }
}
