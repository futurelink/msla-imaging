package futurelink.msla.formats.iface.annotations;

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
    /**
     * Standard option name constants
     */
    String LayerOverrides = "Per layer overrides";
    String LayerHeight = "Layer height";
    String BottomExposureTime = "Bottom layers exposure time";
    String BottomLayersCount = "Bottom layers count";
    String BottomLiftHeight = "Bottom layers lift height";
    String BottomLiftSpeed = "Bottom layers lift speed";
    String ExposureTime = "Normal layers exposure time";
    String WaitAfterCure = "Normal layers wait after cure";
    String WaitBeforeCure = "Normal layers wait before cure";
    String WaitAfterLift = "Normal layers wait after lift";
    String LiftHeight = "Normal layers lift height";
    String LiftSpeed = "Normal layers lift speed";
    String RetractSpeed = "Retract speed";
    String RetractHeight = "Retract height";
    String Volume = "Volume";
    String Price = "Price";
    String Currency = "Currency";
    String Weight = "Weight";
    String Antialias = "Antialias";
    String AntialiasLevel = "Antialias level";
    String Blur = "Blur";
    String BlurLevel = "Blur level";
    String Grey = "Grey";
    String GreyLevel = "Grey level";
    String LightPWM = "Light PWM";

    String value() default "";
    Class<?> type() default Number.class;
}
