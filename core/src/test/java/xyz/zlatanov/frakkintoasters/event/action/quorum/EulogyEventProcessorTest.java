package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.EULOGY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class EulogyEventProcessorTest extends EventTestHarness<EulogyEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(EULOGY);
    }

    @Test
    void afterAnExecutionCostMoraleTheFleetGainsMorale() {
        ops.moraleLostToExecution();

        execute(new EulogyEvent(1));

        assertFollowup(all(gain(MORALE, 1), new DiscardPlayedQuorumCardEvent(EULOGY)));
    }

    @Test
    void notBeforeAnyExecution() {
        assertInvalid(new EulogyEvent(1));
    }
}
