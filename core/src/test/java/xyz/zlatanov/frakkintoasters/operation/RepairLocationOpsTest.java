package xyz.zlatanov.frakkintoasters.operation;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.LocationRepaired;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsPulled;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.DeckShuffled;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.GALACTICA_DAMAGE;

/**
 * Repairing a location: its damage token goes back to its pile, which is shuffled (rule 11.7.2.2).
 */
class RepairLocationOpsTest extends TestHarness {

    @Test
    void theTokenGoesBackToItsPile() {
        galacticaDamage.nextCard(GalacticaDamage.COMMAND);
        ops.galactica().damage();
        int factsBefore = journal.facts().size();

        ops.galactica().repairLocation(COMMAND);

        assertFalse(galacticaBoard.isDamaged(COMMAND));
        List<Fact> facts = journal.facts().subList(factsBefore, journal.facts().size());
        assertEquals(new LocationRepaired(COMMAND), facts.get(0));
        assertEquals(new CardsAddedOnTop(GALACTICA_DAMAGE, List.of(GalacticaDamage.COMMAND)), facts.get(1));
        assertInstanceOf(DeckShuffled.class, facts.get(2));
        assertEquals(1, Collections.frequency(galacticaDamage.cards(), GalacticaDamage.COMMAND));
    }

    @Test
    void aPegasusLocationToo() {
        pegasusDamage.nextCard(PegasusDamage.AIRLOCK);
        ops.pegasus().damage();

        ops.pegasus().repairLocation(AIRLOCK);

        assertFalse(pegasusBoard.isDamaged(AIRLOCK));
        assertEquals(1, Collections.frequency(pegasusDamage.cards(), PegasusDamage.AIRLOCK));
    }

    // Not every location has a token, e.g. one the sandbox damaged.
    @Test
    void aLocationWithoutATokenIsOnlyRepaired() {
        ops.sandbox().damage(COMMUNICATIONS);

        ops.galactica().repairLocation(COMMUNICATIONS);

        assertFalse(galacticaBoard.isDamaged(COMMUNICATIONS));
        assertEquals(new LocationRepaired(COMMUNICATIONS), journal.facts().getLast());
    }

    // The sandbox takes the token out of its pile, as a draw would, so a repair returns the only copy.
    @Test
    void theSandboxPullsTheToken() {
        ops.sandbox().damage(ARMORY);

        assertTrue(journal.facts().contains(new CardsPulled(GALACTICA_DAMAGE, List.of(GalacticaDamage.ARMORY))));
        assertFalse(galacticaDamage.cards().contains(GalacticaDamage.ARMORY));

        ops.galactica().repairLocation(ARMORY);

        assertEquals(1, Collections.frequency(galacticaDamage.cards(), GalacticaDamage.ARMORY));
    }
}
