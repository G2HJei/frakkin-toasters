package xyz.zlatanov.frakkintoasters.event.action;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;

/**
 * Every Quorum Card has exactly one {@link QuorumCardEvent}.
 */
class QuorumCardEventsTest {

    // the cards that later phases of quorum-cards-plan.md build
    private static final Set<QuorumCard> NOT_BUILT_YET = EnumSet.of(
            ACCEPT_PROPHECY, ASSIGN_ARBITRATOR, ASSIGN_MISSION_SPECIALIST, ASSIGN_VICE_PRESIDENT, ASSIGN_CHIEF_OF_STAFF,
            PROBATION);

    @Test
    void eachCardHasOneEvent() {
        val eventsByCard = cardEvents().stream().collect(groupingBy(QuorumCardEventsTest::cardOf));

        eventsByCard.forEach((card, events) -> assertEquals(1, events.size(), card + ": " + events));
        val expected = Arrays.stream(QuorumCard.values()).filter(card -> !NOT_BUILT_YET.contains(card)).collect(toSet());
        assertEquals(expected, eventsByCard.keySet());
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
