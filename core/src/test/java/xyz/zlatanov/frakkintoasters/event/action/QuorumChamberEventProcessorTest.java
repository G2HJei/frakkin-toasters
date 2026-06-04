package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class QuorumChamberEventProcessorTest extends EventTestHarness<QuorumChamberEvent> {

    @Test
    void shouldDrawQuorumCardAndFollowup() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);

        execute(new QuorumChamberEvent(1));

        assertEquals(1, presidentHand.cards().size());
        assertFollowup(
                one(
                        new DrawQuorumCardEvent(1),
                        new PlayerDecisionEvent<>(1, PlayQuorumCardEvent.class)));
    }

}
