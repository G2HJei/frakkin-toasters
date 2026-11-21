package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTIVATE_CYLON_SHIPS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SIMON_ONEIL;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MODIFICATIONS;
import static xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation.*;

class ModificationsEventProcessorTest extends EventTestHarness<ModificationsEvent> {

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(2).character(SIMON_ONEIL);
        moveTo(CYLON_FLEET, SIMON_ONEIL);
    }

    // The activation he picks replaces the card's icon.
    @Test
    void activatesWhatHePicks() {
        ops.changeStep(ACTIVATE_CYLON_SHIPS);

        execute(new ModificationsEvent(2, LAUNCH_RAIDERS));

        assertEquals(0, player(2).miracleTokens());
        assertTrue(game.turn().hasUsed(2, MODIFICATIONS));
        assertFollowup(new LaunchRaidersEvent());
    }

    // The card's activation needs no answer, so the choice is his.
    @Test
    void offeredInsteadOfTheCardsIcon() {
        assertEquals(one(
                        new ActivateRaidersEvent(),
                        new ModificationsEvent(2, ACTIVATE_RAIDERS),
                        new ModificationsEvent(2, LAUNCH_RAIDERS),
                        new ModificationsEvent(2, ACTIVATE_HEAVY_RAIDERS),
                        new ModificationsEvent(2, ACTIVATE_BASESTARS)),
                ModificationsEventProcessor.offer(game, new ActivateRaidersEvent()));
        assertInvalid(new ModificationsEvent(2, LAUNCH_RAIDERS)); // not the Activate Cylon Ships step yet
    }

    @Test
    void theCardsIconWithoutTheAbility() {
        ops.player(2).loseMiracleTokens();

        assertEquals(single(new ActivateCylonBasestarsEvent()),
                ModificationsEventProcessor.offer(game, new ActivateCylonBasestarsEvent()));
        ops.changeStep(ACTIVATE_CYLON_SHIPS);
        assertInvalid(new ModificationsEvent(2, ACTIVATE_HEAVY_RAIDERS));
    }

    @Test
    void onlyInTheActivateCylonShipsStep() {
        assertInvalid(new ModificationsEvent(2, ACTIVATE_HEAVY_RAIDERS));
    }
}
