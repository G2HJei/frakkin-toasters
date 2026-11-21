package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.*;

class ChooseGalacticaDamageTokensEventProcessorTest extends EventTestHarness<ChooseGalacticaDamageTokensEvent> {

    @Test
    void shouldResolveTheChosenTokensAndReturnTheOthers() {
        galacticaDamage.clear();
        val food = galacticaBoard.food();

        execute(new ChooseGalacticaDamageTokensEvent(1, List.of(ARMORY, FOOD), List.of(COMMAND, HANGAR_DECK)));

        assertTrue(galacticaBoard.isDamaged(Location.ARMORY));
        assertEquals(food - 1, galacticaBoard.food());
        // The food token goes back once resolved, like the two returned.
        assertEquals(3, galacticaDamage.size());
        assertTrue(galacticaDamage.cards().containsAll(List.of(FOOD, COMMAND, HANGAR_DECK)));
    }

    @Test
    void shouldRejectMissingTokens() {
        assertInvalid(new ChooseGalacticaDamageTokensEvent(1, null, List.of()));
    }
}
