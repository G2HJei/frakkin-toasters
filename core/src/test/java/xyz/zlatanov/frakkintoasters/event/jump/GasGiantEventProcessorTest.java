package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class GasGiantEventProcessorTest extends EventTestHarness<GasGiantEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    // "The Admiral may destroy 1 viper to gain 1 fuel."
    @Test
    void aViperInTheReservesIsDestroyedForFuel() {
        val fuel = galacticaBoard.fuel();
        val vipers = galacticaBoard.reserves().stream().filter(Viper.class::isInstance).count();

        execute(new GasGiantEvent(2));

        assertEquals(vipers - 1, galacticaBoard.reserves().stream().filter(Viper.class::isInstance).count());
        assertEquals(fuel + 1, galacticaBoard.fuel());
    }

    @Test
    void onlyTheAdmiralCanDoIt() {
        assertInvalid(new GasGiantEvent(1));
    }
}
