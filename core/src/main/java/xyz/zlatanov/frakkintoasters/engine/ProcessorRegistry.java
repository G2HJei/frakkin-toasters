package xyz.zlatanov.frakkintoasters.engine;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ProcessorRegistry {

    private static final String                        ROOT_PACKAGE = EventProcessor.class.getPackageName();
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTORS = findConstructors();

    public static boolean hasProcessor(Class<? extends Event> eventClass) {
        return CONSTRUCTORS.containsKey(eventClass);
    }

    /**
     * @return a new processor for the event. A processor holds the context of one event, so it should not be reused
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <E extends Event> EventProcessor<E> create(E event) {
        return (EventProcessor<E>) CONSTRUCTORS.get(event.getClass()).newInstance();
    }

    // The root package can be in several classpath roots, e.g. the main and the test classes.
    @SneakyThrows
    private static Map<Class<?>, Constructor<?>> findConstructors() {
        val packageUrls = ProcessorRegistry.class.getClassLoader().getResources(ROOT_PACKAGE.replace('.', '/'));
        return Collections.list(packageUrls).stream()
                .flatMap(url -> processorNames(url).stream())
                .distinct()
                .map(ProcessorRegistry::load)
                .filter(processor -> !Modifier.isAbstract(processor.getModifiers()))
                .collect(toMap(ProcessorRegistry::eventOf, ProcessorRegistry::constructorOf));
    }

    @SneakyThrows
    private static List<String> processorNames(URL packageUrl) {
        val uri = packageUrl.toURI();
        if (!uri.getScheme().equals("jar")) {
            return processorNames(Path.of(uri));
        }
        try (val jar = FileSystems.newFileSystem(uri, Map.of())) {
            return processorNames(jar.provider().getPath(uri));
        }
    }

    // The top-level classes named XxxEventProcessor under the package directory. A nested class has a '$' in its name.
    @SneakyThrows
    private static List<String> processorNames(Path packageDir) {
        try (val files = Files.walk(packageDir)) {
            return files.map(file -> packageDir.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "."))
                    .filter(name -> name.endsWith("EventProcessor.class") && !name.contains("$"))
                    .map(name -> ROOT_PACKAGE + "." + name.substring(0, name.length() - ".class".length()))
                    .toList();
        }
    }

    private static Class<?> eventOf(Class<?> processor) {
        return load(processor.getName().replaceFirst("Processor$", ""));
    }

    @SneakyThrows
    private static Class<?> load(String className) {
        return Class.forName(className);
    }

    @SneakyThrows
    private static Constructor<?> constructorOf(Class<?> processor) {
        return processor.getConstructor();
    }
}
