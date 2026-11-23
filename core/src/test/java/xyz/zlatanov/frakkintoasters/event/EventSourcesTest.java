package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActions;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovements;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy;
import xyz.zlatanov.frakkintoasters.event.action.character.EndRemoteActionEvent;
import xyz.zlatanov.frakkintoasters.event.character.OfferForwardThinkerEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.OfferUnmannedViperActivationEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.OfferPlayOrPassEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FORWARD_THINKER;

/**
 * Every event that exists for one character ability, and every offer, names its {@link Source}, so the table can say
 * why a player is asked.
 */
class EventSourcesTest {

    private static final String CHARACTER = "xyz.zlatanov.frakkintoasters.event.character";
    private static final String ACTION_CHARACTER = "xyz.zlatanov.frakkintoasters.event.action.character";

    // the events whose fields name their source, or that serve more than one
    private static final Set<Class<?>> WITHOUT_SOURCE = Set.of(
            EndRemoteActionEvent.class, // "Organized" and "Resolute"
            OfferPlayOrPassEvent.class, // names its card
            OfferUnmannedViperActivationEvent.class); // Crisis Cards and "Command"

    @Test
    void readsTheSourceOfAnEventAndOfAnEnumConstant() {
        assertEquals(Optional.of(FORWARD_THINKER), EventSources.of(OfferForwardThinkerEvent.class));
        assertEquals(Optional.of(SkillCardType.EXECUTIVE_ORDER), EventSources.of(GrantedBy.EXECUTIVE_ORDER));
        assertEquals(Optional.empty(), EventSources.of(EndRemoteActionEvent.class));
    }

    @Test
    void everyAbilityEventAndOfferNamesItsSource() {
        val missing = records().stream()
                .filter(type -> type.getPackageName().equals(CHARACTER) || type.getPackageName().equals(ACTION_CHARACTER)
                        || type.getSimpleName().startsWith("Offer"))
                .filter(type -> !WITHOUT_SOURCE.contains(type))
                .filter(type -> EventSources.of(type).isEmpty())
                .toList();

        assertEquals(List.of(), missing);
    }

    @Test
    void everySourceNamesExactlyOneComponent() {
        // throws on an annotation that names none or two
        records().forEach(EventSources::of);
    }

    @Test
    void anActionOrMovementAbilityIsTheSourceOfItsEvent() {
        for (val type : records()) {
            if (CharacterActionEvent.class.isAssignableFrom(type)) {
                assertEquals(type, CharacterActions.eventClass(ability(type)), type.getSimpleName());
            }
            if (CharacterMovementEvent.class.isAssignableFrom(type)) {
                assertEquals(type, CharacterMovements.eventClass(ability(type)), type.getSimpleName());
            }
        }
    }

    @Test
    void everyGrantNamesWhatGrantedIt() {
        assertTrue(Arrays.stream(GrantedBy.values()).allMatch(grantedBy -> EventSources.of(grantedBy).isPresent()));
    }

    private static List<Class<? extends Event>> records() {
        return EventCatalog.types().stream().filter(Class::isRecord).toList();
    }

    private static CharacterAbility ability(Class<?> type) {
        return (CharacterAbility) EventSources.of(type).orElseThrow();
    }
}
