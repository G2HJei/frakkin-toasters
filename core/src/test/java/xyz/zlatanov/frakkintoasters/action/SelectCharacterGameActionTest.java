package xyz.zlatanov.frakkintoasters.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;


class SelectCharacterGameActionTest {

    GameAction action = SelectCharacterGameAction.builder()
            .playerNumber(1)
            .selectedCharacter(SAUL_TIGH)
            .build();
    Game       game   = new Game(KOBOL, 2);

    @Test
    void shouldSelectCharacter() {
        action.apply(game);
        assertEquals(SAUL_TIGH, game.player(1).character());
        assertEquals(COMMAND, game.locate(SAUL_TIGH));
    }


}