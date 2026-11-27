package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.character.OfferAlertViperPilotEvent;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_RESERVES;

class LaunchReservesEventProcessorTest extends EventTestHarness<LaunchReservesEvent> {

    private static final SkillCard LAUNCH_RESERVES_5 = new SkillCard(5, LAUNCH_RESERVES);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(1).gainSkillCards(LAUNCH_RESERVES_5);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.player(2).pilot(viperAt(GALACTICA_SPACE_11_OCLOCK).id());
    }

    // Into the area of anyone's piloted viper, and each one activated once.
    @Test
    void twoVipersJoinAPilotedViper() {
        val first = galacticaBoard.reserves().stream().filter(Viper.class::isInstance).map(Ship::id).toList();

        execute(new LaunchReservesEvent(1, LAUNCH_RESERVES_5, GALACTICA_SPACE_11_OCLOCK, List.of(VIPER, VIPER)));

        assertEquals(3, galacticaBoard.shipsIn(GALACTICA_SPACE_11_OCLOCK, Viper.class).size());
        assertFollowup(all(new ViperActivationRoundEvent(1, first.get(0), 1, false),
                new ViperActivationRoundEvent(1, first.get(1), 1, false)));
    }

    // Assault raptors count as vipers (rule 11.3.2.2).
    @Test
    void anAssaultRaptorIsAViper() {
        execute(new LaunchReservesEvent(1, LAUNCH_RESERVES_5, GALACTICA_SPACE_11_OCLOCK, List.of(ASSAULT_RAPTOR)));

        assertFollowup(new ViperActivationRoundEvent(1, 21, 1, false));
    }

    @Test
    void notWhereNoViperIsPiloted() {
        assertInvalid(new LaunchReservesEvent(1, LAUNCH_RESERVES_5, GALACTICA_SPACE_5_OCLOCK, List.of(VIPER)));
    }

    @Test
    void atMostTwo() {
        assertInvalid(new LaunchReservesEvent(1, LAUNCH_RESERVES_5, GALACTICA_SPACE_11_OCLOCK, List.of(VIPER, VIPER, VIPER)));
    }

    @Test
    void onlyWhatTheReservesHold() {
        assertInvalid(new LaunchReservesEvent(1, LAUNCH_RESERVES_5, GALACTICA_SPACE_11_OCLOCK, List.of(ASSAULT_RAPTOR, ASSAULT_RAPTOR)));
    }

    @Test
    void notARaptor() {
        assertInvalid(new LaunchReservesEvent(1, LAUNCH_RESERVES_5, GALACTICA_SPACE_11_OCLOCK, List.of(RAPTOR)));
    }

    // Lee "Apollo" Adama's "Alert Viper Pilot" is offered when the vipers are placed, before the card activates them.
    @Test
    void alertViperPilotComesBeforeTheActivations() {
        player(3).character(LEE_APOLLO_ADAMA);
        val first = galacticaBoard.reserves().stream().filter(Viper.class::isInstance).map(Ship::id).toList();

        execute(new LaunchReservesEvent(1, LAUNCH_RESERVES_5, GALACTICA_SPACE_11_OCLOCK, List.of(VIPER, VIPER)));

        assertFollowup(all(new OfferAlertViperPilotEvent(List.of(first.get(0), first.get(1))),
                new ViperActivationRoundEvent(1, first.get(0), 1, false),
                new ViperActivationRoundEvent(1, first.get(1), 1, false)));
    }
}
