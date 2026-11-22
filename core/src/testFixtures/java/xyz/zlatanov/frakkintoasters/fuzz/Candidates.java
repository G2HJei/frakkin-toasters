package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * Hostile candidates: an event built from the wide domains, an input a client could send but the legal answers never
 * would. The game has to reject it and stay as it was, or take it, and then the answers have to cover it.
 */
final class Candidates {

    private Candidates() {
    }

    /**
     * @return one event of the class built from the wide domains, an input a client could send but the agent never
     * would; {@code null} when the record refuses every set of values it is tried with
     */
    static Event hostile(Asked asked, Class<? extends Event> type) {
        val components = type.getRecordComponents();
        val domains = Arrays.stream(components).map(component -> Domains.wide(asked, component)).toList();
        val constructor = constructor(type, components);
        for (int attempt = 0; attempt < 16; attempt++) {
            val args = domains.stream().map(domain -> domain.get(asked.random().nextInt(domain.size()))).toArray();
            val event = construct(constructor, args);
            if (event != null) {
                return event;
            }
        }
        return null;
    }

    private static java.lang.reflect.Constructor<?> constructor(Class<?> type, RecordComponent[] components) {
        try {
            return type.getDeclaredConstructor(Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No canonical constructor for " + type.getSimpleName(), e);
        }
    }

    // A constructor that refuses the values is a candidate the codec couldn't build either: it is skipped.
    private static Event construct(java.lang.reflect.Constructor<?> constructor, Object[] args) {
        try {
            return (Event) constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
