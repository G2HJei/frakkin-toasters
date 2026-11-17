package xyz.zlatanov.frakkintoasters.engine;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.Event;

import java.util.Comparator;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

/**
 * Every event type under the root package, found like the processors of {@link ProcessorRegistry}: the records,
 * those without a processor that followups still hold (e.g. {@code PlayerDecisionEvent}), and the interfaces, because a
 * decision can ask for an {@code ActionEvent}.
 */
@NoArgsConstructor(access = PRIVATE)
public final class EventCatalog {

    private static final List<Class<? extends Event>> TYPES = findTypes();

    /**
     * @return the event types, sorted by their simple name
     */
    public static List<Class<? extends Event>> types() {
        return TYPES;
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Event>> findTypes() {
        return ClasspathScanner.topLevelClassNames().stream()
                .filter(name -> !name.endsWith("Processor") && !name.endsWith("Test"))
                .map(ClasspathScanner::load)
                .filter(Event.class::isAssignableFrom)
                .<Class<? extends Event>>map(type -> (Class<? extends Event>) type)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .toList();
    }
}
