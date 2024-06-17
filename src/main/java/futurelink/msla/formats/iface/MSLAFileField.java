package futurelink.msla.formats.iface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mSLA file annotation that marks field as processable by {@link futurelink.msla.formats.io.FileFieldsIO}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ ElementType.FIELD, ElementType.METHOD })
public @interface MSLAFileField {
    int length() default 0;
    String lengthAt() default "";
    String offsetAt() default "";
    int order() default 0;
    boolean dontCount() default false;
    String charset() default "US-ASCII";
}
