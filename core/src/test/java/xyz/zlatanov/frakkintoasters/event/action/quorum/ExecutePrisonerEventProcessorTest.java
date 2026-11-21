package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.EXECUTE_PRISONER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ExecutePrisonerEventProcessorTest extends EventTestHarness<ExecutePrisonerEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        moveTo(BRIG, WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(EXECUTE_PRISONER);
    }

    @Test
    void thePrisonerIsExecuted() {
        execute(new ExecutePrisonerEvent(1, 2));

        assertFollowup(new ExecuteEvent(2));
        assertEquals(List.of(EXECUTE_PRISONER), quorumDeck.discardedCards());
    }

    @Test
    void notSomeoneOutsideTheBrig() {
        assertInvalid(new ExecutePrisonerEvent(1, 3));
    }
}
