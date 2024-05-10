package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

public class CTBOptionMapper extends MSLAOptionMapper {
    private final CTBFile file;
    private final HashMap<String, Option> optionsMap;

    public CTBOptionMapper(CTBFile file) {
        this.file = file;
        this.optionsMap = new HashMap<>();

        file.getHeader().getOptions().forEach((name, type) -> { // Header options
            this.optionsMap.put(name, new Option(type, "Header"));
        });

        file.getPrintParams().getOptions().forEach((name, type) -> { // Print Parameters options
            this.optionsMap.put(name, new Option(type, "PrintParams"));
        });

        file.getSlicerInfo().getOptions().forEach((name, type) -> { // Slicer Info options
            this.optionsMap.put(name, new Option(type, "SlicerInfo"));
        });
    }

    @Override
    protected boolean hasOption(String option, Class<? extends Serializable> aClass) {
        if (!this.optionsMap.containsKey(option)) return false;
        if (aClass != null) return this.optionsMap.get(option).getOptionClass().isAssignableFrom(aClass);
        return true;
    }

    @Override
    public Class<?> getType(String option) { return this.optionsMap.get(option).getOptionClass(); }

    @Override
    protected boolean hasLayerOption(String option, Class<? extends Serializable> aClass) {
        return false;
    }

    @Override
    protected void populateOption(String option, Serializable value) throws MSLAException {

    }

    @Override
    protected Serializable fetchOption(String option) throws MSLAException {
        try {
            var loc = this.optionsMap.get(option).getLocation();
            if (loc.equals("Header")) {
                return (Serializable) file.getHeader().getClass().getMethod("get" + option)
                        .invoke(file.getHeader());
            } else if (loc.equals("PrintParams")) {
                return (Serializable) file.getPrintParams().getClass().getMethod("get" + option)
                        .invoke(file.getPrintParams());
            } else if (loc.equals("SlicerInfo")) {
                return (Serializable) file.getSlicerInfo().getClass().getMethod("get" + option)
                        .invoke(file.getSlicerInfo());
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Error while getting option", e);
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

    @Override
    public Set<String> getAvailable() {
        return optionsMap.keySet();
    }
}
