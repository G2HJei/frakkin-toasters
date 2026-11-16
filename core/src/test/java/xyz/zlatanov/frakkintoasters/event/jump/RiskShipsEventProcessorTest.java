package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.jump.RiskShipsEvent.Risk;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class RiskShipsEventProcessorTest extends EventTestHarness<RiskShipsEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    @Test
    void theAdmiralRollsADieForTheReward() {
        execute(new RiskShipsEvent(2, Risk.ICY_MOON));

        assertFollowup(rollDie(new RiskResultEvent(Risk.ICY_MOON)));
    }

    // "In order to risk a ship, there must be at least one of the required type of ship in the Reserves." (rule 13.5)
    @Test
    void theShipsHaveToBeInTheReserves() {
        galacticaBoard.reserves().stream()
                .filter(Raptor.class::isInstance)
                .forEach(raptor -> ops.ship(raptor.id()).destroy());

        assertInvalid(new RiskShipsEvent(2, Risk.TYLIUM_PLANET));
    }

    @Test
    void onlyTheAdmiralRisksShips() {
        assertInvalid(new RiskShipsEvent(1, Risk.ICY_MOON));
    }
}
