/*
 * This class is derived from JDK 7 source code, which is licensed under
 * the GPL version 2 with classpath exception. See the OpenJDK project.
 *
 * This is a temporary file with no purpose other than getting GeoTools code to
 * compile with Java 5. This class is not used for execution, is not distributed
 * in any of the released GeoTools JAR files, and will be deleted as soon as
 * GeoTools moves to target Java 6.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.SOURCE;


@Retention(SOURCE) @Target({FIELD,METHOD,PACKAGE})        
public @interface XmlSchemaType {
    String name();
    String namespace() default "http://www.w3.org/2001/XMLSchema";
    Class type() default DEFAULT.class;
    static final class DEFAULT {}
}
