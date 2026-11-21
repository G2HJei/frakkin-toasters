package xyz.zlatanov.frakkintoasters.event.action;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * Every "Action:" and "Movement:" card has exactly one {@link SkillCardEvent}, whose interfaces are its timing.
 */
class SkillCardEventsTest {

    // the cards that later phases of skill-cards-plan.md build
    private static final Set<SkillCardType> NOT_BUILT_YET = EnumSet.of(
            POPULAR_INFLUENCE, NEGOTIATION, EXECUTIVE_ORDER, STATE_OF_EMERGENCY, LAUNCH_SCOUT, SCOUTING_FOR_FUEL,
            UNORTHODOX_PLAN, MAXIMUM_FIREPOWER, FULL_THROTTLE, COMBAT_VETERAN, LAUNCH_RESERVES, REPAIR, RAPTOR_SPECIALIST, TEST_THE_LIMITS);

    @Test
    void eachActionOrMovementCardHasOneEvent() {
        val eventsByType = cardEvents().stream().collect(groupingBy(SkillCardEventsTest::typeOf));

        eventsByType.forEach((type, events) -> assertEquals(1, events.size(), type + ": " + events));
        val expected = Arrays.stream(SkillCardType.values())
                .filter(type -> type.isAction() || type.isMovementAbility())
                .filter(type -> !NOT_BUILT_YET.contains(type))
                .collect(toSet());
        assertEquals(expected, eventsByType.keySet());
    }

    @Test
    void theInterfacesAreTheTiming() {
        for (val event : cardEvents()) {
            assertEquals(typeOf(event).isAction(), ActionEvent.class.isAssignableFrom(event), event.getSimpleName());
            assertEquals(typeOf(event).isMovementAbility(), MovementAbilityEvent.class.isAssignableFrom(event),
                    event.getSimpleName());
        }
    }

    private static List<Class<?>> cardEvents() {
        return EventCatalog.types().stream()
                .filter(SkillCardEvent.class::isAssignableFrom)
                .filter(type -> !type.isInterface())
                .<Class<?>>map(type -> type)
                .toList();
    }

    @SneakyThrows
    private static SkillCardType typeOf(Class<?> event) {
        val processor = Class.forName(event.getName() + "Processor").getDeclaredConstructor().newInstance();
        return ((SkillCardEventProcessor<?>) processor).type();
    }
}
