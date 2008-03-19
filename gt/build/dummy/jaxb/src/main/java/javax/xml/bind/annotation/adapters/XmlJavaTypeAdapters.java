/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation.adapters;

import static java.lang.annotation.ElementType.PACKAGE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;

@Retention(SOURCE) @Target({PACKAGE})
public @interface XmlJavaTypeAdapters {
    XmlJavaTypeAdapter[] value();
}
