package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.ship.ChooseGalacticaDamageTokensEvent;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.*;

class CylonPowerDamageGalacticaEventProcessorTest extends EventTestHarness<CylonPowerDamageGalacticaEvent> {

    // Up to 5 tokens are drawn, the Cylon resolves 2 of them and the others go back.
    @Test
    void shouldOfferEveryPairOfFiveTokens() {
        val drawn = List.of(FUEL, FOOD, ARMORY, COMMAND, HANGAR_DECK);
        galacticaDamage.nextCard(drawn.toArray(GalacticaDamage[]::new));

        execute(new CylonPowerDamageGalacticaEvent(1));

        val options = new ArrayList<Event>();
        for (int first = 0; first < drawn.size(); first++) {
            for (int second = first + 1; second < drawn.size(); second++) {
                val returned = new ArrayList<>(drawn);
                returned.remove(second);
                returned.remove(first);
                options.add(new ChooseGalacticaDamageTokensEvent(1, List.of(drawn.get(first), drawn.get(second)), returned));
            }
        }
        assertEquals(10, options.size());
        assertFollowup(one(options.toArray(Event[]::new)));
        assertEquals(3, galacticaDamage.size()); // the drawn tokens are in no pile until resolved or returned
    }

    // With 2 tokens or fewer left, both are resolved.
    @Test
    void shouldResolveTheLastTokens() {
        galacticaDamage.clear();
        galacticaDamage.addOnTop(ARMORY, COMMAND);

        execute(new CylonPowerDamageGalacticaEvent(1));

        assertFollowup(new ChooseGalacticaDamageTokensEvent(1, List.of(ARMORY, COMMAND), List.of()));
    }

    @Test
    void shouldRejectAnEmptyPile() {
        galacticaDamage.clear();

        assertInvalid(new CylonPowerDamageGalacticaEvent(1));
    }
}
