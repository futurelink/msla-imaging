package futurelink.msla.formats.iface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAOption {
    String name() default "";
    Class<?> type() default Number.class;
}
