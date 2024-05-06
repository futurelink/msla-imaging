package futurelink.msla.formats.utils;

import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.iface.MSLAFileBlockFields;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;

public class FileFieldsWriter {
    private final Logger logger = Logger.getLogger(FileFieldsWriter.class.getName());
    public enum Endianness { BigEndian, LittleEndian }

    private final OutputStream stream;
    private final Endianness endianness;

    public FileFieldsWriter(OutputStream out, Endianness endianness) {
        this.stream = out;
        this.endianness = endianness;
    }


    private String getWriteMethodName(Type elementType) {
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

    private void writeArray(DataOutput stream, Type arrayType, Object value, int length) throws IOException {
        var elementType = ((Class<?>) arrayType).getComponentType();
        if ((elementType == byte.class) || (elementType == Byte.class))
            stream.write((length > 0) ? Arrays.copyOfRange((byte[]) value, 0, length) : (byte[]) value);
        else {
            for (int i = 0; i < length; i++) {
                if (elementType == int.class) stream.writeInt(((int[]) value)[i]);
                else if (elementType == short.class) stream.writeShort(((short[]) value)[i]);
                else if (elementType == Integer.class) stream.writeInt(((Integer[]) value)[i]);
                else if (elementType == Short.class) stream.writeShort(((Short[]) value)[i]);
                else throw new IOException("Unknown array component type " + elementType);
            }
        }
    }

    private void writeField(DataOutput stream, Type type, Object value, int length, Charset charset) throws IOException {
        var writeMethodName = getWriteMethodName(type);
        if (writeMethodName != null) {
            try {
                stream.getClass().getDeclaredMethod(writeMethodName, FileFieldsIO.getWriteMethodPrimitiveType(type))
                        .invoke(stream, value);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new IOException("Can't write value", e);
            }
        }
        else if (type == String.class) {
            stream.write((length > 0) ?
                    Arrays.copyOfRange(((String) value).getBytes(charset), 0, length) :
                    ((String) value).getBytes(charset));
        }
        else if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
            var internalType = ((ParameterizedType) type).getRawType();
            if (internalType == ArrayList.class) {
                var listElementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                logger.fine("List generic type is " + listElementType + " of " + listElementType.getClass());
                for (var t : (List<?>) value) writeField(stream, listElementType, t, length, charset);
            } else throw new IOException("Unknown internal type " + internalType);
        }
        else if (((Class<?>) type).isArray()) {
            logger.fine("Writing array of " + type + " length " + length);
            writeArray(stream, type, value, length);
        }
        else if (MSLAFileBlockFields.class.isAssignableFrom((Class<?>) type)) {
            logger.fine("Writing fields block of " + type);
            writeBlock(stream, (MSLAFileBlockFields) value);
        } else throw new IOException("Unknown type " + type);
    }

    private void writeBlock(DataOutput dos, MSLAFileBlockFields fields) throws IOException {
        var fieldsList = FileFieldsIO.getFields(fields.getClass());
        logger.fine(fieldsList.toString());
        try {
            for (var field : fieldsList) {
                if (fields.isFieldExcluded(field.getName())) continue;
                Type type;
                Object value;
                var length = !"".equals(field.getLengthAt()) ?
                        FileFieldsIO.getFieldOrMethodValue(fields, field.getLengthAt()) :
                        field.getLength();
                if (field.getType() == FileFieldsIO.MSLAField.Type.Field) {
                    var f = fields.getClass().getDeclaredField(field.getName());
                    type = f.getGenericType();
                    f.setAccessible(true); value = f.get(fields); f.setAccessible(false);
                } else {
                    var m = fields.getClass().getDeclaredMethod(field.getName());
                    type = m.getGenericReturnType();
                    m.setAccessible(true); value = m.invoke(fields); m.setAccessible(false);
                    if (value == null) throw new IOException("Can't write null value of " + field);
                }
                if (value == null) throw new IOException("Can't write null value of " + field);
                logger.fine("Write field " + field + " of " + type + " - " + type.getClass());
                writeField(dos, type, value, (Integer) length, field.getCharset());
            }
            logger.fine("Finished writing a block of type " + fields.getClass().getName());
        } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IOException("Can't get field value to write", e);
        }
    }

    public final void write(MSLAFileBlockFields fields) throws IOException {
        var dos = (endianness == Endianness.BigEndian) ?
                new DataOutputStream(stream) :
                new LittleEndianDataOutputStream(stream);
        writeBlock(dos, fields);
        logger.fine("Finished writing a table");
    }
}
