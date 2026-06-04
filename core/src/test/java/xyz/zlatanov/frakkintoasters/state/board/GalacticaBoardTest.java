package xyz.zlatanov.frakkintoasters.state.board;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.POSITION_1;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.START;

class GalacticaBoardTest {
    GalacticaBoard board = new GalacticaBoard();

    @Test
    void shouldDestroyColonialOne() {
        board.destroyColonialOne();
        assertTrue(board.locations().stream().noneMatch(l -> Set.of(PRESS_ROOM, PRESIDENTS_OFFICE, ADMINISTRATION).contains(l)));
        assertTrue(board.colonialOneDestroyed());
    }

    @Test
    void shouldDestroyResurrectionShip() {
        board.destroyResurrectionShip();
        assertFalse(board.locations().contains(RESURRECTION_SHIP));
        assertTrue(board.locations().contains(HUB_DESTROYED));
        assertTrue(board.hubDestroyed());
    }

    @Test
    void shouldSendCharactersFromDestroyedColonialOneToSickbay() {
        board.place(PRESIDENTS_OFFICE, GAIUS_BALTAR); //hehe
        board.destroyColonialOne(); //oops
        assertEquals(Optional.of(SICKBAY), board.locate(GAIUS_BALTAR)); // poor Gaius
    }

    @Test
    void shouldStartWithAllResources() {
        assertEquals(8, board.food());
        assertEquals(10, board.morale());
        assertEquals(12, board.population());
    }

    @Test
    void shouldManageReserves() {
        assertDoesNotThrow(() -> board.removeFromReserves(Viper.class));
    }

    @Test
    void shouldManageDamagedShip() {
        val raptor = new Raptor(2);
        board.addToDamagedShips(raptor);
        assertEquals(raptor, board.removeFromDamagedShips(Raptor.class).orElseThrow());
    }

    @Test
    void shouldPlaceFighterShip() {
        val viper = new Viper(3);
        board.place(GALACTICA_SPACE_12_OCLOCK, viper);
        assertEquals(List.of(viper), board.shipsIn(GALACTICA_SPACE_12_OCLOCK));
    }

    @Test
    void shouldPlacePilotedShip() {
        val pilotedViper = new Viper(4).pilot(KARL_HELO_AGATHON);
        board.place(GALACTICA_SPACE_4_OCLOCK, pilotedViper);
        assertEquals(Optional.of(GALACTICA_SPACE_4_OCLOCK), board.locate(KARL_HELO_AGATHON));
    }

    @Test
    void shouldRemoveShip() {
        val viperMarkVii = new ViperMarkVII(5);
        board.place(GALACTICA_SPACE_12_OCLOCK, viperMarkVii);

        board.remove(viperMarkVii);

        assertEquals(List.of(), board.shipsIn(GALACTICA_SPACE_12_OCLOCK));
    }

    @Test
    void shouldTrackJumpPreparation() {
        board.advanceJumpPreparation();
        assertEquals(POSITION_1, board.jumpPreparation());
        board.advanceJumpPreparation();
        board.advanceJumpPreparation();
        board.advanceJumpPreparation();
        board.advanceJumpPreparation();
        assertEquals(START, board.jumpPreparation());
    }
}