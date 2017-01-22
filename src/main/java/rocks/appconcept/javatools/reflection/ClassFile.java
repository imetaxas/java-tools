package rocks.appconcept.javatools.reflection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import rocks.appconcept.javatools.io.StreamUtils;

/**
 * Helper for constructing class files.
 *
 * @author yanimetaxas
 */
public class ClassFile {

  private final int accessFlags;
  private final String className;
  private final String baseClass;
  private final List<String> interfaces;
  private final List<Field> fields = new ArrayList<>();
  private final List<Method> methods = new ArrayList<>();

  @FunctionalInterface
  public interface CodeWriter {

    void write(ConstantPool constantPool, DataOutputStream out) throws IOException;
  }

  public ClassFile(int accessFlags, String className, String baseClass, String... interfaces) {
    this.accessFlags = accessFlags;
    this.className = className;
    this.baseClass = baseClass;
    this.interfaces = Arrays.asList(interfaces);
  }

  public void addField(int accessFlags, String name, String type) {
    this.fields.add(new Field(accessFlags, name, type));
  }

  public void addMethod(int accessFlags, String name, String signature, short maxStack,
      short maxLocals, CodeWriter codeWriter) {
    this.methods.add(new Method(accessFlags, name, signature, maxStack, maxLocals, codeWriter));
  }

