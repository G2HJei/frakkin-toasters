package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActivateUnmannedViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class UnmannedViperRoundEventProcessorTest extends EventTestHarness<UnmannedViperRoundEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LEE_APOLLO_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        viperAt(GALACTICA_SPACE_6_OCLOCK);
    }

    @Test
    void activatesAViperThenAsksAgainOrStops() {
        execute(new UnmannedViperRoundEvent(1, 6));

        assertFollowup(one(
                all(single(new PlayerDecisionEvent<>(1, ActivateUnmannedViperEvent.class)),
                        single(new UnmannedViperRoundEvent(1, 5))),
                single(new NoOpEvent(1))));
    }

    @Test
    void theLastActivationAsksNoMore() {
        execute(new UnmannedViperRoundEvent(1, 1));

        assertFollowup(one(
                all(single(new PlayerDecisionEvent<>(1, ActivateUnmannedViperEvent.class)), NONE),
                single(new NoOpEvent(1))));
    }
}
