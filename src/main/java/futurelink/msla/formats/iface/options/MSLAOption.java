package futurelink.msla.formats.iface.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields in {@link futurelink.msla.formats.iface.MSLAFileBlockFields} that are marked with this annotation are going
 * to be accessible in {@link futurelink.msla.formats.MSLAOptionMapper}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MSLAOption {
    MSLAOptionName value();
}
