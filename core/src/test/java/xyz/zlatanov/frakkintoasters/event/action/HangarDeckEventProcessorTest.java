package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUIS_HOSHI;

class HangarDeckEventProcessorTest extends EventTestHarness<HangarDeckEvent> {

    @Test
    void shouldLaunchViperWithExtraAction() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        execute(new HangarDeckEvent(1));
        assertFollowup(
                all(
                        single(
                                new PlayerDecisionEvent<>(1, LaunchViperEvent.class)),
                        one(
                                new PlayerDecisionEvent<>(1, ActionEvent.class),
                                new NoOpEvent(1))));
    }

    @Test
    void shouldNotAllowNonPilotToUseLocation() {
        player(1).character(LOUIS_HOSHI);
        moveTo(HANGAR_DECK, LOUIS_HOSHI);
        assertInvalid(new HangarDeckEvent(1));
    }
}
