package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class SendToBrigEventProcessorTest extends EventTestHarness<SendToBrigEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LEE_APOLLO_ADAMA);
        player(3).character(WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        moveTo(COMMAND, LEE_APOLLO_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
    }

    @Test
    void shouldSendTargetToBrig() {
        execute(new SendToBrigEvent(1, 2));

        assertEquals(BRIG, locate(LEE_APOLLO_ADAMA));
        assertEquals(HANGAR_DECK, locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldLandPilotedViper() {
        val viper = viperAt(GALACTICA_SPACE_2_OCLOCK);
        ops.player(2).pilot(viper.id());

        execute(new SendToBrigEvent(1, 2));

        assertEquals(BRIG, locate(LEE_APOLLO_ADAMA));
        assertTrue(galacticaBoard.reserves().contains(viper));
        assertNull(viper.pilot());
    }

    @Test
    void shouldRejectCharacterAlreadyInBrig() {
        moveTo(BRIG, LEE_APOLLO_ADAMA);

        assertInvalid(new SendToBrigEvent(1, 2));
    }

    @Test
    void shouldRejectRevealedCylon() {
        revealCylon();

        assertInvalid(new SendToBrigEvent(2, 1));
    }

    @Test
    void shouldStripAdmiralOfTitle() {
        admiral(WILLIAM_ADAMA);

        execute(new SendToBrigEvent(1, 3));

        assertFollowup(new DetermineNextAdmiralEvent());
    }

    @Test
    void shouldStripCagOfTitle() {
        cag(LEE_APOLLO_ADAMA);

        execute(new SendToBrigEvent(1, 2));

        assertFollowup(new DetermineNextCagEvent());
    }

    @Test
    void shouldStripAdmiralAndCagOfTitles() {
        admiral(LEE_APOLLO_ADAMA);
        cag(LEE_APOLLO_ADAMA);

        execute(new SendToBrigEvent(1, 2));

        assertFollowup(all(new DetermineNextAdmiralEvent(), new DetermineNextCagEvent()));
    }

    @Test
    void shouldKeepPresidentTitle() {
        president(LEE_APOLLO_ADAMA);

        execute(new SendToBrigEvent(1, 2));

        assertEquals(LEE_APOLLO_ADAMA, president());
    }
}
