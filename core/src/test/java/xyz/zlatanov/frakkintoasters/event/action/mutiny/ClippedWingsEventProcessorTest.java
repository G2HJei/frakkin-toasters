package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.ReturnVipersToReservesEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.CLIPPED_WINGS;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ClippedWingsEventProcessorTest extends EventTestHarness<ClippedWingsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(CLIPPED_WINGS);
    }

    @Test
    void theVipersComeBackRepairedThenTwoTreachery() {
        viperAt(GALACTICA_SPACE_1_OCLOCK);
        val damaged = viperAt(GALACTICA_SPACE_3_OCLOCK);
        ops.ship(damaged.id()).damage();

        execute(new ClippedWingsEvent(1));

        assertTrue(galacticaBoard.damagedShips().isEmpty());
        assertTrue(galacticaBoard.reserves().stream().anyMatch(ship -> ship.id() == damaged.id()));
        assertFollowup(all(new ReturnVipersToReservesEvent(), treachery(1, 2),
                new DiscardPlayedMutinyCardEvent(CLIPPED_WINGS)));
    }

    @Test
    void notWithoutAViperInSpace() {
        assertInvalid(new ClippedWingsEvent(1));
    }
}
