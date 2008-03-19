package javax.xml.bind.annotation;

import javax.xml.bind.JAXBContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(SOURCE)
public @interface XmlSeeAlso {
    Class[] value();
}