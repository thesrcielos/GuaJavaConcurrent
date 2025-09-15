package org.eci.arep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    public String value();
    String DEFAULT_NONE = "\t\t\t\t\t\t \n \n \t";
    public String defaultValue() default DEFAULT_NONE;
}
