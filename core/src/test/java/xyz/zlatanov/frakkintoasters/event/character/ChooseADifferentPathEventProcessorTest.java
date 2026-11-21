package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.discard;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CHOOSE_A_DIFFERENT_PATH;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.ABANDON_GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;

class ChooseADifferentPathEventProcessorTest extends EventTestHarness<ChooseADifferentPathEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
        player(2).character(LEE_ADAMA);
        moveTo(ADMIRALS_QUARTERS, LEE_ADAMA);
        ops.playCrisisCard(RIOTS_NO_JUMP);
    }

    // Instead of a section, the current player discards 5 Skill Cards.
    @Test
    void theCurrentPlayerDiscardsFive() {
        execute(new ChooseADifferentPathEvent(2, RIOTS_NO_JUMP));

        assertEquals(0, player(2).miracleTokens());
        assertTrue(game.turn().hasUsed(2, CHOOSE_A_DIFFERENT_PATH));
        assertFollowup(discard(1, 5));
    }

    @Test
    void onlyOnTheCardBeingResolved() {
        assertInvalid(new ChooseADifferentPathEvent(2, ABANDON_GALACTICA));
    }

    @Test
    void onlyLeeAdama() {
        assertInvalid(new ChooseADifferentPathEvent(1, RIOTS_NO_JUMP));
    }

    @Test
    void onlyWithAMiracleToken() {
        ops.player(2).loseMiracleTokens();

        assertInvalid(new ChooseADifferentPathEvent(2, RIOTS_NO_JUMP));
    }
}
