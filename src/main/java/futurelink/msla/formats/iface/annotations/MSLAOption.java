package futurelink.msla.formats.iface.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Fields in {@link futurelink.msla.formats.iface.MSLAFileBlockFields} that are marked with this annotation are going
 * to be accessible in {@link futurelink.msla.formats.MSLAOptionMapper}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MSLAOption {
    /**
     * Standard option name constants
     */
    String LayerHeight = "Layer height";
    String BottomExposureTime = "Bottom layers exposure time";
    String BottomLayersCount = "Bottom layers count";
    String BottomLiftHeight = "Bottom layers lift height";
    String BottomLiftSpeed = "Bottom layers lift speed";
    String ExposureTime = "Normal layers exposure time";
    String WaitBeforeCure = "Normal layers wait before cure";
    String LiftHeight = "Normal layers lift height";
    String LiftSpeed = "Normal layers lift speed";
    String RetractSpeed = "Retract speed";
    String RetractHeight = "Retract height";
    String Volume = "Volume";
    String Weight = "Weight";
    String Antialias = "Antialias";
    String AntialiasLevel = "Antialias level";
    String Blur = "Blur";
    String BlurLevel = "Blur level";
    String Grey = "Grey";
    String GreyLevel = "Grey level";

    String value() default "";
    Class<?> type() default Number.class;
}
