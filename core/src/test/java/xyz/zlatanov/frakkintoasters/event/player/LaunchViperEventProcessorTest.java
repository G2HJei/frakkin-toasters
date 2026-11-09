package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;

class LaunchViperEventProcessorTest extends EventTestHarness<LaunchViperEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LEE_ADAMA);
        player(2).character(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldNotAllowIllegalLocation() {
        assertInvalid(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldNotAllowMissingLocation() {
        assertInvalid(new LaunchViperEvent(1, VIPER, null));
    }

    @Test
    void shouldNotAllowCylonShipsAndRaptors() {
        assertInvalid(new LaunchViperEvent(1, RAPTOR, GALACTICA_SPACE_4_OCLOCK));
    }

    @Test
    void shouldNotAllowNonPilotCharacters() {
        player(3).character(GAIUS_BALTAR);
        assertInvalid(new LaunchViperEvent(3, VIPER, GALACTICA_SPACE_4_OCLOCK, GAIUS_BALTAR, null));
    }

    @Test
    void shouldNotLaunchAnotherPlayersCharacter() {
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        assertInvalid(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK, KARA_STARBUCK_THRACE, null));
    }

    @Test
    void shouldNotAllowNoPilotWithConstraint() {
        assertInvalid(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_6_OCLOCK, null, null, true));
    }

    @Test
    void shouldLaunchViper() {
        execute(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK));
        assertShipCount(GALACTICA_SPACE_4_OCLOCK, Viper.class, 1);
    }

    @Test
    void shouldNotLaunchAShipTypeMissingFromTheReserves() {
        assertInvalid(new LaunchViperEvent(1, VIPER_MARK_VII, GALACTICA_SPACE_6_OCLOCK));
    }

    @Test
    void shouldLaunchViperMarkVII() {
        galacticaBoard.addToReserves(new ViperMarkVII(0));
        execute(new LaunchViperEvent(1, VIPER_MARK_VII, GALACTICA_SPACE_6_OCLOCK));
        assertShipCount(GALACTICA_SPACE_6_OCLOCK, ViperMarkVII.class, 1);
    }

    @Test
    void shouldLaunchAssaultRaptor() {
        galacticaBoard.addToReserves(new AssaultRaptor(0));
        execute(new LaunchViperEvent(1, ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK));
        assertShipCount(GALACTICA_SPACE_6_OCLOCK, AssaultRaptor.class, 1);
    }

    @Test
    void shouldLaunchViperWithPilot() {
        moveTo(HANGAR_DECK, LEE_ADAMA);

        execute(new LaunchViperEvent(1, ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK, LEE_ADAMA, null));

        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(LEE_ADAMA));
        assertEquals(LEE_ADAMA, galacticaBoard.shipsIn(GALACTICA_SPACE_6_OCLOCK, AssaultRaptor.class).getFirst().pilot());
    }


    @Test
    void shouldLandUnmannedViperToPilotIt() {
        moveTo(HANGAR_DECK, LEE_ADAMA);
        launchAllHumanFighters();
        val viper = galacticaBoard.shipsIn(GALACTICA_SPACE_10_OCLOCK, Viper.class).getFirst();

        execute(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_6_OCLOCK, LEE_ADAMA, viper.id()));

        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(LEE_ADAMA));
        assertEquals(LEE_ADAMA, galacticaBoard.shipsIn(GALACTICA_SPACE_6_OCLOCK, Viper.class).getFirst().pilot());
        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(viper));
    }

    @Test
    void shouldNotTakeAPilotedViper() {
        moveTo(HANGAR_DECK, LEE_ADAMA);
        launchAllHumanFighters();
        val piloted = galacticaBoard.shipsIn(GALACTICA_SPACE_10_OCLOCK, Viper.class).getFirst();
        piloted.pilot(KARA_STARBUCK_THRACE);

        assertInvalid(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_6_OCLOCK, LEE_ADAMA, piloted.id()));
    }

    @Test
    void shouldRequireHumanFighterToLandOnlyToPilotIt() {
        assertInvalid(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK, LEE_ADAMA, 1));
    }

    @Test
    void shouldRequireValidHumanFighterToLand() {
        moveTo(HANGAR_DECK, LEE_ADAMA);
        launchAllHumanFighters();
        val damagedViperId = galacticaBoard.removeFromDamagedShips(ViperMarkVII.class).orElseThrow().id();
        assertInvalid(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK, LEE_ADAMA, damagedViperId));
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
