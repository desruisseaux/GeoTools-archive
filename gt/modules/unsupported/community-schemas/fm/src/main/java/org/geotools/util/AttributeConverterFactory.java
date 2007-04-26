package org.geotools.util;

import org.geotools.factory.Hints;
import org.opengis.feature.Attribute;

public class AttributeConverterFactory implements ConverterFactory {

    public Converter createConverter(Class source, Class target, Hints hints) {
        if (!(Attribute.class.isAssignableFrom(source))) {
            return null;
        }
        return new Converter() {
            public Object convert(Object source, Class target) throws Exception {
                Attribute att = (Attribute) source;
                Object value = att.get();
                if (value == null) {
                    return null;
                }
                Object convertedValue = Converters.convert(value, target);
                return convertedValue;
            }
        };
    }

}
