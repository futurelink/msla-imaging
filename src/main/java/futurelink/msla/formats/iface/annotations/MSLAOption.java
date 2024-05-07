package futurelink.msla.formats.iface.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Fields in {@link futurelink.msla.formats.iface.MSLAFileBlockFields} that are marked with this annotation are going
 * to be accessible in {@link futurelink.msla.formats.MSLAOptionMapper}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAOption {
    String name() default "";
    Class<?> type() default Number.class;
}
