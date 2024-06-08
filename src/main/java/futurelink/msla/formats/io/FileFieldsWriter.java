package futurelink.msla.formats.io;

import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;

public class FileFieldsWriter {
    private final Logger logger = Logger.getLogger(FileFieldsWriter.class.getName());

    private final OutputStream stream;
    private final FileFieldsIO.Endianness endianness;

    public FileFieldsWriter(OutputStream out, FileFieldsIO.Endianness endianness) {
        this.stream = out;
        this.endianness = endianness;
    }

    private void writeArray(DataOutput stream, Type arrayType, Object value, int length) throws IOException {
        if (!((Class<?>) arrayType).isArray()) throw new IOException("Not an array");
        var arrayLength = Array.getLength(value);
        if (length > arrayLength) throw new IOException("Length " + length + " too large (max is " + arrayLength + ")");
        var elementType = ((Class<?>) arrayType).getComponentType();
        for (int i = 0; i < length; i++) {
            if (elementType == int.class) stream.writeInt(((int[]) value)[i]);
            else if (elementType == Integer.class) stream.writeInt(((Integer[]) value)[i]);
            else if (elementType == short.class) stream.writeShort(((short[]) value)[i]);
            else if (elementType == Short.class) stream.writeShort(((Short[]) value)[i]);
            else if (elementType == double.class) stream.writeDouble(((double[]) value)[i]);
            else if (elementType == Double.class) stream.writeDouble(((Double[]) value)[i]);
            else if (elementType == long.class) stream.writeLong(((long[]) value)[i]);
            else if (elementType == Long.class) stream.writeLong(((Long[]) value)[i]);
            else if (elementType == byte.class) stream.writeByte(((byte[]) value)[i]);
            else if (elementType == Byte.class) stream.writeByte(((Byte[]) value)[i]);
            else throw new IOException("Unknown array component type " + elementType);
        }
    }

    private void writeField(DataOutput stream, Type type, Object value, int length, Charset charset)
            throws FileFieldsException, IOException
    {
        var writeMethodName = FileFieldsIO.getWriteMethodName(type);
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
            logger.fine("Writing fields of " + type);
            writeFields(stream, (MSLAFileBlockFields) value);
        }
        else if (MSLAFileBlock.class.isAssignableFrom((Class<?>) type)) {
            logger.fine("Writing block of " + type);
            writeBlock(stream, (MSLAFileBlock) value);
        } else throw new IOException("Unknown type " + type);
    }

    private void writeFields(DataOutput dos, MSLAFileBlockFields fields) throws FileFieldsException, IOException {
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

    private void writeBlock(DataOutput dos, MSLAFileBlock block) throws FileFieldsException, IOException {
        try {
            block.beforeWrite();
        } catch (MSLAException e) {
            throw new FileFieldsException("Could not execute before write operations", e);
        }
        writeFields(dos, block.getBlockFields());
    }

    public final void write(MSLAFileBlock block) throws FileFieldsException, IOException {
        var dos = (endianness == FileFieldsIO.Endianness.BigEndian) ?
                new DataOutputStream(stream) :
                new LittleEndianDataOutputStream(stream);
        writeBlock(dos, block);
        logger.fine("Finished writing a table");
    }
}
