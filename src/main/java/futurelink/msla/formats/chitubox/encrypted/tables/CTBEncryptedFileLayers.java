package futurelink.msla.formats.chitubox.encrypted.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class CTBEncryptedFileLayers implements MSLAFileBlock {
    private final Fields fileFields;
    private final ArrayList<CTBEncryptedFileLayerDef> LayerDefinition = new ArrayList<>();

    @Getter
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Integer LayerOffset;
        @MSLAFileField(order = 1) private Integer PageNumber;
        @MSLAFileField(order = 2) private final Integer LayerTableSize = 88; // always 0x58
        @MSLAFileField(order = 3) private final Integer Padding2 = 0; // 0
    }

    public CTBEncryptedFileLayers() { fileFields = new Fields(); }

    @Override public String getName() { return "Layers"; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }
}
