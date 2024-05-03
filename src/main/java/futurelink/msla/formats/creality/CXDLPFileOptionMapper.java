package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

class CXDLPFileOptionMapper extends MSLAOptionMapper {
    private final CXDLPFile file;
    private record Option(Class<?> aClass, String location) {}
    private final HashMap<String, Option> optionsMap;

    public CXDLPFileOptionMapper(CXDLPFile file) {
        this.file = file;
        this.optionsMap = new HashMap<>();

        file.sliceInfo.getOptions().forEach((name, type) -> { // SliceInfo options
            this.optionsMap.put(name, new Option(type, "SliceInfo"));
        });

        file.sliceInfoV3.getOptions().forEach((name, type) -> { // SliceInfoV3 options
            this.optionsMap.put(name, new Option(type, "SliceInfoV3"));
        });
    }

    @Override
    public Set<String> getAvailable() {
        throw new RuntimeException("Options list is not implemented yet");
    }

    @Override
    protected boolean hasOption(String option, Class<? extends Serializable> aClass) {
        if (!this.optionsMap.containsKey(option)) return false;
        if (aClass != null) return this.optionsMap.get(option).aClass.isAssignableFrom(aClass);
        return true;
    }

    @Override
    protected Class<?> optionClass(String option) {
        return this.optionsMap.get(option).aClass;
    }

    @Override
    protected boolean hasLayerOption(String option, Class<? extends Serializable> value) {
        return false;
    }
    @Override
    protected void populateOption(String option, Serializable value) {}
    @Override
    protected Serializable fetchOption(String option) throws MSLAException {
        try {
            var loc = this.optionsMap.get(option).location;
            if (loc.equals("SliceInfo")) {
                return (Serializable) file.sliceInfo.getClass().getMethod("get" + option).invoke(file.sliceInfo);
            } else if (loc.equals("SliceInfoV3")) {
                return (Serializable) file.sliceInfoV3.getClass().getMethod("get" + option).invoke(file.sliceInfoV3);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Error while getting option", e);
        }
        return null;
    }
    @Override
    protected void populateLayerOption(String option, int layer, Serializable value) {}
    @Override
    protected Serializable fetchLayerOption(String option, int layer) {
        return null;
    }
}
