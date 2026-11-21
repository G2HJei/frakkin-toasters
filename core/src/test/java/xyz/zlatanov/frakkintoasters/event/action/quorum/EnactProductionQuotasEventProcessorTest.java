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

    @Test
    void notWithoutTheCard() {
        presidentHand.removeTop(1);

        assertInvalid(new EnactProductionQuotasEvent(1));
    }
}
