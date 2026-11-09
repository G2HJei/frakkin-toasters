package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class AdmiralsQuartersEventProcessorTest extends EventTestHarness<AdmiralsQuartersEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(Location.ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        player(3).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
    }

    @Test
    void shouldActivateAdmiralsQuartersSkillCheck() {
        execute(new AdmiralsQuartersEvent(1, 3));
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldRememberTheChosenCharacter() {
        execute(new AdmiralsQuartersEvent(1, 3));
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
        assertEquals(3, game.activeSkillCheck().targetPlayerNumber());
    }

    @Test
    void shouldNotChooseACharacterAlreadyInTheBrig() {
        moveTo(BRIG, GAIUS_BALTAR);
        assertInvalid(new AdmiralsQuartersEvent(1, 3));
    }

    @Test
    void shouldNotChooseACylon() {
        player(2).character(CAVIL);
        assertInvalid(new AdmiralsQuartersEvent(1, 2));
    }

    @Test
    void shouldNotChooseSomeoneWhoIsNotPlaying() {
        assertInvalid(new AdmiralsQuartersEvent(1, 4));
    }
}
