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

    private void writeField(DataOutput stream, Type type, Object value, int length, Charset charset) throws IOException {
        if (type == int.class || type == Integer.class) stream.writeInt((Integer) value);
        else if (type == long.class || type == Long.class) stream.writeLong((Long) value);
        else if (type == float.class || type == Float.class) stream.writeFloat((Float) value);
        else if (type == double.class || type == Double.class) stream.writeDouble((Double) value);
        else if (type == boolean.class || type == Boolean.class) stream.writeBoolean((Boolean) value);
        else if (type == char.class || type == Character.class) stream.writeChar((Character) value);
        else if (type == byte.class || type == Byte.class) stream.writeByte((Byte) value);
        else if (type == short.class || type == Short.class) stream.writeShort((Short) value);
        else if (type == String.class) {
            stream.write((length > 0) ?
                    Arrays.copyOfRange(((String) value).getBytes(charset), 0, length) :
                    ((String) value).getBytes(charset));
        }
        else if (type == byte[].class) {
            stream.write((length > 0) ?
                    Arrays.copyOfRange((byte[]) value, 0, length) :
                    (byte[]) value);
        }
        else if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
            var internalType = ((ParameterizedType) type).getRawType();
            if (internalType == ArrayList.class) {
                var listElementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                logger.fine("List generic type is " + listElementType + " of " + listElementType.getClass());
                for (var t : (List<?>) value) writeField(stream, listElementType, t, length, charset);
            } else throw new IOException("Unknown internal type " + internalType);
        }
        else if (MSLAFileBlockFields.class.isAssignableFrom((Class<?>) type))
            writeBlock(stream, (MSLAFileBlockFields) value);
        else throw new IOException("Unknown type " + type);
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
                        FileFieldsIO.getFieldValue(fields, field.getLengthAt()) :
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
        } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IOException("Can't get field value to write", e);
        }
    }

    public final void write(MSLAFileBlockFields fields) throws IOException {
        var dos = (endianness == Endianness.BigEndian) ?
                new DataOutputStream(stream) :
                new LittleEndianDataOutputStream(stream);
        writeBlock(dos, fields);
    }
}
