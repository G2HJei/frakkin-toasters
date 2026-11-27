package xyz.zlatanov.frakkintoasters.event.action.title;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class LaunchNukeEventProcessorTest extends EventTestHarness<LaunchNukeEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
    }

    // A space area without ships is a legal target too: the nuke then has no effect (rule 11.6.1).
    @Test
    void theAdmiralSpendsANukeAndRolls() {
        execute(new LaunchNukeEvent(1, GALACTICA_SPACE_5_OCLOCK));

        assertEquals(1, game.nukes());
        assertFollowup(rollDie(new NukeResultEvent(1, GALACTICA_SPACE_5_OCLOCK)));
    }

    @Test
    void onlyTheAdmiral() {
        assertInvalid(new LaunchNukeEvent(2, GALACTICA_SPACE_5_OCLOCK));
    }

    @Test
    void notWithoutANuke() {
        ops.discardNuke();
        ops.discardNuke();

        assertInvalid(new LaunchNukeEvent(1, GALACTICA_SPACE_5_OCLOCK));
    }

    @Test
    void onlyAtASpaceArea() {
        assertInvalid(new LaunchNukeEvent(1, COMMAND));
    }

    @Test
    void notWithoutASpaceArea() {
        assertInvalid(new LaunchNukeEvent(1, null));
    }

    // An Admiral who keeps the title in the "Brig" (every human is there) keeps its action, as the President does.
    @Test
    void alsoFromTheBrig() {
        moveTo(BRIG, WILLIAM_ADAMA);

        execute(new LaunchNukeEvent(1, GALACTICA_SPACE_5_OCLOCK));

        assertFollowup(rollDie(new NukeResultEvent(1, GALACTICA_SPACE_5_OCLOCK)));
    }
}
