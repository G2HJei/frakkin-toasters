package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EnactProductionQuotasEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;

class PopularInfluenceChoiceEventProcessorTest extends EventTestHarness<PopularInfluenceChoiceEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK);
        player(2).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
    }

    // The decision takes only the event of the card held.
    @Test
    void thePresidentGetsOneAndThePlayerPlaysOrDiscardsTheOther() {
        execute(new PopularInfluenceChoiceEvent(1, EULOGY, ENACT_PRODUCTION_QUOTAS));

        assertEquals(List.of(EULOGY), presidentHand.cards());
        assertEquals(List.of(ENACT_PRODUCTION_QUOTAS), player(1).quorumCards().cards());
        assertFollowup(one(new PlayerDecisionEvent<>(1, EnactProductionQuotasEvent.class),
                new DiscardHeldQuorumCardEvent(1, ENACT_PRODUCTION_QUOTAS)));
    }

    // Only the President has a title to give away.
    @Test
    void aResignationCanOnlyBeDiscardedByAnotherPlayer() {
        execute(new PopularInfluenceChoiceEvent(1, EULOGY, RESIGNATION));

        assertFollowup(new DiscardHeldQuorumCardEvent(1, RESIGNATION));
    }
}
