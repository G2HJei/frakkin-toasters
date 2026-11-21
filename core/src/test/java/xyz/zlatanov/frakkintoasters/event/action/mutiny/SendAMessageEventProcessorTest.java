package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import lombok.val;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.damageGalactica;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.SEND_A_MESSAGE;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class SendAMessageEventProcessorTest extends EventTestHarness<SendAMessageEvent> {

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
        player(1).mutinyCards().addOnTop(SEND_A_MESSAGE);
    }

    @Test
    void damageGalacticaThenAttackTheCenturion() {
        ops.galactica().placeCenturion();
        val centurion = galacticaBoard.boardingPartyTrack().keySet().iterator().next();

        execute(new SendAMessageEvent(1, centurion.id()));

        assertFollowup(all(damageGalactica(), rollDie(new SendAMessageResultEvent(centurion.id())),
                single(new DiscardPlayedMutinyCardEvent(SEND_A_MESSAGE))));
    }

    @Test
    void withAnEmptyTrackOnlyTheDamage() {
        execute(new SendAMessageEvent(1, null));

        assertFollowup(all(damageGalactica(), single(new DiscardPlayedMutinyCardEvent(SEND_A_MESSAGE))));
    }

    @Test
    void aCenturionOnTheTrackHasToBeAttacked() {
        ops.galactica().placeCenturion();

        assertInvalid(new SendAMessageEvent(1, null));
    }
}
