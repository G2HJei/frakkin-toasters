package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation;
import xyz.zlatanov.frakkintoasters.event.character.ModificationsEvent;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEvent;

import static xyz.zlatanov.frakkintoasters.state.character.Character.SIMON_ONEIL;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.AMBUSHED_BY_THE_PRESS;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.FULFILLER_OF_PROPHECY;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTIVATE_CYLON_SHIPS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;

class ActivateCylonShipsStepEventProcessorTest extends EventTestHarness<ActivateCylonShipsStepEvent> {

    @Test
    void aHumanHasTheStep() {
        player(1).character(KARA_STARBUCK_THRACE);

        execute(new ActivateCylonShipsStepEvent());

        assertEquals(ACTIVATE_CYLON_SHIPS, game.turn().step());
    }

    // "Cylon ships may appear, move or attack based on the icon on the bottom left of the Crisis Card" (rule 8.6)
    @Test
    void theIconOfTheCardThatWasDrawnActivatesTheShips() {
        player(1).character(KARA_STARBUCK_THRACE);
        ops.playCrisisCard(AMBUSHED_BY_THE_PRESS);

        execute(new ActivateCylonShipsStepEvent());

        assertFollowup(new ActivateRaidersEvent());
    }

    // The icon of the card drawn last, when the turn went back to the Crisis step.
    @Test
    void onlyTheLastCardActivatesTheShips() {
        player(1).character(KARA_STARBUCK_THRACE);
        ops.playCrisisCard(AMBUSHED_BY_THE_PRESS);
        ops.playCrisisCard(DETENTE);

        execute(new ActivateCylonShipsStepEvent());

        assertFollowup(new LaunchRaidersEvent());
    }

    // Without a Crisis step, e.g. in the "Brig", there is no card and no icon.
    @Test
    void withoutACardNothingActivates() {
        player(1).character(KARA_STARBUCK_THRACE);

        execute(new ActivateCylonShipsStepEvent());

        assertEquals(ACTIVATE_CYLON_SHIPS, game.turn().step());
    }

    // "After the Activate Cylon Ships step, return to the Resolve Crisis step."
    @Test
    void aCardCanSendTheTurnBackToTheCrisisStep() {
        player(1).character(KARA_STARBUCK_THRACE);
        ops.playCrisisCard(FULFILLER_OF_PROPHECY);
        ops.returnToCrisisStep(true);

        execute(new ActivateCylonShipsStepEvent());

        assertFollowup(all(new ActivateCylonBasestarsEvent(), new CrisisStepEvent(true)));
        assertFalse(game.turn().returnsToCrisisStep());
    }

    // "Exceptions: Revealed Cylons do not perform this step." (rule 8.6)
    @Test
    void aRevealedCylonHasNoStep() {
        player(1).character(KARA_STARBUCK_THRACE);
        revealCylon();

        execute(new ActivateCylonShipsStepEvent());

        assertEquals(SETUP, game.turn().step());
    }

    // The turn ended early, e.g. the current player was executed (rule 7.1), so the step doesn't happen.
    @Test
    void shouldNotHappenAfterTheTurnEndedEarly() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.endTurnEarly();

        execute(new ActivateCylonShipsStepEvent());

        assertEquals(SETUP, game.turn().step());
    }

    // "Modifications": Simon O'Neill may pick the activation instead of the card's icon.
    @Test
    void simonONeillMayPickTheActivation() {
        setUpGame(4);
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(SIMON_ONEIL);
        ops.playCrisisCard(AMBUSHED_BY_THE_PRESS);

        execute(new ActivateCylonShipsStepEvent());

        assertFollowup(one(new ActivateRaidersEvent(),
                new ModificationsEvent(2, CylonActivation.ACTIVATE_RAIDERS),
                new ModificationsEvent(2, CylonActivation.LAUNCH_RAIDERS),
                new ModificationsEvent(2, CylonActivation.ACTIVATE_HEAVY_RAIDERS),
                new ModificationsEvent(2, CylonActivation.ACTIVATE_BASESTARS)));
    }

    // "Modifications" is asked at the start of any player's step, also one without a Crisis Card: nothing activates
    // unless Simon O'Neill picks something.
    @Test
    void simonONeillMayPickTheActivationWithoutACard() {
        setUpGame(4);
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(SIMON_ONEIL);

        execute(new ActivateCylonShipsStepEvent());

        assertFollowup(one(new NoOpEvent(2),
                new ModificationsEvent(2, CylonActivation.ACTIVATE_RAIDERS),
                new ModificationsEvent(2, CylonActivation.LAUNCH_RAIDERS),
                new ModificationsEvent(2, CylonActivation.ACTIVATE_HEAVY_RAIDERS),
                new ModificationsEvent(2, CylonActivation.ACTIVATE_BASESTARS)));
    }
}
