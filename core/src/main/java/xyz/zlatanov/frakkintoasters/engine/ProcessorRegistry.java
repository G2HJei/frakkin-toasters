package xyz.zlatanov.frakkintoasters.engine;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ProcessorRegistry {

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTORS = findConstructors();

    /**
     * @return a new processor for the event. A processor holds the context of one event, so it should not be reused
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <E extends Event> EventProcessor<E> create(E event) {
        val constructor = CONSTRUCTORS.get(event.getClass());
        if (constructor == null) {
            // Every event must have a processor, or it is still under development.
            throw new FrakCallTheAdmiralException("No processor for " + event.getClass().getSimpleName());
        }
        return (EventProcessor<E>) constructor.newInstance();
    }

    /**
     * @return whether the event class has a processor
     */
    public static boolean hasProcessor(Class<?> eventClass) {
        return CONSTRUCTORS.containsKey(eventClass);
    }

    // The top-level classes named XxxEventProcessor.
    private static Map<Class<?>, Constructor<?>> findConstructors() {
        return ClasspathScanner.topLevelClassNames().stream()
                .filter(name -> name.endsWith("EventProcessor"))
                .map(ProcessorRegistry::load)
                .filter(processor -> !Modifier.isAbstract(processor.getModifiers()))
                .collect(toMap(ProcessorRegistry::eventOf, ProcessorRegistry::constructorOf));
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
