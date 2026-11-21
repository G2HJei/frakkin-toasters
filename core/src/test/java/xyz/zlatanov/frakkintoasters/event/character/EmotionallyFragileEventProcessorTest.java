package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ANASTASIA_DEE_DUALLA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.EMOTIONALLY_FRAGILE;

class EmotionallyFragileEventProcessorTest extends EventTestHarness<EmotionallyFragileEvent> {

    // Morale reduced to 2 or less: she is executed, and the execution costs no morale.
    @Test
    void moraleDownTo2ExecutesDee() {
        player(1).character(ANASTASIA_DEE_DUALLA);
        ops.galactica().decreaseMorale(8);

        assertEquals(Map.of(1, Set.of(EMOTIONALLY_FRAGILE)), game.turn().triggeredAbilities());
        assertEquals(single(new EmotionallyFragileEvent(1)), AbilityTriggers.check(game));

        execute(new EmotionallyFragileEvent(1));

        assertFollowup(new ExecuteEvent(1, true));
        assertEquals(Map.of(), game.turn().triggeredAbilities());
    }

    @Test
    void notWhileMoraleStaysAbove2() {
        player(1).character(ANASTASIA_DEE_DUALLA);
        ops.galactica().decreaseMorale(7);

        assertEquals(Map.of(), game.turn().triggeredAbilities());
        assertEquals(NONE, AbilityTriggers.check(game));
        assertInvalid(new EmotionallyFragileEvent(1));
    }

    // A revealed Cylon's abilities are ignored.
    @Test
    void notForARevealedCylon() {
        player(1).character(ANASTASIA_DEE_DUALLA);
        revealCylon();
        ops.galactica().decreaseMorale(8);

        assertEquals(Map.of(), game.turn().triggeredAbilities());
        assertInvalid(new EmotionallyFragileEvent(1));
    }
}
