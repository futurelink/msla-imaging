package futurelink.msla.formats.iface.options;

import lombok.Getter;

@Getter
public enum MSLAOptionName {
    /* Props */
    Volume("Volume"),
    Weight("Weight"),
    Price("Price"),
    Currency("Currency"),
    PrintTime("Print time"),
    ResinType("Resin type"),

    /* Print settings */
    LayerHeight("Layer height"),
    TransitionLayersCount("Number of transition layers"),
    TransitionLayersType("Transition layer type"),
    Antialias("Antialias"),
    AntialiasLevel("Antialias level"),
    ImageBlur("Image blur"),
    ImageBlurLevel("Image blur level"),
    Grey("Grey"),
    GreyLevel("Grey level"),
    AdvancedMode("Advanced Mode"),
    IntelligentMode("Intelligent Mode"),
    MirrorX("Mirror X"),
    MirrorY("Mirror Y"),

    /* Creality specific options */
    DistortionCompensation("Distortion compensation"),
    DistortionCompensationThickness("Distortion compensation thickness"),
    DistortionCompensationLength("Distortion compensation focal length"),
    XYAxisProfileCompensation("XY axis profile compensation"),
    XYAxisProfileCompensationValue("XY axis profile compensation value"),
    ZPenetrationCompensation("Z penetration compensation"),
    ZPenetrationCompensationLevel("Z penetration compensation level"),

    /**
     * All layers specific options
     */
    LiftHeight("Lift height"),
    LiftHeight2("Lift height 2"),
    LiftSpeed("Lift speed"),
    LiftSpeed2("Lift speed 2"),
    RetractSpeed("Retract speed"),
    RetractSpeed2("Retract speed 2"),
    RetractHeight("Retract height"),
    RetractHeight2("Retract height 2"),
    LightOffTime("Light off time"),
    WaitBeforeCure("Wait time before cure"),
    WaitAfterRetract("Wait time after retract"),
    WaitAfterRetract2("Wait time after retract 2"),
    WaitAfterLift("Wait time after lift"),
    WaitAfterLift2("Wait time after lift 2"),
    WaitAfterLift3("Wait time after lift 3"),
    WaitBeforeLift("Wait time before lift"),
    LightPWM("Light PWM"),

    /**
     * Bottom layers specific options
     */
    BottomLayersCount("Bottom layers count"),
    BottomLayersExposureTime("Bottom layers exposure time"),
    BottomLayersLightOffDelay("Bottom layers light off delay"),
    BottomLayersGradient("Bottom layers gradient"),
    BottomLayersLiftHeight("Bottom layers lift height"),
    BottomLayersLiftHeight1("Bottom layers lift height 1"),
    BottomLayersLiftHeight2("Bottom layers lift height 2"),
    BottomLayersLiftSpeed("Bottom layers lift speed"),
    BottomLayersLiftSpeed1("Bottom layers lift speed 1"),
    BottomLayersLiftSpeed2("Bottom layers lift speed 2"),
    BottomLayersRetractSpeed("Bottom layers retract speed"),
    BottomLayersRetractSpeed1("Bottom layers retract speed 1"),
    BottomLayersRetractSpeed2("Bottom layers retract speed 2"),
    BottomLayersRetractHeight("Bottom layers retract height"),
    BottomLayersRetractHeight2("Bottom layers retract height 2"),
    BottomLayersWaitAfterCure("Bottom layers wait after cure"),
    BottomLayersWaitBeforeCure("Bottom layers wait before cure"),
    BottomLayersWaitAfterLift("Bottom layers wait after lift"),
    BottomLayersLightPWM("Bottom layers light PWM"),

    /**
     * Normal layer specific options
     */
    NormalLayersExposureTime("Normal layers exposure time"),
    NormalLayersLightOffDelay("Normal layers light off delay"),
    NormalLayersGradient("Normal layers gradient"),
    NormalLayersLiftHeight("Normal layers lift height"),
    NormalLayersLiftHeight1("Normal layers lift height 1"),
    NormalLayersLiftHeight2("Normal layers lift height 2"),
    NormalLayersLiftSpeed("Normal layers lift speed"),
    NormalLayersLiftSpeed1("Normal layers lift speed 1"),
    NormalLayersLiftSpeed2("Normal layers lift speed 2"),
    NormalLayersRetractSpeed("Normal layers retract speed"),
    NormalLayersRetractSpeed1("Normal layers retract speed 1"),
    NormalLayersRetractSpeed2("Normal layers retract speed 2"),
    NormalLayersRetractHeight("Normal layers retract height"),
    NormalLayersRetractHeight2("Normal layers retract height 2"),
    NormalLayersWaitAfterCure("Normal layers wait after cure"),
    NormalLayersWaitBeforeCure("Normal layers wait before cure"),
    NormalLayersWaitAfterLift("Normal layers wait after lift"),
    NormalLayersWaitBeforeLift("Normal layers wait before lift"),
    NormalLayersWaitAfterRetract("Normal layers wait after retract"),
    NormalLayersLightPWM("Normal layers light PWM"),

    /**
     * Layer specific options
     */
    LayerSettings("Per-layer settings"),
    LayerExposureTime("Layer exposure time"),
    LayerWaitAfterCure("Layer wait after cure"),
    LayerWaitBeforeCure("Layer wait before cure"),
    LayerWaitAfterLift("Layer wait after lift"),
    LayerWaitBeforeLift("Layer wait before lift"),
    LayerWaitAfterRetract("Layer wait after retract"),
    LayerLiftHeight("Layer lift height"),
    LayerLiftSpeed("Layer lift speed"),
    LayerLightOffDelay("Layer light off delay"),
    LayerGradient("Layer gradient"),
    LayerLiftHeight1("Layer lift height 1"),
    LayerLiftSpeed1("Layer lift speed 1"),
    LayerRetractSpeed1("layer retract speed 1"),
    LayerLiftHeight2("Layer lift height 2"),
    LayerLiftSpeed2("Layer lift speed 2"),
    LayerRetractSpeed("Layer retract speed"),
    LayerRetractSpeed2("Layer retract speed 2"),
    LayerRetractHeight("Layer retract height"),
    LayerRetractHeight2("Layer retract height 2"),
    LayerLightPWM("Layer light PWM");

    private final String name;
    MSLAOptionName(String name) { this.name = name; }
    @Override public String toString() { return name; }
}
