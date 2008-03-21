/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Inherited;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Inherited @Retention(SOURCE) @Target({PACKAGE, TYPE})
public @interface XmlAccessorType {
    XmlAccessType value() default XmlAccessType.PUBLIC_MEMBER;
}
