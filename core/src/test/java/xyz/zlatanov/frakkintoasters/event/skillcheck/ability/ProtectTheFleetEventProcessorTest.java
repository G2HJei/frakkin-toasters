package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.MoveShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent.Attacker.VIPER;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER_MARK_VII;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class ProtectTheFleetEventProcessorTest extends EventTestHarness<ProtectTheFleetEvent> {

    private SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
        skillCheck.nonMatchingPile().addOnTop(new SkillCard(0, PROTECT_THE_FLEET), new SkillCard(3, MAXIMUM_FIREPOWER));
    }

    @Test
    void shouldDoNothing() {
        execute(new ProtectTheFleetEvent(1, null, null, null, null, null, null));
    }

    @Test
    void shouldLaunchFromReserves() {
        val assRaptor = new AssaultRaptor(-1);
        galacticaBoard.addToReserves(assRaptor);

        execute(ProtectTheFleetEvent.launch(1, ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK));

        assertFollowup(one(new LaunchViperEvent(1, ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK)));
    }

    @Test
    void shouldMoveHumanFighter() {
        val viper = viperMarkVIIAt(GALACTICA_SPACE_4_OCLOCK);
        execute(ProtectTheFleetEvent.move(1, viper.id(), GALACTICA_SPACE_2_OCLOCK));
        assertFollowup(one(new MoveShipEvent(viper.id(), GALACTICA_SPACE_2_OCLOCK)));
    }

    @Test
    void shouldAttackBasestar() {
        val viper = viperAt(GALACTICA_SPACE_10_OCLOCK);
        val bs = basestarAt(GALACTICA_SPACE_10_OCLOCK);

        execute(ProtectTheFleetEvent.attack(1, viper.id(), bs.id()));

        assertFollowup(one(new AttackBasestarEvent(VIPER, bs.id())));
    }

    @Test
    void shouldEscortCivilianShip() {
        val viper = viperAt(GALACTICA_SPACE_2_OCLOCK);
        val civ = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);

        execute(ProtectTheFleetEvent.escort(1, viper.id(), civ.id()));

        assertTrue(galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, CivilianShip.class).isEmpty());
    }

    @Test
    void shouldNeed3PilotingStrengthInTheCheck() {
        skillCheck.nonMatchingPile().remove(new SkillCard(3, MAXIMUM_FIREPOWER));
        skillCheck.nonMatchingPile().addOnTop(new SkillCard(2, MAXIMUM_FIREPOWER));
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(ProtectTheFleetEvent.move(1, viper.id(), GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldActivateAnUnmannedViper() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        viper.pilot(KARA_STARBUCK_THRACE);

        assertInvalid(ProtectTheFleetEvent.move(1, viper.id(), GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldMoveToAnAdjacentSpaceArea() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        assertInvalid(ProtectTheFleetEvent.move(1, viper.id(), GALACTICA_SPACE_12_OCLOCK));
    }

    @Test
    void shouldAttackACylonShipInTheSameSpaceArea() {
        val viper = viperAt(GALACTICA_SPACE_8_OCLOCK);
        val bs = basestarAt(GALACTICA_SPACE_10_OCLOCK);

        assertInvalid(ProtectTheFleetEvent.attack(1, viper.id(), bs.id()));
    }

    @Test
    void shouldEscortACivilianShipInTheSameSpaceArea() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val civ = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);

        assertInvalid(ProtectTheFleetEvent.escort(1, viper.id(), civ.id()));
    }

    @Test
    void shouldLaunchAShipTypeInTheReserves() {
        assertInvalid(ProtectTheFleetEvent.launch(1, VIPER_MARK_VII, GALACTICA_SPACE_6_OCLOCK));
    }

    @Test
    void shouldNameOnlyOneActivation() {
        val viper = viperAt(GALACTICA_SPACE_2_OCLOCK);
        val civ = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);

        assertInvalid(new ProtectTheFleetEvent(1, null, null, viper.id(), GALACTICA_SPACE_4_OCLOCK, null, civ.id()));
    }
}
