package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.launch;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.move;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

class GrantedViperActivationEventProcessorTest extends EventTestHarness<GrantedViperActivationEvent> {

    private Viper unmanned;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        unmanned = viperAt(GALACTICA_SPACE_6_OCLOCK);
    }

    @Test
    void anUnmannedViperMoves() {
        execute(new GrantedViperActivationEvent(1, move(unmanned.id(), GALACTICA_SPACE_8_OCLOCK)));

        assertEquals(GALACTICA_SPACE_8_OCLOCK, locate(unmanned));
    }

    // Never one another player is piloting (rule 11.1).
    @Test
    void notAnotherPlayersViper() {
        player(2).character(LEE_APOLLO_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        ops.player(2).pilot(unmanned.id());

        assertInvalid(new GrantedViperActivationEvent(1, move(unmanned.id(), GALACTICA_SPACE_8_OCLOCK)));
    }

    // The viper is in space already.
    @Test
    void notALaunch() {
        assertInvalid(new GrantedViperActivationEvent(1, launch(VIPER, GALACTICA_SPACE_4_OCLOCK)));
    }
}
