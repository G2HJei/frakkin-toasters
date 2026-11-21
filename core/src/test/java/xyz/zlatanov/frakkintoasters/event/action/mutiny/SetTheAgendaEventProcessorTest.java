package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.SET_THE_AGENDA;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class SetTheAgendaEventProcessorTest extends EventTestHarness<SetTheAgendaEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(SET_THE_AGENDA);
    }

    @Test
    void thePresidentDrawsTwoThenThePlayerLooks() {
        execute(new SetTheAgendaEvent(1));

        assertFollowup(all(new DrawQuorumCardEvent(), new DrawQuorumCardEvent(), new SetTheAgendaLookEvent(1),
                new DiscardPlayedMutinyCardEvent(SET_THE_AGENDA)));
    }

    @Test
    void notByThePresident() {
        president(LAURA_ROSLIN);

        assertInvalid(new SetTheAgendaEvent(1));
    }
}
