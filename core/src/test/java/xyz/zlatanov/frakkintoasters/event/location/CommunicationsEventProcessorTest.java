package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.RevealedCivilianShip;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class CommunicationsEventProcessorTest extends EventTestHarness<CommunicationsEvent> {

    @Test
    void shouldProceedWithRevealingCivilianShips() {
        val civ1 = civilianShip();
        val civ2 = civilianShip();
        val id1 = civ1.id();
        val id2 = civ2.id();
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

}