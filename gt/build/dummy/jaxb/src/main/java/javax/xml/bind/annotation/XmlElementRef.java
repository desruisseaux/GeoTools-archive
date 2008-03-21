/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@Retention(SOURCE)
@Target({FIELD,METHOD})
public @interface XmlElementRef {
    Class type() default DEFAULT.class;
    String namespace() default "";
    String name() default "##default";
    static final class DEFAULT {}
}
