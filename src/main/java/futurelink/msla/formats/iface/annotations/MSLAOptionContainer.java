package futurelink.msla.formats.iface.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Being applied to
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAOptionContainer {
    Class<?> value();
}
