package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.title.NukeResultEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.UNAUTHORIZED_USAGE;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class UnauthorizedUsageEventProcessorTest extends EventTestHarness<UnauthorizedUsageEvent> {

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
        player(1).mutinyCards().addOnTop(UNAUTHORIZED_USAGE);
    }

    @Test
    void everyNukeIsSpentOnTheLaunch() {
        execute(new UnauthorizedUsageEvent(1, GALACTICA_SPACE_8_OCLOCK));

        assertEquals(0, game.nukes());
        assertFollowup(all(rollDie(new NukeResultEvent(1, GALACTICA_SPACE_8_OCLOCK)),
                single(new RemoveMutinyCardFromGameEvent(UNAUTHORIZED_USAGE))));
    }

    @Test
    void notWithoutNukes() {
        ops.discardNuke();
        ops.discardNuke();

        assertInvalid(new UnauthorizedUsageEvent(1, GALACTICA_SPACE_8_OCLOCK));
    }

    @Test
    void notOutsideASpaceArea() {
        assertInvalid(new UnauthorizedUsageEvent(1, COMMAND));
    }
}
