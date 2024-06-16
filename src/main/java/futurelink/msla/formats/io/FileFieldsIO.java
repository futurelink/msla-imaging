package futurelink.msla.formats.io;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import lombok.Getter;

import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.*;

public class FileFieldsIO {
    private static final HashMap<Class<?>, LinkedList<MSLAField>> fields = new HashMap<>();
    public enum Endianness { BigEndian, LittleEndian }

    @Getter
    public static class MSLAField {
        public enum Type { Field, Method }
        private final String name;
        private final Type type;
        private final int order;
        private final int length;
        private final boolean dontCount;
        private final String lengthAt;
        private final String offsetAt;
        private final Charset charset;
        private MSLAField(Field field, MSLAFileField anno) {
            this.name = field.getName();
            this.type = Type.Field;
            this.length = anno.length();
            this.order = anno.order();
            this.dontCount = anno.dontCount();
            this.lengthAt = anno.lengthAt();
            this.offsetAt = anno.offsetAt();
            this.charset = Charset.forName(anno.charset());
        }
        private MSLAField(Method field, MSLAFileField anno) {
            this.name = field.getName();
            this.type = Type.Method;
            this.order = anno.order();
            this.length = anno.length();
            this.dontCount = anno.dontCount();
            this.lengthAt = anno.lengthAt();
            this.offsetAt = anno.offsetAt();
            this.charset = Charset.forName(anno.charset());
        }
        @Override public String toString() { return name + ":" + length; }
    }

    static boolean isStringOrByteArray(Type type) {
        return type == String.class || type == byte[].class || type == Byte[].class;
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

    static int getTypeSize(Type type) throws FileFieldsException {
        if (type == int.class || type == Integer.class) return 4;
        else if (type == long.class || type == Long.class) return 8;
        else if (type == float.class || type == Float.class) return 4;
        else if (type == double.class || type == Double.class) return 8;
        else if (type == boolean.class || type == Boolean.class) return 1;
        else if (type == char.class || type == Character.class) return 1;
        else if (type == byte.class || type == Byte.class) return 1;
        else if (type == short.class || type == Short.class) return 2;
        throw new FileFieldsException("Type " + type + " is unknown, can't determine size");
    }

    static Object getFieldOrMethodValue(MSLAFileBlockFields fields, String fieldOrMethodName) throws FileFieldsException {
        if (fields == null) throw new FileFieldsException("Field block can't be null");
        if (fieldOrMethodName == null || fieldOrMethodName.isEmpty())
            throw new FileFieldsException("Field or method can't be null or empty");
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
                throw new FileFieldsException("No such field or method '" + fieldOrMethodName + "'");
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

    /**
     * Internal function that calculates block field length.
     * @param blockFields file data block fields object to calculate length
     * @param field a field description
     */
    private static Integer getBlockFieldLength(MSLAFileBlockFields blockFields, MSLAField field, Object value)
            throws FileFieldsException
    {
        Integer len = 0;
        try {
            // Get field / method type
            var type = switch (field.getType()) {
                case Field -> blockFields.getClass().getDeclaredField(field.getName()).getGenericType();
                case Method -> blockFields.getClass().getDeclaredMethod(field.getName()).getGenericReturnType();
            };

            // Field is a generic type
            if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                var internalType = ((ParameterizedType) type).getRawType();
                if (List.class.isAssignableFrom((Class<?>) internalType)) {
                    var val = getFieldOrMethodValue(blockFields, field.name);
                    var listElementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    for (var t : (List<?>) val) {
                        if (MSLAFileBlockFields.class.isAssignableFrom((Class<?>) listElementType)) {
                            len += getBlockLength((MSLAFileBlockFields) t);
                        } else throw new FileFieldsException("List of " + listElementType + " is not supported");
                    }
                } else throw new FileFieldsException("Unknown generic internal type " + internalType);
            }
            // Field is an instance of MSLAFileBlockFields
            else if (MSLAFileBlockFields.class.isAssignableFrom((Class<?>) type)) {
                len = getBlockLength((MSLAFileBlockFields) getFieldOrMethodValue(blockFields, field.name));
            }
            // Field is an instance of MSLAFileBlock
            else if (MSLAFileBlock.class.isAssignableFrom((Class<?>) type)) {
                len = getBlockLength((MSLAFileBlock) value);
            }
            // Field is a string or array of bytes
            else if (isStringOrByteArray(type)) {
                len = field.length;
                if (len == 0) len = (Integer) getFieldOrMethodValue(blockFields, field.lengthAt);
            }
            // Any other type either primitive or primitive wrapper (int, double, long etc.)
            else len = getTypeSize(type);

        } catch (NoSuchFieldException e) {
            throw new FileFieldsException("Field '" + field.getName() + "' doesn't exist in " + blockFields.getClass());
        } catch (NoSuchMethodException e) {
            throw new FileFieldsException("Method '" + field.getName() + "' doesn't exist in " + blockFields.getClass());
        }

        if (len == null)
            throw new FileFieldsException("Can't calculate block field '" + field.getName() + "' length, it is null");

        return len;
    }

    /**
     * Calculates file fields block length up to the field specified by {@param lastFieldName}.
     * Effectively this returns an offset of a specific field in a block.
     * @param block file data block fields object to calculate length
     * @param lastFieldName calculate length of a block up to this field
     */
    public static Integer getBlockLength(MSLAFileBlockFields block, String lastFieldName)
            throws FileFieldsException
    {
        var length = 0;
        var fields = getFields(block.getClass());
        for (var field : fields) {
            if (lastFieldName != null && lastFieldName.equals(field.name)) break;
            if (block.isFieldExcluded(field.name)) continue;
            length += getBlockFieldLength(block, field, getFieldOrMethodValue(block, field.name));
        }
        return length;
    }

    /**
     * Calculates file fields block length.
     * @param block file data block object to calculate length
     */
    public static Integer getBlockLength(MSLAFileBlockFields block) throws FileFieldsException {
        return getBlockLength(block, null);
    }

    /**
     * Calculates file fields block partial length up to specific field.
     * @param block file data block object to calculate length
     * @param lastFieldName calculate length of a block up to this field
     */
    public static Integer getBlockLength(MSLAFileBlock block, String lastFieldName) throws FileFieldsException {
        return getBlockLength(block.getBlockFields(), lastFieldName);
    }

    /**
     * Calculates file fields block length.
     * @param block file data block object to calculate length
     */
    public static Integer getBlockLength(MSLAFileBlock block) throws FileFieldsException {
        return getBlockLength(block.getBlockFields(), null);
    }
}
