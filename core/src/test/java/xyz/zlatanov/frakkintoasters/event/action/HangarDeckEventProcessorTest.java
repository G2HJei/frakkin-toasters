package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUIS_HOSHI;

class HangarDeckEventProcessorTest extends EventTestHarness<HangarDeckEvent> {

    @BeforeEach
    void setUp() {
        selectCharacter(1, LOUIS_HOSHI);
        moveTo(HANGAR_DECK, LOUIS_HOSHI);
    }

    @Test
    void shouldLaunchViperWithExtraAction() {
        executeAndAssertFollowup(new HangarDeckEvent(1),
                all(
                        new PlayerDecisionEvent<>(1, LaunchViperEvent.class),
                        new PlayerDecisionEvent<>(1, ActionEvent.class)
                ));
    }
}
