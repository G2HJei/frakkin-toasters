package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ENACT_PRODUCTION_QUOTAS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class EnactProductionQuotasEventProcessorTest extends EventTestHarness<EnactProductionQuotasEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(ENACT_PRODUCTION_QUOTAS);
    }

    @Test
    void thePresidentTradesMoraleForFood() {
        execute(new EnactProductionQuotasEvent(1));

        assertEquals(List.of(), presidentHand.cards());
        assertFollowup(all(gain(FOOD, 1), lose(MORALE, 1), new DiscardPlayedQuorumCardEvent(ENACT_PRODUCTION_QUOTAS)));
    }

    @Test
    void onlyThePresidentPlaysIt() {
        assertInvalid(new EnactProductionQuotasEvent(2));
    }

    // "Popular Influence" left player 2 the card to play.
    @Test
    void aHeldCardIsPlayedByItsHolder() {
        ops.player(2).holdQuorumCard(ENACT_PRODUCTION_QUOTAS);

        execute(new EnactProductionQuotasEvent(2));

        assertEquals(List.of(), player(2).quorumCards().cards());
        assertEquals(List.of(ENACT_PRODUCTION_QUOTAS), presidentHand.cards()); // the President's copy stays
        assertFollowup(all(gain(FOOD, 1), lose(MORALE, 1), new DiscardPlayedQuorumCardEvent(ENACT_PRODUCTION_QUOTAS)));
    }

    @Test
    void thePresidentPlaysHisHeldCopyFirst() {
        ops.player(1).holdQuorumCard(ENACT_PRODUCTION_QUOTAS);

        execute(new EnactProductionQuotasEvent(1));

        assertEquals(List.of(), player(1).quorumCards().cards());
        assertEquals(List.of(ENACT_PRODUCTION_QUOTAS), presidentHand.cards());
        assertFollowup(all(gain(FOOD, 1), lose(MORALE, 1), new DiscardPlayedQuorumCardEvent(ENACT_PRODUCTION_QUOTAS)));
    }

    @Test
    void notWithoutTheCard() {
        presidentHand.removeTop(1);

        assertInvalid(new EnactProductionQuotasEvent(1));
    }
}
