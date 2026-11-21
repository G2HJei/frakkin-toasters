package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PRESIDENTIAL_PARDON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class PresidentialPardonEventProcessorTest extends EventTestHarness<PresidentialPardonEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        moveTo(BRIG, WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(PRESIDENTIAL_PARDON);
    }

    @Test
    void anotherPrisonerLeavesTheBrig() {
        execute(new PresidentialPardonEvent(1, 2, COMMAND));

        assertFollowup(all(new MoveFromBrigEvent(2, COMMAND), new DiscardPlayedQuorumCardEvent(PRESIDENTIAL_PARDON)));
    }

    @Test
    void onlyToALocationOnGalactica() {
        assertInvalid(new PresidentialPardonEvent(1, 2, QUORUM_CHAMBER));
    }

    @Test
    void notThePresidentHimself() {
        moveTo(BRIG, LAURA_ROSLIN);

        assertInvalid(new PresidentialPardonEvent(1, 1, COMMAND));
    }
}
