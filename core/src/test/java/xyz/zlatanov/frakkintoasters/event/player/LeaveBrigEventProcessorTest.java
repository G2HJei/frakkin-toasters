package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DESTINATION_GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;

class LeaveBrigEventProcessorTest extends EventTestHarness<LeaveBrigEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(GAIUS_BALTAR);
        moveTo(BRIG, GAIUS_BALTAR);
    }

    @ParameterizedTest
    @EnumSource(value = Location.class, names = {"COMMAND", "PRESS_ROOM"})
    void shouldLeaveBrig(Location destination) {
        execute(new LeaveBrigEvent(1, destination));

        assertEquals(destination, locate(GAIUS_BALTAR));
    }

    @Test
    void shouldMoveToGalacticaLocation() {
        execute(new LeaveBrigEvent(1, ARMORY, DESTINATION_GALACTICA));

        assertEquals(ARMORY, locate(GAIUS_BALTAR));
    }

    @Test
    void shouldRejectLocationOutsideGalactica() {
        assertInvalid(new LeaveBrigEvent(1, PRESS_ROOM, DESTINATION_GALACTICA));
    }

    @ParameterizedTest
    @EnumSource(value = Location.class, names = {"BRIG", "SICKBAY", "RESURRECTION_SHIP", "CAPRICA",
            "GALACTICA_SPACE_5_OCLOCK"})
    void shouldRejectIneligibleDestination(Location destination) {
        assertInvalid(new LeaveBrigEvent(1, destination));
    }

    @Test
    void shouldRejectCharacterNotInBrig() {
        moveTo(COMMAND, GAIUS_BALTAR);

        assertInvalid(new LeaveBrigEvent(1, ARMORY));
    }

    // Every human was in the "Brig", so the Admiral and the CAG are there: the first to leave takes their titles (rules
    // 4.4.2.3 and 4.4.3.2).
    @Test
    void shouldHandTheTitlesOnWhenTheAdmiralAndTheCagAreInTheBrig() {
        ops.titles().assignAdmiral(GAIUS_BALTAR);
        ops.titles().assignCag(GAIUS_BALTAR);
        player(2).character(WILLIAM_ADAMA);
        moveTo(BRIG, WILLIAM_ADAMA);

        execute(new LeaveBrigEvent(2, COMMAND));

        assertFollowup(all(new DetermineNextAdmiralEvent(), new DetermineNextCagEvent()));
    }

    @Test
    void shouldKeepTheTitlesOfAnAdmiralOutsideTheBrig() {
        player(2).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        ops.titles().assignAdmiral(WILLIAM_ADAMA);

        execute(new LeaveBrigEvent(1, ARMORY));
    }

    @Test
    void shouldNotLeaveForADestroyedPegasus() {
        pegasusBoard.destroy();

        assertInvalid(new LeaveBrigEvent(1, AIRLOCK));
    }
}
