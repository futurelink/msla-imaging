package io.msla.formats.iface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

public interface MSLAFileBlockFields {
    default boolean isFieldExcluded(String fieldName) { return false; }

    default String capitalize(String value) {
        return value.replaceFirst(".", value.substring(0, 1).toUpperCase());
    }

    default String fieldsAsString(String nameValueSeparator, String fieldsSeparator) {
        var fields = getClass().getDeclaredFields();
        var out = new LinkedList<String>();
        try {
            for (var f : fields) {
                if (isFieldExcluded(f.getName())) continue;
                if (f.getAnnotation(MSLAFileField.class) != null) {
                    Method method = null;
                    String methodName = "get" + capitalize(f.getName());
                    try {
                        method = getClass().getDeclaredMethod(methodName);
                    } catch (NoSuchMethodException ignored) {}

                    if (method != null) {
                        method.setAccessible(true);
                        out.add(f.getName() + nameValueSeparator + method.invoke(this));
                        method.setAccessible(false);
                    }
                    else if (f.canAccess(this)) out.add(f.getName() + nameValueSeparator + f.get(this));
                }
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {}

        return String.join(fieldsSeparator, out);
    }
}
