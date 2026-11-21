package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.BRENDAN_HOTDOG_COSTANZA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MEMENTO;

// A population loss notes "Memento" for Brendan "Hot Dog" Costanza, and the engine's check after the event offers it.
class AbilityTriggersTest extends TestHarness {

    @Test
    void aPopulationLossTriggersMemento() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(BRENDAN_HOTDOG_COSTANZA);

        ops.galactica().decreaseResource(FOOD, 1);
        assertEquals(Map.of(), game.turn().triggeredAbilities());

        ops.galactica().decreaseResource(POPULATION, 1);
        assertEquals(Map.of(2, Set.of(MEMENTO)), game.turn().triggeredAbilities());
        assertEquals(single(new OfferMementoEvent(2)), AbilityTriggers.check(game));

        // a second loss before the offer adds nothing
        ops.galactica().decreaseResource(POPULATION, 1);
        assertEquals(single(new OfferMementoEvent(2)), AbilityTriggers.check(game));
    }

    @Test
    void nothingWithoutHotDog() {
        player(1).character(LAURA_ROSLIN);

        ops.galactica().decreaseResource(POPULATION, 1);

        assertEquals(NONE, AbilityTriggers.check(game));
    }

    @Test
    void nothingOnceUsedThisTurn() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);
        ops.player(1).useAbility(MEMENTO);

        ops.galactica().decreaseResource(POPULATION, 1);

        assertEquals(NONE, AbilityTriggers.check(game));
    }
}
