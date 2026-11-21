package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TORY_FOSTER;

class PlaceInSetupLocationEventProcessorTest extends EventTestHarness<PlaceInSetupLocationEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(HELENA_CAIN);
    }

    @Test
    void shouldPlaceCharacterOnChosenSetupLocation() {
        execute(new PlaceInSetupLocationEvent(1, PEGASUS_CIC));
        assertEquals(PEGASUS_CIC, locate(HELENA_CAIN));
    }

    @Test
    void shouldPlaceCylonLeaderOnChosenSetupLocation() {
        player(2).character(DANNA_BIERS);
        execute(new PlaceInSetupLocationEvent(2, HUMAN_FLEET));
        assertEquals(HUMAN_FLEET, locate(DANNA_BIERS));
        assertFalse(player(2).isInfiltrating());
    }

    // A Cylon Leader who starts on a location of the humans starts Infiltrating, as Sharon "Athena" Agathon does.
    @Test
    void shouldStartACylonLeaderInfiltratingAtThePressRoom() {
        player(2).character(DANNA_BIERS);

        execute(new PlaceInSetupLocationEvent(2, PRESS_ROOM));

        assertEquals(PRESS_ROOM, locate(DANNA_BIERS));
        assertTrue(player(2).isInfiltrating());
    }

    // A start location that is unavailable sends the new character to "Sickbay" (rule 7.4).
    @Test
    void shouldStartInSickbayInsteadOfADestroyedColonialOne() {
        player(2).character(TORY_FOSTER);
        ops.keepCrisisCardInPlay(SkillCheckType.BOMB_ON_COLONIAL_1);

        execute(new PlaceInSetupLocationEvent(2, SICKBAY));

        assertEquals(SICKBAY, locate(TORY_FOSTER));
    }

    // Helena Cain starts at "Command" once Pegasus is destroyed.
    @Test
    void shouldStartAtTheOtherSetupLocationOncePegasusIsDestroyed() {
        pegasusBoard.destroy();

        execute(new PlaceInSetupLocationEvent(1, COMMAND));

        assertEquals(COMMAND, locate(HELENA_CAIN));
    }

    @Test
    void shouldNotPlaceOnADestroyedPegasus() {
        pegasusBoard.destroy();

        assertInvalid(new PlaceInSetupLocationEvent(1, PEGASUS_CIC));
    }

    @Test
    void shouldNotPlaceOnADestroyedColonialOne() {
        player(2).character(TORY_FOSTER);
        ops.keepCrisisCardInPlay(SkillCheckType.BOMB_ON_COLONIAL_1);
        assertInvalid(new PlaceInSetupLocationEvent(2, PRESS_ROOM));
    }

    @Test
    void shouldNotPlaceOnAnotherGalacticaLocationInstead() {
        player(2).character(TORY_FOSTER);
        ops.keepCrisisCardInPlay(SkillCheckType.BOMB_ON_COLONIAL_1);
        assertInvalid(new PlaceInSetupLocationEvent(2, ARMORY));
    }

    @Test
    void shouldNotPlaceOutsideTheSetupLocations() {
        assertInvalid(new PlaceInSetupLocationEvent(1, ARMORY));
    }

    @Test
    void shouldNotPlaceCharacterAlreadyOnTheBoard() {
        moveTo(COMMAND, HELENA_CAIN);
        assertInvalid(new PlaceInSetupLocationEvent(1, PEGASUS_CIC));
    }
}
