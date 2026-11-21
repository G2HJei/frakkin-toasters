package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.UNSAVORY_CONNECTIONS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class UnsavoryConnectionsEventProcessorTest extends EventTestHarness<UnsavoryConnectionsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(UNSAVORY_CONNECTIONS);
    }

    @Test
    void thePresidentTradesCardsForFuel() {
        execute(new UnsavoryConnectionsEvent(1, FUEL));

        assertFollowup(all(new DiscardRandomSkillCardsEvent(1, 2), treachery(1, 2), gain(FUEL, 1),
                new DiscardPlayedQuorumCardEvent(UNSAVORY_CONNECTIONS)));
    }

    @Test
    void onlyFoodOrFuel() {
        assertInvalid(new UnsavoryConnectionsEvent(1, MORALE));
    }
}
