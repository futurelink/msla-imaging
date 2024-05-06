package futurelink.msla.formats.utils;

import com.google.common.io.LittleEndianDataInputStream;
import futurelink.msla.formats.iface.MSLAFileBlockFields;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    private long readField(DataInput dis, MSLAFileBlockFields fields, FileFieldsIO.MSLAField field) throws IOException {
        var dataRead = 0L;
        try {
            var length = !"".equals(field.getLengthAt()) ?
                    FileFieldsIO.getFieldOrMethodValue(fields, field.getLengthAt()) :
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
                    logger.fine("Reading block of type " + type);
                    dataRead = readBlock(dis, (MSLAFileBlockFields) f.get(fields));
                }
                else {
                    f.set(fields, readFieldValue(dis, type, (int) length, field.getCharset()));
                    if (!field.isDontCount()) dataRead = FileFieldsIO.getTypeSize(type, (int) length);
                }
                f.setAccessible(false);
                logger.fine("Setting value for " + field + " of " + type + " - " + type.getClass());
            } else {
                var m = fields.getClass().getDeclaredMethod(field.getName());
                var type = m.getReturnType();
                var value = readFieldValue(dis, type, (int) length, field.getCharset()); // Read but just skip, do not set anywhere
                if (!field.isDontCount()) dataRead = FileFieldsIO.getTypeSize(type, (int) length);

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

    private Object[] getReadArrayObject(Type elementType, int length) {
        if (elementType == Integer.class) return new Integer[length];
        else if (elementType == Short.class) return new Short[length];
        else if (elementType == Float.class) return new Float[length];
        else if (elementType == int.class) return new Integer[length];
        else if (elementType == short.class) return new Short[length];
        else if (elementType == float.class) return new Float[length];
        return null;
    }

    private Object readStringOrBytes(DataInput stream, Type type, int length, Charset charset) throws IOException {
        logger.fine("Reading String or byte[] of " + type + " length " + length);
        if (length <= 0) throw new IOException("Length is not set for String or byte[] field - don't know how much to read");
        byte[] b = new byte[length];
        stream.readFully(b, 0, length);
        return (type == String.class) ? new String(b, charset).trim() : b;
    }

    private Object readFieldArray(DataInput stream, Type arrayType, int length) throws IOException {
        logger.fine("Reading array of " + arrayType + " length " + length);
        if (length <= 0) throw new IOException("Length is not set for array field - don't know how much to read");
        if (!((Class<?>) arrayType).isArray()) throw new IOException("Type " + arrayType + " is not an array ");
        try {
            var elementType = ((Class<?>) arrayType).getComponentType();
            var readMethodName = FileFieldsIO.getReadMethodName(elementType);
            if (readMethodName == null) throw new IOException("Unknown array component type " + elementType);
            var inst = getReadArrayObject(elementType, length);
            if (inst == null) throw new IOException("Can't instantiate internal data block object");
            var method = stream.getClass().getDeclaredMethod(readMethodName);
            for (int i = 0; i < length; i++) inst[i] = method.invoke(stream);
            return inst;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IOException("Can't read array into internal data block object", e);
        }
    }

    private Object readFieldValue(DataInput stream, Type type, int length, Charset charset) throws IOException {
        var readMethodName = FileFieldsIO.getReadMethodName(type);
        if (readMethodName != null) {
            try {
                return stream.getClass().getDeclaredMethod(readMethodName).invoke(stream);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new IOException("Can't read value", e);
            }
        }
        else if (type == String.class || type == byte[].class) return readStringOrBytes(stream, type, length, charset);
        else if (((Class<?>) type).isArray()) return readFieldArray(stream, type, length);
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
