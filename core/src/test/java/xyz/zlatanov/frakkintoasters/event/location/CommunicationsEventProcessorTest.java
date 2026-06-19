package xyz.zlatanov.frakkintoasters.event.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.RevealedCivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class CommunicationsEventProcessorTest extends EventTestHarness<CommunicationsEvent> {

    CivilianShip civ1;
    CivilianShip civ2;
    int          id1;
    int          id2;

    @BeforeEach
    void setUp() {
        civ1 = civilianShip();
        civ2 = civilianShip();
        id1 = civ1.id();
        id2 = civ2.id();
    }

    @Test
    void shouldProceedWithRevealingCivilianShips() {
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civ1);
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, civ2);

        execute(new CommunicationsEvent(1, id1, id2));

        assertFollowup(
                all(
                        one(new NoOpEvent(1),
                                new MoveCivilianShipEvent(id1, GALACTICA_SPACE_12_OCLOCK),
                                new MoveCivilianShipEvent(id1, GALACTICA_SPACE_4_OCLOCK)),
                        one(new NoOpEvent(1),
                                new MoveCivilianShipEvent(id2, GALACTICA_SPACE_4_OCLOCK),
                                new MoveCivilianShipEvent(id2, GALACTICA_SPACE_8_OCLOCK))
                )
        );
        assertEquals(List.of(
                        new RevealedCivilianShip(id1, 2),
                        new RevealedCivilianShip(id2, 2)),
                player(1).revealedCivilianShips());
    }

    @Test
    void shouldProceedWithRevealingOnlyOneCivilianShip() {
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civ1);
        execute(new CommunicationsEvent(2, id1, null));
        assertFollowup(all(
                one(new NoOpEvent(2),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_2_OCLOCK),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_6_OCLOCK))));
    }

    @Test
    void shouldDoNothingWhenNoCivilianShipSelected() {
        execute(new CommunicationsEvent(3, null, null));
        assertFollowup(NONE);
    }

}