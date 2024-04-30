package futurelink.msla.formats.iface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAOptionContainer {
    Class<?> className();
}
