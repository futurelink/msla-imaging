package io.msla.formats.elegoo.tables;

import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLAFileField;
import lombok.Getter;

/**
 * ELEGOO GOO file format footer block.
 */
@Getter
public class GOOFileFooter extends GOOFileTable {
    private final Fields blockFields = new Fields();

    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField byte Padding1;
        @MSLAFileField(order = 1) byte Padding2;
        @MSLAFileField(order = 2) byte Padding3;
        @MSLAFileField(order = 3, length = 8) byte[] Magic = {0x07, 0x00, 0x00, 0x00, 0x44, 0x4C, 0x50, 0x00}; // 8 bytes;
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() { return 0; }
}