package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAFileBlock;
import futurelink.msla.formats.MSLAOption;
import futurelink.msla.formats.MSLAOptionContainer;

import java.util.Arrays;
import java.util.HashMap;

public abstract class CXDLPFileTable implements MSLAFileBlock {
    public HashMap<String, Class<?>> getOptions() {
        var a = getClass().getAnnotation(MSLAOptionContainer.class);
        if (a != null) {
            var optionsMap = new HashMap<String, Class<?>>();
            Arrays.stream(a.className().getDeclaredFields())
                    .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                    .forEach((f) -> optionsMap.put(f.getName(), f.getAnnotation(MSLAOption.class).type()));
            return optionsMap;
        }
        return null;
    }
}
