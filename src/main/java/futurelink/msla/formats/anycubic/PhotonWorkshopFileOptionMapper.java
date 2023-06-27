package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

class PhotonWorkshopFileOptionMapper extends MSLAOptionMapper {
    private final PhotonWorkshopFile file;

    private record Option(Class<?> aClass, String location) {}
    private final HashMap<String, Option> optionsMap;
    private final String[] layerOptions = { "LiftHeight", "LiftSpeed", "ExposureTime", "LayerHeight" };

    PhotonWorkshopFileOptionMapper(PhotonWorkshopFile file) {
        this.file = file;
        this.optionsMap = new HashMap<>();

        file.extra.getOptions().forEach((name, type) -> { // EXTRA options (version 2.4 and greater)
            this.optionsMap.put(name, new Option(type, "EXTRA"));
        });

        file.header.getOptions().forEach((name, type) -> { // HEADER options
            this.optionsMap.put(name, new Option(type, "HEADER"));
        });
    }

    @Override
    protected boolean hasOption(String option, Serializable aClass) {
        if (!this.optionsMap.containsKey(option)) return false;
        if (aClass != null) return (this.optionsMap.get(option).aClass.equals(aClass));
        return true;
    }

    @Override
    protected boolean hasLayerOption(String option, Serializable aClass) {
        return false;
    }

    @Override
    protected void populateOption(String option, Serializable value) {
        try {
            var loc = this.optionsMap.get(option).location;
            var cls = this.optionsMap.get(option).aClass;
            if (loc.equals("EXTRA")) {
                var m = file.extra.getClass().getMethod("set" + option, cls);
                m.invoke(file.extra, value);
            } else if (loc.equals("HEADER")) {
                var m = file.header.getClass().getMethod("set" + option, cls);
                m.invoke(file.header, value);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void populateLayerOption(String option, int layer, Serializable value) {

    }
}
