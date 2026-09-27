package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUANNE_KAT_KATRAINE;

class ShipOperationsDestroyTest extends TestHarness {

    ShipOperations shipOps;

    @BeforeEach
    void setUp() {
        shipOps = new ShipOperations(game);
    }

    @Test
    void shouldDestroyRaider() {
        val raider = raiderAt(GALACTICA_SPACE_6_OCLOCK);

        shipOps.destroy(raider.id());

        assertNoShips(GALACTICA_SPACE_6_OCLOCK);
        assertTrue(cylonShips.raiders().contains(raider));
    }

    @Test
    void shouldDestroyHeavyRaider() {
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_4_OCLOCK);

        shipOps.destroy(heavyRaider.id());

        assertNoShips(GALACTICA_SPACE_4_OCLOCK);
        assertTrue(cylonShips.heavyRaiders().contains(heavyRaider));
    }

    @Test
    void shouldDestroyBasestar() {
        val basestar = basestarAt(GALACTICA_SPACE_2_OCLOCK);

        shipOps.destroy(basestar.id());

        assertNoShips(GALACTICA_SPACE_2_OCLOCK);
        assertTrue(cylonShips.basestars().contains(basestar));
    }

    @Test
    void shouldDestroyAssaultRaptor() {
        val assRaptor = assaultRaptorAt(GALACTICA_SPACE_12_OCLOCK);

        shipOps.destroy(assRaptor.id());

        assertNoShips(GALACTICA_SPACE_12_OCLOCK);
        assertTrue(game.removedComponents().contains(assRaptor));
    }

    @Test
    void shouldDestroyViper() {
        val viper = viperAt(GALACTICA_SPACE_10_OCLOCK);

        shipOps.destroy(viper.id());

        assertNoShips(GALACTICA_SPACE_10_OCLOCK);
        assertTrue(game.removedComponents().contains(viper));
    }

    @Test
    void shouldDestroyViperMarkVII() {
        val viperMk7 = viperMarkVIIAt(GALACTICA_SPACE_10_OCLOCK);

        shipOps.destroy(viperMk7.id());

        assertNoShips(GALACTICA_SPACE_10_OCLOCK);
        assertTrue(game.removedComponents().contains(viperMk7));
    }

    @Test
    void shouldDestroyRaptor() {
        val raptor = galacticaBoard.reserves().stream().filter(s -> s instanceof Raptor).findFirst().orElseThrow();
        shipOps.destroy(raptor.id());
        assertTrue(game.removedComponents().contains(raptor));
    }

    @Test
    void shouldDestroyAssaultRaptorAndSendItsPilotToSickbay() {
        val assRaptor = assaultRaptorAt(GALACTICA_SPACE_8_OCLOCK)
                .pilot(LOUANNE_KAT_KATRAINE);
        shipOps.destroy(assRaptor.id());
        assertEquals(SICKBAY, locate(LOUANNE_KAT_KATRAINE));
    }
}