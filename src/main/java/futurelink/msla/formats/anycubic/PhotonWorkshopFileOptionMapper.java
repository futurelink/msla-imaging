package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
    public Set<String> getAvailable() {
        return this.optionsMap.keySet();
    }

    @Override
    protected boolean hasOption(String option, Class<? extends Serializable> aClass) {
        if (!this.optionsMap.containsKey(option)) return false;
        // If class is not specified we only check for option exists
        if (aClass != null) return this.optionsMap.get(option).aClass.isAssignableFrom(aClass);
        return true;
    }

    @Override
    protected boolean hasLayerOption(String option, Class<? extends Serializable> aClass) {
        return false;
    }

    @Override
    protected void populateOption(String option, Serializable value) throws MSLAException {
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
            throw new MSLAException(e.getMessage());
        }
    }

    @Override
    protected Class<?> optionClass(String option) {
        return this.optionsMap.get(option).aClass;
    }

    @Override
    protected Serializable fetchOption(String option) throws MSLAException {
        try {
            var loc = this.optionsMap.get(option).location;
            if (loc.equals("EXTRA")) {
                var m = file.extra.getClass().getMethod("get" + option);
                return (Serializable) m.invoke(file.extra);
            } else if (loc.equals("HEADER")) {
                var m = file.header.getClass().getMethod("get" + option);
                return (Serializable) m.invoke(file.header);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException(e.getMessage());
        }

        return null;
    }

    @Override
    protected void populateLayerOption(String option, int layer, Serializable value) {

    }

    @Override
    protected Serializable fetchLayerOption(String option, int layer) {
        return null;
    }
}
