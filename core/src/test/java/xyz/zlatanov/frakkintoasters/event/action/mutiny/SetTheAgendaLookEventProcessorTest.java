package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsSeen;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.QUORUM;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class SetTheAgendaLookEventProcessorTest extends EventTestHarness<SetTheAgendaLookEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(WILLIAM_ADAMA);
    }

    @Test
    void thePlayerSeesTheHandThenChooses() {
        presidentHand.addOnTop(EULOGY);
        presidentHand.addOnTop(ARREST_ORDER);

        execute(new SetTheAgendaLookEvent(1));

        assertEquals(List.of(new CardsSeen(1, QUORUM, presidentHand.cards())),
                journal.facts().stream().filter(CardsSeen.class::isInstance).toList());
        assertFollowup(new PlayerDecisionEvent<>(1, SetTheAgendaChoiceEvent.class));
    }

    @Test
    void anEmptyHandHasNothingToChoose() {
        execute(new SetTheAgendaLookEvent(1));
    }
}
