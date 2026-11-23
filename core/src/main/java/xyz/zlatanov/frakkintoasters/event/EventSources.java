package xyz.zlatanov.frakkintoasters.event;

import lombok.val;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Reads the {@link Source} of an event type or of an enum constant.
 */
public final class EventSources {

    private EventSources() {
    }

    /**
     * @return the card or ability the {@link Source} on a record or an enum constant names, empty without one
     * @throws IllegalStateException when it names none or more than one
     */
    public static Optional<Enum<?>> of(AnnotatedElement element) {
        val source = element.getAnnotation(Source.class);
        if (source == null) {
            return Optional.empty();
        }
        val named = Stream.<Enum<?>[]>of(source.ability(), source.skillCard(), source.crisisCard(),
                        source.location(), source.title())
                .flatMap(Arrays::stream)
                .toList();
        if (named.size() != 1) {
            throw new IllegalStateException(element + " has to name exactly one source, not " + named);
        }
        return Optional.of(named.getFirst());
    }

    /**
     * @return the source of an enum constant, e.g. the card a {@code GrantedBy} stands for
     */
    public static Optional<Enum<?>> of(Enum<?> constant) {
        try {
            return of(constant.getDeclaringClass().getField(constant.name()));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("An enum constant is a field of its enum", e);
        }
    }
}
