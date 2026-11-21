package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ANASTASIA_DEE_DUALLA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.EFFICIENT;

class CommunicationsEventProcessorTest extends EventTestHarness<CommunicationsEvent> {

    CivilianShip civ1;
    CivilianShip civ2;
    int          id1;
    int          id2;

    @BeforeEach
    void setUp() {
        player(1).character(SAUL_TIGH);
        moveTo(COMMUNICATIONS, SAUL_TIGH);
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
        assertEquals(Set.of(id1, id2),
                player(1).revealedCivilianShipIds());
    }

    @Test
    void shouldRevealTheOnlyCivilianShipInSpace() {
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civ1);
        execute(new CommunicationsEvent(1, null, id1));
        assertFollowup(all(
                one(new NoOpEvent(1),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_2_OCLOCK),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_6_OCLOCK))));
    }

    @Test
    void shouldDoNothingWhenNoCivilianShipIsInSpace() {
        execute(new CommunicationsEvent(1, null, null));
    }

    @Test
    void shouldLookAt2CivilianShipsWhenThereAreThatMany() {
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civ1);
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, civ2);
        assertInvalid(new CommunicationsEvent(1, id1, null));
    }

    @Test
    void shouldNotLookAtTheSameCivilianShipTwice() {
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civ1);
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, civ2);
        assertInvalid(new CommunicationsEvent(1, id1, id1));
    }

    // Anastasia "Dee" Dualla's "Efficient": every civilian ship on the board, each of which she may move.
    @Test
    void deeLooksAtEveryCivilianShip() {
        player(2).character(ANASTASIA_DEE_DUALLA);
        moveTo(COMMUNICATIONS, ANASTASIA_DEE_DUALLA);
        val civ3 = civilianShip();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civ1);
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, civ2);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civ3);

        execute(new CommunicationsEvent(2, null, null));

        // by id: the ships were made in reverse
        assertFollowup(all(
                one(new NoOpEvent(2),
                        new MoveCivilianShipEvent(civ3.id(), GALACTICA_SPACE_2_OCLOCK),
                        new MoveCivilianShipEvent(civ3.id(), GALACTICA_SPACE_6_OCLOCK)),
                one(new NoOpEvent(2),
                        new MoveCivilianShipEvent(id2, GALACTICA_SPACE_4_OCLOCK),
                        new MoveCivilianShipEvent(id2, GALACTICA_SPACE_8_OCLOCK)),
                one(new NoOpEvent(2),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_12_OCLOCK),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_4_OCLOCK))));
        assertEquals(Set.of(id1, id2, civ3.id()), player(2).revealedCivilianShipIds());
        assertTrue(game.turn().hasUsed(2, EFFICIENT));
    }

    @Test
    void deeNeedNotNameTwoShips() {
        player(2).character(ANASTASIA_DEE_DUALLA);
        moveTo(COMMUNICATIONS, ANASTASIA_DEE_DUALLA);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civ1);
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, civ2);

        execute(new CommunicationsEvent(2, id1, null));

        assertEquals(Set.of(id1, id2), player(2).revealedCivilianShipIds());
        assertFollowup(all(
                one(new NoOpEvent(2),
                        new MoveCivilianShipEvent(id2, GALACTICA_SPACE_4_OCLOCK),
                        new MoveCivilianShipEvent(id2, GALACTICA_SPACE_8_OCLOCK)),
                one(new NoOpEvent(2),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_12_OCLOCK),
                        new MoveCivilianShipEvent(id1, GALACTICA_SPACE_4_OCLOCK))));
    }

    @Test
    void shouldOnlyLookAtCivilianShipsInSpace() {
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civ1);
        assertInvalid(new CommunicationsEvent(1, id2, null));
    }
}
