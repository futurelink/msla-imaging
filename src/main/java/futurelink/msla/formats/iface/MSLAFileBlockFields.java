package futurelink.msla.formats.iface;

public interface MSLAFileBlockFields {
    default boolean isFieldExcluded(String fieldName) { return false; }
}
