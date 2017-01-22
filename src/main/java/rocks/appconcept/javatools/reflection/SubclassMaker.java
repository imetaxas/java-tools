package rocks.appconcept.javatools.reflection;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Based on sun.misc.ProxyGenerator
 *
 * @author yanimetaxas
 */
public class SubclassMaker implements ClassConstants {

  /**
   * name of field for storing a proxy instance's invocation handler
   */
  private final static String handlerFieldName = "h";

  /**
   * Generate a proxy class given a name and a list of proxy interfaces.
   */
  public static byte[] makeSubclass(String name, Class superClass) {
    return new SubclassMaker(name, superClass).generateClassFile();
  }

  /* preloaded Method objects for methods in java.lang.Object */
  private static Method hashCodeMethod;
  private static Method equalsMethod;
  private static Method toStringMethod;

  static {
    try {
      hashCodeMethod = Object.class.getMethod("hashCode");
      equalsMethod = Object.class.getMethod("equals", Object.class);
      toStringMethod = Object.class.getMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new NoSuchMethodError(e.getMessage());
    }
  }

  /**
   * name of proxy class
   */
  private String className;

  /**
   * proxy interfaces
   */
  private Class superClass;
  private List<String> interfaces = Collections.emptyList();

  /**
   * constant pool of class being generated
   */
  private ConstantPool cp = new ConstantPool();

  /**
   * FieldInfo struct for each field of generated class
   */
  private List<FieldInfo> fields = new ArrayList<>();

  /**
   * MethodInfo struct for each method of generated class
   */
  private List<MethodInfo> methods = new ArrayList<>();

  /**
   * maps method signature string to list of ProxyMethod objects for
   * proxy methods with that signature
   */
  private Map<String, List<ProxyMethod>> proxyMethods =
      new HashMap<>();

  /**
   * count of ProxyMethod objects added to proxyMethods
   */
  private int proxyMethodCount = 0;

  /**
   * Construct a ProxyGenerator to generate a proxy class with the
   * specified name and for the given interfaces.
   * <p>
   * A ProxyGenerator object contains the state for the ongoing
   * generation of a particular proxy class.
   */
  private SubclassMaker(String className, Class superClass) {
    this.className = className;
    this.superClass = superClass;
  }

  /**
   * Generate a class file for the proxy class.  This method drives the
   * class file generation process.
   */
  private byte[] generateClassFile() {

        /* ============================================================
         * Step 1: Assemble ProxyMethod objects for all methods to
         * generate proxy dispatching code for.
         */

        /*
         * Record that proxy methods are needed for the hashCode, equals,
         * and toString methods of java.lang.Object.  This is done before
         * the methods from the proxy interfaces so that the methods from
         * java.lang.Object take precedence over duplicate methods in the
         * proxy interfaces.
         */
    addProxyMethod(hashCodeMethod, Object.class);
    addProxyMethod(equalsMethod, Object.class);
    addProxyMethod(toStringMethod, Object.class);

        /*
         * Now record all of the methods from the proxy interfaces, giving
         * earlier interfaces precedence over later ones with duplicate
         * methods.
         */
    {
      Method[] methods = superClass.getMethods();
      for (Method method : methods) {
        if (Modifier.isFinal(method.getModifiers())) {
          continue;
        }
        addProxyMethod(method, superClass);
      }
    }


        /*
         * For each set of proxy methods with the same signature,
         * verify that the methods' return types are compatible.
         */
    proxyMethods.values().forEach(SubclassMaker::checkReturnTypes);

    fields.add(new FieldInfo(handlerFieldName,
        "Ljava/lang/reflect/InvocationHandler;",
        ACC_PRIVATE));

        /* ============================================================
         * Step 2: Assemble FieldInfo and MethodInfo structs for all of
         * fields and methods in the class we are generating.
         */
    try {
      methods.add(generateConstructor());

      for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
        for (ProxyMethod pm : sigmethods) {

          // add static field for method's Method object
          fields.add(new FieldInfo(pm.methodFieldName,
              "Ljava/lang/reflect/Method;",
              ACC_PRIVATE | ACC_STATIC));

          // generate code for proxy method and add it
          methods.add(pm.generateMethod());
        }
      }

      methods.add(generateStaticInitializer());

    } catch (IOException e) {
      throw new InternalError("unexpected I/O Exception");
    }

    if (methods.size() > 65535) {
      throw new IllegalArgumentException("method limit exceeded");
    }
    if (fields.size() > 65535) {
      throw new IllegalArgumentException("field limit exceeded");
    }

