package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;

class MainBatteriesEventProcessorTest extends EventTestHarness<MainBatteriesEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(HELENA_CAIN);
        moveTo(MAIN_BATTERIES, HELENA_CAIN);
    }

    @Test
    void shouldRollForTheChosenSpaceArea() {
        execute(new MainBatteriesEvent(1, GALACTICA_SPACE_2_OCLOCK));

        assertFollowup(rollDie(new MainBatteriesResultEvent(GALACTICA_SPACE_2_OCLOCK)));
    }

    @Test
    void shouldOnlyAffectAGalacticaSpaceArea() {
        assertInvalid(new MainBatteriesEvent(1, COMMAND));
    }

    @Test
    void shouldChooseASpaceArea() {
        assertInvalid(new MainBatteriesEvent(1, null));
    }
}
