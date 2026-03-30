package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PEGASUS_CIC;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;


class SelectCharacterActionTest {
    Game game = new Game(KOBOL, 2);

    @Test
    void shouldSelectCharacter() {
        select(SAUL_TIGH).apply(game);
        assertEquals(SAUL_TIGH, game.player(1).character());
        assertEquals(COMMAND, game.locate(SAUL_TIGH));
    }

    @Test
    void shouldFollowupWithSetupOptions() {
        val followup = select(HELENA_CAIN).apply(game);

        val expected = List.of(
                new MoveAction(1, PEGASUS_CIC),
                new MoveAction(1, COMMAND));
        assertEquals(expected, followup);
    }

    @Test
    void shouldNotPlaceVanillaHelo() {
        select(KARL_HELO_AGATHON).apply(game);
        assertThrows(FrakCallTheAdmiralException.class, () -> game.locate(KARL_HELO_AGATHON));
    }

    @Test
    void shouldFollowupForApollo() {
        select(LEE_APOLLO_ADAMA).apply(game);
        //expect LaunchViperActions
    }

    static SelectCharacterAction select(Character character) {
        return new SelectCharacterAction(1, character);
    }

}