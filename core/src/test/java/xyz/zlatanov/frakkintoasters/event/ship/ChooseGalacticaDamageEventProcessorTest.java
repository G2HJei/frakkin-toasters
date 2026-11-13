package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.COMMAND;

class ChooseGalacticaDamageEventProcessorTest extends EventTestHarness<ChooseGalacticaDamageEvent> {

    // The chosen token is resolved, the other goes back to the pile, which is shuffled.
    @Test
    void shouldResolveTheChosenTokenAndReturnTheOther() {
        galacticaDamage.nextCard(ARMORY, COMMAND);
        val drawn = ops.galactica().drawDamage(2);
        val size = galacticaDamage.size();

        execute(new ChooseGalacticaDamageEvent(1, drawn.get(0), drawn.get(1)));

        assertTrue(galacticaBoard.isDamaged(Location.ARMORY));
        assertEquals(size + 1, galacticaDamage.size());
        assertTrue(galacticaDamage.cards().contains(COMMAND));
        assertTrue(galacticaDamage.wasShuffled());
    }
}
