package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUIS_HOSHI;

class HangarDeckActionEventTest {

    Game game = new Game(KOBOL, 3);

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(LOUIS_HOSHI);
        game.moveTo(HANGAR_DECK, LOUIS_HOSHI);
    }

    @Test
    void shouldLaunchViperWithExtraAction() {
        val followup = new HangarDeckActionEvent(1).execute(game);

        val expectedFollowUp = List.of(
                new PlayerDecisionEvent(1, LaunchViperEvent.class),
                new PlayerDecisionEvent(1, ActionEvent.class)
        );
        assertEquals(expectedFollowUp, followup);
    }
}