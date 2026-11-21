package xyz.zlatanov.frakkintoasters.event.action;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every Quorum Card has exactly one {@link QuorumCardEvent}.
 */
class QuorumCardEventsTest {

    @Test
    void eachCardHasOneEvent() {
        val eventsByCard = cardEvents().stream().collect(groupingBy(QuorumCardEventsTest::cardOf));

        eventsByCard.forEach((card, events) -> assertEquals(1, events.size(), card + ": " + events));
        assertEquals(EnumSet.allOf(QuorumCard.class), eventsByCard.keySet());
    }

    private static List<Class<?>> cardEvents() {
        return EventCatalog.types().stream()
                .filter(QuorumCardEvent.class::isAssignableFrom)
                .filter(type -> !type.isInterface())
                .<Class<?>>map(type -> type)
                .toList();
    }

    @SneakyThrows
    private static QuorumCard cardOf(Class<?> event) {
        val processor = Class.forName(event.getName() + "Processor").getDeclaredConstructor().newInstance();
        return ((QuorumCardEventProcessor<?>) processor).card();
    }
}
