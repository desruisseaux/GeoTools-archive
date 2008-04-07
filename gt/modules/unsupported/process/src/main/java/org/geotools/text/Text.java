package org.geotools.text;

import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

public class Text {
    // additional methods needed to register additional
    // properties files at a later time.
    /**
     * Create a international string based on the provided English text.
     * <p>
     * We will hook up this method to a properties file at a later time,
     * making other translations available via the Factory SPI mechanism.
     * 
     * @param english
     */
    public static InternationalString text(String english){
        return new SimpleInternationalString( english );
    }
}
