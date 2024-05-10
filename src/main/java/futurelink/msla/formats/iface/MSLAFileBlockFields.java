package futurelink.msla.formats.iface;

import futurelink.msla.formats.iface.annotations.MSLAFileField;

public interface MSLAFileBlockFields {
    default boolean isFieldExcluded(String fieldName) { return false; }
    default String fieldsAsString(String nameValueSeparator, String fieldsSeparator) {
        var sb = new StringBuilder();
        var fields = getClass().getDeclaredFields();
        try {
            for (var f : fields) {
                if (isFieldExcluded(f.getName())) continue;
                if (f.getAnnotation(MSLAFileField.class) != null) {
                    f.setAccessible(true);
                    sb.append(f.getName());
                    sb.append(nameValueSeparator);
                    sb.append(f.get(this));
                    if (!f.equals(fields[fields.length-1])) sb.append(fieldsSeparator);
                    f.setAccessible(false);
                }
            }
        } catch (IllegalAccessException ignored) {}

        return sb.toString();
    }

}
