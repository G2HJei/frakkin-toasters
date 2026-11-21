package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.CIVILIAN_SELF_DEFENSE;

class CivilianSelfDefenseResultEventProcessorTest extends EventTestHarness<CivilianSelfDefenseResultEvent> {

    @Test
    void onTwoTheCivilianShipIsDestroyed() {
        val ship = civilianShipAt(GALACTICA_SPACE_2_OCLOCK).id();
        rolled(2);

        execute(new CivilianSelfDefenseResultEvent(ship));

        assertFollowup(new DestroyCivilianShipEvent(ship));
        assertEquals(List.of(CIVILIAN_SELF_DEFENSE), quorumDeck.discardedCards());
    }

    @Test
    void higherTheShipIsSafe() {
        val ship = civilianShipAt(GALACTICA_SPACE_2_OCLOCK).id();
        rolled(3);

        execute(new CivilianSelfDefenseResultEvent(ship));

        assertEquals(List.of(CIVILIAN_SELF_DEFENSE), quorumDeck.discardedCards());
    }
}
