package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAOptionMapper;

import java.io.Serializable;

class ChituBoxDLPFileOptionMapper extends MSLAOptionMapper {

    @Override
    protected boolean hasOption(String option, Serializable value) {
        return false;
    }

    @Override
    protected boolean hasLayerOption(String option, Serializable value) {
        return false;
    }

    @Override
    protected void populateOption(String option, Serializable value) {

    }

    @Override
    protected void populateLayerOption(String option, int layer, Serializable value) {

    }
}
