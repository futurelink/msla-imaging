package futurelink.msla.formats.io;

import com.google.common.io.LittleEndianDataInputStream;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class FileFieldsReader {
    private final Logger logger = Logger.getLogger(FileFieldsReader.class.getName());
    private final int MAX_LENGTH = 10000000;

    private final InputStream stream;
    private final FileFieldsIO.Endianness endianness;

    public FileFieldsReader(InputStream in, FileFieldsIO.Endianness endianness) {
        this.stream = in;
        this.endianness = endianness;
    }

    private long readField(DataInput dis, MSLAFileBlockFields fields, FileFieldsIO.MSLAField field) throws FileFieldsException {
        var dataRead = 0L;
        try {
            var length = !"".equals(field.getLengthAt()) ?
                    FileFieldsIO.getFieldOrMethodValue(fields, field.getLengthAt()) :
                    field.getLength();
            if (field.getType() == FileFieldsIO.MSLAField.Type.Field) {
                var f = fields.getClass().getDeclaredField(field.getName());
                var type = f.getGenericType();
                logger.fine("Setting value for " + field + " of " + type + " - " + type.getClass());

                Method m = null;
                boolean hasSetter = false;
                try { m = fields.getClass().getDeclaredMethod("set" + field.getName(), f.getType()); hasSetter = true; }
                catch (NoSuchMethodException ignored) {}

                // Field type is a generic ParameterizedType
                if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                    var internalType = ((ParameterizedType) type).getRawType();
                    if (internalType == ArrayList.class) {
                        var listElementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        logger.fine("List generic type is " + listElementType + " of " + listElementType.getClass());
                        logger.fine("Reading " + length + " components");
                        var constr = ((Class<?>) listElementType).getDeclaredConstructor();
                        var array = new ArrayList<>();
                        if (hasSetter) {
                            logger.fine("Calling setter for array " + field + " of " + type);
                            m.setAccessible(true);
                            m.invoke(fields, array);
                            m.setAccessible(false);
                        } else {
                            f.setAccessible(true);
                            f.set(fields, array);
                            f.setAccessible(false);
                        }
                        for (int i = 0; i < (int) length; i++) {
                            var instance = constr.newInstance();
                            dataRead += readFields(dis, (MSLAFileBlockFields) instance);
                            array.add(instance);
                        }
                    } else throw new FileFieldsException("Unknown internal type " + internalType);
                }

                // Field type is a MSLAFileBlockFields
                else if (MSLAFileBlockFields.class.isAssignableFrom((Class<?>) type)) {
                    logger.fine("Reading block of type " + type);
                    f.setAccessible(true);
                    dataRead = readFields(dis, (MSLAFileBlockFields) f.get(fields));
                    f.setAccessible(false);
                }

                // Field type is a MSLAFileBlockFields
                else if (MSLAFileBlock.class.isAssignableFrom((Class<?>) type)) {
                    logger.fine("Reading block of type " + type);
                    f.setAccessible(true);
                    dataRead = readBlock(dis, (MSLAFileBlock) f.get(fields));
                    f.setAccessible(false);
                }

                // Field type is another common type
                else {
                    var value = readFieldValue(dis, type, (int) length, field.getCharset());
                    if (hasSetter) {
                        logger.fine("Calling setter for " + field + " of " + type);
                        m.setAccessible(true);
                        m.invoke(fields, value);
                        m.setAccessible(false);
                    } else {
                        f.setAccessible(true);
                        f.set(fields, value);
                        f.setAccessible(false);
                    }
                    if (!field.isDontCount()) {
                        if (FileFieldsIO.isStringOrByteArray(type)) dataRead += (int) length;
                        else dataRead = FileFieldsIO.getTypeSize(type);
                    }
                }
            } else {
                var m = fields.getClass().getDeclaredMethod(field.getName());
                var type = m.getReturnType();
                var value = readFieldValue(dis, type, (int) length, field.getCharset());
                if (!field.isDontCount()) {
                    if (type == String.class) dataRead = (int) length; // Assume string character has 1 byte length
                    else if (value.getClass().isArray()) {
                        dataRead = (long) ((int) length) * FileFieldsIO.getTypeSize(value.getClass().getComponentType());
                    } else dataRead = FileFieldsIO.getTypeSize(type);
                }

                boolean hasSetter = false;
                try { m = fields.getClass().getDeclaredMethod("set" + field.getName(), type); hasSetter = true; }
                catch (NoSuchMethodException ignored) {}
                if (hasSetter) {
                    logger.fine("Calling setter for " + field + " of " + type);
                    m.setAccessible(true);
                    m.invoke(fields, value);
                    m.setAccessible(false);
                }
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new FileFieldsException("Can't set field '" + field.getName() + "' with value that has been read", e);
        } catch (InstantiationException e) {
            throw new FileFieldsException("Can't instantiate internal data block object", e);
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
        else if (elementType == Byte.class) return new Byte[length];
        return null;
    }

    private Object readStringOrBytes(DataInput stream, Type type, int length, Charset charset) throws FileFieldsException {
        logger.fine("Reading String or byte[] of " + type + " length " + length);
        if (length <= 0) throw new FileFieldsException("Length is not set for String or byte[] field - don't know how much to read");
        if (length > MAX_LENGTH) throw new FileFieldsException("Field length " + length + " is too large");
        var data = new byte[length];
        try {
            stream.readFully(data, 0, length);
        } catch (IOException e) { throw new FileFieldsException("Could not read data", e); }
        if (type == String.class) return new String(data, charset).trim();
        else if (type == byte[].class) return data;
        else if (type == Byte[].class) {
            var b = new Byte[length];
            Arrays.setAll(b, i -> data[i]);
            return b;
        }
        else throw new FileFieldsException("Could not read String or byte[] data, type is invalid");
    }

    private Object readFieldArray(DataInput stream, Type arrayType, int length) throws FileFieldsException {
        logger.fine("Reading array of " + arrayType + " length " + length);
        if (length <= 0) throw new FileFieldsException("Length is not set for array field - don't know how much to read");
        if (length > MAX_LENGTH) throw new FileFieldsException("Array field length " + length + " is too large");
        if (!((Class<?>) arrayType).isArray()) throw new FileFieldsException("Type " + arrayType + " is not an array ");
        try {
            var elementType = ((Class<?>) arrayType).getComponentType();
            var readMethodName = FileFieldsIO.getReadMethodName(elementType);
            if (readMethodName == null) throw new FileFieldsException("Unknown array component type " + elementType);
            var inst = getReadArrayObject(elementType, length);
            if (inst == null) throw new FileFieldsException("Can't instantiate internal data block object");
            var method = stream.getClass().getDeclaredMethod(readMethodName);
            for (int i = 0; i < length; i++) inst[i] = method.invoke(stream);
            return inst;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new FileFieldsException("Can't read array into internal data block object", e);
        }
    }

    private Object readFieldValue(DataInput stream, Type type, int length, Charset charset) throws FileFieldsException {
        var readMethodName = FileFieldsIO.getReadMethodName(type);
        if (readMethodName != null) {
            try {
                return stream.getClass().getDeclaredMethod(readMethodName).invoke(stream);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new FileFieldsException("Can't read value", e);
            }
        }
        else if (FileFieldsIO.isStringOrByteArray(type)) return readStringOrBytes(stream, type, length, charset);
        else if (((Class<?>) type).isArray()) return readFieldArray(stream, type, length);
        else throw new FileFieldsException("Unknown type " + type);
    }

    private long readFields(DataInput dis, MSLAFileBlockFields fields) throws FileFieldsException {
        var dataRead = 0L;
        var fieldsList = FileFieldsIO.getFields(fields.getClass());
        logger.fine(fieldsList.toString());
        for (var field : fieldsList) {
            if (fields.isFieldExcluded(field.getName())) continue;
            dataRead += readField(dis, fields, field);
        }
        return dataRead;
    }

    private long readBlock(DataInput dis, MSLAFileBlock block) throws FileFieldsException {
        block.beforeRead();
        var readBytes = readFields(dis, block.getFileFields());
        block.afterRead();
        return readBytes;
    }

    public final long read(MSLAFileBlock block) throws FileFieldsException {
        var dis = (endianness == FileFieldsIO.Endianness.BigEndian) ?
                new DataInputStream(stream) :
                new LittleEndianDataInputStream(stream);
        return readBlock(dis, block);
    }
}