  public byte[] toByteArray() {
    try {
      return StreamUtils.captureBytes(stream -> {

        ConstantPool constantPool = new ConstantPool();

        // 1. write fields and methods first, in order to compute the constant pool
        byte[] classData = StreamUtils.captureBytes(innerStream -> {
          DataOutputStream out = new DataOutputStream(innerStream);
          out.writeShort(accessFlags);
          out.writeShort(constantPool.getClass(className));
          out.writeShort(constantPool.getClass(baseClass));
          out.writeShort(interfaces.size());
          for (String iface : interfaces) {
            out.writeShort(constantPool.getClass(iface));
          }
          out.writeShort(fields.size());
          for (Field field : fields) {
            field.write(constantPool, out);
          }
          out.writeShort(methods.size());
          for (Method method : methods) {
            method.write(constantPool, out);
          }
          out.writeShort(0);
        });

        // 2. then prepend the class header and constant pool
        DataOutputStream out = new DataOutputStream(stream);
        out.writeInt(0xCAFEBABE);
        out.writeShort(0);
        out.writeShort(49);
        constantPool.write(out);
        out.write(classData);
        out.flush();
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class Field {

    private final int accessFlags;
    private final String name;
    private final String type;

    public Field(int accessFlags, String name, String type) {
      this.accessFlags = accessFlags;
      this.name = name;
      this.type = type;
    }

    public void write(ConstantPool constantPool, DataOutputStream out) throws IOException {
      out.writeShort(accessFlags);
      out.writeShort(constantPool.getUtf8(name));
      out.writeShort(constantPool.getUtf8(type.replaceAll("<[^>]+>+", "")));
      out.writeShort(1);
      out.writeShort(constantPool.getUtf8("Signature"));
      out.writeInt(2);
      out.writeShort(constantPool.getUtf8(type));
    }
  }

  private static class Method {

    private final int accessFlags;
    private final String name;
    private final String signature;
    private final short maxStack;
    private final short maxLocals;
    private final CodeWriter codeWriter;

    public Method(int accessFlags, String name, String signature, short maxStack, short maxLocals,
        CodeWriter codeWriter) {
      this.accessFlags = accessFlags;
      this.name = name;
      this.signature = signature;
      this.maxStack = maxStack;
      this.maxLocals = maxLocals;
      this.codeWriter = codeWriter;
    }

    public void write(ConstantPool constantPool, DataOutputStream out) throws IOException {
      out.writeShort(accessFlags);
      out.writeShort(constantPool.getUtf8(name));
      out.writeShort(constantPool.getUtf8(signature.replaceAll("<[^>]+>+", "")));
      out.writeShort(2); // attribute count
      out.writeShort(constantPool.getUtf8("Signature"));
      out.writeInt(2);
      out.writeShort(constantPool.getUtf8(signature));
      byte[] code = StreamUtils
          .captureBytes(s -> codeWriter.write(constantPool, new DataOutputStream(s)));
      out.writeShort(constantPool.getUtf8("Code"));
      out.writeInt(12 + code.length);
      out.writeShort(maxStack);
      out.writeShort(maxLocals);
      out.writeInt(code.length);
      out.write(code);
      out.writeShort(0);
      out.writeShort(0);
    }
  }

  public static class ConstantPool {

    private final List<ConstantPoolEntry> pool = new ArrayList<>(32);
    private final Map<Object, Short> map = new HashMap<>();

    public short getUtf8(String name) {
      return getIndex(name);
    }

    public short getClass(String className) {
      return getIndex(ClassConstants.CONSTANT_CLASS, getUtf8(className));
    }

    public short getNameAndType(String name, String descriptor) {
      return getIndex(ClassConstants.CONSTANT_NAMEANDTYPE, getUtf8(name), getUtf8(descriptor));
    }

    public short getFieldRef(String className, String fieldName, String signature) {
      return getIndex(ClassConstants.CONSTANT_FIELD, getClass(className),
          getNameAndType(fieldName, signature));
    }

    public short getMethodRef(String className, String name, String descriptor) {
      return getIndex(ClassConstants.CONSTANT_METHOD, getClass(className),
          getNameAndType(name, descriptor));
    }

    public short getIndex(Object value) {
      return getValue(new ConstantPoolEntry(value, 0, (short) 0, (short) 0));
    }

    public short getIndex(int tag, short value) {
      return getValue(new ConstantPoolEntry(null, tag, value, (short) 0));
    }

    public short getIndex(int tag, short value, short secondary) {
      return getValue(new ConstantPoolEntry(null, tag, value, secondary));
    }

    private short getValue(ConstantPoolEntry entry) {
      Short res = map.get(entry);
      if (res == null) {
        pool.add(entry);
        res = (short) pool.size();
        map.put(entry, res);
      }
      return res;
    }

    public void write(DataOutputStream out) throws IOException {
      out.writeShort(pool.size() + 1);
      for (ConstantPoolEntry entry : pool) {
        entry.write(out);
      }
    }

    private static class ConstantPoolEntry {

      private final Object value;
      private final int tag;
      private final short index0;
      private final short index1;

      public ConstantPoolEntry(Object value, int tag, short index0, short index1) {
        this.value = value;
        this.tag = tag;
        this.index0 = index0;
        this.index1 = index1;
      }

      public void write(DataOutputStream out) throws IOException {
        if (value instanceof String) {
          out.writeByte(ClassConstants.CONSTANT_UTF8);
          out.writeUTF((String) value);
        } else if (value instanceof Integer) {
          out.writeByte(ClassConstants.CONSTANT_INTEGER);
          out.writeInt(((Integer) value).intValue());
        } else if (value instanceof Float) {
          out.writeByte(ClassConstants.CONSTANT_FLOAT);
          out.writeFloat(((Float) value).floatValue());
        } else if (value instanceof Long) {
          out.writeByte(ClassConstants.CONSTANT_LONG);
          out.writeLong(((Long) value).longValue());
        } else if (value instanceof Double) {
          out.writeByte(ClassConstants.CONSTANT_DOUBLE);
          out.writeDouble(((Double) value).doubleValue());
        } else if (tag != 0) {
          out.writeByte(tag);
          out.writeShort(index0);
          if (tag == ClassConstants.CONSTANT_FIELD || tag == ClassConstants.CONSTANT_METHOD
              || tag == ClassConstants.CONSTANT_INTERFACEMETHOD
              || tag == ClassConstants.CONSTANT_NAMEANDTYPE) {
            out.writeShort(index1);
          }
        } else {
          throw new RuntimeException("Invalid constant pool");
        }
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }
        ConstantPoolEntry that = (ConstantPoolEntry) o;
        return Objects.equals(tag, that.tag) &&
            Objects.equals(index0, that.index0) &&
            Objects.equals(index1, that.index1) &&
            Objects.equals(value, that.value);
      }

      @Override
      public int hashCode() {
        return Objects.hash(value, tag, index0, index1);
      }
    }
  }
}
