package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;

class MovementStepEventProcessorTest extends EventTestHarness<MovementStepEvent> {

    // "the current player may move his character token to another location if he wishes" (rule 8.3)
    @Test
    void theCurrentPlayerMayMove() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);

        execute(new MovementStepEvent());

        assertEquals(MOVEMENT, game.turn().step());
        assertFollowup(one(new PlayerDecisionEvent<>(1, MoveEvent.class), new NoOpEvent(1)));
    }

    // "Characters in the "Brig" may not move" (rule 9.2)
    @Test
    void aCharacterInTheBrigCantMove() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);

        execute(new MovementStepEvent());

        assertEquals(MOVEMENT, game.turn().step());
    }

    @Test
    void strandedHeloCantMove() {
        player(1).character(KARL_HELO_AGATHON);

        execute(new MovementStepEvent());

        assertEquals(MOVEMENT, game.turn().step());
    }

    // The turn ended early, e.g. the current player was executed (rule 7.1), so the step doesn't happen.
    @Test
    void shouldNotHappenAfterTheTurnEndedEarly() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.endTurnEarly();

        execute(new MovementStepEvent());

        assertEquals(SETUP, game.turn().step());
    }
}
