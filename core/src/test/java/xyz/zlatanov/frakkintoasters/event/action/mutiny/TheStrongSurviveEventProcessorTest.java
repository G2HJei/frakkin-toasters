package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.THE_STRONG_SURVIVE;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class TheStrongSurviveEventProcessorTest extends EventTestHarness<TheStrongSurviveEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(THE_STRONG_SURVIVE);
    }

    @Test
    void aCivilianShipForTheJumpTrack() {
        execute(new TheStrongSurviveEvent(1));

        assertFollowup(all(new DrawAndDestroyCivilianShipsEvent(1), new RemoveMutinyCardFromGameEvent(THE_STRONG_SURVIVE),
                new IncreaseJumpPreparationEvent()));
    }
}
