package futurelink.msla.formats.iface.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAFileField {
    int length() default 0;
    String lengthAt() default "";
    int order() default 0;
    boolean dontCount() default false;
    String charset() default "US-ASCII";
}
