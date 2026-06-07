package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;

class LaunchViperEventProcessorTest extends EventTestHarness<LaunchViperEvent> {


    @Test
    void shouldNotAllowIllegalLocation() {
        assertInvalid(new LaunchViperEvent(VIPER, GALACTICA_SPACE_2_OCLOCK, null, null));
    }

    @Test
    void shouldNotAllowCylonShipsAndRaptors() {
        assertInvalid(new LaunchViperEvent(RAPTOR, GALACTICA_SPACE_4_OCLOCK, null, null));
    }

    @Test
    void shouldNotAllowNonPilotCharacters() {
        assertInvalid(new LaunchViperEvent(VIPER, GALACTICA_SPACE_4_OCLOCK, GAIUS_BALTAR, null));
    }

    @Test
    void shouldLaunchViper() {
        execute(new LaunchViperEvent(VIPER, GALACTICA_SPACE_4_OCLOCK, null, null));
        assertShipCount(GALACTICA_SPACE_4_OCLOCK, Viper.class, 1);
    }

    @Test
    void shouldLaunchViperMarkVII() {
        galacticaBoard.addToReserves(new ViperMarkVII(0));
        execute(new LaunchViperEvent(VIPER_MARK_VII, GALACTICA_SPACE_6_OCLOCK, null, null));
        assertShipCount(GALACTICA_SPACE_6_OCLOCK, ViperMarkVII.class, 1);
    }

    @Test
    void shouldLaunchAssaultRaptor() {
        galacticaBoard.addToReserves(new AssaultRaptor(0));
        execute(new LaunchViperEvent(ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK, null, null));
        assertShipCount(GALACTICA_SPACE_6_OCLOCK, AssaultRaptor.class, 1);
    }

    @Test
    void shouldLaunchViperWithPilot() {
        moveTo(HANGAR_DECK, LEE_ADAMA);

        execute(new LaunchViperEvent(ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK, LEE_ADAMA, null));

        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(LEE_ADAMA));
        assertEquals(LEE_ADAMA, galacticaBoard.shipsIn(GALACTICA_SPACE_6_OCLOCK, AssaultRaptor.class).getFirst().pilot());
    }


    @Test
    void shouldLandUnmannedViperToPilotIt() {
        moveTo(HANGAR_DECK, LEE_ADAMA);
        launchAllHumanFighters();
        val viper = galacticaBoard.shipsIn(GALACTICA_SPACE_10_OCLOCK).getFirst();

        execute(new LaunchViperEvent(VIPER, GALACTICA_SPACE_6_OCLOCK, LEE_ADAMA, viper.id()));

        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(LEE_ADAMA));
        assertEquals(LEE_ADAMA, galacticaBoard.shipsIn(GALACTICA_SPACE_6_OCLOCK, Viper.class).getFirst().pilot());
        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(viper));
    }

    private void launchAllHumanFighters() {
        val humanFighter = galacticaBoard.removeFromReserves(Viper.class)
                .map(HumanFighter.class::cast)
                .orElseGet(() -> galacticaBoard.removeFromReserves(AssaultRaptor.class)
                        .orElse(null));
        if (humanFighter != null) {
            galacticaBoard.place(GALACTICA_SPACE_10_OCLOCK, humanFighter);
            launchAllHumanFighters();
        }
    }
}