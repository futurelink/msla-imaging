package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

class PhotonWorkshopFileOptionMapper extends MSLAOptionMapper {
    private final PhotonWorkshopFile file;

    private record Option(Class<?> aClass, String location) {}
    private final HashMap<String, Option> optionsMap;
    private final String[] layerOptions = { "LiftHeight", "LiftSpeed", "ExposureTime", "LayerHeight" };

    PhotonWorkshopFileOptionMapper(PhotonWorkshopFile file) {
        this.file = file;
        this.optionsMap = new HashMap<>();

        if (file.getExtra() != null)
            file.getExtra().getOptions().forEach((name, type) -> { // EXTRA options (version 2.4 and greater)
                this.optionsMap.put(name, new Option(type, "EXTRA"));
            });

        file.getHeader().getOptions().forEach((name, type) -> { // HEADER options
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
    protected void populateOption(String option, Serializable value) throws MSLAException {
        try {
            var loc = this.optionsMap.get(option).location;
            var cls = this.optionsMap.get(option).aClass;
            if (loc.equals("EXTRA")) {
                var m = file.getExtra().getClass().getMethod("set" + option, cls);
                m.invoke(file.getExtra(), cls.cast(value));
            } else if (loc.equals("HEADER")) {
                var m = file.getHeader().getClass().getMethod("set" + option, cls);
                m.invoke(file.getHeader(), cls.cast(value));
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException(e.getMessage());
        }
    }

    @Override
    public Class<?> getType(String option) { return this.optionsMap.get(option).aClass; }

    @Override
    protected Serializable fetchOption(String option) throws MSLAException {
        try {
            var loc = this.optionsMap.get(option).location;
            if (loc.equals("EXTRA")) {
                var m = file.getExtra().getClass().getMethod("get" + option);
                return (Serializable) m.invoke(file.getExtra());
            } else if (loc.equals("HEADER")) {
                var m = file.getHeader().getClass().getMethod("get" + option);
                return (Serializable) m.invoke(file.getHeader());
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Error while getting option", e);
        }

        return null;
    }

    @Override protected boolean hasLayerOption(String option, Class<? extends Serializable> aClass) { return false; }
    @Override protected void populateLayerOption(String option, int layer, Serializable value) {}
    @Override protected Serializable fetchLayerOption(String option, int layer) { return null; }
}
