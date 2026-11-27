package xyz.zlatanov.frakkintoasters.engine;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;

import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * It walks the classpath as directories or as jars, a jar nested in the Spring Boot jar included.
 */
@NoArgsConstructor(access = PRIVATE)
final class ClasspathScanner {

    static final String ROOT_PACKAGE = EventProcessor.class.getPackageName();

    // The root package can be in several classpath roots, e.g. the main and the test classes.
    @SneakyThrows
    static List<String> topLevelClassNames() {
        val packageUrls = ClasspathScanner.class.getClassLoader().getResources(ROOT_PACKAGE.replace('.', '/'));
        return Collections.list(packageUrls).stream()
                .flatMap(url -> classNames(url).stream())
                .distinct()
                .sorted()
                .toList();
    }

    @SneakyThrows
    static Class<?> load(String className) {
        return Class.forName(className, false, ClasspathScanner.class.getClassLoader());
    }

    @SneakyThrows
    private static List<String> classNames(URL packageUrl) {
        val uri = packageUrl.toURI();
        if (!uri.getScheme().equals("jar")) {
            return classNames(Path.of(uri));
        }
        try (val jar = FileSystems.newFileSystem(uri, Map.of())) {
            return classNames(jar.provider().getPath(uri));
        }
    }

    // A nested class has a '$' in its name.
    @SneakyThrows
    private static List<String> classNames(Path packageDir) {
        try (val files = Files.walk(packageDir)) {
            return files.map(file -> packageDir.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "."))
                    .filter(name -> name.endsWith(".class") && !name.contains("$"))
                    .map(name -> ROOT_PACKAGE + "." + name.substring(0, name.length() - ".class".length()))
                    .toList();
        }
    }
}
