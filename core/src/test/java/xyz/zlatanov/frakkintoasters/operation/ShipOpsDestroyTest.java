package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUANNE_KAT_KATRAINE;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;

class ShipOpsDestroyTest extends TestHarness {

    @Test
    void shouldDestroyRaider() {
        val raider = raiderAt(GALACTICA_SPACE_6_OCLOCK);

        ops.ship(raider.id()).destroy();

        assertNoShips(GALACTICA_SPACE_6_OCLOCK);
        assertTrue(cylonShips.raiders().contains(raider));
    }

    @Test
    void shouldDestroyHeavyRaider() {
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_4_OCLOCK);

        ops.ship(heavyRaider.id()).destroy();

        assertNoShips(GALACTICA_SPACE_4_OCLOCK);
        assertTrue(cylonShips.heavyRaiders().contains(heavyRaider));
    }

    @Test
    void shouldDestroyBasestar() {
        val basestar = basestarAt(GALACTICA_SPACE_2_OCLOCK);

        ops.ship(basestar.id()).destroy();

        assertNoShips(GALACTICA_SPACE_2_OCLOCK);
        assertTrue(cylonShips.basestars().contains(basestar));
    }

    @Test
    void shouldReturnBasestarDamageToTheDeckAndRepairTheBasestar() {
        val basestar = basestarAt(GALACTICA_SPACE_2_OCLOCK);
        val fullDeckSize = basestarDamageDeck.size();
        basestarDamageDeck.nextCard(STRUCTURAL_DAMAGE);
        basestar.damage(basestarDamageDeck.draw());

        ops.ship(basestar.id()).destroy();

        assertTrue(basestar.damage().isEmpty());
        assertEquals(fullDeckSize, basestarDamageDeck.size());
    }

    @Test
    void shouldDestroyAssaultRaptor() {
        val assRaptor = assaultRaptorAt(GALACTICA_SPACE_12_OCLOCK);

        ops.ship(assRaptor.id()).destroy();

        assertNoShips(GALACTICA_SPACE_12_OCLOCK);
        assertTrue(game.removedComponents().contains(assRaptor));
    }

    @Test
    void shouldDestroyViper() {
        val viper = viperAt(GALACTICA_SPACE_10_OCLOCK);

        ops.ship(viper.id()).destroy();

        assertNoShips(GALACTICA_SPACE_10_OCLOCK);
        assertTrue(game.removedComponents().contains(viper));
    }

    @Test
    void shouldDestroyViperMarkVII() {
        val viperMk7 = viperMarkVIIAt(GALACTICA_SPACE_10_OCLOCK);

        ops.ship(viperMk7.id()).destroy();

        assertNoShips(GALACTICA_SPACE_10_OCLOCK);
        assertTrue(game.removedComponents().contains(viperMk7));
    }

    @Test
    void shouldDestroyRaptor() {
        val raptor = galacticaBoard.reserves().stream().filter(s -> s instanceof Raptor).findFirst().orElseThrow();
        ops.ship(raptor.id()).destroy();
        assertTrue(game.removedComponents().contains(raptor));
    }

    @Test
    void shouldDestroyAssaultRaptorAndSendItsPilotToSickbay() {
        player(1).character(LOUANNE_KAT_KATRAINE);
        val assRaptor = assaultRaptorAt(GALACTICA_SPACE_8_OCLOCK)
                .pilot(LOUANNE_KAT_KATRAINE);

        ops.ship(assRaptor.id()).destroy();

        assertEquals(SICKBAY, locate(LOUANNE_KAT_KATRAINE));
    }

    @Test
    void shouldDestroyCivilianShipInSpace() {
        val civShipId = -1;
        civilianShips.nextCard(new CivilianShip(civShipId, 1, 2, 3));
        place(GALACTICA_SPACE_2_OCLOCK, civilianShip());

        ops.ship(-1).destroy();

        assertEquals(7, galacticaBoard.fuel());
        assertEquals(8, galacticaBoard.morale());
        assertEquals(9, galacticaBoard.population());
    }

}