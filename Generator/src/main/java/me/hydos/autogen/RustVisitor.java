package me.hydos.autogen;

import org.objectweb.asm.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RustVisitor extends ClassVisitor {

    public String className;
    public String rawName;
    public FileWriter rust;

    public List<String> imports = new ArrayList<>();
    public Map<String, String> methods = new HashMap<>();

    public RustVisitor(File outputFile, String rawName) {
        super(Opcodes.ASM9);
        try {
            if (!outputFile.getParentFile().exists())
                outputFile.getParentFile().mkdirs();
            if (!outputFile.exists())
                outputFile.createNewFile();

            this.rust = new FileWriter(outputFile);
            this.rawName = rawName;
            this.className = processName(rawName);

            imports.add("jni::JNIEnv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String processName(String className) {
        String[] split = className.split("/");
        return split[split.length - 1];
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        Type[] args = Type.getArgumentTypes(descriptor);
        Type returnType = Type.getReturnType(descriptor);

        List<Type> types = new ArrayList<>(List.of(args));
        types.add(returnType);

        for (Type type : types) {
            Type loopType = type;

            while (loopType.getSort() == Type.ARRAY) {
                loopType = loopType.getElementType();
            }

            if (loopType.getSort() == Type.OBJECT) {
                String imp = loopType.getClassName().replace(".", "::");

                if (!imports.contains(imp)) imports.add(imp);
            }
        }

        if (access > Opcodes.ACC_STATIC) {

        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (access > Opcodes.ACC_STATIC) {
//            System.out.println(name + " is static!");
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        try {
            // Imports
            for (String imp : imports) {
                rust.write("use " + imp + ";\n");
            }
            rust.write("\n");

            // Struct
            rust.write("struct " + className + "<'a> (JNIEnv<'a>);\n");
            rust.write("\n");

            // Implementation
            rust.write("impl " + className + "<'_> {\n");
            rust.write("\n");

            rust.write("}\n");
            rust.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
