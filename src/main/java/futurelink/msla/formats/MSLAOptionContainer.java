package futurelink.msla.formats;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAOptionContainer {
    Class<?> className();
}
