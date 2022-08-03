package me.hydos.autogen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.jar.JarFile;

public class BridgeGenerator {
    public static final String MC_VERSION = "1.8.9";
    public static final String MAPPINGS_GROUP = "net.legacyfabric.yarn.1_8_9";
    public static final String MAPPINGS = "1.8.9+build.382";

    public static final Path MINECRAFT = Paths.get("/home/arthur/.gradle/caches/fabric-loom/" + MC_VERSION + "/" + MAPPINGS_GROUP + "." + MAPPINGS + "-v2/minecraft-merged-named.jar");
    public static final Path GENERATED_DIR = Paths.get("./RustBootstrap/src/generated");

    public static void main(String[] args) throws IOException {
        visitJar(MINECRAFT, BridgeGenerator::createVisitor);
    }

    private static ClassVisitor createVisitor(String name) {
        return new RustVisitor(new File(GENERATED_DIR.toString(), name + ".rs"), name);
    }

    public static void visitJar(Path jarPath, Function<String, ClassVisitor> visitor) throws IOException {
        JarFile jar = new JarFile(jarPath.toFile());
        jar.stream().forEach(entry -> {
            if (entry.getName().endsWith(".class")) {
                try (InputStream inputStream = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(inputStream);
                    reader.accept(visitor.apply(entry
                            .getName()
                            .replace(".class", "")
                    ), ClassReader.EXPAND_FRAMES);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
