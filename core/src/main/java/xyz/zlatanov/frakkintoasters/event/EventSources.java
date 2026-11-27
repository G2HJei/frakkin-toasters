package xyz.zlatanov.frakkintoasters.event;

import lombok.val;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public final class EventSources {

    private EventSources() {
    }

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

    public static Optional<Enum<?>> of(Enum<?> constant) {
        try {
            return of(constant.getDeclaringClass().getField(constant.name()));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("An enum constant is a field of its enum", e);
        }
    }
}
