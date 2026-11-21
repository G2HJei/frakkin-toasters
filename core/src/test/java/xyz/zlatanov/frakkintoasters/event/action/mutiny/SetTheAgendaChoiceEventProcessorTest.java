package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class SetTheAgendaChoiceEventProcessorTest extends EventTestHarness<SetTheAgendaChoiceEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(WILLIAM_ADAMA);
        presidentHand.addOnTop(EULOGY);
        presidentHand.addOnTop(ARREST_ORDER);
        presidentHand.addOnTop(PROBATION);
    }

    @Test
    void twoCardsGoToTheBottomInTheChosenOrder() {
        quorumDeck.clear();

        execute(new SetTheAgendaChoiceEvent(1, List.of(PROBATION, EULOGY)));

        assertEquals(List.of(ARREST_ORDER), presidentHand.cards());
        assertEquals(List.of(PROBATION, EULOGY), quorumDeck.cards());
    }

    @Test
    void notOneWhenHeHoldsMore() {
        assertInvalid(new SetTheAgendaChoiceEvent(1, List.of(PROBATION)));
    }

    @Test
    void notACardHeDoesNotHold() {
        assertInvalid(new SetTheAgendaChoiceEvent(1, List.of(PROBATION, RESIGNATION)));
    }

    @Test
    void theOnlyCardWhenHeHoldsOne() {
        presidentHand.remove(EULOGY);
        presidentHand.remove(PROBATION);

        execute(new SetTheAgendaChoiceEvent(1, List.of(ARREST_ORDER)));

        assertEquals(List.of(), presidentHand.cards());
    }
}
