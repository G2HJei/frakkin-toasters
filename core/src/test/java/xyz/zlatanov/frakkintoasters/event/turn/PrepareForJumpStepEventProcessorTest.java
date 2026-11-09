package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.GameStep.PREPARE_FOR_JUMP;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.CONTROVERSIAL_MANUSCRIPT;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.LABOR_DISPUTE;

class PrepareForJumpStepEventProcessorTest extends EventTestHarness<PrepareForJumpStepEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
    }

    // "If the drawn Crisis Card has the "prepare for jump" icon on its lower right corner, then the fleet token moves one
    // space ahead (toward the "Auto Jump" space) on the Jump Preparation track." (rule 8.7)
    @Test
    void theIconMovesTheFleetToken() {
        ops.playCrisisCard(CONTROVERSIAL_MANUSCRIPT);

        execute(new PrepareForJumpStepEvent());

        assertEquals(PREPARE_FOR_JUMP, game.turn().step());
        assertFollowup(new IncreaseJumpPreparationEvent());
    }

    @Test
    void thereIsNoStepWithoutTheIcon() {
        ops.playCrisisCard(LABOR_DISPUTE);

        execute(new PrepareForJumpStepEvent());

        assertEquals(SETUP, game.turn().step());
    }

    // E.g. a revealed Cylon or a character in the "Brig"
    @Test
    void thereIsNoStepWithoutACrisisCard() {
        execute(new PrepareForJumpStepEvent());

        assertEquals(SETUP, game.turn().step());
    }

    // "This step is not skipped on Cylon turns. For example, if a Cylon player activated the "Caprica" location and
    // chose a Crisis Card that has a "prepare for jump" icon on it, the fleet marker advances" (rule 6.1.3.4)
    @Test
    void aRevealedCylonHasTheStepForACardChosenAtCaprica() {
        revealCylon();
        ops.playCrisisCard(CONTROVERSIAL_MANUSCRIPT);

        execute(new PrepareForJumpStepEvent());

        assertFollowup(new IncreaseJumpPreparationEvent());
    }

    // "During the "Prepare for Jump" step of the Mutineer's turn, if one or more "prepare for jump" icons are resolved,
    // he must draw a Mutiny Card." (rule 8.7.1)
    @Test
    void theMutineerDrawsAMutinyCard() {
        player(1).loyaltyCards().addOnTop(MUTINEER).reveal(MUTINEER);
        ops.playCrisisCard(CONTROVERSIAL_MANUSCRIPT);

        execute(new PrepareForJumpStepEvent());

        assertFollowup(all(new IncreaseJumpPreparationEvent(), new DrawMutinyCardEvent(1)));
    }

    @Test
    void theMutineerDrawsNothingWithoutTheIcon() {
        player(1).loyaltyCards().addOnTop(MUTINEER).reveal(MUTINEER);
        ops.playCrisisCard(LABOR_DISPUTE);

        execute(new PrepareForJumpStepEvent());
    }

    @Test
    void theMutineerDrawsOnlyOnTheirOwnTurn() {
        player(2).loyaltyCards().addOnTop(MUTINEER).reveal(MUTINEER);
        ops.playCrisisCard(CONTROVERSIAL_MANUSCRIPT);

        execute(new PrepareForJumpStepEvent());

        assertFollowup(new IncreaseJumpPreparationEvent());
    }
}
