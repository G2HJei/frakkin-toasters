package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.QUORUM_CHAMBER;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ENACT_PRODUCTION_QUOTAS;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.EULOGY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ROMO_LAMPKIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;

class QuorumChamberEventProcessorTest extends EventTestHarness<QuorumChamberEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(ROMO_LAMPKIN);
        moveTo(QUORUM_CHAMBER, ROMO_LAMPKIN);
    }

    // The President draws, then may draw again, play a card (the one just drawn too) or stop.
    @Test
    void thePresidentDrawsThenMayDrawAgainOrPlay() {
        president(ROMO_LAMPKIN);
        quorumDeck.nextCard(ENACT_PRODUCTION_QUOTAS);

        execute(new QuorumChamberEvent(1));

        assertEquals(List.of(ENACT_PRODUCTION_QUOTAS), presidentHand.cards());
        assertFollowup(one(
                new DrawQuorumCardEvent(),
                new PlayerDecisionEvent<>(1, QuorumCardEvent.class),
                new NoOpEvent(1)));
    }

    @Test
    void noPlayWithoutACardThatCanBePlayed() {
        president(ROMO_LAMPKIN);
        quorumDeck.nextCard(EULOGY); // nobody was executed

        execute(new QuorumChamberEvent(1));

        assertFollowup(one(new DrawQuorumCardEvent(), new NoOpEvent(1)));
    }

    // Every card is in a hand, in play or out of the game.
    @Test
    void nothingToDrawAndNothingToPlay() {
        president(ROMO_LAMPKIN);
        quorumDeck.clear();

        execute(new QuorumChamberEvent(1));

        assertEquals(List.of(), presidentHand.cards());
    }

    @Test
    void shouldOnlyBeUsedByThePresident() {
        president(TOM_ZAREK);
        assertInvalid(new QuorumChamberEvent(1));
    }
}