    return writeClassFileToBytes();
  }

  private byte[] writeClassFileToBytes() {

            /* ============================================================
         * Step 3: Write the final class file.
         */

        /*
         * Make sure that constant pool indexes are reserved for the
         * following items before starting to write the final class file.
         */
    cp.getClass(dotToSlash(className));
    cp.getClass(dotToSlash(superClass.getName()));
    for (String anInterface1 : interfaces) {
      cp.getClass(dotToSlash(anInterface1));
    }

        /*
         * Disallow new constant pool additions beyond this point, since
         * we are about to write the final constant pool table.
         */
    cp.setReadOnly();

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(bout);

    try {
            /*
             * Write all the items of the "ClassFile" structure.
             * See JVMS section 4.1.
             */
      // u4 magic;
      dout.writeInt(0xCAFEBABE);
      // u2 minor_version;
      dout.writeShort(CLASSFILE_MINOR_VERSION);
      // u2 major_version;
      dout.writeShort(CLASSFILE_MAJOR_VERSION);

      cp.write(dout);             // (write constant pool)

      // u2 access_flags;
      dout.writeShort(ACC_PUBLIC | ACC_FINAL | ACC_SUPER);
      // u2 this_class;
      dout.writeShort(cp.getClass(dotToSlash(className)));
      // u2 super_class;
      dout.writeShort(cp.getClass(dotToSlash(superClass.getName())));

      // u2 interfaces_count;
      dout.writeShort(interfaces.size());
      // u2 interfaces[interfaces_count];
      for (String anInterface : interfaces) {
        dout.writeShort(cp.getClass(dotToSlash(anInterface)));
      }

      // u2 fields_count;
      dout.writeShort(fields.size());
      // field_info fields[fields_count];
      for (FieldInfo f : fields) {
        f.write(dout);
      }

      // u2 methods_count;
      dout.writeShort(methods.size());
      // method_info methods[methods_count];
      for (MethodInfo m : methods) {
        m.write(dout);
      }

      // u2 attributes_count;
      dout.writeShort(0); // (no ClassFile attributes for proxy classes)

    } catch (IOException e) {
      throw new InternalError("unexpected I/O Exception");
    }

    return bout.toByteArray();
  }

  /**
   * Add another method to be proxied, either by creating a new
   * ProxyMethod object or augmenting an old one for a duplicate
   * method.
   * <p>
   * "fromClass" indicates the proxy interface that the method was
   * found through, which may be different from (a subinterface of)
   * the method's "declaring class".  Note that the first Method
   * object passed for a given name and descriptor identifies the
   * Method object (and thus the declaring class) that will be
   * passed to the invocation handler's "invoke" method for a given
   * set of duplicate methods.
   */
  private void addProxyMethod(Method m, Class fromClass) {
    String name = m.getName();
    Class[] parameterTypes = m.getParameterTypes();
    Class returnType = m.getReturnType();
    Class[] exceptionTypes = m.getExceptionTypes();

    String sig = name + getParameterDescriptors(parameterTypes);
    List<ProxyMethod> sigmethods = proxyMethods.get(sig);
    if (sigmethods != null) {
      for (ProxyMethod pm : sigmethods) {
        if (returnType == pm.returnType) {
                    /*
                     * Found a match: reduce exception types to the
                     * greatest set of exceptions that can thrown
                     * compatibly with the throws clauses of both
                     * overridden methods.
                     */
          List<Class> legalExceptions = new ArrayList<>();
          collectCompatibleTypes(
              exceptionTypes, pm.exceptionTypes, legalExceptions);
          collectCompatibleTypes(
              pm.exceptionTypes, exceptionTypes, legalExceptions);
          pm.exceptionTypes = new Class[legalExceptions.size()];
          pm.exceptionTypes =
              legalExceptions.toArray(pm.exceptionTypes);
          return;
        }
      }
    } else {
      sigmethods = new ArrayList<>(3);
      proxyMethods.put(sig, sigmethods);
    }
    sigmethods.add(new ProxyMethod(name, parameterTypes, returnType,
        exceptionTypes, fromClass));
  }

  /**
   * For a given set of proxy methods with the same signature, check
   * that their return types are compatible according to the Proxy
   * specification.
   * <p>
   * Specifically, if there is more than one such method, then all
   * of the return types must be reference types, and there must be
   * one return type that is assignable to each of the rest of them.
   */
  private static void checkReturnTypes(List<ProxyMethod> methods) {
        /*
         * If there is only one method with a given signature, there
         * cannot be a conflict.  This is the only case in which a
         * primitive (or void) return type is allowed.
         */
    if (methods.size() < 2) {
      return;
    }

        /*
         * List of return types that are not yet known to be
         * assignable from ("covered" by) any of the others.
         */
    LinkedList<Class> uncoveredReturnTypes = new LinkedList<>();

    nextNewReturnType:
    for (ProxyMethod pm : methods) {
      Class<?> newReturnType = pm.returnType;
      if (newReturnType.isPrimitive()) {
        throw new IllegalArgumentException(
            "methods with same signature " +
                getFriendlyMethodSignature(pm.methodName,
                    pm.parameterTypes) +
                " but incompatible return types: " +
                newReturnType.getName() + " and others");
      }
      boolean added = false;

            /*
             * Compare the new return type to the existing uncovered
             * return types.
             */
      ListIterator<Class> liter = uncoveredReturnTypes.listIterator();
      while (liter.hasNext()) {
        Class<?> uncoveredReturnType = liter.next();

                /*
                 * If an existing uncovered return type is assignable
                 * to this new one, then we can forget the new one.
                 */
        if (newReturnType.isAssignableFrom(uncoveredReturnType)) {
          assert !added;
          continue nextNewReturnType;
        }

                /*
                 * If the new return type is assignable to an existing
                 * uncovered one, then should replace the existing one
                 * with the new one (or just forget the existing one,
                 * if the new one has already be put in the list).
                 */
        if (uncoveredReturnType.isAssignableFrom(newReturnType)) {
          // (we can assume that each return type is unique)
          if (!added) {
            liter.set(newReturnType);
            added = true;
          } else {
            liter.remove();
          }
        }
      }

            /*
             * If we got through the list of existing uncovered return
             * types without an assignability relationship, then add
             * the new return type to the list of uncovered ones.
             */
      if (!added) {
        uncoveredReturnTypes.add(newReturnType);
      }
    }

        /*
         * We shouldn't end up with more than one return type that is
         * not assignable from any of the others.
         */
    if (uncoveredReturnTypes.size() > 1) {
      ProxyMethod pm = methods.get(0);
      throw new IllegalArgumentException(
          "methods with same signature " +
              getFriendlyMethodSignature(pm.methodName, pm.parameterTypes) +
              " but incompatible return types: " + uncoveredReturnTypes);
    }
  }


  /**
   * A FieldInfo object contains information about a particular field
   * in the class being generated.  The class mirrors the data items of
   * the "field_info" structure of the class file format (see JVMS 4.5).
   */
  private class FieldInfo {

    public int accessFlags;
    public String name;
    public String descriptor;

    public FieldInfo(String name, String descriptor, int accessFlags) {
      this.name = name;
      this.descriptor = descriptor;
      this.accessFlags = accessFlags;

            /*
             * Make sure that constant pool indexes are reserved for the
             * following items before starting to write the final class file.
             */
      cp.getUtf8(name);
      cp.getUtf8(descriptor);
    }

    public void write(DataOutputStream out) throws IOException {
            /*
             * Write all the items of the "field_info" structure.
             * See JVMS section 4.5.
             */
      // u2 access_flags;
      out.writeShort(accessFlags);
      // u2 name_index;
      out.writeShort(cp.getUtf8(name));
      // u2 descriptor_index;
      out.writeShort(cp.getUtf8(descriptor));
      // u2 attributes_count;
      out.writeShort(0);  // (no field_info attributes for proxy classes)
    }
  }

  /**
   * An ExceptionTableEntry object holds values for the data items of
   * an entry in the "exception_table" item of the "Code" attribute of
   * "method_info" structures (see JVMS 4.7.3).
   */
  private static class ExceptionTableEntry {

    public short startPc;
    public short endPc;
    public short handlerPc;
    public short catchType;

    public ExceptionTableEntry(short startPc, short endPc,
        short handlerPc, short catchType) {
      this.startPc = startPc;
      this.endPc = endPc;
      this.handlerPc = handlerPc;
      this.catchType = catchType;
    }
  }


  /**
   * A MethodInfo object contains information about a particular method
   * in the class being generated.  This class mirrors the data items of
   * the "method_info" structure of the class file format (see JVMS 4.6).
   */
  private class MethodInfo {

    public int accessFlags;
    public String name;
    public String descriptor;
    public short maxStack;
    public short maxLocals;
    public ByteArrayOutputStream code = new ByteArrayOutputStream();
    public List<ExceptionTableEntry> exceptionTable = new ArrayList<>();
    public short[] declaredExceptions;

    public MethodInfo(String name, String descriptor, int accessFlags) {
      this.name = name;
      this.descriptor = descriptor;
      this.accessFlags = accessFlags;

            /*
             * Make sure that constant pool indexes are reserved for the
             * following items before starting to write the final class file.
             */
      cp.getUtf8(name);
      cp.getUtf8(descriptor);
      cp.getUtf8("Code");
      cp.getUtf8("Exceptions");
    }

    public void write(DataOutputStream out) throws IOException {
            /*
             * Write all the items of the "method_info" structure.
             * See JVMS section 4.6.
             */
      // u2 access_flags;
      out.writeShort(accessFlags);
      // u2 name_index;
      out.writeShort(cp.getUtf8(name));
      // u2 descriptor_index;
      out.writeShort(cp.getUtf8(descriptor));
      // u2 attributes_count;

      int attributeCount = 1;
      if (declaredExceptions.length > 0) {
        attributeCount += 1;
      }

      out.writeShort(attributeCount);  // (two method_info attributes:)

      // Write "Code" attribute. See JVMS section 4.7.3.

      // u2 attribute_name_index;
      out.writeShort(cp.getUtf8("Code"));
      // u4 attribute_length;
      out.writeInt(12 + code.size() + 8 * exceptionTable.size());
      // u2 max_stack;
      out.writeShort(maxStack);
      // u2 max_locals;
      out.writeShort(maxLocals);
      // u2 code_length;
      out.writeInt(code.size());
      // u1 code[code_length];
      code.writeTo(out);
      // u2 exception_table_length;
      out.writeShort(exceptionTable.size());
      for (ExceptionTableEntry e : exceptionTable) {
        // u2 start_pc;
        out.writeShort(e.startPc);
        // u2 end_pc;
        out.writeShort(e.endPc);
        // u2 handler_pc;
        out.writeShort(e.handlerPc);
        // u2 catch_type;
        out.writeShort(e.catchType);
      }
      // u2 attributes_count;
      out.writeShort(0);

      // write "Exceptions" attribute.  See JVMS section 4.7.4.

      if (declaredExceptions.length > 0) {
        // u2 attribute_name_index;
        out.writeShort(cp.getUtf8("Exceptions"));
        // u4 attributes_length;
        out.writeInt(2 + 2 * declaredExceptions.length);
        // u2 number_of_exceptions;
        out.writeShort(declaredExceptions.length);
        // u2 exception_index_table[number_of_exceptions];
        for (short declaredException : declaredExceptions) {
          out.writeShort(declaredException);
        }
      }
    }
  }

  /**
   * A ProxyMethod object represents a proxy method in the proxy class
   * being generated: a method whose implementation will encode and
   * dispatch invocations to the proxy instance's invocation handler.
   */
  private class ProxyMethod {

    public String methodName;
    public Class[] parameterTypes;
    public Class returnType;
    public Class[] exceptionTypes;
    public Class fromClass;
    public String methodFieldName;

    private ProxyMethod(String methodName, Class[] parameterTypes,
        Class returnType, Class[] exceptionTypes,
        Class fromClass) {
      this.methodName = methodName;
      this.parameterTypes = parameterTypes;
      this.returnType = returnType;
      this.exceptionTypes = exceptionTypes;
      this.fromClass = fromClass;
      this.methodFieldName = "m" + proxyMethodCount++;
    }

    /**
     * Return a MethodInfo object for this method, including generating
     * the code and exception table entry.
     */
    private MethodInfo generateMethod() throws IOException {
      String desc = getMethodDescriptor(parameterTypes, returnType);
      MethodInfo minfo = new MethodInfo(methodName, desc,
          ACC_PUBLIC | ACC_FINAL);

      int[] parameterSlot = new int[parameterTypes.length];
      int nextSlot = 1;
      for (int i = 0; i < parameterSlot.length; i++) {
        parameterSlot[i] = nextSlot;
        nextSlot += getWordsPerType(parameterTypes[i]);
      }
      int localSlot0 = nextSlot;
      short pc, tryBegin = 0, tryEnd;

      DataOutputStream out = new DataOutputStream(minfo.code);

      code_aload(0, out);

      out.writeByte(opc_getfield);
      out.writeShort(cp.getFieldRef(
          dotToSlash(className),
          handlerFieldName, "Ljava/lang/reflect/InvocationHandler;"));

      code_aload(0, out);
      //out.writeByte(ClassConstants.opc_aconst_null); // load null instead of proxy object

      out.writeByte(opc_getstatic);
      out.writeShort(cp.getFieldRef(
          dotToSlash(className),
          methodFieldName, "Ljava/lang/reflect/Method;"));

      if (parameterTypes.length > 0) {

        code_ipush(parameterTypes.length, out);

        out.writeByte(opc_anewarray);
        out.writeShort(cp.getClass("java/lang/Object"));

        for (int i = 0; i < parameterTypes.length; i++) {

          out.writeByte(opc_dup);

          code_ipush(i, out);

          codeWrapArgument(parameterTypes[i], parameterSlot[i], out);

          out.writeByte(opc_aastore);
        }
      } else {

        out.writeByte(opc_aconst_null);
      }

      out.writeByte(opc_invokeinterface);
      out.writeShort(cp.getInterfaceMethodRef(
          "java/lang/reflect/InvocationHandler",
          "invoke",
          "(Ljava/lang/Object;Ljava/lang/reflect/Method;" +
              "[Ljava/lang/Object;)Ljava/lang/Object;"));
      out.writeByte(4);
      out.writeByte(0);

      if (returnType == void.class) {

        out.writeByte(opc_pop);

        out.writeByte(opc_return);

      } else {

        codeUnwrapReturnValue(returnType, out);
      }

      tryEnd = pc = (short) minfo.code.size();

      List<Class> catchList = computeUniqueCatchList(exceptionTypes);
      if (catchList.size() > 0) {

        for (Class ex : catchList) {
          minfo.exceptionTable.add(new ExceptionTableEntry(
              tryBegin, tryEnd, pc,
              cp.getClass(dotToSlash(ex.getName()))));
        }

        out.writeByte(opc_athrow);

        pc = (short) minfo.code.size();

        minfo.exceptionTable.add(new ExceptionTableEntry(
            tryBegin, tryEnd, pc, cp.getClass("java/lang/Throwable")));

        code_astore(localSlot0, out);

        out.writeByte(opc_new);
        out.writeShort(cp.getClass(
            "java/lang/reflect/UndeclaredThrowableException"));

        out.writeByte(opc_dup);

        code_aload(localSlot0, out);

        out.writeByte(opc_invokespecial);

        out.writeShort(cp.getMethodRef(
            "java/lang/reflect/UndeclaredThrowableException",
            "<init>", "(Ljava/lang/Throwable;)V"));

        out.writeByte(opc_athrow);
      }

      if (minfo.code.size() > 65535) {
        throw new IllegalArgumentException("code size limit exceeded");
      }

      minfo.maxStack = 10;
      minfo.maxLocals = (short) (localSlot0 + 1);
      minfo.declaredExceptions = new short[exceptionTypes.length];
      for (int i = 0; i < exceptionTypes.length; i++) {
        minfo.declaredExceptions[i] = cp.getClass(
            dotToSlash(exceptionTypes[i].getName()));
      }

      return minfo;
    }

    /**
     * Generate code for wrapping an argument of the given type
     * whose value can be found at the specified local variable
     * index, in order for it to be passed (as an Object) to the
     * invocation handler's "invoke" method.  The code is written
     * to the supplied stream.
     */
    private void codeWrapArgument(Class type, int slot,
        DataOutputStream out)
        throws IOException {
      if (type.isPrimitive()) {
        PrimitiveTypeInfo prim = PrimitiveTypeInfo.get(type);

        if (type == int.class ||
            type == boolean.class ||
            type == byte.class ||
            type == char.class ||
            type == short.class) {
          code_iload(slot, out);
        } else if (type == long.class) {
          code_lload(slot, out);
        } else if (type == float.class) {
          code_fload(slot, out);
        } else if (type == double.class) {
          code_dload(slot, out);
        } else {
          throw new AssertionError();
        }

        out.writeByte(opc_invokestatic);
        out.writeShort(cp.getMethodRef(
            prim.wrapperClassName,
            "valueOf", prim.wrapperValueOfDesc));

      } else {

        code_aload(slot, out);
      }
    }

    /**
     * Generate code for unwrapping a return value of the given
     * type from the invocation handler's "invoke" method (as type
     * Object) to its correct type.  The code is written to the
     * supplied stream.
     */
    private void codeUnwrapReturnValue(Class type, DataOutputStream out)
        throws IOException {
      if (type.isPrimitive()) {
        PrimitiveTypeInfo prim = PrimitiveTypeInfo.get(type);

        out.writeByte(opc_checkcast);
        out.writeShort(cp.getClass(prim.wrapperClassName));

        out.writeByte(opc_invokevirtual);
        out.writeShort(cp.getMethodRef(
            prim.wrapperClassName,
            prim.unwrapMethodName, prim.unwrapMethodDesc));

        if (type == int.class ||
            type == boolean.class ||
            type == byte.class ||
            type == char.class ||
            type == short.class) {
          out.writeByte(opc_ireturn);
        } else if (type == long.class) {
          out.writeByte(opc_lreturn);
        } else if (type == float.class) {
          out.writeByte(opc_freturn);
        } else if (type == double.class) {
          out.writeByte(opc_dreturn);
        } else {
          throw new AssertionError();
        }

      } else {

        out.writeByte(opc_checkcast);
        out.writeShort(cp.getClass(dotToSlash(type.getName())));

        out.writeByte(opc_areturn);
      }
    }

    /**
     * Generate code for initializing the static field that stores
     * the Method object for this proxy method.  The code is written
     * to the supplied stream.
     */
    private void codeFieldInitialization(DataOutputStream out)
        throws IOException {
      codeClassForName(fromClass, out);

      code_ldc(cp.getString(methodName), out);

      code_ipush(parameterTypes.length, out);

      out.writeByte(opc_anewarray);
      out.writeShort(cp.getClass("java/lang/Class"));

      for (int i = 0; i < parameterTypes.length; i++) {

        out.writeByte(opc_dup);

        code_ipush(i, out);

        if (parameterTypes[i].isPrimitive()) {
          PrimitiveTypeInfo prim =
              PrimitiveTypeInfo.get(parameterTypes[i]);

          out.writeByte(opc_getstatic);
          out.writeShort(cp.getFieldRef(
              prim.wrapperClassName, "TYPE", "Ljava/lang/Class;"));

        } else {
          codeClassForName(parameterTypes[i], out);
        }

        out.writeByte(opc_aastore);
      }

      out.writeByte(opc_invokevirtual);
      out.writeShort(cp.getMethodRef(
          "java/lang/Class",
          "getMethod",
          "(Ljava/lang/String;[Ljava/lang/Class;)" +
              "Ljava/lang/reflect/Method;"));

      out.writeByte(opc_putstatic);
      out.writeShort(cp.getFieldRef(
          dotToSlash(className),
          methodFieldName, "Ljava/lang/reflect/Method;"));
    }
  }

  /**
   * Generate the constructor method for the proxy class.
   */
  private MethodInfo generateConstructor() throws IOException {

    MethodInfo minfo = new MethodInfo(
        "<init>", "(Ljava/lang/reflect/InvocationHandler;)V",
        ACC_PUBLIC);

    DataOutputStream out = new DataOutputStream(minfo.code);

    code_aload(0, out);
    out.writeByte(opc_invokespecial);
    out.writeShort(cp.getMethodRef(
        dotToSlash(superClass.getName()),
        "<init>", "()V")); // "(Ljava/lang/reflect/InvocationHandler;)V"

    code_aload(0, out);
    code_aload(1, out);
    out.writeByte(opc_putfield);
    out.writeShort(cp.getFieldRef(
        dotToSlash(className),
        handlerFieldName, "Ljava/lang/reflect/InvocationHandler;"));

    out.writeByte(opc_return);

    minfo.maxStack = 10;
    minfo.maxLocals = 2;
    minfo.declaredExceptions = new short[0];

    return minfo;
  }

  /**
   * Generate the static initializer method for the proxy class.
   */
  private MethodInfo generateStaticInitializer() throws IOException {
    MethodInfo minfo = new MethodInfo(
        "<clinit>", "()V", ACC_STATIC);

    int localSlot0 = 1;
    short pc, tryBegin = 0, tryEnd;

    DataOutputStream out = new DataOutputStream(minfo.code);

    for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
      for (ProxyMethod pm : sigmethods) {
        pm.codeFieldInitialization(out);
      }
    }

    out.writeByte(opc_return);

    tryEnd = pc = (short) minfo.code.size();

    minfo.exceptionTable.add(new ExceptionTableEntry(
        tryBegin, tryEnd, pc,
        cp.getClass("java/lang/NoSuchMethodException")));

    code_astore(localSlot0, out);

    out.writeByte(opc_new);
    out.writeShort(cp.getClass("java/lang/NoSuchMethodError"));

    out.writeByte(opc_dup);

    code_aload(localSlot0, out);

    out.writeByte(opc_invokevirtual);
    out.writeShort(cp.getMethodRef(
        "java/lang/Throwable", "getMessage", "()Ljava/lang/String;"));

    out.writeByte(opc_invokespecial);
    out.writeShort(cp.getMethodRef(
        "java/lang/NoSuchMethodError", "<init>", "(Ljava/lang/String;)V"));

    out.writeByte(opc_athrow);

    pc = (short) minfo.code.size();

    minfo.exceptionTable.add(new ExceptionTableEntry(
        tryBegin, tryEnd, pc,
        cp.getClass("java/lang/ClassNotFoundException")));

    code_astore(localSlot0, out);

    out.writeByte(opc_new);
    out.writeShort(cp.getClass("java/lang/NoClassDefFoundError"));

    out.writeByte(opc_dup);

    code_aload(localSlot0, out);

    out.writeByte(opc_invokevirtual);
    out.writeShort(cp.getMethodRef(
        "java/lang/Throwable", "getMessage", "()Ljava/lang/String;"));

    out.writeByte(opc_invokespecial);
    out.writeShort(cp.getMethodRef(
        "java/lang/NoClassDefFoundError",
        "<init>", "(Ljava/lang/String;)V"));

    out.writeByte(opc_athrow);

    if (minfo.code.size() > 65535) {
      throw new IllegalArgumentException("code size limit exceeded");
    }

    minfo.maxStack = 10;
    minfo.maxLocals = (short) (localSlot0 + 1);
    minfo.declaredExceptions = new short[0];

    return minfo;
  }


    /*
     * =============== Code Generation Utility Methods ===============
     */

    /*
     * The following methods generate code for the load or store operation
     * indicated by their name for the given local variable.  The code is
     * written to the supplied stream.
     */

  private void code_iload(int lvar, DataOutputStream out)
      throws IOException {
    codeLocalLoadStore(lvar, opc_iload, opc_iload_0, out);
  }

  private void code_lload(int lvar, DataOutputStream out)
      throws IOException {
    codeLocalLoadStore(lvar, opc_lload, opc_lload_0, out);
  }

  private void code_fload(int lvar, DataOutputStream out)
      throws IOException {
    codeLocalLoadStore(lvar, opc_fload, opc_fload_0, out);
  }

  private void code_dload(int lvar, DataOutputStream out)
      throws IOException {
    codeLocalLoadStore(lvar, opc_dload, opc_dload_0, out);
  }

  private void code_aload(int lvar, DataOutputStream out)
      throws IOException {
    codeLocalLoadStore(lvar, opc_aload, opc_aload_0, out);
  }

