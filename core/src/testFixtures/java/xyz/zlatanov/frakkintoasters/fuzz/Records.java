package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * Events built from their components, as the codec builds a client's input.
 */
final class Records {

    private Records() {
    }

    static Object[] values(Event event) {
        val components = event.getClass().getRecordComponents();
        val values = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            try {
                val accessor = components[i].getAccessor();
                accessor.setAccessible(true);
                values[i] = accessor.invoke(event);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Can't read " + components[i], e);
            }
        }
        return values;
    }

    /**
     * @return the event with one component changed, {@code null} when the record refuses the value
     */
    static Event with(Event event, int index, Object value) {
        val values = values(event);
        values[index] = value;
        return construct(event.getClass(), values);
    }

    /**
     * @return the event, {@code null} when the record refuses the values: the codec couldn't build it either
     */
    static Event construct(Class<?> type, Object[] values) {
        try {
            return (Event) constructor(type).newInstance(values);
        } catch (InvocationTargetException refused) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static int indexOf(Class<?> type, String component) {
        val components = type.getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i].getName().equals(component)) {
                return i;
            }
        }
        throw new IllegalArgumentException(type.getSimpleName() + " has no " + component);
    }

    static Constructor<?> constructor(Class<?> type) {
        try {
            val constructor = type.getDeclaredConstructor(Arrays.stream(type.getRecordComponents())
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new));
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No canonical constructor for " + type.getSimpleName(), e);
        }
    }
}
