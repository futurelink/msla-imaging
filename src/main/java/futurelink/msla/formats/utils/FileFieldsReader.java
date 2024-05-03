package futurelink.msla.formats.utils;

import com.google.common.io.LittleEndianDataInputStream;
import futurelink.msla.formats.iface.MSLAFileBlockFields;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FileFieldsReader {
    private final Logger logger = Logger.getLogger(FileFieldsReader.class.getName());
    public enum Endianness { BigEndian, LittleEndian }

    private final InputStream stream;
    private final Endianness endianness;

    public FileFieldsReader(InputStream in, Endianness endianness) {
        this.stream = in;
        this.endianness = endianness;
    }

    private int getSize(Type type, int length) {
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

    private long readField(DataInput dis, MSLAFileBlockFields fields, FileFieldsIO.MSLAField field) throws IOException {
        var dataRead = 0L;
        try {
            var length = !"".equals(field.getLengthAt()) ?
                    FileFieldsIO.getFieldValue(fields, field.getLengthAt()) :
                    field.getLength();
            if (field.getType() == FileFieldsIO.MSLAField.Type.Field) {
                var f = fields.getClass().getDeclaredField(field.getName());
                var type = f.getGenericType();
                f.setAccessible(true);
                if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                    var internalType = ((ParameterizedType) type).getRawType();
                    if (internalType == ArrayList.class) {
                        var listElementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        logger.fine("List generic type is " + listElementType + " of " + listElementType.getClass());
                        logger.fine("Reading " + length + " components");
                        var constr = ((Class<?>) listElementType).getDeclaredConstructor();
                        var array = new ArrayList<>();
                        f.set(fields, array);
                        for (int i = 0; i < (int) length; i++) {
                            var instance = constr.newInstance();
                            dataRead += readBlock(dis, (MSLAFileBlockFields) instance);
                            array.add(instance);
                        }
                    } else throw new IOException("Unknown internal type " + internalType);
                }
                else if (MSLAFileBlockFields.class.isAssignableFrom((Class<?>) type)) {
                    read((MSLAFileBlockFields) field);
                }
                else {
                    f.set(fields, readFieldValue(dis, type, (int) length, field.getCharset()));
                    if (!field.isDontCount()) dataRead = getSize(type, (int) length);
                }
                f.setAccessible(false);
                logger.fine("Setting value for " + field + " of " + type + " - " + type.getClass());
            } else {
                var m = fields.getClass().getDeclaredMethod(field.getName());;
                var type = m.getReturnType();
                var value = readFieldValue(dis, type, (int) length, field.getCharset()); // Read but just skip, do not set anywhere
                if (!field.isDontCount()) dataRead = getSize(type, (int) length);

                boolean hasSetter = false;
                try { m = fields.getClass().getDeclaredMethod("set" + field.getName(), type); hasSetter = true; }
                catch (NoSuchMethodException ignored) {}
                if (hasSetter) {
                    m.setAccessible(true);
                    m.invoke(fields, value);
                    m.setAccessible(false);
                    logger.fine("Calling setter for " + field + " of " + type);
                }
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IOException("Can't set field '" + field.getName() + "' with value that has been read", e);
        } catch (InstantiationException e) {
            throw new IOException("Can't instantiate internal data block object", e);
        }
        return dataRead;
    }

    private Object readFieldValue(DataInput stream, Type type, int length, Charset charset) throws IOException {
        if (type == int.class || type == Integer.class) return stream.readInt();
        else if (type == long.class || type == Long.class) return stream.readLong();
        else if (type == float.class || type == Float.class) return stream.readFloat();
        else if (type == double.class || type == Double.class) return stream.readDouble();
        else if (type == boolean.class || type == Boolean.class) return stream.readBoolean();
        else if (type == char.class || type == Character.class) return stream.readChar();
        else if (type == byte.class || type == Byte.class) return stream.readByte();
        else if (type == short.class || type == Short.class) return stream.readShort();
        else if (type == String.class || type == byte[].class) {
            if (length <= 0) throw new IOException("Length is not set for String or Array field - don't know how much to read");
            byte[] b = new byte[length];
            stream.readFully(b, 0, length);
            return (type == String.class) ? new String(b, charset).trim() : b;
        }
        else throw new IOException("Unknown type " + type);
    }

    private long readBlock(DataInput dis, MSLAFileBlockFields fields) throws IOException {
        var dataRead = 0L;
        var fieldsList = FileFieldsIO.getFields(fields.getClass());
        logger.fine(fieldsList.toString());
        for (var field : fieldsList) {
            if (fields.isFieldExcluded(field.getName())) continue;
            dataRead += readField(dis, fields, field);
        }
        return dataRead;
    }

    public final long read(MSLAFileBlockFields fields) throws IOException {
        var dis = (endianness == Endianness.BigEndian) ?
                new DataInputStream(stream) :
                new LittleEndianDataInputStream(stream);
        return readBlock(dis, fields);
    }
}
