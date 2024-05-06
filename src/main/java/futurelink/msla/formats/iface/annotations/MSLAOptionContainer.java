package futurelink.msla.formats.iface.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAOptionContainer {
    Class<?> className();
}
