package xyz.zlatanov.frakkintoasters.event.action;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.state.character.AbilityKind;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterActionEventsTest {

    @Test
    void eachActionAbilityHasOneEvent() {
        val eventsByAbility = abilityEvents().stream().collect(groupingBy(CharacterActionEventsTest::abilityOf));

        eventsByAbility.forEach((ability, events) -> assertEquals(1, events.size(), ability + ": " + events));
        assertEquals(Arrays.stream(CharacterAbility.values())
                        .filter(ability -> ability.kind() == AbilityKind.ACTION)
                        .collect(Collectors.toSet()),
                eventsByAbility.keySet());
    }

    @Test
    void eachAbilityNamesItsEvent() {
        for (val event : abilityEvents()) {
            assertEquals(event, CharacterActions.eventClass(abilityOf(event)));
        }
    }

    private static List<Class<?>> abilityEvents() {
        return EventCatalog.types().stream()
                .filter(CharacterActionEvent.class::isAssignableFrom)
                .filter(type -> !type.isInterface())
                .<Class<?>>map(type -> type)
                .toList();
    }

    @SneakyThrows
    private static CharacterAbility abilityOf(Class<?> event) {
        val processor = Class.forName(event.getName() + "Processor").getDeclaredConstructor().newInstance();
        return ((CharacterActionEventProcessor<?>) processor).ability();
    }
}
