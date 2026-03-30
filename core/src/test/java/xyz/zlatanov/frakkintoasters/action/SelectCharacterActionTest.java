package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;


class SelectCharacterActionTest {
    Game game = new Game(KOBOL, 2);

    @Test
    void shouldSelectCharacter() {
        select(SAUL_TIGH).execute(game);
        assertEquals(SAUL_TIGH, game.player(1).character());
        assertEquals(COMMAND, game.locate(SAUL_TIGH));
    }

    @Test
    void shouldFollowupWithSetupOptions() {
        val followup = select(HELENA_CAIN).execute(game);

        val expected = List.of(
                new MoveAction(1, PEGASUS_CIC),
                new MoveAction(1, COMMAND));
        assertEquals(expected, followup);
    }

    @Test
    void shouldNotPlaceVanillaHelo() {
        select(KARL_HELO_AGATHON).execute(game);
        assertThrows(FrakCallTheAdmiralException.class, () -> game.locate(KARL_HELO_AGATHON));
    }

    @Test
    void shouldFollowupForApollo() {
        val followup = select(LEE_APOLLO_ADAMA).execute(game);
        val expected = List.of(
                new LaunchViperAction(1, VIPER, GALACTICA_SPACE_4_OCLOCK),
                new LaunchViperAction(1, VIPER, GALACTICA_SPACE_6_OCLOCK),
                new LaunchViperAction(1, ASSAULT_RAPTOR, GALACTICA_SPACE_4_OCLOCK),
                new LaunchViperAction(1, ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK)
        );
        assertEquals(expected, followup);
    }

    @Test
    void shouldNotAllowDoubleSelection() {
        select(KARA_STARBUCK_THRACE).execute(game);
        val invalidAction = new SelectCharacterAction(2, KARA_STARBUCK_THRACE);
        assertThrows(InvalidActionException.class, () -> invalidAction.execute(game));
    }

    @Test
    void shouldNotAllowDoubleSelectionOfAlternateVersion() {
        select(GAIUS_BALTAR).execute(game);
        val invalidAction = new SelectCharacterAction(2, GAIUS_BALTAR_ALT);
        assertThrows(InvalidActionException.class, () -> invalidAction.execute(game));
    }

    static SelectCharacterAction select(Character character) {
        return new SelectCharacterAction(1, character);
    }

}