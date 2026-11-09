package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Player;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class StartTurnEventProcessorTest extends EventTestHarness<StartTurnEvent> {

    // Rule 8.1
    private static final Followup STEPS = all(
            new ReceiveSkillsStepEvent(),
            new MovementStepEvent(),
            new ActionStepEvent(),
            new CrisisStepEvent(),
            new ActivateCylonShipsStepEvent(),
            new PrepareForJumpStepEvent(),
            new DiscardStepEvent(),
            new EndTurnEvent());

    @Test
    void theTurnCountsForTheGameAndTheCurrentPlayer() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        ops.startTurn();
        ops.changeCurrentPlayer(2);

        execute(new StartTurnEvent());

        assertEquals(2, game.turn().number());
        assertEquals(List.of(1, 1, 0), game.players().stream().map(Player::turns).toList());
        assertFollowup(STEPS);
    }

    // "Stranded - Your character is not placed on the game board at the start of the game. ... At the start of your 2nd
    // turn, place your character on the "Hangar Deck" location."
    @Test
    void strandedHeloStaysOffTheBoardInHisFirstTurn() {
        player(1).character(KARL_HELO_AGATHON);

        execute(new StartTurnEvent());

        assertTrue(game.locate(KARL_HELO_AGATHON).isEmpty());
        assertFollowup(STEPS);
    }

    @Test
    void strandedHeloReachesTheHangarDeckAtTheStartOfHisSecondTurn() {
        player(1).character(KARL_HELO_AGATHON);
        ops.startTurn();

        execute(new StartTurnEvent());

        assertEquals(Optional.of(HANGAR_DECK), game.locate(KARL_HELO_AGATHON));
        assertFollowup(STEPS);
    }

    // The 2nd turn of the game is another player's.
    @Test
    void strandedHeloCountsOnlyHisOwnTurns() {
        player(1).character(KARL_HELO_AGATHON);
        player(2).character(WILLIAM_ADAMA);
        ops.startTurn();
        ops.changeCurrentPlayer(2);

        execute(new StartTurnEvent());

        assertEquals(2, game.turn().number());
        assertTrue(game.locate(KARL_HELO_AGATHON).isEmpty());
        assertFollowup(STEPS);
    }
}