//  private void code_istore(int lvar, DataOutputStream out)
//      throws IOException
//  {
//      codeLocalLoadStore(lvar, opc_istore, opc_istore_0, out);
//  }

//  private void code_lstore(int lvar, DataOutputStream out)
//      throws IOException
//  {
//      codeLocalLoadStore(lvar, opc_lstore, opc_lstore_0, out);
//  }

//  private void code_fstore(int lvar, DataOutputStream out)
//      throws IOException
//  {
//      codeLocalLoadStore(lvar, opc_fstore, opc_fstore_0, out);
//  }

//  private void code_dstore(int lvar, DataOutputStream out)
//      throws IOException
//  {
//      codeLocalLoadStore(lvar, opc_dstore, opc_dstore_0, out);
//  }

  private void code_astore(int lvar, DataOutputStream out)
      throws IOException {
    codeLocalLoadStore(lvar, opc_astore, opc_astore_0, out);
  }

  /**
   * Generate code for a load or store instruction for the given local
   * variable.  The code is written to the supplied stream.
   * <p>
   * "opcode" indicates the opcode form of the desired load or store
   * instruction that takes an explicit local variable index, and
   * "opcode_0" indicates the corresponding form of the instruction
   * with the implicit index 0.
   */
  private void codeLocalLoadStore(int lvar, int opcode, int opcode_0,
      DataOutputStream out)
      throws IOException {
    assert lvar >= 0 && lvar <= 0xFFFF;
    if (lvar <= 3) {
      out.writeByte(opcode_0 + lvar);
    } else if (lvar <= 0xFF) {
      out.writeByte(opcode);
      out.writeByte(lvar & 0xFF);
    } else {
            /*
             * Use the "wide" instruction modifier for local variable
             * indexes that do not fit into an unsigned byte.
             */
      out.writeByte(opc_wide);
      out.writeByte(opcode);
      out.writeShort(lvar & 0xFFFF);
    }
  }

  /**
   * Generate code for an "ldc" instruction for the given constant pool
   * index (the "ldc_w" instruction is used if the index does not fit
   * into an unsigned byte).  The code is written to the supplied stream.
   */
  private void code_ldc(int index, DataOutputStream out)
      throws IOException {
    assert index >= 0 && index <= 0xFFFF;
    if (index <= 0xFF) {
      out.writeByte(opc_ldc);
      out.writeByte(index & 0xFF);
    } else {
      out.writeByte(opc_ldc_w);
      out.writeShort(index & 0xFFFF);
    }
  }

  /**
   * Generate code to push a constant integer value on to the operand
   * stack, using the "iconst_<i>", "bipush", or "sipush" instructions
   * depending on the size of the value.  The code is written to the
   * supplied stream.
   */
  private void code_ipush(int value, DataOutputStream out)
      throws IOException {
    if (value >= -1 && value <= 5) {
      out.writeByte(opc_iconst_0 + value);
    } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
      out.writeByte(opc_bipush);
      out.writeByte(value & 0xFF);
    } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
      out.writeByte(opc_sipush);
      out.writeShort(value & 0xFFFF);
    } else {
      throw new AssertionError();
    }
  }

  /**
   * Generate code to invoke the Class.forName with the name of the given
   * class to get its Class object at runtime.  The code is written to
   * the supplied stream.  Note that the code generated by this method
   * may caused the checked ClassNotFoundException to be thrown.
   */
  private void codeClassForName(Class cl, DataOutputStream out)
      throws IOException {
    code_ldc(cp.getString(cl.getName()), out);

    out.writeByte(opc_invokestatic);
    out.writeShort(cp.getMethodRef(
        "java/lang/Class",
        "forName", "(Ljava/lang/String;)Ljava/lang/Class;"));
  }


    /*
     * ==================== General Utility Methods ====================
     */

  /**
   * Convert a fully qualified class name that uses '.' as the package
   * separator, the external representation used by the Java language
   * and APIs, to a fully qualified class name that uses '/' as the
   * package separator, the representation used in the class file
   * format (see JVMS section 4.2).
   */
  private static String dotToSlash(String name) {
    return name.replace('.', '/');
  }

  /**
   * Return the "method descriptor" string for a method with the given
   * parameter types and return type.  See JVMS section 4.3.3.
   */
  private static String getMethodDescriptor(Class[] parameterTypes,
      Class returnType) {
    return getParameterDescriptors(parameterTypes) +
        ((returnType == void.class) ? "V" : getFieldType(returnType));
  }

  /**
   * Return the list of "parameter descriptor" strings enclosed in
   * parentheses corresponding to the given parameter types (in other
   * words, a method descriptor without a return descriptor).  This
   * string is useful for constructing string keys for methods without
   * regard to their return type.
   */
  private static String getParameterDescriptors(Class[] parameterTypes) {
    StringBuilder desc = new StringBuilder("(");
    for (Class parameterType : parameterTypes) {
      desc.append(getFieldType(parameterType));
    }
    desc.append(')');
    return desc.toString();
  }

  /**
   * Return the "field type" string for the given type, appropriate for
   * a field descriptor, a parameter descriptor, or a return descriptor
   * other than "void".  See JVMS section 4.3.2.
   */
  private static String getFieldType(Class type) {
    if (type.isPrimitive()) {
      return PrimitiveTypeInfo.get(type).baseTypeString;
    } else if (type.isArray()) {
            /*
             * According to JLS 20.3.2, the getName() method on Class does
             * return the VM type descriptor format for array classes (only);
             * using that should be quicker than the otherwise obvious code:
             *
             *     return "[" + getTypeDescriptor(type.getComponentType());
             */
      return type.getName().replace('.', '/');
    } else {
      return "L" + dotToSlash(type.getName()) + ";";
    }
  }

  /**
   * Returns a human-readable string representing the signature of a
   * method with the given name and parameter types.
   */
  private static String getFriendlyMethodSignature(String name,
      Class[] parameterTypes) {
    StringBuilder sig = new StringBuilder(name);
    sig.append('(');
    for (int i = 0; i < parameterTypes.length; i++) {
      if (i > 0) {
        sig.append(',');
      }
      Class parameterType = parameterTypes[i];
      int dimensions = 0;
      while (parameterType.isArray()) {
        parameterType = parameterType.getComponentType();
        dimensions++;
      }
      sig.append(parameterType.getName());
      while (dimensions-- > 0) {
        sig.append("[]");
      }
    }
    sig.append(')');
    return sig.toString();
  }

  /**
   * Return the number of abstract "words", or consecutive local variable
   * indexes, required to contain a value of the given type.  See JVMS
   * section 3.6.1.
   * <p>
   * Note that the original version of the JVMS contained a definition of
   * this abstract notion of a "word" in section 3.4, but that definition
   * was removed for the second edition.
   */
  private static int getWordsPerType(Class type) {
    if (type == long.class || type == double.class) {
      return 2;
    } else {
      return 1;
    }
  }

  /**
   * Add to the given list all of the types in the "from" array that
   * are not already contained in the list and are assignable to at
   * least one of the types in the "with" array.
   * <p>
   * This method is useful for computing the greatest common set of
   * declared exceptions from duplicate methods inherited from
   * different interfaces.
   */
  private static void collectCompatibleTypes(Class[] from, Class<?>[] with,
      List<Class> list) {
    for (Class aFrom : from) {
      if (!list.contains(aFrom)) {
        for (Class<?> aWith : with) {
          if (aWith.isAssignableFrom(aFrom)) {
            list.add(aFrom);
            break;
          }
        }
      }
    }
  }

  /**
   * Given the exceptions declared in the throws clause of a proxy method,
   * compute the exceptions that need to be caught from the invocation
   * handler's invoke method and rethrown intact in the method's
   * implementation before catching other Throwables and wrapping them
   * in UndeclaredThrowableExceptions.
   * <p>
   * The exceptions to be caught are returned in a List object.  Each
   * exception in the returned list is guaranteed to not be a subclass of
   * any of the other exceptions in the list, so the catch blocks for
   * these exceptions may be generated in any order relative to each other.
   * <p>
   * Error and RuntimeException are each always contained by the returned
   * list (if none of their superclasses are contained), since those
   * unchecked exceptions should always be rethrown intact, and thus their
   * subclasses will never appear in the returned list.
   * <p>
   * The returned List will be empty if java.lang.Throwable is in the
   * given list of declared exceptions, indicating that no exceptions
   * need to be caught.
   */
  private static List<Class> computeUniqueCatchList(Class[] exceptions) {
    List<Class> uniqueList = new ArrayList<>();
    // unique exceptions to catch

    uniqueList.add(Error.class);            // always catch/rethrow these
    uniqueList.add(RuntimeException.class);

    nextException:
    for (Class<?> ex : exceptions) {
      if (ex.isAssignableFrom(Throwable.class)) {
                /*
                 * If Throwable is declared to be thrown by the proxy method,
                 * then no catch blocks are necessary, because the invoke
                 * can, at most, throw Throwable anyway.
                 */
        uniqueList.clear();
        break;
      } else if (!Throwable.class.isAssignableFrom(ex)) {
                /*
                 * Ignore types that cannot be thrown by the invoke method.
                 */
        continue;
      }
            /*
             * Compare this exception against the current list of
             * exceptions that need to be caught:
             */
      for (int j = 0; j < uniqueList.size(); ) {
        Class<?> ex2 = uniqueList.get(j);
        if (ex2.isAssignableFrom(ex)) {
                    /*
                     * if a superclass of this exception is already on
                     * the list to catch, then ignore this one and continue;
                     */
          continue nextException;
        } else if (ex.isAssignableFrom(ex2)) {
                    /*
                     * if a subclass of this exception is on the list
                     * to catch, then remove it;
                     */
          uniqueList.remove(j);
        } else {
          j++;        // else continue comparing.
        }
      }
      // This exception is unique (so far): add it to the list to catch.
      uniqueList.add(ex);
    }
    return uniqueList;
  }

  /**
   * A PrimitiveTypeInfo object contains assorted information about
   * a primitive type in its public fields.  The struct for a particular
   * primitive type can be obtained using the static "get" method.
   */
  private static class PrimitiveTypeInfo {

    /**
     * "base type" used in various descriptors (see JVMS section 4.3.2)
     */
    public String baseTypeString;

    /**
     * name of corresponding wrapper class
     */
    public String wrapperClassName;

    /**
     * method descriptor for wrapper class "valueOf" factory method
     */
    public String wrapperValueOfDesc;

    /**
     * name of wrapper class method for retrieving primitive value
     */
    public String unwrapMethodName;

    /**
     * descriptor of same method
     */
    public String unwrapMethodDesc;

    private static Map<Class, PrimitiveTypeInfo> table =
        new HashMap<>();

    static {
      add(byte.class, Byte.class);
      add(char.class, Character.class);
      add(double.class, Double.class);
      add(float.class, Float.class);
      add(int.class, Integer.class);
      add(long.class, Long.class);
      add(short.class, Short.class);
      add(boolean.class, Boolean.class);
    }

    private static void add(Class primitiveClass, Class wrapperClass) {
      table.put(primitiveClass,
          new PrimitiveTypeInfo(primitiveClass, wrapperClass));
    }

    private PrimitiveTypeInfo(Class primitiveClass, Class wrapperClass) {
      assert primitiveClass.isPrimitive();

      baseTypeString =
          Array.newInstance(primitiveClass, 0)
              .getClass().getName().substring(1);
      wrapperClassName = dotToSlash(wrapperClass.getName());
      wrapperValueOfDesc =
          "(" + baseTypeString + ")L" + wrapperClassName + ";";
      unwrapMethodName = primitiveClass.getName() + "Value";
      unwrapMethodDesc = "()" + baseTypeString;
    }

    public static PrimitiveTypeInfo get(Class cl) {
      return table.get(cl);
    }
  }


  /**
   * A ConstantPool object represents the constant pool of a class file
   * being generated.  This representation of a constant pool is designed
   * specifically for use by ProxyGenerator; in particular, it assumes
   * that constant pool entries will not need to be resorted (for example,
   * by their type, as the Java compiler does), so that the final index
   * value can be assigned and used when an entry is first created.
   * <p>
   * Note that new entries cannot be created after the constant pool has
   * been written to a class file.  To prevent such logic errors, a
   * ConstantPool instance can be marked "read only", so that further
   * attempts to add new entries will fail with a runtime exception.
   * <p>
   * See JVMS section 4.4 for more information about the constant pool
   * of a class file.
   */
  private static class ConstantPool {

    /**
     * list of constant pool entries, in constant pool index order.
     * <p>
     * This list is used when writing the constant pool to a stream
     * and for assigning the next index value.  Note that element 0
     * of this list corresponds to constant pool index 1.
     */
    private List<Entry> pool = new ArrayList<>(32);

    /**
     * maps constant pool data of all types to constant pool indexes.
     * <p>
     * This map is used to look up the index of an existing entry for
     * values of all types.
     */
    private Map<Object, Short> map = new HashMap<>(16);

    /**
     * true if no new constant pool entries may be added
     */
    private boolean readOnly = false;

    /**
     * Get or assign the index for a CONSTANT_Utf8 entry.
     */
    public short getUtf8(String s) {
      if (s == null) {
        throw new NullPointerException();
      }
      return getValue(s);
    }

    /**
     * Get or assign the index for a CONSTANT_Integer entry.
     */
 /*       public short getInteger(int i) {
            return getValue(i);
        }
*/
    /**
     * Get or assign the index for a CONSTANT_Float entry.
     */
/*        public short getFloat(float f) {
            return getValue(f);
        }
*/

    /**
     * Get or assign the index for a CONSTANT_Class entry.
     */
    public short getClass(String name) {
      short utf8Index = getUtf8(name);
      return getIndirect(new IndirectEntry(
          CONSTANT_CLASS, utf8Index));
    }

    /**
     * Get or assign the index for a CONSTANT_String entry.
     */
    public short getString(String s) {
      short utf8Index = getUtf8(s);
      return getIndirect(new IndirectEntry(
          CONSTANT_STRING, utf8Index));
    }

    /**
     * Get or assign the index for a CONSTANT_FieldRef entry.
     */
    public short getFieldRef(String className,
        String name, String descriptor) {
      short classIndex = getClass(className);
      short nameAndTypeIndex = getNameAndType(name, descriptor);
      return getIndirect(new IndirectEntry(
          CONSTANT_FIELD, classIndex, nameAndTypeIndex));
    }

    /**
     * Get or assign the index for a CONSTANT_MethodRef entry.
     */
    public short getMethodRef(String className,
        String name, String descriptor) {
      short classIndex = getClass(className);
      short nameAndTypeIndex = getNameAndType(name, descriptor);
      return getIndirect(new IndirectEntry(
          CONSTANT_METHOD, classIndex, nameAndTypeIndex));
    }

    /**
     * Get or assign the index for a CONSTANT_InterfaceMethodRef entry.
     */
    public short getInterfaceMethodRef(String className, String name,
        String descriptor) {
      short classIndex = getClass(className);
      short nameAndTypeIndex = getNameAndType(name, descriptor);
      return getIndirect(new IndirectEntry(
          CONSTANT_INTERFACEMETHOD, classIndex, nameAndTypeIndex));
    }

    /**
     * Get or assign the index for a CONSTANT_NameAndType entry.
     */
    public short getNameAndType(String name, String descriptor) {
      short nameIndex = getUtf8(name);
      short descriptorIndex = getUtf8(descriptor);
      return getIndirect(new IndirectEntry(
          CONSTANT_NAMEANDTYPE, nameIndex, descriptorIndex));
    }

    /**
     * Set this ConstantPool instance to be "read only".
     * <p>
     * After this method has been called, further requests to get
     * an index for a non-existent entry will cause an InternalError
     * to be thrown instead of creating of the entry.
     */
    public void setReadOnly() {
      readOnly = true;
    }

    /**
     * Write this constant pool to a stream as part of
     * the class file format.
     * <p>
     * This consists of writing the "constant_pool_count" and
     * "constant_pool[]" items of the "ClassFile" structure, as
     * described in JVMS section 4.1.
     */
    public void write(OutputStream out) throws IOException {
      DataOutputStream dataOut = new DataOutputStream(out);

      // constant_pool_count: number of entries plus one
      dataOut.writeShort(pool.size() + 1);

      for (Entry e : pool) {
        e.write(dataOut);
      }
    }

    /**
     * Add a new constant pool entry and return its index.
     */
    private short addEntry(Entry entry) {
      pool.add(entry);
            /*
             * Note that this way of determining the index of the
             * added entry is wrong if this pool supports
             * CONSTANT_Long or CONSTANT_Double entries.
             */
      if (pool.size() >= 65535) {
        throw new IllegalArgumentException(
            "constant pool size limit exceeded");
      }
      return (short) pool.size();
    }

    /**
     * Get or assign the index for an entry of a type that contains
     * a direct value.  The type of the given object determines the
     * type of the desired entry as follows:
     * <p>
     * java.lang.String        CONSTANT_Utf8
     * java.lang.Integer       CONSTANT_Integer
     * java.lang.Float         CONSTANT_Float
     * java.lang.Long          CONSTANT_Long
     * java.lang.Double        CONSTANT_DOUBLE
     */
    private short getValue(Object key) {
      Short index = map.get(key);
      if (index != null) {
        return index;
      } else {
        if (readOnly) {
          throw new InternalError(
              "late constant pool addition: " + key);
        }
        short i = addEntry(new ValueEntry(key));
        map.put(key, i);
        return i;
      }
    }

    /**
     * Get or assign the index for an entry of a type that contains
     * references to other constant pool entries.
     */
    private short getIndirect(IndirectEntry e) {
      Short index = map.get(e);
      if (index != null) {
        return index;
      } else {
        if (readOnly) {
          throw new InternalError("late constant pool addition");
        }
        short i = addEntry(e);
        map.put(e, i);
        return i;
      }
    }

    /**
     * Entry is the abstact superclass of all constant pool entry types
     * that can be stored in the "pool" list; its purpose is to define a
     * common method for writing constant pool entries to a class file.
     */
    private static abstract class Entry {

      public abstract void write(DataOutputStream out)
          throws IOException;
    }

    /**
     * ValueEntry represents a constant pool entry of a type that
     * contains a direct value (see the comments for the "getValue"
     * method for a list of such types).
     * <p>
     * ValueEntry objects are not used as keys for their entries in the
     * Map "map", so no useful hashCode or equals methods are defined.
     */
    private static class ValueEntry extends Entry {

      private Object value;

      public ValueEntry(Object value) {
        this.value = value;
      }

      public void write(DataOutputStream out) throws IOException {
        if (value instanceof String) {
          out.writeByte(CONSTANT_UTF8);
          out.writeUTF((String) value);
        } else if (value instanceof Integer) {
          out.writeByte(CONSTANT_INTEGER);
          out.writeInt(((Integer) value).intValue());
        } else if (value instanceof Float) {
          out.writeByte(CONSTANT_FLOAT);
          out.writeFloat(((Float) value).floatValue());
        } else if (value instanceof Long) {
          out.writeByte(CONSTANT_LONG);
          out.writeLong(((Long) value).longValue());
        } else if (value instanceof Double) {
          out.writeDouble(CONSTANT_DOUBLE);
          out.writeDouble(((Double) value).doubleValue());
        } else {
          throw new InternalError("bogus value entry: " + value);
        }
      }
    }

    /**
     * IndirectEntry represents a constant pool entry of a type that
     * references other constant pool entries, i.e., the following types:
     * <p>
     * CONSTANT_Class, CONSTANT_String, CONSTANT_Fieldref,
     * CONSTANT_Methodref, CONSTANT_InterfaceMethodref, and
     * CONSTANT_NameAndType.
     * <p>
     * Each of these entry types contains either one or two indexes of
     * other constant pool entries.
     * <p>
     * IndirectEntry objects are used as the keys for their entries in
     * the Map "map", so the hashCode and equals methods are overridden
     * to allow matching.
     */
    private static class IndirectEntry extends Entry {

      private int tag;
      private short index0;
      private short index1;

      /**
       * Construct an IndirectEntry for a constant pool entry type
       * that contains one index of another entry.
       */
      public IndirectEntry(int tag, short index) {
        this.tag = tag;
        this.index0 = index;
        this.index1 = 0;
      }

      /**
       * Construct an IndirectEntry for a constant pool entry type
       * that contains two indexes for other entries.
       */
      public IndirectEntry(int tag, short index0, short index1) {
        this.tag = tag;
        this.index0 = index0;
        this.index1 = index1;
      }

      public void write(DataOutputStream out) throws IOException {
        out.writeByte(tag);
        out.writeShort(index0);
                /*
                 * If this entry type contains two indexes, write
                 * out the second, too.
                 */
        if (tag == CONSTANT_FIELD ||
            tag == CONSTANT_METHOD ||
            tag == CONSTANT_INTERFACEMETHOD ||
            tag == CONSTANT_NAMEANDTYPE) {
          out.writeShort(index1);
        }
      }

      public int hashCode() {
        return tag + index0 + index1;
      }

      public boolean equals(Object obj) {
        if (obj instanceof IndirectEntry) {
          IndirectEntry other = (IndirectEntry) obj;
          if (tag == other.tag &&
              index0 == other.index0 && index1 == other.index1) {
            return true;
          }
        }
        return false;
      }
    }
  }
}