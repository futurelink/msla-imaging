package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;
import java.util.HashMap;

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
    protected boolean hasOption(String option, Class<? extends Serializable> aClass) {
        if (!this.optionsMap.containsKey(option)) return false;
        if (aClass != null) return this.optionsMap.get(option).aClass.isAssignableFrom(aClass);
        return true;
    }

    @Override
    protected boolean hasLayerOption(String option, Class<? extends Serializable> value) {
        return false;
    }

    @Override
    protected void populateOption(String option, Serializable value) {}

    @Override
    protected void populateLayerOption(String option, int layer, Serializable value) {}
}
