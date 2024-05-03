package futurelink.msla.formats.utils;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

public class FileFieldsIO {
    private static final HashMap<Class<?>, LinkedList<MSLAField>> fields = new HashMap<>();

    @Getter
    static class MSLAField {
        public enum Type { Field, Method }
        private final String name;
        private final Type type;
        private final int order;
        private final int length;
        private final boolean dontCount;
        private final String lengthAt;
        private final Charset charset;
        private MSLAField(Field field, MSLAFileField anno) {
            this.name = field.getName();
            this.type = Type.Field;
            this.length = anno.length();
            this.order = anno.order();
            this.dontCount = anno.dontCount();
            this.lengthAt = anno.lengthAt();
            this.charset = Charset.forName(anno.charset());
        }
        private MSLAField(Method field, MSLAFileField anno) {
            this.name = field.getName();
            this.type = Type.Method;
            this.order = anno.order();
            this.length = anno.length();
            this.dontCount = anno.dontCount();
            this.lengthAt = anno.lengthAt();
            this.charset = Charset.forName(anno.charset());
        }
        @Override public String toString() { return name + ":" + length; }
    }

    static Object getFieldValue(MSLAFileBlockFields fields, String fieldName)
            throws NoSuchFieldException, IllegalAccessException
    {
        var f = fields.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        var value = f.get(fields);
        f.setAccessible(false);
        return value;
    }

    static List<MSLAField> getFields(Class<? extends MSLAFileBlockFields> cls) {
        if (!fields.containsKey(cls)) {
            var list = new LinkedList<MSLAField>();
            fields.put(cls, list);
            var f = Arrays.stream(cls.getDeclaredFields()) // Enumerate fields
                    .filter(item -> item.getAnnotation(MSLAFileField.class) != null)
                    .toArray(Field[]::new);
            for (var t : f) list.add(new MSLAField(t, t.getAnnotation(MSLAFileField.class)));
            var m = Arrays.stream(cls.getDeclaredMethods()) // Enumerate methods
                    .filter(item -> item.getAnnotation(MSLAFileField.class) != null)
                    .toArray(Method[]::new);
            for (var t : m) list.add(new MSLAField(t, t.getAnnotation(MSLAFileField.class)));
            list.sort(Comparator.comparingInt(item -> item.order)); // Sort by order and output
        }
        return fields.get(cls);
    }
}
