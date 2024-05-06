package futurelink.msla.formats.utils;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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

    static String getReadMethodName(Type elementType) {
        if ((elementType == int.class) || (elementType == Integer.class)) return "readInt";
        else if (elementType == short.class || elementType == Short.class) return "readShort";
        else if (elementType == long.class || elementType == Long.class) return "readLong";
        else if (elementType == float.class || elementType == Float.class) return "readFloat";
        else if (elementType == double.class || elementType == Double.class) return "readDouble";
        else if (elementType == boolean.class || elementType == Boolean.class) return "readBoolean";
        else if (elementType == char.class || elementType == Character.class) return "readChar";
        else if (elementType == byte.class || elementType == Byte.class) return "readByte";
        else return null;
    }

    static Class<?> getWriteMethodPrimitiveType(Type type) {
        if (type == int.class || type == Integer.class) return int.class;
        else if (type == short.class || type == Short.class) return int.class; // writeShort requires int argument
        else if (type == long.class || type == Long.class) return long.class;
        else if (type == float.class || type == Float.class) return float.class;
        else if (type == double.class || type == Double.class) return double.class;
        else if (type == boolean.class || type == Boolean.class) return boolean.class;
        else if (type == char.class || type == Character.class) return char.class;
        else if (type == byte.class || type == Byte.class) return int.class;  // writeByte requires int argument
        else return null;
    }

    static String getWriteMethodName(Type elementType) {
        if (elementType == int.class || elementType == Integer.class) return "writeInt";
        else if (elementType == short.class || elementType == Short.class) return "writeShort";
        else if (elementType == long.class || elementType == Long.class) return "writeLong";
        else if (elementType == float.class || elementType == Float.class) return "writeFloat";
        else if (elementType == double.class || elementType == Double.class) return "writeDouble";
        else if (elementType == boolean.class || elementType == Boolean.class) return "writeBoolean";
        else if (elementType == char.class || elementType == Character.class) return "writeChar";
        else if (elementType == byte.class || elementType == Byte.class) return "writeByte";
        else return null;
    }

    static int getTypeSize(Type type, int length) {
        if (type == int.class || type == Integer.class) return 4;
        else if (type == long.class || type == Long.class) return 8;
        else if (type == float.class || type == Float.class) return 4;
        else if (type == double.class || type == Double.class) return 8;
        else if (type == boolean.class || type == Boolean.class) return 1;
        else if (type == char.class || type == Character.class) return 1;
        else if (type == byte.class || type == Byte.class) return 1;
        else if (type == short.class || type == Short.class) return 2;
        else if (type == String.class || type == byte[].class) return length;
        return 0;
    }

    static Object getFieldOrMethodValue(MSLAFileBlockFields fields, String fieldOrMethodName) throws IOException {
        Object value;
        try {
            var f = fields.getClass().getDeclaredField(fieldOrMethodName);
            f.setAccessible(true);
            value = f.get(fields);
            f.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                var m = fields.getClass().getDeclaredMethod(fieldOrMethodName);
                m.setAccessible(true);
                value = m.invoke(fields);
                m.setAccessible(false);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e2) {
                throw new IOException("No such field or method " + fieldOrMethodName);
            }
        }

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
