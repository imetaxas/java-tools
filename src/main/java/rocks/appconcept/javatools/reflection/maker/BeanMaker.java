package rocks.appconcept.javatools.reflection.maker;

import java.io.IOException;
import java.util.Map;

/**
 * @author yanimetaxas
 */
public class BeanMaker {

  public static byte[] makeBean(String classname, String baseclass, Map<String, String> properties,
      Map<String, String> lists) {
    ClassFile classFile = new ClassFile(
        ClassConstants.ACC_PUBLIC | ClassConstants.ACC_FINAL | ClassConstants.ACC_SUPER,
        dotToSlash(classname), dotToSlash(baseclass));

    // add regular properties and their getters/setters
    properties.forEach((property, propertyType) -> {
      classFile.addField(ClassConstants.ACC_PRIVATE, property, typeName(propertyType));
      classFile
          .addMethod(ClassConstants.ACC_PUBLIC, getterName(property), "()" + typeName(propertyType),
              (short) 2, (short) 2, (cp, out) -> {
                out.writeByte(ClassConstants.opc_aload_0);
                out.writeByte(ClassConstants.opc_getfield);
                out.writeShort(cp.getFieldRef(dotToSlash(classname), property,
                    typeName(propertyType).replaceAll("<[^>]+>+", "")));
                out.writeByte(returnOpcode(typeName(propertyType)));
              });
      classFile.addMethod(ClassConstants.ACC_PUBLIC, setterName(property),
          "(" + typeName(propertyType) + ")V", (short) 4, (short) 4, (cp, out) -> {
            out.writeByte(ClassConstants.opc_aload_0);
            out.writeByte(load1Opcode(typeName(propertyType)));
            out.writeByte(ClassConstants.opc_putfield);
            out.writeShort(cp.getFieldRef(dotToSlash(classname), property,
                typeName(propertyType).replaceAll("<[^>]+>+", "")));
            out.writeByte(ClassConstants.opc_return);
          });
    });

    // add list properties and their getters
    lists.forEach((property, listType) -> {
      classFile.addField(ClassConstants.ACC_PRIVATE, property,
          "Ljava/util/List<" + typeName(listType) + ">;");
      classFile.addMethod(ClassConstants.ACC_PUBLIC, getterName(property),
          "()Ljava/util/List<" + typeName(listType) + ">;", (short) 2, (short) 2, (cp, out) -> {
            out.writeByte(ClassConstants.opc_aload_0);
            out.writeByte(ClassConstants.opc_getfield);
            out.writeShort(cp.getFieldRef(dotToSlash(classname), property,
                typeName(listType).replaceAll("<[^>]+>+", "")));
            out.writeByte(ClassConstants.opc_areturn);
          });
    });

    // create constructor which initializes all lists
    classFile
        .addMethod(ClassConstants.ACC_PUBLIC, "<init>", "()V", (short) 4, (short) 2, (cp, out) -> {
          out.writeByte(ClassConstants.opc_aload_0);
          out.writeByte(ClassConstants.opc_invokespecial);
          out.writeShort(cp.getMethodRef(dotToSlash(baseclass), "<init>", "()V"));

          lists.keySet().forEach(property -> {
            try {
              out.writeByte(ClassConstants.opc_aload_0);
              out.writeByte(ClassConstants.opc_new);
              out.writeShort(cp.getClass("java/util/ArrayList"));
              out.writeByte(ClassConstants.opc_dup);
              out.writeByte(ClassConstants.opc_invokespecial);
              out.writeShort(cp.getMethodRef("java/util/ArrayList", "<init>", "()V"));
              out.writeByte(ClassConstants.opc_putfield);
              out.writeShort(cp.getFieldRef(dotToSlash(classname), property, "Ljava/util/List;"));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

          out.writeByte(ClassConstants.opc_return);
        });

    return classFile.toByteArray();
  }

  private static String getterName(String property) {
    return "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
  }

  private static String setterName(String property) {
    return "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
  }

  private static int returnOpcode(String v) {
    switch (v) {
      case "B":
      case "I":
      case "Z":
      case "C":
      case "S":
        return ClassConstants.opc_ireturn;
      case "F":
        return ClassConstants.opc_freturn;
      case "D":
        return ClassConstants.opc_dreturn;
      case "J":
        return ClassConstants.opc_lreturn;
      default:
        return ClassConstants.opc_areturn;
    }
  }

  private static int load1Opcode(String v) {
    switch (v) {
      case "B":
      case "I":
      case "Z":
      case "C":
      case "S":
        return ClassConstants.opc_iload_1;
      case "F":
        return ClassConstants.opc_fload_1;
      case "D":
        return ClassConstants.opc_dload_1;
      case "J":
        return ClassConstants.opc_lload_1;
      default:
        return ClassConstants.opc_aload_1;
    }
  }

  private static String typeName(String v) {
    switch (v) {
      case "byte":
        return "B";
      case "char":
        return "C";
      case "double":
        return "D";
      case "float":
        return "F";
      case "int":
        return "I";
      case "long":
        return "J";
      case "short":
        return "S";
      case "boolean":
        return "Z";
    }
    return "L" + dotToSlash(v).replaceAll("<([^<>]+)(.)", "<L$1;$2") + ";";
  }

  private static String dotToSlash(String name) {
    return name.replace('.', '/');
  }
}
