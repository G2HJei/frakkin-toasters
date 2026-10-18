package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;

class ViolentOutburstsEventProcessorTest extends EventTestHarness<ViolentOutburstsEvent> {
    @BeforeEach
    void setUp() {
        player(1).character(SHARON_BOOMER_VALERII);
        player(2).character(KARL_HELO_AGATHON);
        moveTo(ARMORY, SHARON_BOOMER_VALERII);
    }

    @Test
    void shouldSendToBrig() {
        execute(new ViolentOutburstsEvent());
        assertEquals(BRIG, game.locate(SHARON_BOOMER_VALERII));
    }

    @Test
    void shouldNotSendRevealedCylonToBrig() {
        revealCylon();
        execute(new ViolentOutburstsEvent());
        assertEquals(ARMORY, game.locate(SHARON_BOOMER_VALERII));
    }

    @Test
    void shouldNotSendUnarrivedHeloToBrig() {
        game.currentPlayerNumber(2);
        execute(new ViolentOutburstsEvent());
        assertNull(galacticaBoard.characters().get(KARL_HELO_AGATHON));
    }

    @Test
    void shouldSendHeloToBrig() {
        game.currentPlayerNumber(2).turn(2);
        execute(new ViolentOutburstsEvent());
        assertEquals(BRIG, game.locate(KARL_HELO_AGATHON));
    }

}