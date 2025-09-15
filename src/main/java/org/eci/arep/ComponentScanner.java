package org.eci.arep;

import org.eci.arep.annotations.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ComponentScanner {

    public static List<Class<?>> scanForControllers(String basePackage) throws ClassNotFoundException, IOException {
        List<Class<?>> classes = new ArrayList<>();

        URL root = ComponentScanner.class.getClassLoader().getResource("");
        if (root != null) {
            File baseDir = new File(root.getPath());
            findClasses(baseDir, "", classes);
        } else {
            classes.addAll(findClassesInJar(basePackage));
        }

        return classes.stream()
                .filter(c -> c.isAnnotationPresent(RestController.class))
                .toList();
    }

    private static void findClasses(File dir, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        if (!dir.exists()) return;

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                findClasses(file, packageName + (packageName.isEmpty() ? "" : ".") + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    classes.add(Class.forName(className));
                } catch (NoClassDefFoundError ignored) {

                }
            }
        }
    }

    private static List<Class<?>> findClassesInJar(String basePackage) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        CodeSource codeSource = ComponentScanner.class.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            String jarPath = codeSource.getLocation().getPath();
            try (JarFile jarFile = new JarFile(jarPath)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        String className = name.replace('/', '.').replace(".class", "");
                        if (className.startsWith(basePackage)) {
                            classes.add(Class.forName(className));
                        }
                    }
                }
            }
        }
        return classes;
    }
}
