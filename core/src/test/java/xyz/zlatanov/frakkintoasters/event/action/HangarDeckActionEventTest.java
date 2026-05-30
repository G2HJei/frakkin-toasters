package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUIS_HOSHI;

class HangarDeckActionEventTest extends EventTest {
    
    @BeforeEach
    void setUp() {
        player(1).selectCharacter(LOUIS_HOSHI);
        game.moveTo(HANGAR_DECK, LOUIS_HOSHI);
    }

    @Test
    void shouldLaunchViperWithExtraAction() {
        val followup = execute(new HangarDeckActionEvent(1));

        val expectedFollowUp = all(
                new PlayerDecisionEvent<>(1, LaunchViperEvent.class),
                new PlayerDecisionEvent<>(1, ActionEvent.class)
        );
        assertEquals(expectedFollowUp, followup);
    }
}
