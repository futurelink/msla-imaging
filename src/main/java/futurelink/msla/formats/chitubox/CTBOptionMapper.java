package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;
import java.util.Set;

public class CTBOptionMapper extends MSLAOptionMapper {

    public CTBOptionMapper(CTBFile file) {}

    @Override
    protected boolean hasOption(String option, Class<? extends Serializable> aClass) {
        return false;
    }

    @Override
    public Class<?> getType(String option) { return null; }

    @Override
    protected boolean hasLayerOption(String option, Class<? extends Serializable> aClass) {
        return false;
    }

    @Override
    protected void populateOption(String option, Serializable value) throws MSLAException {

    }

    @Override
    protected Serializable fetchOption(String option) throws MSLAException {
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
        return Set.of();
    }
}
