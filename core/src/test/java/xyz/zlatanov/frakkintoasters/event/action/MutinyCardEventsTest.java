package xyz.zlatanov.frakkintoasters.event.action;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MutinyCardEventsTest {

    @Test
    void eachCardHasOneEvent() {
        val eventsByCard = cardEvents().stream().collect(groupingBy(MutinyCardEventsTest::cardOf));

        eventsByCard.forEach((card, events) -> assertEquals(1, events.size(), card + ": " + events));
        assertEquals(EnumSet.allOf(MutinyCard.class), eventsByCard.keySet());
    }

    @Test
    void eachCardNamesItsEvent() {
        for (val event : cardEvents()) {
            assertEquals(event, MutinyCardPlays.eventClass(cardOf(event)));
        }
    }

    private static List<Class<?>> cardEvents() {
        return EventCatalog.types().stream()
                .filter(MutinyCardEvent.class::isAssignableFrom)
                .filter(type -> !type.isInterface())
                .<Class<?>>map(type -> type)
                .toList();
    }

    @SneakyThrows
    private static MutinyCard cardOf(Class<?> event) {
        val processor = Class.forName(event.getName() + "Processor").getDeclaredConstructor().newInstance();
        return ((MutinyCardEventProcessor<?>) processor).card();
    }
}
