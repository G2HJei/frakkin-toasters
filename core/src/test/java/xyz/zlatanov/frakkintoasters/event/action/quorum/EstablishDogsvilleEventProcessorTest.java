package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ESTABLISH_DOGSVILLE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class EstablishDogsvilleEventProcessorTest extends EventTestHarness<EstablishDogsvilleEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(ESTABLISH_DOGSVILLE);
    }

    @Test
    void thePresidentTradesMoraleForPopulation() {
        execute(new EstablishDogsvilleEvent(1));

        assertFollowup(all(gain(POPULATION, 1), lose(MORALE, 1), new DiscardPlayedQuorumCardEvent(ESTABLISH_DOGSVILLE)));
    }

    // Revealed Cylons can't use Quorum Cards (rule 6.1.2.3).
    @Test
    void notByARevealedCylon() {
        revealCylon();

        assertInvalid(new EstablishDogsvilleEvent(1));
    }
}
