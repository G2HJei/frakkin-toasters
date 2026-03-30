package xyz.zlatanov.frakkintoasters.setup;

import lombok.val;
import org.junit.jupiter.api.Test;

import static xyz.zlatanov.frakkintoasters.setup.FirstPlayerSelection.PLAYER_2;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;

class GameSetupDriverTest {


    @Test
    void shouldCreateNewGame() {
        val setup = new GameSetupDriver()
                .objective(KOBOL)
                .playerCount(3)
                .firstPlayerSelection(PLAYER_2);
        //val game = setup.startGame();

    }
}