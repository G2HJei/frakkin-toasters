package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUANNE_KAT_KATRAINE;

class SacrificeEventProcessorTest extends EventTestHarness<SacrificeEvent> {

    private static final xyz.zlatanov.frakkintoasters.state.board.Location AREA = GALACTICA_SPACE_4_OCLOCK;

    @BeforeEach
    void setUp() {
        player(1).character(LOUANNE_KAT_KATRAINE);
        ops.player(1).pilot(viperAt(AREA).id());
    }

    @Test
    void goesToSickbayToDestroyFiveRaiders() {
        val raiders = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++) {
            raiders.add(raiderAt(AREA).id());
        }

        execute(new SacrificeEvent(1, raiders.subList(0, 5)));

        assertEquals(SICKBAY, locate(LOUANNE_KAT_KATRAINE));
        assertShipCount(AREA, Raider.class, 1);
    }

    @Test
    void allTheRaidersWhenFewerAreThere() {
        val raider = raiderAt(AREA).id();

        execute(new SacrificeEvent(1, List.of(raider)));

        assertShipCount(AREA, Raider.class, 0);
    }

    @Test
    void aDestroyedBasestarOpensTheMajorVictoryWindow() {
        val basestar = basestarAt(AREA).id();

        execute(new SacrificeEvent(1, List.of(basestar)));

        assertFollowup(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));
    }

    @Test
    void notFewerRaidersThanThereAre() {
        val first = raiderAt(AREA).id();
        raiderAt(AREA);

        assertInvalid(new SacrificeEvent(1, List.of(first)));
    }

    @Test
    void notShipsOfAnotherArea() {
        val elsewhere = raiderAt(GALACTICA_SPACE_6_OCLOCK).id();
        raiderAt(AREA);

        assertInvalid(new SacrificeEvent(1, List.of(elsewhere)));
    }

    @Test
    void notWhenSheIsntPiloting() {
        moveTo(HANGAR_DECK, LOUANNE_KAT_KATRAINE);
        val raider = raiderAt(AREA).id();

        assertInvalid(new SacrificeEvent(1, List.of(raider)));
    }

    // The civilian ship is destroyed the usual way, so Brendan "Hot Dog" Costanza may "Escort" it.
    @Test
    void aCivilianShipIsDestroyedTheUsualWay() {
        val ship = civilianShipAt(AREA).id();

        execute(new SacrificeEvent(1, List.of(ship)));

        assertEquals(SICKBAY, locate(LOUANNE_KAT_KATRAINE));
        assertFollowup(new DestroyCivilianShipEvent(ship));
    }
}
