package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ARREST_ORDER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class ArrestOrderEventProcessorTest extends EventTestHarness<ArrestOrderEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(ARREST_ORDER);
    }

    @Test
    void theTargetGoesToTheBrig() {
        execute(new ArrestOrderEvent(1, 2));

        assertFollowup(all(new SendToBrigEvent(2), new DiscardPlayedQuorumCardEvent(ARREST_ORDER)));
    }

    @Test
    void notACharacterAlreadyInTheBrig() {
        moveTo(BRIG, WILLIAM_ADAMA);

        assertInvalid(new ArrestOrderEvent(1, 2));
    }

    @Test
    void notSomeoneWhoIsNoPlayer() {
        assertInvalid(new ArrestOrderEvent(1, 9));
    }
}
