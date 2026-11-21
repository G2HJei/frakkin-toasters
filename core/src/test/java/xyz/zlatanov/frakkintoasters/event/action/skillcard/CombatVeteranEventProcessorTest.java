package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.launch;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.move;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.COMBAT_VETERAN;

class CombatVeteranEventProcessorTest extends EventTestHarness<CombatVeteranEvent> {

    private static final SkillCard COMBAT_VETERAN_3 = new SkillCard(3, COMBAT_VETERAN);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(1).gainSkillCards(COMBAT_VETERAN_3);
    }

    // A viper in the "Reserves" is launched first (rule 11.1.1), and the rounds follow the one the launch takes.
    @Test
    void aViperFromTheReservesIsLaunchedFirst() {
        int launched = galacticaBoard.reserves().stream().filter(Viper.class::isInstance).findFirst().orElseThrow().id();

        execute(new CombatVeteranEvent(1, COMBAT_VETERAN_3, launch(VIPER, GALACTICA_SPACE_4_OCLOCK)));

        assertFollowup(all(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK),
                new ViperActivationRoundEvent(1, launched, 2, false)));
    }

    @Test
    void aViperInSpaceIsActivatedThreeTimes() {
        Viper unmanned = viperAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new CombatVeteranEvent(1, COMBAT_VETERAN_3, move(unmanned.id(), GALACTICA_SPACE_8_OCLOCK)));

        assertEquals(GALACTICA_SPACE_8_OCLOCK, locate(unmanned));
        assertFollowup(new ViperActivationRoundEvent(1, unmanned.id(), 2, false));
    }

    // An unmanned viper only (rule 11.1).
    @Test
    void notAPilotedViper() {
        player(2).character(LEE_APOLLO_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        Viper piloted = viperAt(GALACTICA_SPACE_6_OCLOCK);
        ops.player(2).pilot(piloted.id());

        assertInvalid(new CombatVeteranEvent(1, COMBAT_VETERAN_3, move(piloted.id(), GALACTICA_SPACE_8_OCLOCK)));
    }

    @Test
    void theFirstActivationIsNamed() {
        assertInvalid(new CombatVeteranEvent(1, COMBAT_VETERAN_3, null));
    }
}
